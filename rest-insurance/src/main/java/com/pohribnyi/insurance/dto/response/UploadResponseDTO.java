package com.pohribnyi.insurance.dto.response;

public record UploadResponseDTO(int successCount, int failureCount, String message) {
}