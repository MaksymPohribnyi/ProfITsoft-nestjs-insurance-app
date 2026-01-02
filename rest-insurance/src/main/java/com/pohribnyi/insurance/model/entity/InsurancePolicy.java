package com.pohribnyi.insurance.model.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Insurance_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePolicy {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String policyNumber;

	private String policyType;

	private LocalDate startDate;

	private LocalDate endDate;

	@ElementCollection
	@CollectionTable(name = "policy_covered_risks", joinColumns = @JoinColumn(name = "policy_id"))
	private List<String> coveredRisks;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

}
