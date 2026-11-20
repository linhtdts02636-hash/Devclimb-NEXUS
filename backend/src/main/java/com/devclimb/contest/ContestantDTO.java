package com.devclimb.contest;

// DTO phản hồi thông tin thí sinh và điểm số tích lũy cho frontend
public record ContestantDTO(
		Integer userId,
		String userName,
		String userImage,
		boolean isAdmin,
		long points) {
}
