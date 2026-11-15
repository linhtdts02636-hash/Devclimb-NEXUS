package com.devclimb.submission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;

@Component
public class SubmissionWrapper {

	private static final String FUNCTION_PLACEHOLDER = "{FUNCT}";
	private static final String CLASS_PLACEHOLDER = "{CLASS}";
	private static final Pattern CLASS_DECLARATION = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");

	private final ResourceLoader resourceLoader;
	private final Map<Integer, String> templates = new ConcurrentHashMap<>();

	public SubmissionWrapper(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String wrap(String userCode, Integer judge0LanguageId, String functionName) {
		// Code của người dùng phải đứng trước wrapper vì wrapper gọi hàm mà họ vừa viết.
		String template = templates.computeIfAbsent(judge0LanguageId, this::loadTemplate);
		String wrapper = template.replace(FUNCTION_PLACEHOLDER, functionName);
		if (wrapper.contains(CLASS_PLACEHOLDER)) {
			wrapper = wrapper.replace(CLASS_PLACEHOLDER, extractClassName(userCode));
		}
		return userCode + "\n" + wrapper;
	}

	private String loadTemplate(Integer judge0LanguageId) {
		Resource resource = resourceLoader.getResource("classpath:judge0/" + judge0LanguageId + ".txt");
		if (!resource.exists()) {
			throw new ApiException(ErrorCode.LANGUAGE_NOT_SUPPORTED);
		}
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read the submission wrapper.", exception);
		}
	}

	private String extractClassName(String userCode) {
		Matcher matcher = CLASS_DECLARATION.matcher(userCode);
		if (!matcher.find()) {
			throw new ApiException(ErrorCode.INVALID_SOURCE_CODE);
		}
		return matcher.group(1);
	}
}
