package com.devclimb.contest;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Payload khi tạo mới một cuộc thi
public record CreateContestRequest(
		@NotBlank @Size(max = 250) String title,
		@NotBlank String content,
		String banner,
		@NotNull Instant startTime,
		@NotNull Instant endTime,
		@NotNull @Positive Integer communityId) {
}
