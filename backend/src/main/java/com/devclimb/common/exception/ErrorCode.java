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
	PUZZLE_LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "This puzzle does not support the selected language."),
	PUZZLE_LANGUAGE_INVALID(HttpStatus.BAD_REQUEST, "Select a supported language for this puzzle."),
	TESTCASES_NOT_FOUND(HttpStatus.NOT_FOUND, "This puzzle has no test cases yet."),
	LANGUAGE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "This language is not supported for submissions yet."),
	INVALID_FUNCTION_HEADER(HttpStatus.BAD_REQUEST, "The puzzle function header is invalid."),
	FUNCTION_HEADER_COMPILE_ERROR(HttpStatus.BAD_REQUEST, "A function header does not compile. Check its syntax."),
	INVALID_SOURCE_CODE(HttpStatus.BAD_REQUEST, "The submitted source code is not valid for this language."),
	JUDGE0_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "The code runner is unavailable. Try again in a moment."),
	JUDGE0_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "The code runner took too long. Try again in a moment."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "The request could not be understood. Check the details and try again."),
	TOO_MANY_IDS(HttpStatus.BAD_REQUEST, "Too many communities were requested at once."),
	COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "This community does not exist."),
	COMMUNITY_NAME_TAKEN(HttpStatus.CONFLICT, "A community with this name already exists."),
	COMMUNITY_NAME_INVALID(HttpStatus.BAD_REQUEST, "Use 3 to 50 lowercase letters, digits or hyphens. Do not start or end with a hyphen."),
	COMMUNITY_SHOUTOUT_TOO_LONG(HttpStatus.BAD_REQUEST, "The description can be at most 300 characters long."),
	COMMUNITY_FORBIDDEN(HttpStatus.FORBIDDEN, "Only the owner of this community can do that."),
	ALREADY_MEMBER(HttpStatus.CONFLICT, "You are already a member of this community."),
	NOT_A_MEMBER(HttpStatus.NOT_FOUND, "You are not a member of this community."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "This post does not exist."),
	POST_FORBIDDEN(HttpStatus.FORBIDDEN, "Only the author can change this post."),
	POST_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "The post content cannot be empty."),
	POST_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "Post titles can be at most 255 characters long."),
	PERMISSION_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to do that in this community."),
	PERMISSION_UNKNOWN(HttpStatus.BAD_REQUEST, "One of the selected permissions does not exist."),
	ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "This role does not exist in this community."),
	ROLE_NAME_INVALID(HttpStatus.BAD_REQUEST, "Role names must be 1 to 50 characters and cannot be Owner or Member."),
	ROLE_NAME_TAKEN(HttpStatus.CONFLICT, "This community already has a role with this name."),
	ROLE_COLOR_INVALID(HttpStatus.BAD_REQUEST, "Use a hex color like #4AD08A."),
	ROLE_PROTECTED(HttpStatus.FORBIDDEN, "The Owner and Member roles cannot be changed, deleted or assigned this way."),
	ROLE_LIMIT_REACHED(HttpStatus.CONFLICT, "A community can have at most 25 custom roles."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "This person is not a member of this community."),
	MEMBER_PROTECTED(HttpStatus.FORBIDDEN, "The owner and yourself cannot be changed or removed here."),
	CONTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "This contest does not exist."),
	CONTEST_ENDED(HttpStatus.BAD_REQUEST, "This contest has already ended."),
	CONTEST_SCHEDULE_INVALID(HttpStatus.BAD_REQUEST, "End time must be after start time."),
	CONTEST_COMMUNITY_MISMATCH(HttpStatus.BAD_REQUEST, "Contest does not belong to this community."),
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
