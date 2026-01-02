package com.pohribnyi.insurance.dto.response.insurancePolicy;

import java.time.LocalDate;
import java.util.UUID;

import com.pohribnyi.insurance.model.entity.InsurancePolicy;

public record PolicySummaryResponseDTO(
		UUID id, 
		String policyNumber, 
		String policyType, 
		LocalDate startDate,
		LocalDate endDate) {
	
	public static PolicySummaryResponseDTO fromEntity(InsurancePolicy policy) {
		return new PolicySummaryResponseDTO(
				policy.getId(), 
				policy.getPolicyNumber(), 
				policy.getPolicyType(),
				policy.getStartDate(), 
				policy.getEndDate());
	}
}