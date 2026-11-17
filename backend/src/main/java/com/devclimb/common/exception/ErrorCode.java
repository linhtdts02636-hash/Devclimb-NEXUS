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
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "The request could not be understood. Check the details and try again."),
	TOO_MANY_IDS(HttpStatus.BAD_REQUEST, "Too many communities were requested at once."),
	COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "This community does not exist."),
	COMMUNITY_NAME_TAKEN(HttpStatus.CONFLICT, "A community with this name already exists."),
	COMMUNITY_NAME_INVALID(HttpStatus.BAD_REQUEST, "Use 3 to 50 lowercase letters, digits or hyphens. Do not start or end with a hyphen."),
	COMMUNITY_SHOUTOUT_TOO_LONG(HttpStatus.BAD_REQUEST, "The description can be at most 300 characters long."),
	COMMUNITY_FORBIDDEN(HttpStatus.FORBIDDEN, "Only the owner of this community can do that."),
	ALREADY_MEMBER(HttpStatus.CONFLICT, "You are already a member of this community."),
	NOT_A_MEMBER(HttpStatus.NOT_FOUND, "You are not a member of this community."),
	OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "The owner cannot leave. Delete the community instead."),
	DELETE_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "Type the community name exactly to confirm."),
	IMAGE_URL_INVALID(HttpStatus.BAD_REQUEST, "This image was not uploaded through DevClimb. Upload it again."),
	UPLOAD_REJECTED(HttpStatus.BAD_REQUEST, "Use a JPEG, PNG or WebP image within the size limit."),
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
