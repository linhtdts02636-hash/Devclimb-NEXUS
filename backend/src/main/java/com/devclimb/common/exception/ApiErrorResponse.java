package com.devclimb.common.exception;

public record ApiErrorResponse(String code, String message) {

	public static ApiErrorResponse from(ErrorCode errorCode) {
		return new ApiErrorResponse(errorCode.name(), errorCode.getMessage());
	}
}
