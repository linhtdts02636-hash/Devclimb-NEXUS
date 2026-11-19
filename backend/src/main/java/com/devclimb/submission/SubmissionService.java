package com.devclimb.submission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.judge0.Judge0Client;
import com.devclimb.judge0.Judge0Exception;
import com.devclimb.judge0.Judge0Testcase;
import com.devclimb.judge0.Judge0TimeoutException;
import com.devclimb.language.PuzzleLanguage;
import com.devclimb.language.PuzzleLanguageId;
import com.devclimb.language.PuzzleLanguageRepository;
import com.devclimb.puzzle.Puzzle;
import com.devclimb.testcase.Testcase;
import com.devclimb.testcase.TestcaseRepository;
import com.devclimb.user.User;
import tools.jackson.databind.JsonNode;

import jakarta.persistence.EntityManager;

@Service
public class SubmissionService {

	private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

	private static final int ACCEPTED_STATUS_ID = 3;
	private static final int TIME_LIMIT_STATUS_ID = 5;
	private static final int COMPILE_ERROR_STATUS_ID = 6;
	private static final int FIRST_RUNTIME_ERROR_STATUS_ID = 7;
	private static final int LAST_RUNTIME_ERROR_STATUS_ID = 12;

	private final PuzzleLanguageRepository puzzleLanguageRepository;
	private final TestcaseRepository testcaseRepository;
	private final SubmissionRepository submissionRepository;
	private final AuthenticationService authenticationService;
	private final SubmissionWrapper submissionWrapper;
	private final Judge0Client judge0Client;
	private final TransactionTemplate transactionTemplate;
	private final EntityManager entityManager;

	public SubmissionService(
			PuzzleLanguageRepository puzzleLanguageRepository,
			TestcaseRepository testcaseRepository,
			SubmissionRepository submissionRepository,
			AuthenticationService authenticationService,
			SubmissionWrapper submissionWrapper,
			Judge0Client judge0Client,
			PlatformTransactionManager transactionManager,
			EntityManager entityManager) {
		this.puzzleLanguageRepository = puzzleLanguageRepository;
		this.testcaseRepository = testcaseRepository;
		this.submissionRepository = submissionRepository;
		this.authenticationService = authenticationService;
		this.submissionWrapper = submissionWrapper;
		this.judge0Client = judge0Client;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.entityManager = entityManager;
	}

	public SubmissionResult submit(SubmitCodeRequest request, AuthenticatedFirebaseUser firebaseUser) {
		// Nạp dữ liệu trong transaction ngắn rồi mới gọi Judge0 để không giữ kết nối DB trong lúc chờ chấm.
		SubmissionContext context = loadContext(request, firebaseUser);
		String wrappedCode = submissionWrapper.wrap(
				request.code(),
				context.judge0LanguageId(),
				context.functionName());

		List<String> tokens;
		List<JsonNode> results;
		try {
			tokens = judge0Client.submitBatch(
					wrappedCode,
					context.judge0LanguageId(),
					context.testcases());
			results = judge0Client.awaitBatch(tokens);
		} catch (Judge0TimeoutException exception) {
			logger.warn("Judge0 grading timed out", exception);
			throw new ApiException(ErrorCode.JUDGE0_TIMEOUT);
		} catch (Judge0Exception exception) {
			logger.warn("Judge0 grading failed", exception);
			throw new ApiException(ErrorCode.JUDGE0_UNAVAILABLE);
		}

		return gradeAndSave(context, request.code(), results);
	}

	private SubmissionContext loadContext(SubmitCodeRequest request, AuthenticatedFirebaseUser firebaseUser) {
		return Objects.requireNonNull(transactionTemplate.execute(status -> {
			User user = authenticationService.findCurrentUser(firebaseUser);
			PuzzleLanguage puzzleLanguage = puzzleLanguageRepository
					.findById(new PuzzleLanguageId(request.puzzleId(), request.languageId()))
					.orElseThrow(() -> new ApiException(ErrorCode.PUZZLE_LANGUAGE_NOT_FOUND));
			// languageId ở đây là lang_id trong database; Judge0 dùng id riêng lấy từ bảng language.
			Integer judge0LanguageId = puzzleLanguage.getLanguage().getJudge0Id();

			List<Testcase> testcases = testcaseRepository.findByPuzzle_IdOrderByIdAsc(request.puzzleId());
			if (testcases.isEmpty()) {
				throw new ApiException(ErrorCode.TESTCASES_NOT_FOUND);
			}

			String functionName = FunctionHeader.extractFunctionName(puzzleLanguage.getFunctionHeader());
			List<Judge0Testcase> judge0Testcases = testcases.stream()
					.map(testcase -> new Judge0Testcase(
							testcase.getId(),
							testcase.getInput(),
							testcase.getOutput()))
					.toList();
			return new SubmissionContext(
					user.getId(),
					puzzleLanguage.getPuzzle().getId(),
					judge0LanguageId,
					functionName,
					judge0Testcases);
		}), "Submission context is required.");
	}

	private SubmissionResult gradeAndSave(SubmissionContext context, String sourceCode, List<JsonNode> results) {
		List<SubmissionTestcaseResult> testResults = new ArrayList<>();
		List<FailedTestcaseResult> failedResults = new ArrayList<>();
		boolean compileError = false;
		boolean runtimeError = false;
		boolean timeLimit = false;
		boolean allPassed = true;
		double maximumTimeSeconds = 0;
		long maximumMemory = 0;
		String firstCompileOutput = "";
		String firstStderr = "";
		String firstStatusDescription = "";

		for (int index = 0; index < results.size(); index++) {
			JsonNode result = results.get(index);
			Judge0Testcase testcase = context.testcases().get(index);
			JsonNode status = result.path("status");
			int statusId = status.path("id").asInt();
			String statusDescription = textField(status, "description");
			String stdout = Judge0Client.decodeBase64(textField(result, "stdout"));
			String stderr = Judge0Client.decodeBase64(textField(result, "stderr"));
			String compileOutput = Judge0Client.decodeBase64(textField(result, "compile_output"));
			String time = textField(result, "time");
			Long memory = result.path("memory").isNumber() ? result.path("memory").asLong() : null;

			if (firstStatusDescription.isEmpty()) {
				firstStatusDescription = statusDescription;
			}
			if (firstCompileOutput.isEmpty() && !compileOutput.isBlank()) {
				firstCompileOutput = compileOutput;
			}
			if (firstStderr.isEmpty() && !stderr.isBlank()) {
				firstStderr = stderr;
			}

			maximumTimeSeconds = Math.max(maximumTimeSeconds, parseSeconds(time));
			if (memory != null) {
				maximumMemory = Math.max(maximumMemory, memory);
			}

			boolean passed = statusId == ACCEPTED_STATUS_ID
					&& stdout.trim().equals(testcase.expectedOutput().trim());
			allPassed = allPassed && passed;
			testResults.add(new SubmissionTestcaseResult(
					index + 1,
					testcase.testcaseId(),
					statusDescription,
					passed,
					time,
					memory));

			if (!passed) {
				failedResults.add(new FailedTestcaseResult(
						index + 1,
						testcase.testcaseId(),
						statusDescription,
						testcase.input(),
						testcase.expectedOutput(),
						stdout,
						stderr,
						compileOutput,
						time,
						memory));
			}

			if (statusId == COMPILE_ERROR_STATUS_ID) {
				compileError = true;
			} else if (statusId == TIME_LIMIT_STATUS_ID) {
				timeLimit = true;
			} else if (statusId >= FIRST_RUNTIME_ERROR_STATUS_ID && statusId <= LAST_RUNTIME_ERROR_STATUS_ID) {
				runtimeError = true;
			}
		}

		SubmissionStatus overallStatus = overallStatus(allPassed, compileError, runtimeError, timeLimit);
		Integer timeInMilliseconds = (int) Math.round(maximumTimeSeconds * 1_000);
		Integer submissionId = saveSubmission(context, sourceCode, overallStatus, timeInMilliseconds);

		return new SubmissionResult(
				submissionId,
				overallStatus,
				overallStatus == SubmissionStatus.ACCEPTED,
				context.testcases().size(),
				context.testcases().size() - failedResults.size(),
				failedResults.size(),
				compileError || runtimeError,
				messageFor(overallStatus, firstStatusDescription),
				firstCompileOutput,
				firstStderr,
				maximumTimeSeconds,
				maximumMemory,
				testResults,
				failedResults);
	}

	private Integer saveSubmission(
			SubmissionContext context,
			String sourceCode,
			SubmissionStatus status,
			Integer timeInMilliseconds) {
		return Objects.requireNonNull(transactionTemplate.execute(transactionStatus -> {
			Puzzle puzzle = entityManager.getReference(Puzzle.class, context.puzzleId());
			User user = entityManager.getReference(User.class, context.userId());
			Submission submission = new Submission(puzzle, user, sourceCode, status, timeInMilliseconds);
			return submissionRepository.save(submission).getId();
		}), "Submission id is required.");
	}

	private SubmissionStatus overallStatus(
			boolean allPassed,
			boolean compileError,
			boolean runtimeError,
			boolean timeLimit) {
		// Ưu tiên lỗi biên dịch/chạy trước, vì một test lỗi đã đủ đổi kết luận của cả bài.
		if (compileError) {
			return SubmissionStatus.COMPILE_ERROR;
		}
		if (runtimeError) {
			return SubmissionStatus.RUNTIME_ERROR;
		}
		if (timeLimit) {
			return SubmissionStatus.TIME_LIMIT;
		}
		return allPassed ? SubmissionStatus.ACCEPTED : SubmissionStatus.WRONG_ANSWER;
	}

	private String messageFor(SubmissionStatus status, String fallback) {
		return switch (status) {
			case ACCEPTED -> "Accepted";
			case WRONG_ANSWER -> "Wrong Answer";
			case TIME_LIMIT -> "Time Limit Exceeded";
			case RUNTIME_ERROR -> "Runtime Error";
			case COMPILE_ERROR -> "Compilation Error";
			case PENDING -> fallback.isBlank() ? "Processing" : fallback;
		};
	}

	private String textField(JsonNode node, String field) {
		JsonNode value = node.path(field);
		return value.isMissingNode() || value.isNull() ? "" : value.asText("");
	}

	private double parseSeconds(String value) {
		if (value == null || value.isBlank()) {
			return 0;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException exception) {
			return 0;
		}
	}

	private record SubmissionContext(
			Integer userId,
			Integer puzzleId,
			Integer judge0LanguageId,
			String functionName,
			List<Judge0Testcase> testcases) {
	}
}
