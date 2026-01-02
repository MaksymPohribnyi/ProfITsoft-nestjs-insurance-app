package com.pohribnyi.insurance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pohribnyi.insurance.BaseIntegrationTest;
import com.pohribnyi.insurance.dto.request.CreateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.request.PolicyPageableRequestDTO;
import com.pohribnyi.insurance.dto.request.UpdateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.model.entity.Client;
import com.pohribnyi.insurance.repository.ClientRepository;
import com.pohribnyi.insurance.repository.InsurancePolicyRepository;

@DisplayName("Insurance Policy Controller Integration Tests")
class InsurancePolicyControllerTest extends BaseIntegrationTest {

	private static final String API_POLICY_URL = "/api/insurance_policy";
	private static final String API_LIST_POLICY_URL = API_POLICY_URL + "/_list";
	private static final String API_REPORT_POLICY_URL = API_POLICY_URL + "/_report";
	private static final String API_UPLOAD_POLICY_URL = API_POLICY_URL+ "/upload";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private InsurancePolicyRepository policyRepository;

	private UUID testClientId;

	@BeforeEach
	void setUp() {
		objectMapper.registerModule(new JavaTimeModule());
		policyRepository.deleteAll();
		clientRepository.deleteAll();

		Client client = Client.builder()
				.firstName("Test")
				.lastName("Client")
				.email("testclient@example.com")
				.build();
		testClientId = clientRepository.save(client).getId();
	}

	@Test
    @DisplayName("Test create valid policy functionality")
    void shouldCreatePolicyWithValidData() throws Exception {
        // given
        CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
                "POL-2024-001",
                "Health Insurance",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                List.of("Medical", "Hospital", "Surgery"),
                testClientId
        );
        
        // when
        
        // then
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.policyNumber").value("POL-2024-001"))
				.andExpect(jsonPath("$.policyType").value("Health Insurance"))
				.andExpect(jsonPath("$.coveredRisks", hasSize(3)))
				.andExpect(jsonPath("$.client.id").value(testClientId.toString()));
	}

	@ParameterizedTest(name = "Test create invalid policy scenario={0} functionality")
	@MethodSource("invalidCreatePolicyProvider")
	void shouldRejectPolicyWithInvalidData(String scenario, CreateInsurancePolicyRequestDTO request,
			String expectedErrorField) throws Exception {
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors['" + expectedErrorField + "']").exists());
	}
	
	@Test
	@DisplayName("Test duplicate policy number functionality")
	void shouldRejectDuplicatePolicyNumber() throws Exception {
		// given
		CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
				"POL-DUP-001", 
				"Health",
				LocalDate.now().plusDays(1), 
				LocalDate.now().plusYears(1), 
				List.of("Medical"), 
				testClientId);
		
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));
		// when

		//then
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(containsString("already exists")));
	}

	@ParameterizedTest(name = "Test invalid date for start={0}, end={1} ({2}) functionality")
	@MethodSource("invalidDateCombinations")
	void shouldRejectInvalidDateCombinations(LocalDate start, LocalDate end, String scenario) throws Exception {
		// given
		CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
				"POL-DATE-001", 
				"Health", 
				start,
				end, 
				List.of("Medical"), 
				testClientId);
		// when
		
		// then
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Test not-existed client ID functionality")
	void shouldRejectNonExistentClientId() throws Exception {
		// given
		UUID notExistedId = UUID.randomUUID();
		CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
				"POL-CLIENT-001", 
				"Health",
				LocalDate.now().plusDays(1),
				LocalDate.now().plusYears(1), 
				List.of("Medical"), 
				notExistedId);

		// when

		// then
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value(containsString("not found")));
	}

	@Test
	@DisplayName("Test get policy by ID with client functionality")
	void shouldGetPolicyByIdWithClientDetails() throws Exception {
		// given
		CreateInsurancePolicyRequestDTO createRequest = new CreateInsurancePolicyRequestDTO(
				"POL-GET-001", 
				"Auto",
				LocalDate.now().plusDays(1), 
				LocalDate.now().plusYears(1), 
				List.of("Collision"), 
				testClientId);
		
		String response = mockMvc
				.perform(post(API_POLICY_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andReturn().getResponse().getContentAsString();
		
		UUID policyId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

		// when
		
		// then
		mockMvc.perform(get(API_POLICY_URL + "/" + policyId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(policyId.toString()))
				.andExpect(jsonPath("$.policyNumber").value("POL-GET-001"))
				.andExpect(jsonPath("$.client").exists())
				.andExpect(jsonPath("$.client.id").value(testClientId.toString()))
				.andExpect(jsonPath("$.client.email").value("testclient@example.com"));
	}

	@Test
	@DisplayName("Test 404 code for not-existed policy functionality")
	void shouldReturn404ForNotExistedPolicy() throws Exception {
		// given
		UUID notExistedId = UUID.randomUUID();

		// when
		
		// then
		mockMvc.perform(get(API_POLICY_URL + "/" + notExistedId)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Test update valid policy functionality")
	void shouldUpdatePolicyWithValidData() throws Exception {
		// given
		CreateInsurancePolicyRequestDTO createRequest = new CreateInsurancePolicyRequestDTO(
				"POL-UPD-001", 
				"Life",
				LocalDate.now().plusDays(1), 
				LocalDate.now().plusYears(1), 
				List.of("Death"), 
				testClientId);
		
		String response = mockMvc
				.perform(post(API_POLICY_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andReturn().getResponse().getContentAsString();
		
		UUID policyId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

		// when
		UpdateInsurancePolicyRequestDTO updateRequest = new UpdateInsurancePolicyRequestDTO(
				"Life Premium",
				LocalDate.now().plusDays(2), 
				LocalDate.now().plusYears(2),
				List.of("Death", "Disability"),
				testClientId);

		// then
		mockMvc.perform(put(API_POLICY_URL + "/" + policyId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyType").value("Life Premium"))
				.andExpect(jsonPath("$.coveredRisks", hasSize(2)))
				.andExpect(jsonPath("$.coveredRisks", containsInAnyOrder("Death", "Disability")));
	}

	@Test
	@DisplayName("Test 404 code when update not-existed policy functionality")
	void shouldReturn404WhenUpdatingNonExistentPolicy() throws Exception {
		// given
		UUID notExistedId = UUID.randomUUID();
		
		UpdateInsurancePolicyRequestDTO request = new UpdateInsurancePolicyRequestDTO(
				"Health",
				LocalDate.now().plusDays(1), 
				LocalDate.now().plusYears(1), 
				List.of("Medical"), 
				testClientId);

		// when
		
		// then
		mockMvc.perform(put(API_POLICY_URL + "/" + notExistedId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@ParameterizedTest(name = "Test update invalid policy with scenario={0} functionality")
	@MethodSource("invalidUpdatePolicyProvider")
	void shouldRejectInvalidUpdatePolicyData(String scenario, UpdateInsurancePolicyRequestDTO request,
			String expectedErrorField) throws Exception {
		UUID notExistedId = UUID.randomUUID();

		mockMvc.perform(put(API_POLICY_URL + "/" + notExistedId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors['" + expectedErrorField + "']").exists());
	}
	
	@Test
	@DisplayName("Test delete existing policy functionality")
	void shouldDeleteExistingPolicy() throws Exception {
		// given
		CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
				"POL-DEL-001", 
				"Property",
				LocalDate.now().plusDays(1), 
				LocalDate.now().plusYears(1), 
				List.of("Fire"), 
				testClientId);
		
		String response = mockMvc
				.perform(post(API_POLICY_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andReturn().getResponse().getContentAsString();
		
		UUID policyId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

		// when
		
		// then
		mockMvc.perform(delete(API_POLICY_URL + "/" + policyId)).andExpect(status().isNoContent());
		mockMvc.perform(get(API_POLICY_URL + "/" + policyId)).andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Test 404 code when delete not-existed policy functionality")
	void shouldReturn404WhenDeletingNotExistedPolicy() throws Exception {
		// given
		UUID notExistedId = UUID.randomUUID();
		// when
		
		// then
		mockMvc.perform(delete(API_POLICY_URL + "/" + notExistedId)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Test paginated result functionality")
	void shouldReturnPaginatedListOfPolicies() throws Exception {
		// given
		for (int i = 1; i <= 5; i++) {
			CreateInsurancePolicyRequestDTO request = new CreateInsurancePolicyRequestDTO(
					"POL-PAGE-" + i, 
					"Health",
					LocalDate.now().plusDays(1), 
					LocalDate.now().plusYears(1), 
					List.of("Medical"), 
					testClientId);
			
			mockMvc.perform(post(API_POLICY_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)));
		}

		// when
		PolicyPageableRequestDTO listRequest = new PolicyPageableRequestDTO(null, null, null, 0, 3);

		// then
		mockMvc.perform(post(API_LIST_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(listRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(3)))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	@DisplayName("Test filter policies by client ID functionality")
	void shouldFilterPoliciesByClientId() throws Exception {
		// given
		Client anotherClient = clientRepository.save(Client.builder()
				.firstName("Test1")
				.lastName("Client1")
				.email("Test1Client1@example.com")
				.build());

		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateInsurancePolicyRequestDTO(
						"POL-C1-001", 
						"Health",
						LocalDate.now().plusDays(1), 
						LocalDate.now().plusYears(1),
						List.of("Medical"), 
						testClientId))
						));

		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateInsurancePolicyRequestDTO(
								"POL-C2-001", 
								"Auto", 
								LocalDate.now().plusDays(1),
								LocalDate.now().plusYears(1), 
								List.of("Collision"),
								anotherClient.getId()))
						));

		// when
		PolicyPageableRequestDTO request = new PolicyPageableRequestDTO(testClientId, null, null, 0, 5);

		// then
		mockMvc.perform(post(API_LIST_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.list[0].policyNumber").value("POL-C1-001"));
	}

	@Test
	@DisplayName("Test filter policies by policy number functionality")
	void shouldFilterPoliciesByPolicyNumber() throws Exception {
		// given
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateInsurancePolicyRequestDTO(
						"POL-HEALTH-001", 
						"Health",
						LocalDate.now().plusDays(1), 
						LocalDate.now().plusYears(1), 
						List.of("Medical"),
						testClientId))
						));

		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateInsurancePolicyRequestDTO(
						"POL-AUTO-001", 
						"Auto", 
						LocalDate.now().plusDays(1),
						LocalDate.now().plusYears(1), 
						List.of("Collision"), 
						testClientId))
						));

		// when
		PolicyPageableRequestDTO request = new PolicyPageableRequestDTO(null, null, "POL-HEALTH-001", 0, 5);

		// then
		mockMvc.perform(post(API_LIST_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.list[0].policyNumber").value("POL-HEALTH-001"));
	}

	@Test
	@DisplayName("Test generate CSV report functionality")
	void shouldGenerateCsvReport() throws Exception {
		// given
		mockMvc.perform(post(API_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateInsurancePolicyRequestDTO(
						"POL-REPORT-001", 
						"Life",
						LocalDate.now().plusDays(1), 
						LocalDate.now().plusYears(1), 
						List.of("Death"), 
						testClientId))
						)).andExpect(status().isCreated());

		// when
		PolicyPageableRequestDTO request = new PolicyPageableRequestDTO(null, null, null, 0, 5);

		// then
		byte [] reportContent = mockMvc.perform(post(API_REPORT_POLICY_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Type", "text/csv"))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Content-Disposition", containsString("insurance_policies_report.csv")))
				.andReturn().getResponse().getContentAsByteArray();
		
		String csvString = new String(reportContent, StandardCharsets.UTF_8);

		assertThat(csvString).contains("ID", "Policy Number", "Policy Type", "Start Date", "End Date", "Client Name",
				"Client Email");
		assertThat(csvString).contains("POL-REPORT-001");
		assertThat(csvString).contains("Life");
		assertThat(csvString).contains(LocalDate.now().plusDays(1).toString());
		assertThat(csvString).contains(LocalDate.now().plusYears(1).toString());
		assertThat(csvString).contains("Test Client");
		assertThat(csvString).contains("testclient@example.com");
	}

	@ParameterizedTest(name = "File: {0} -> Expected Success: {1}, Fail: {2}")
	@CsvSource({ "/upload/expected/valid.json, 2, 0", 
		"/upload/expected/mixed.json,  1, 2", 
		"/upload/expected/empty.json, 0, 0" })
	@DisplayName("Test upload policies from external JSON files functionality")
	void shouldUploadPoliciesFromFile(String filePath, int expectedSuccess, int expectedFailure) throws Exception {
		String jsonContent = readResourceFile(filePath);
		jsonContent = jsonContent.replace("{{clientId}}", testClientId.toString());

		MockMultipartFile file = new MockMultipartFile(
				"file", 
				"policies.json",
				MediaType.APPLICATION_JSON_VALUE,
				jsonContent.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart(API_UPLOAD_POLICY_URL).file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.successCount").value(expectedSuccess))
				.andExpect(jsonPath("$.failureCount").value(expectedFailure));
	}

	private static Stream<Arguments> invalidDateCombinations() {
		LocalDate today = LocalDate.now();
		return Stream.of(
				Arguments.of(today.minusDays(1), today.plusYears(1), "past start date"),
				Arguments.of(today.plusDays(1), today, "end before start"),
				Arguments.of(today.plusDays(5), today.plusDays(5), "end equals start"));
	}

	private String readResourceFile(String path) {
		try {
			return new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read test resource: " + path, e);
		}
	}
	
	private static Stream<Arguments> invalidCreatePolicyProvider() {
		UUID randomId = UUID.randomUUID();
		LocalDate today = LocalDate.now();

		String validNumber = "POL-TEST-001";
		String validType = "General";
		LocalDate validStart = today.plusDays(1);
		LocalDate validEnd = today.plusYears(1);
		List<String> validRisks = List.of("Risk A");

		return Stream.of(
				Arguments.of("Policy Number Blank",
						new CreateInsurancePolicyRequestDTO(" ", validType, validStart, validEnd, validRisks, randomId),
						"policyNumber"),
				Arguments.of("Policy Number null",
						new CreateInsurancePolicyRequestDTO(null, validType, validStart, validEnd, validRisks, randomId),
						"policyNumber"),
				Arguments.of("Policy Number Too Long (>100)",
						new CreateInsurancePolicyRequestDTO("N".repeat(101), validType, validStart, validEnd,
								validRisks, randomId),
						"policyNumber"),
				
				Arguments.of("Policy Type Blank",
						new CreateInsurancePolicyRequestDTO(validNumber, " ", validStart, validEnd, validRisks,
								randomId),
						"policyType"),
				Arguments.of("Policy Type null",
						new CreateInsurancePolicyRequestDTO(validNumber, null, validStart, validEnd, validRisks,
								randomId),
						"policyType"),
				Arguments.of("Policy Type Too Long (>100)",
						new CreateInsurancePolicyRequestDTO(validNumber, "T".repeat(101), validStart, validEnd,
								validRisks, randomId),
						"policyType"),

				Arguments.of("Null Start Date",
						new CreateInsurancePolicyRequestDTO(validNumber, validType, null, validEnd, validRisks,
								randomId),
						"startDate"),
				Arguments.of("Null End Date",
						new CreateInsurancePolicyRequestDTO(validNumber, validType, validStart, null, validRisks,
								randomId),
						"endDate"),

				Arguments.of("Risks List Empty",
						new CreateInsurancePolicyRequestDTO(validNumber, validType, validStart, validEnd,
								Collections.emptyList(), randomId),
						"coveredRisks"),
				Arguments.of("Risks List Null",
						new CreateInsurancePolicyRequestDTO(validNumber, validType, validStart, validEnd, null,
								randomId),
						"coveredRisks"),
				Arguments.of("Risk Item Blank",
						new CreateInsurancePolicyRequestDTO(validNumber, validType, validStart, validEnd, List.of(""),
								randomId),
						"coveredRisks[0]"), 

				Arguments.of("Client ID Null", new CreateInsurancePolicyRequestDTO(validNumber, validType, validStart,
						validEnd, validRisks, null), "clientId"));
	}
	
	private static Stream<Arguments> invalidUpdatePolicyProvider() {
		UUID randomId = UUID.randomUUID();
		LocalDate today = LocalDate.now();

		String validType = "Updated Type";
		LocalDate validStart = today.plusDays(5);
		LocalDate validEnd = today.plusYears(2);
		List<String> validRisks = List.of("Updated Risk");

		return Stream.of(
				Arguments.of("Update: Type Blank",
						new UpdateInsurancePolicyRequestDTO(" ", validStart, validEnd, validRisks, randomId),
						"policyType"),
				
				Arguments.of("Update: Type null",
						new UpdateInsurancePolicyRequestDTO(null, validStart, validEnd, validRisks, randomId),
						"policyType"),

				Arguments.of("Update: Start Date Past",
						new UpdateInsurancePolicyRequestDTO(validType, today.minusDays(1), validEnd, validRisks,
								randomId),
						"startDate"),
				Arguments.of("Update: Start Date null",
						new UpdateInsurancePolicyRequestDTO(validType, null, validEnd, validRisks,
								randomId),
						"startDate"),
				Arguments.of("Update: End Date Past",
						new UpdateInsurancePolicyRequestDTO(validType, validStart, today.minusDays(1), validRisks,
								randomId),
						"endDate"),
				Arguments.of("Update: End Date null",
						new UpdateInsurancePolicyRequestDTO(validType, validStart, null, validRisks,
								randomId),
						"endDate"),

				Arguments.of("Update: Risks Empty",
						new UpdateInsurancePolicyRequestDTO(validType, validStart, validEnd, Collections.emptyList(),
								randomId),
						"coveredRisks"),
				Arguments.of("Update: Risk Item Blank",
						new UpdateInsurancePolicyRequestDTO(validType, validStart, validEnd, List.of(" "), randomId),
						"coveredRisks[0]"),

				Arguments.of("Update: Client Null",
						new UpdateInsurancePolicyRequestDTO(validType, validStart, validEnd, validRisks, null),
						"clientId"));
	}
	
}