package com.pohribnyi.insurance.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Clients")
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String firstName;

	private String lastName;

	@Column(nullable = false, unique = true)
	private String email;

	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<InsurancePolicy> insurancePolicies = new ArrayList<>();

}
