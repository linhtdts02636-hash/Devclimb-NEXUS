package com.devclimb.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String REJECTED_REQUEST_MESSAGE = "The request could not be processed.";

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		return ResponseEntity
				.status(exception.getStatus())
				.body(ErrorResponse.of(exception.getCode(), exception.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		log.warn("Database constraint violated", exception);
		ErrorCode errorCode = ErrorCode.ACCOUNT_CONFLICT;
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.of(errorCode.name(), errorCode.getMessage()));
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MethodArgumentNotValidException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of("VALIDATION_ERROR", "Dữ liệu yêu cầu không hợp lệ."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
		if (exception instanceof org.springframework.web.ErrorResponse frameworkError) {
			HttpStatusCode statusCode = frameworkError.getStatusCode();
			return ResponseEntity.status(statusCode)
					.body(ErrorResponse.of(String.valueOf(statusCode.value()), REJECTED_REQUEST_MESSAGE));
		}
		log.error("Unhandled server error", exception);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of("INTERNAL_ERROR", "Đã xảy ra lỗi máy chủ."));
	}
}
