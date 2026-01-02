package com.pohribnyi.insurance.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pohribnyi.insurance.dto.request.CreateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.response.UploadResponseDTO;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {

	private final InsurancePolicyService policyService;
	private final Validator validator;
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public UploadResponseDTO uploadPolicies(MultipartFile file) throws IOException {
		
		if (file.isEmpty()) {
			return new UploadResponseDTO(0, 0, "File is empty");
		}

		CreateInsurancePolicyRequestDTO[] requests = objectMapper.readValue(file.getInputStream(),
				CreateInsurancePolicyRequestDTO[].class);

		List<CreateInsurancePolicyRequestDTO> requestList = Arrays.asList(requests);

		int successCount = 0;
		int failureCount = 0;

		for (CreateInsurancePolicyRequestDTO request : requestList) {
			if (processSinglePolicy(request)) {
				successCount++;
			} else {
				failureCount++;
			}
		}

		return new UploadResponseDTO(
				successCount, 
				failureCount,
				"Upload completed: " + successCount + " successful, " + failureCount + " failed");
	}
	
	private boolean processSinglePolicy(CreateInsurancePolicyRequestDTO request) {
		if (!validator.validate(request).isEmpty()) {
			return false;
		}
		try {
			policyService.createPolicy(request);
			return true;
		} catch (Exception e) {
			return false; 
		}
	}
	
}
