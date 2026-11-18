package com.devclimb.judge0;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.JsonNode;

@Component
public class Judge0Client {

	private static final int QUEUED_STATUS_ID = 1;
	private static final int PROCESSING_STATUS_ID = 2;

	private final RestClient restClient;
	private final long pollIntervalMs;
	private final int maxPollAttempts;

	public Judge0Client(
			@Value("${judge0.base-url:https://ce.judge0.com}") String baseUrl,
			@Value("${judge0.auth-token:}") String authToken,
			@Value("${judge0.poll-interval-ms:500}") long pollIntervalMs,
			@Value("${judge0.max-poll-attempts:60}") int maxPollAttempts) {
		RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
		if (StringUtils.hasText(authToken)) {
			builder.defaultHeader("X-Auth-Token", authToken);
		}
		this.restClient = builder.build();
		this.pollIntervalMs = pollIntervalMs;
		this.maxPollAttempts = maxPollAttempts;
	}

	public List<String> submitBatch(String sourceCode, Integer judge0LanguageId, List<Judge0Testcase> testcases) {
		List<Map<String, Object>> submissions = testcases.stream()
				.map(testcase -> Map.<String, Object>of(
						"source_code", base64Encode(sourceCode),
						"language_id", judge0LanguageId,
						"stdin", base64Encode(testcase.input()),
						"expected_output", base64Encode(testcase.expectedOutput())))
				.toList();

		try {
			JsonNode response = restClient.post()
					.uri(uriBuilder -> uriBuilder.path("/submissions/batch")
							.queryParam("base64_encoded", "true")
							.build())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("submissions", submissions))
					.retrieve()
					.body(JsonNode.class);
			return readTokens(response, testcases.size());
		} catch (RestClientException exception) {
			throw new Judge0Exception("Could not submit code to Judge0.", exception);
		}
	}

	public List<JsonNode> awaitBatch(List<String> tokens) {
		for (int attempt = 0; attempt < maxPollAttempts; attempt++) {
			sleep();
			JsonNode submissions = fetchBatch(tokens);
			if (!isProcessing(submissions)) {
				return reorder(tokens, submissions);
			}
		}
		throw new Judge0TimeoutException("Judge0 did not finish grading in time.");
	}

	public static String decodeBase64(String encoded) {
		if (!StringUtils.hasText(encoded)) {
			return "";
		}
		try {
			return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException exception) {
			// Dữ liệu không phải base64 thì coi như không có output thay vì làm hỏng cả lượt chấm.
			return "";
		}
	}

	private JsonNode fetchBatch(List<String> tokens) {
		String joinedTokens = String.join(",", tokens);
		try {
			JsonNode response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/submissions/batch")
							.queryParam("tokens", joinedTokens)
							.queryParam("base64_encoded", "true")
							.build())
					.retrieve()
					.body(JsonNode.class);
			if (response == null || !response.path("submissions").isArray()) {
				throw new Judge0Exception("Judge0 returned an invalid batch response.");
			}
			return response.path("submissions");
		} catch (RestClientException exception) {
			throw new Judge0Exception("Could not read code results from Judge0.", exception);
		}
	}

	private List<String> readTokens(JsonNode response, int expectedCount) {
		if (response == null || !response.isArray() || response.size() != expectedCount) {
			throw new Judge0Exception("Judge0 returned an invalid submission response.");
		}
		List<String> tokens = new ArrayList<>(expectedCount);
		for (JsonNode submission : response) {
			String token = submission.path("token").asText("");
			if (!StringUtils.hasText(token)) {
				throw new Judge0Exception("Judge0 did not return a submission token.");
			}
			tokens.add(token);
		}
		return tokens;
	}

	private boolean isProcessing(JsonNode submissions) {
		if (!submissions.isArray() || submissions.isEmpty()) {
			throw new Judge0Exception("Judge0 returned no submissions.");
		}
		for (JsonNode submission : submissions) {
			JsonNode status = submission.path("status");
			if (!status.has("id")) {
				throw new Judge0Exception("Judge0 returned a submission without a status.");
			}
			int statusId = status.path("id").asInt();
			if (statusId == QUEUED_STATUS_ID || statusId == PROCESSING_STATUS_ID) {
				return true;
			}
		}
		return false;
	}

	private List<JsonNode> reorder(List<String> tokens, JsonNode submissions) {
		// Kết quả batch không đảm bảo đúng thứ tự token nên phải xếp lại theo danh sách ban đầu.
		Map<String, JsonNode> byToken = new HashMap<>();
		for (JsonNode submission : submissions) {
			byToken.put(submission.path("token").asText(""), submission);
		}
		List<JsonNode> ordered = new ArrayList<>(tokens.size());
		for (String token : tokens) {
			JsonNode submission = byToken.get(token);
			if (submission == null) {
				throw new Judge0Exception("Judge0 did not return a result for every submission.");
			}
			ordered.add(submission);
		}
		return ordered;
	}

	private void sleep() {
		try {
			Thread.sleep(pollIntervalMs);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new Judge0Exception("Interrupted while waiting for Judge0.", exception);
		}
	}

	private static String base64Encode(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}
}
