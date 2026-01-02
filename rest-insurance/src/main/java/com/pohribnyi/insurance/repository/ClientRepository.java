package com.pohribnyi.insurance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pohribnyi.insurance.model.entity.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {

	boolean existsByEmail(String email);

	Optional<Client> findByEmail(String email);

}
