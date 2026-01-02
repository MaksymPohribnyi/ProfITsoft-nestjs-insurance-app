package com.pohribnyi.insurance.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pohribnyi.insurance.dto.request.ClientRequestDTO;
import com.pohribnyi.insurance.dto.response.client.ClientResponseDTO;
import com.pohribnyi.insurance.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

	private final ClientService clientService;

	@GetMapping
	public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
		return ResponseEntity.ok(clientService.getAllClients());
	}

	@PostMapping
	public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO request) {
		ClientResponseDTO response = clientService.createClient(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable("id") UUID id,
			@Valid @RequestBody ClientRequestDTO request) {
		ClientResponseDTO response = clientService.updateClient(id, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteClient(@PathVariable("id") UUID id) {
		clientService.deleteClient(id);
		return ResponseEntity.noContent().build();
	}
}