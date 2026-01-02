package com.pohribnyi.insurance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pohribnyi.insurance.dto.request.CreateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.request.PolicyPageableRequestDTO;
import com.pohribnyi.insurance.dto.request.UpdateInsurancePolicyRequestDTO;
import com.pohribnyi.insurance.dto.response.insurancePolicy.InsurancePolicyResponseDTO;
import com.pohribnyi.insurance.dto.response.insurancePolicy.PolicyPageableResponseDTO;
import com.pohribnyi.insurance.dto.response.insurancePolicy.PolicySummaryResponseDTO;
import com.pohribnyi.insurance.model.entity.Client;
import com.pohribnyi.insurance.model.entity.InsurancePolicy;
import com.pohribnyi.insurance.repository.InsurancePolicyRepository;
import com.pohribnyi.insurance.repository.InsurancePolicySpecification;
import com.pohribnyi.insurance.util.exception.DuplicateResourceException;
import com.pohribnyi.insurance.util.exception.ResourceNotFoundException;
import com.pohribnyi.insurance.util.exception.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsurancePolicyService {

	private final InsurancePolicyRepository policyRepository;
	private final ClientService clientService;

	@Transactional
	public InsurancePolicyResponseDTO createPolicy(CreateInsurancePolicyRequestDTO request) {
		validateDates(request.startDate(), request.endDate());

		if (policyRepository.existsByPolicyNumber(request.policyNumber())) {
			throw new DuplicateResourceException("Policy with number " + request.policyNumber() + " already exists");
		}

		Client client = clientService.getClientEntity(request.clientId());
		InsurancePolicy policy = request.toEntity(client);

		InsurancePolicy saved = policyRepository.save(policy);
		return InsurancePolicyResponseDTO.fromEntity(saved);
	}

	@Transactional(readOnly = true)
	public InsurancePolicyResponseDTO getPolicyById(UUID id) {
		InsurancePolicy policy = policyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
		return InsurancePolicyResponseDTO.fromEntity(policy);
	}

	@Transactional
	public InsurancePolicyResponseDTO updatePolicy(UUID id, UpdateInsurancePolicyRequestDTO request) {
		validateDates(request.startDate(), request.endDate());

		InsurancePolicy policy = policyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

		Client client = policy.getClient();
		if (!client.getId().equals(request.clientId())) {
			client = clientService.getClientEntity(request.clientId());
		}

		policy.setPolicyType(request.policyType());
		policy.setStartDate(request.startDate());
		policy.setEndDate(request.endDate());
		policy.setCoveredRisks(request.coveredRisks());
		policy.setClient(client);

		InsurancePolicy updated = policyRepository.save(policy);
		return InsurancePolicyResponseDTO.fromEntity(updated);
	}

	@Transactional
	public void deletePolicy(UUID id) {
		if (!policyRepository.existsById(id)) {
			throw new ResourceNotFoundException("Policy not found with id: " + id);
		}
		policyRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public PolicyPageableResponseDTO getPolicies(PolicyPageableRequestDTO request) {
		Pageable pageable = PageRequest.of(request.page(), request.size());
		Page<InsurancePolicy> page = policyRepository.findAll(InsurancePolicySpecification.withFilters(request),
				pageable);

		List<PolicySummaryResponseDTO> summaries = page.getContent().stream()
				.map(PolicySummaryResponseDTO::fromEntity)
				.toList();

		return new PolicyPageableResponseDTO(summaries, page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public List<InsurancePolicy> getPoliciesForReport(PolicyPageableRequestDTO request) {
		return policyRepository.findAll(InsurancePolicySpecification.withFilters(request));
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
			throw new ValidationException("End date must be after start date");
		}
	}
}