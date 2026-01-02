package com.pohribnyi.insurance.dto.response.client;

import java.util.UUID;

import com.pohribnyi.insurance.model.entity.Client;

public record ClientResponseDTO(UUID id, String firstName, String lastName, String email) {
	
	public static ClientResponseDTO fromEntity(Client client) {
		return new ClientResponseDTO(
				client.getId(), 
				client.getFirstName(),
				client.getLastName(),
				client.getEmail());
	}
	
}
