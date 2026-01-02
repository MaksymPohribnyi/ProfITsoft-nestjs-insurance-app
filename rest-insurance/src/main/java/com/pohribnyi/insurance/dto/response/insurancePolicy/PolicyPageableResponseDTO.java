package com.pohribnyi.insurance.dto.response.insurancePolicy;

import java.util.List;

public record PolicyPageableResponseDTO(List<PolicySummaryResponseDTO> list, int totalPages) {
}