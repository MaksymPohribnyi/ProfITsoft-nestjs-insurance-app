package com.pohribnyi.insurance.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pohribnyi.insurance.BaseIntegrationTest;
import com.pohribnyi.insurance.dto.request.ClientRequestDTO;
import com.pohribnyi.insurance.repository.ClientRepository;

@DisplayName("Client Controller Integration Tests")
class ClientControllerTest extends BaseIntegrationTest {
    
    private static final String API_CLIENT_URL = "/api/client";

	@Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Test create new client functionality")
    void shouldCreateClient() throws Exception {
        // given
        ClientRequestDTO request = new ClientRequestDTO("Test", "User", "test@example.com");
        
        // when 
        
        // then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.firstName").value("Test"))
				.andExpect(jsonPath("$.lastName").value("User"))
				.andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
	
	@NullSource
	@ValueSource(strings = { "", "  ", "\t", "\n" })
	@ParameterizedTest(name = "Test ({0}) empty firstName functionality")
	void shouldRejectEmptyFirstName(String firstName) throws Exception {
		// given
		ClientRequestDTO request = new ClientRequestDTO(firstName, "User", "test@example.com");

		// when

		// then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.firstName").exists())
				.andExpect(jsonPath("$.validationErrors.firstName").value(containsString("required")));
	}
    
    @NullSource
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    @ParameterizedTest(name = "Test ({0}) empty lastName functionality")
    void shouldRejectEmptyLastName(String lastName) throws Exception {
        // given
        ClientRequestDTO request = new ClientRequestDTO("Test", lastName, "test@example.com");
        
        // when
        
        // then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.lastName").exists());
	}
    
    @ValueSource(strings = {"", " ", "test123", "test@", "@example.com", "test@@example.com", "test space@example.com"})
    @ParameterizedTest(name = "Test ({0}) invalid email format functionality")
    void shouldRejectInvalidEmailFormat(String email) throws Exception {
        // given
        ClientRequestDTO request = new ClientRequestDTO("Test", "User", email);
        
        // when
        
        // then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.email").exists())
				.andExpect(jsonPath("$.validationErrors.email").value(containsString("Email")));
    }

	@Test
	@DisplayName("Test reject email over 255 chars functionality")
	void shouldRejectTooLongEmail() throws Exception {
		// given
		String longEmail = "a".repeat(255) + "@example.com";
		ClientRequestDTO request = new ClientRequestDTO("Test", "User", longEmail);

		// when 
		
		// then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.email").exists())
				.andExpect(jsonPath("$.validationErrors.email").value(containsString("Email")));;
	}
    
	@Test
	@DisplayName("Test return 409 code for duplicate email functionality")
	void shouldReturnConflictForDuplicateEmail() throws Exception {
		// given
		ClientRequestDTO request = new ClientRequestDTO("Test", "User", "duplicate@example.com");
		
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// when
		ClientRequestDTO duplicateEmail = new ClientRequestDTO("TEST", "USER", "duplicate@example.com");

		// then
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicateEmail)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(containsString("already exists")));
	}
    
	@Test
	@DisplayName("Test update existing client functionality")
	void shouldUpdateExistingClient() throws Exception {
		// given
		ClientRequestDTO createRequest = new ClientRequestDTO("Test", "User", "test@example.com");
		String createResponse = mockMvc
				.perform(post(API_CLIENT_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andReturn().getResponse().getContentAsString();
		
		UUID clientId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

		// when
		ClientRequestDTO updateRequest = new ClientRequestDTO("TestUpdated", "UserUpdated", "testUpdated@example.com");

		// then
		mockMvc.perform(put(API_CLIENT_URL + "/" + clientId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(clientId.toString()))
				.andExpect(jsonPath("$.firstName").value("TestUpdated"))
				.andExpect(jsonPath("$.lastName").value("UserUpdated"))
				.andExpect(jsonPath("$.email").value("testUpdated@example.com"));
	}

	@Test
	@DisplayName("Test 404 code when update not-existed client functionality")
	void shouldReturn404WhenUpdatingNotExistedClient() throws Exception {
		// given
		UUID notExistedId = UUID.randomUUID();
		ClientRequestDTO request = new ClientRequestDTO("Test", "User", "test@example.com");

		// when
		
		// then
		mockMvc.perform(put(API_CLIENT_URL + "/" + notExistedId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Test delete existing client functionality")
	void shouldDeleteExistingClient() throws Exception {
		// given
		ClientRequestDTO request = new ClientRequestDTO("Test", "User", "test@example.com");
		
		String response = mockMvc
				.perform(post(API_CLIENT_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andReturn().getResponse().getContentAsString();
		UUID clientId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

		// when 
		
		// then 
		mockMvc.perform(delete(API_CLIENT_URL + "/" + clientId))
			.andExpect(status().isNoContent());
		mockMvc.perform(delete(API_CLIENT_URL + "/" + clientId))
			.andExpect(status().isNotFound());
	}
	
	@Test
    @DisplayName("Test 404 code when delete not-existed client functionality")
    void shouldReturn404WhenDeletingNotExistedClient() throws Exception {
        // given
        UUID notExistedtId = UUID.randomUUID();
        
        // when
        
        // then
        mockMvc.perform(delete(API_CLIENT_URL + "/" + notExistedtId))
                .andExpect(status().isNotFound());
    }
        
	@Test
	@DisplayName("Test empty list when no clients exist functionality")
	void shouldReturnEmptyListWhenNoClients() throws Exception {
		// given:

		// when:

		// then:
		mockMvc.perform(get(API_CLIENT_URL))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}
	
	@Test
	@DisplayName("Test return all clients functionality")
	void shouldReturnAllClients() throws Exception {
		// given
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(new ClientRequestDTO("Test", "User", "test@example.com"))));
		
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(new ClientRequestDTO("Test1", "User1", "test1@example.com"))));
		
		mockMvc.perform(post(API_CLIENT_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(new ClientRequestDTO("Test2", "User2", "test2@example.com"))));

		// when
		
		// then
		mockMvc.perform(get(API_CLIENT_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].email",
						containsInAnyOrder("test@example.com", "test1@example.com", "test2@example.com")));
	}
	
}
