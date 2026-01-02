package com.pohribnyi.insurance.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pohribnyi.insurance.dto.request.CreateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.request.PolicyPageableRequestDTO;
import com.pohribnyi.insurance.dto.request.UpdateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.response.UploadResponseDTO;
import com.pohribnyi.insurance.dto.response.insurancePolicy.InsurancePolicyResponseDTO;
import com.pohribnyi.insurance.dto.response.insurancePolicy.PolicyPageableResponseDTO;
import com.pohribnyi.insurance.service.InsurancePolicyService;
import com.pohribnyi.insurance.service.ReportService;
import com.pohribnyi.insurance.service.UploadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/insurance_policy")
public class InsurancePolicyController {

	private final InsurancePolicyService policyService;
	private final ReportService reportService;
	private final UploadService uploadService;

	@PostMapping
	public ResponseEntity<InsurancePolicyResponseDTO> createPolicy(
			@Valid @RequestBody CreateInsurancePolicyRequestDTO request) {
		InsurancePolicyResponseDTO response = policyService.createPolicy(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<InsurancePolicyResponseDTO> getPolicyById(@PathVariable("id") UUID id) {
		InsurancePolicyResponseDTO response = policyService.getPolicyById(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<InsurancePolicyResponseDTO> updatePolicy(@PathVariable("id") UUID id,
			@Valid @RequestBody UpdateInsurancePolicyRequestDTO request) {
		InsurancePolicyResponseDTO response = policyService.updatePolicy(id, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePolicy(@PathVariable("id") UUID id) {
		policyService.deletePolicy(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/_list")
	public ResponseEntity<PolicyPageableResponseDTO> listPolicies(@Valid @RequestBody PolicyPageableRequestDTO request) {
		PolicyPageableResponseDTO response = policyService.getPolicies(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/_report")
	public ResponseEntity<byte[]> generateReport(@Valid @RequestBody PolicyPageableRequestDTO request) throws IOException {
		byte[] report = reportService.generateCsvReport(request);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("text/csv"));
		headers.setContentDispositionFormData("attachment", "insurance_policies_report.csv");

		return ResponseEntity.ok().headers(headers).body(report);
	}

	@PostMapping("/upload")
	public ResponseEntity<UploadResponseDTO> uploadPolicies(@RequestParam("file") MultipartFile file)
			throws IOException {
		UploadResponseDTO response = uploadService.uploadPolicies(file);
		return ResponseEntity.ok(response);
	}

}
