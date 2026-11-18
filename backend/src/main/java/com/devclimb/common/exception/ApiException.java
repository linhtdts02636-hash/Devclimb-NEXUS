package com.devclimb.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final ErrorCode errorCode;
	private final HttpStatus status;
	private final String code;

	public ApiException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.status = errorCode.getHttpStatus();
		this.code = errorCode.name();
	}

	public ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.errorCode = null;
		this.status = status;
		this.code = code;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
