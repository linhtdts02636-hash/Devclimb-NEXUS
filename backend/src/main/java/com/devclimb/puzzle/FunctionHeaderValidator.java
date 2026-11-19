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
	// Ngôn ngữ thông dịch đôi khi trả lỗi parser dưới dạng runtime error. Các dấu
	// hiệu này giúp phân biệt lỗi syntax thật với lỗi do probe thiếu tham số.
	private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
			"(?i)(syntaxerror|indentationerror|taberror|unexpected end of input|unexpected token|invalid syntax)");

	private final SubmissionWrapper submissionWrapper;
	private final Judge0Client judge0Client;

	public FunctionHeaderValidator(SubmissionWrapper submissionWrapper, Judge0Client judge0Client) {
		this.submissionWrapper = submissionWrapper;
		this.judge0Client = judge0Client;
	}

	// Ý tưởng: ghép function header với đúng SubmissionWrapper mà lúc chấm bài sẽ
	// dùng, rồi compile và chạy thử. Lỗi runtime không phải lỗi syntax bị bỏ qua vì
	// probe cố tình không truyền tham số; chỉ compile fail hoặc parser fail mới kết
	// luận header sai.
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
			// stdin rỗng để probe chạy nhanh. Wrapper có thể lỗi runtime khi hàm cần
			// tham số, nhưng lúc đó code đã compile xong nên vẫn đủ để kiểm tra header.
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
		// Kiểm tra cả compile_output vì một số language/Judge0 version không trả
		// status 6 dù compiler đã báo lỗi cú pháp.
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
		// Header một dòng của Python/JS cần thêm thân rỗng tối thiểu để parser chấp nhận.
		return switch (name) {
			case "python" -> functionHeader.endsWith(":") ? functionHeader + "\n    pass" : functionHeader;
			case "javascript" -> functionHeader.endsWith("{") ? functionHeader + "\n}" : functionHeader;
			case "java" -> buildJavaProbe(functionHeader);
			default -> functionHeader;
		};
	}

	private String buildJavaProbe(String functionHeader) {
		// Nếu header đã là cả một class Java thì nó đã đủ compilation unit. Lưu ý:
		// sau này thêm C# có thể cần nhánh riêng vì cú pháp/khung class khác Java.
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
