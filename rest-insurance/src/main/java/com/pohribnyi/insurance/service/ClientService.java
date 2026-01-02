package com.pohribnyi.insurance.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pohribnyi.insurance.dto.request.ClientRequestDTO;
import com.pohribnyi.insurance.dto.response.client.ClientResponseDTO;
import com.pohribnyi.insurance.model.entity.Client;
import com.pohribnyi.insurance.repository.ClientRepository;
import com.pohribnyi.insurance.util.exception.DuplicateResourceException;
import com.pohribnyi.insurance.util.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(ClientResponseDTO::fromEntity)
                .toList();
    }
    
    @Transactional
    public ClientResponseDTO createClient(ClientRequestDTO request) {
        if (clientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Client with email " + request.email() + " already exists");
        }
        
        Client client = request.toEntity();
        Client saved = clientRepository.save(client);
        return ClientResponseDTO.fromEntity(saved);
    }
    
    @Transactional
    public ClientResponseDTO updateClient(UUID id, ClientRequestDTO request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (!client.getEmail().equals(request.email()) && 
            clientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Client with email " + request.email() + " already exists");
        }
        
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setEmail(request.email());
        
        Client updated = clientRepository.save(client);
        return ClientResponseDTO.fromEntity(updated);
    }
    
    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Client getClientEntity(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}