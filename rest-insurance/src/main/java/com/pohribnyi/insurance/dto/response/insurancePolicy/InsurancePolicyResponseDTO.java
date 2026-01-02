package com.pohribnyi.insurance.dto.response.insurancePolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pohribnyi.insurance.dto.response.client.ClientResponseDTO;
import com.pohribnyi.insurance.model.entity.InsurancePolicy;

public record InsurancePolicyResponseDTO(
		UUID id, 
		String policyNumber, 
		String policyType, 
		LocalDate startDate,
		LocalDate endDate, 
		List<String> coveredRisks,
		ClientResponseDTO client) 
{
	public static InsurancePolicyResponseDTO fromEntity(InsurancePolicy policy) {
		return new InsurancePolicyResponseDTO(
				policy.getId(), 
				policy.getPolicyNumber(), 
				policy.getPolicyType(),
				policy.getStartDate(), 
				policy.getEndDate(), 
				policy.getCoveredRisks(),
				ClientResponseDTO.fromEntity(policy.getClient()));
	}
	
}
