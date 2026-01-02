package com.pohribnyi.insurance.dto.request;

import com.pohribnyi.insurance.model.entity.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequestDTO(
	
		@NotBlank(message = "First name is required")
		@Size(max = 255, message = "First name should not exceed 255 characters") 
		String firstName,

		@NotBlank(message = "Last name is required") 
		@Size(max = 255, message = "Last name should not exceed 255 characters") 
		String lastName,

		@NotBlank(message = "Email is required") 
		@Email(message = "Invalid Email format") 
		@Size(max = 255, message = "Email should not exceed 255 characters") 
		String email) {
	
	public Client toEntity() {
		return Client.builder()
				.firstName(firstName)
				.lastName(lastName)
				.email(email)
				.build();
	}
	
}