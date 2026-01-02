package com.pohribnyi.insurance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import com.pohribnyi.insurance.model.entity.InsurancePolicy;

public interface InsurancePolicyRepository
		extends JpaRepository<InsurancePolicy, UUID>, JpaSpecificationExecutor<InsurancePolicy> {

	@EntityGraph(attributePaths = { "client" })
	List<InsurancePolicy> findAll();

	@EntityGraph(attributePaths = { "client" })
	List<InsurancePolicy> findAll(@Nullable Specification<InsurancePolicy> spec);
	
	boolean existsByPolicyNumber(String policyNumber);

}
