package com.devclimb.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String REJECTED_REQUEST_MESSAGE = "The request could not be processed.";

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
		return respondWith(exception.getErrorCode());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		logger.warn("Database constraint violated", exception);
		return respondWith(ErrorCode.ACCOUNT_CONFLICT);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
		if (exception instanceof ErrorResponse frameworkError) {
			HttpStatusCode statusCode = frameworkError.getStatusCode();
			return ResponseEntity.status(statusCode)
					.body(new ApiErrorResponse(String.valueOf(statusCode.value()), REJECTED_REQUEST_MESSAGE));
		}
		logger.error("Unexpected error", exception);
		return respondWith(ErrorCode.INTERNAL_ERROR);
	}

	private ResponseEntity<ApiErrorResponse> respondWith(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiErrorResponse.from(errorCode));
	}
}
