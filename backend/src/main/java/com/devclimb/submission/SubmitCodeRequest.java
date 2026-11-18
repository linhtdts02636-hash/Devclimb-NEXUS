package com.devclimb.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SubmitCodeRequest(
		@NotNull @Positive Integer puzzleId,
		@NotNull @Positive Integer languageId,
		@NotBlank @Size(max = 100_000) String code) {
}
