package com.devclimb.puzzle;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Payload đầy đủ khi tạo puzzle: thông tin puzzle, function header từng ngôn ngữ,
// tag và testcase ẩn — tất cả được ghi trong cùng một transaction.
public record CreatePuzzleRequest(
		@NotBlank @Size(max = 200) String title,
		@NotNull PuzzleDifficulty difficulty,
		@NotBlank String content,
		@NotNull @Positive Integer communityId,
		@NotEmpty @Size(max = 3) List<@Valid PuzzleLanguageRequest> languages,
		@Size(max = 20) List<@NotBlank @Size(max = 50) String> tags,
		@NotEmpty @Size(max = 50) List<@Valid TestcaseRequest> testcases) {

	public record PuzzleLanguageRequest(
			@NotNull @Positive Integer languageId,
			@NotBlank String functionHeader) {
	}

	public record TestcaseRequest(
			@NotBlank String input,
			@NotBlank String output) {
	}
}
