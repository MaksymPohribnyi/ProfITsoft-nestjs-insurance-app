package com.pohribnyi.insurance.util.exception.handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pohribnyi.insurance.dto.response.ErrorResponseDTO;
import com.pohribnyi.insurance.util.exception.DuplicateResourceException;
import com.pohribnyi.insurance.util.exception.ResourceNotFoundException;
import com.pohribnyi.insurance.util.exception.ValidationException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex,
			HttpServletRequest request) {

		ErrorResponseDTO error = new ErrorResponseDTO(
				LocalDateTime.now(), 
				HttpStatus.NOT_FOUND.value(), 
				"Not Found",
				ex.getMessage(), 
				request.getRequestURI(), 
				null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponseDTO> handleDuplicateResource(DuplicateResourceException ex,
			HttpServletRequest request) {

		ErrorResponseDTO error = new ErrorResponseDTO(
				LocalDateTime.now(), 
				HttpStatus.CONFLICT.value(), 
				"Conflict",
				ex.getMessage(), 
				request.getRequestURI(), 
				null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidation(ValidationException ex, HttpServletRequest request) {

		ErrorResponseDTO error = new ErrorResponseDTO(
				LocalDateTime.now(), 
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request", 
				ex.getMessage(), 
				request.getRequestURI(), 
				null);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		Map<String, String> validationErrors = new HashMap<>();
		
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			validationErrors.put(fieldName, errorMessage);
		});

		ErrorResponseDTO error = new ErrorResponseDTO(
				LocalDateTime.now(), 
				HttpStatus.BAD_REQUEST.value(),
				"Validation Failed", 
				"Invalid request parameters", 
				request.getRequestURI(), 
				validationErrors);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {

		ErrorResponseDTO error = new ErrorResponseDTO(
				LocalDateTime.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error", 
				ex.getMessage(), 
				request.getRequestURI(), 
				null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}