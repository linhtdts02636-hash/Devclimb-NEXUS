package com.devclimb.submission;

import java.util.List;

public record SubmissionResult(
		Integer submissionId,
		SubmissionStatus status,
		boolean passed,
		int testCount,
		int testPassed,
		int testFailed,
		boolean error,
		String message,
		String compileOutput,
		String stderr,
		double time,
		long memory,
		List<SubmissionTestcaseResult> tests,
		List<FailedTestcaseResult> failed) {
}
