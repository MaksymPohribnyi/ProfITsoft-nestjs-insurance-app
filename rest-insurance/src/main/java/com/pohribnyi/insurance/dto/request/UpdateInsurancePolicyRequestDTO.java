package com.pohribnyi.insurance.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateInsurancePolicyRequestDTO(
		
		@NotBlank(message = "Policy type is required") 
		@Size(max = 100, message = "Policy type must not exceed 100 characters") 
		String policyType,

		@NotNull(message = "Start date is required") 
		@FutureOrPresent(message = "Start date should be today or in the future")
		LocalDate startDate,

		@NotNull(message = "End date is required") 
		@Future(message = "End date should be in the future") 
		LocalDate endDate,

		@NotEmpty(message = "At least one covered risk is required") 
		List<@NotBlank(message = "Risk cannot be blank") String> coveredRisks,

		@NotNull(message = "Client ID is required") 
		UUID clientId) {
}
