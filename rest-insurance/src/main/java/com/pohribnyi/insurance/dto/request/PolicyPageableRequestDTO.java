package com.pohribnyi.insurance.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record PolicyPageableRequestDTO(
		UUID clientId,
		String policyType, 
		String policyNumber,

		@Min(value = 0, message = "Page should be non-negative") 
		Integer page,

		@Positive(message = "Size must be positive") 
		Integer size) {
	
	public PolicyPageableRequestDTO {
		if (page == null)
			page = 0;
		if (size == null)
			size = 20;
	}
	
}
