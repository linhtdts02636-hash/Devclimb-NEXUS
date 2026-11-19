package com.devclimb.puzzle;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.judge0.Judge0Client;
import com.devclimb.judge0.Judge0Exception;
import com.devclimb.judge0.Judge0Testcase;
import com.devclimb.judge0.Judge0TimeoutException;
import com.devclimb.language.Language;
import com.devclimb.submission.FunctionHeader;
import com.devclimb.submission.SubmissionWrapper;

import tools.jackson.databind.JsonNode;

@Component
public class FunctionHeaderValidator {

	private static final Logger logger = LoggerFactory.getLogger(FunctionHeaderValidator.class);
	private static final int COMPILE_ERROR_STATUS_ID = 6;
	// Interpreted languages sometimes report parser failures as runtime errors.
	// These markers separate a real syntax failure from our probe's missing args.
	private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
			"(?i)(syntaxerror|indentationerror|taberror|unexpected end of input|unexpected token|invalid syntax)");

	private final SubmissionWrapper submissionWrapper;
	private final Judge0Client judge0Client;

	public FunctionHeaderValidator(SubmissionWrapper submissionWrapper, Judge0Client judge0Client) {
		this.submissionWrapper = submissionWrapper;
		this.judge0Client = judge0Client;
	}

	// Compile and run a tiny program made from the header and the real submission
	// wrapper. Non-syntax runtime errors are ignored because the probe has no useful
	// arguments; a compile failure or parser failure means the header is invalid.
	public void validate(List<LanguageHeader> headers) {
		for (LanguageHeader header : headers) {
			validateOne(header);
		}
	}

	private void validateOne(LanguageHeader languageHeader) {
		Language language = languageHeader.language();
		String functionHeader = languageHeader.functionHeader().trim();
		String functionName = FunctionHeader.extractFunctionName(functionHeader);
		String probeSource = buildProbeSource(language.getName(), functionHeader);
		String wrappedSource = submissionWrapper.wrap(probeSource, language.getJudge0Id(), functionName);

		List<JsonNode> results;
		try {
			// An empty stdin keeps the probe cheap. The wrapper may fail at runtime
			// when the function expects arguments, but it will still have compiled.
			List<String> tokens = judge0Client.submitBatch(
					wrappedSource,
					language.getJudge0Id(),
					List.of(new Judge0Testcase(0, "", "")));
			results = judge0Client.awaitBatch(tokens);
		} catch (Judge0TimeoutException exception) {
			logger.warn("Judge0 timed out while validating a puzzle function header", exception);
			throw new ApiException(ErrorCode.JUDGE0_TIMEOUT);
		} catch (Judge0Exception exception) {
			logger.warn("Judge0 failed while validating a puzzle function header", exception);
			throw new ApiException(ErrorCode.JUDGE0_UNAVAILABLE);
		}

		if (results.size() != 1) {
			throw new ApiException(ErrorCode.JUDGE0_UNAVAILABLE);
		}

		JsonNode result = results.get(0);
		int statusId = result.path("status").path("id").asInt(-1);
		if (statusId < 0) {
			throw new ApiException(ErrorCode.JUDGE0_UNAVAILABLE);
		}
		String compileOutput = Judge0Client.decodeBase64(textField(result, "compile_output"));
		String stderr = Judge0Client.decodeBase64(textField(result, "stderr"));
		if (statusId == COMPILE_ERROR_STATUS_ID
				|| SYNTAX_ERROR_PATTERN.matcher(compileOutput).find()
				|| SYNTAX_ERROR_PATTERN.matcher(stderr).find()) {
			logger.warn(
					"Function header for {} failed syntax validation. compile_output={} stderr={}",
					language.getName(),
					compileOutput,
					stderr);
			throw new ApiException(ErrorCode.FUNCTION_HEADER_COMPILE_ERROR);
		}
	}

	private String buildProbeSource(String languageName, String functionHeader) {
		String name = languageName == null ? "" : languageName.trim().toLowerCase(Locale.ROOT);
		return switch (name) {
			case "python" -> functionHeader.endsWith(":") ? functionHeader + "\n    pass" : functionHeader;
			case "javascript" -> functionHeader.endsWith("{") ? functionHeader + "\n}" : functionHeader;
			case "java" -> buildJavaProbe(functionHeader);
			default -> functionHeader;
		};
	}

	private String buildJavaProbe(String functionHeader) {
		// A full Java class is already a complete compilation unit.
		// note on this, there would be C# implemented later, so this may or may not work.
		if (functionHeader.contains("class ")) {
			return functionHeader;
		}

		String method = functionHeader;
		if (functionHeader.endsWith("{")) {
			method = functionHeader + "\n        throw new UnsupportedOperationException(\"Not implemented\");\n    }";
		} else if (!functionHeader.endsWith("}")) {
			method = functionHeader + "\n        throw new UnsupportedOperationException(\"Not implemented\");";
		}
		return "import java.util.*;\n\nclass Solution {\n    " + method + "\n}";
	}

	private String textField(JsonNode node, String field) {
		JsonNode value = node.path(field);
		return value.isMissingNode() || value.isNull() ? "" : value.asText("");
	}

	public record LanguageHeader(Language language, String functionHeader) {
	}
}
