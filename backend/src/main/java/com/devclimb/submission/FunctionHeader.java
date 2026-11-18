package com.devclimb.submission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;

public final class FunctionHeader {

	private static final Pattern FUNCTION_NAME = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

	private FunctionHeader() {
	}

	public static String extractFunctionName(String functionHeader) {
		if (functionHeader == null) {
			throw new ApiException(ErrorCode.INVALID_FUNCTION_HEADER);
		}
		Matcher matcher = FUNCTION_NAME.matcher(functionHeader);
		if (!matcher.find()) {
			throw new ApiException(ErrorCode.INVALID_FUNCTION_HEADER);
		}
		return matcher.group(1);
	}
}
