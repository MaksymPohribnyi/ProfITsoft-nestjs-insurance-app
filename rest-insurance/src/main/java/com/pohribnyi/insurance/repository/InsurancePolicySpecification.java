package com.pohribnyi.insurance.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.pohribnyi.insurance.dto.request.PolicyPageableRequestDTO;
import com.pohribnyi.insurance.model.entity.InsurancePolicy;

import jakarta.persistence.criteria.Predicate;

public class InsurancePolicySpecification {

	public static Specification<InsurancePolicy> withFilters(PolicyPageableRequestDTO request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (request.clientId() != null) {
				predicates.add(cb.equal(root.get("client").get("id"), request.clientId()));
			}

			if (request.policyType() != null && !request.policyType().isBlank()) {
				predicates.add(cb.equal(root.get("policyType"), request.policyType()));
			}

			if (request.policyNumber() != null && !request.policyNumber().isBlank()) {
				predicates.add(cb.equal(root.get("policyNumber"), request.policyNumber()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}