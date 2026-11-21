package com.devclimb.contest;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Payload khi cập nhật thông tin cuộc thi ở chế độ chỉnh sửa
public record UpdateContestRequest(
		@NotBlank @Size(max = 250) String title,
		@NotBlank String content,
		String banner,
		@NotNull Instant startTime,
		@NotNull Instant endTime) {
}
