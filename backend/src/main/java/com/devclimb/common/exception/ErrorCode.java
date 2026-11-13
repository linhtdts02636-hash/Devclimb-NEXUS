package com.devclimb.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "Sign in to continue."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Your session is no longer valid. Sign in again."),
	EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Verify your email address to continue. Check your inbox."),
	EMAIL_UNAVAILABLE(HttpStatus.BAD_REQUEST, "The sign-in provider did not share an email address. Make your email public there, or sign up with email."),
	EMAIL_TOO_LONG(HttpStatus.BAD_REQUEST, "Email addresses can be at most 50 characters long."),
	ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "This account has been disabled."),
	ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "An account with these details already exists."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "No account found for this session. Sign in again."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side. Try again in a moment.");

	private final HttpStatus httpStatus;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getMessage() {
		return message;
	}
}
