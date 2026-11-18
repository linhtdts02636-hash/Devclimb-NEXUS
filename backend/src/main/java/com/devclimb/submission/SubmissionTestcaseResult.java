package com.devclimb.submission;

public record SubmissionTestcaseResult(
		int index,
		Integer testcaseId,
		String status,
		boolean passed,
		String time,
		Long memory) {
}
