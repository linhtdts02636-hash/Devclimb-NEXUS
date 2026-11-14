package com.devclimb.puzzle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePuzzleRequest(
		@NotBlank @Size(max = 200) String title,
		@NotNull PuzzleDifficulty difficulty,
		@NotBlank String content) {
}
