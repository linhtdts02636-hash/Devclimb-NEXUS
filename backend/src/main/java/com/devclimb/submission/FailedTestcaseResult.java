package com.devclimb.submission;

public record FailedTestcaseResult(
		int index,
		Integer testcaseId,
		String status,
		String input,
		String expected,
		String got,
		String stderr,
		String compileOutput,
		String time,
		Long memory) {
}
