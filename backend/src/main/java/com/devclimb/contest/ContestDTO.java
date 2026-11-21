package com.devclimb.contest;

import java.time.Instant;

// DTO phản hồi dữ liệu cuộc thi cho frontend
public record ContestDTO(
		Integer id,
		String title,
		String content,
		String banner,
		Instant startTime,
		Instant endTime,
		Integer communityId,
		String communityName,
		String communityImageUrl,
		long registeredCount,
		long puzzleCount,
		String status) {

	public static ContestDTO from(Contest contest) {
		return from(contest, 0, 0);
	}

	public static ContestDTO from(Contest contest, long registeredCount, long puzzleCount) {
		Instant now = Instant.now();
		String status = "UPCOMING";
		if (now.isAfter(contest.getEndTime())) {
			status = "CLOSED";
		} else if (now.isAfter(contest.getStartTime())) {
			status = "OPEN";
		}

		return new ContestDTO(
				contest.getId(),
				contest.getTitle(),
				contest.getContent(),
				contest.getBanner(),
				contest.getStartTime(),
				contest.getEndTime(),
				contest.getCommunity() != null ? contest.getCommunity().getId() : null,
				contest.getCommunity() != null ? contest.getCommunity().getName() : null,
				contest.getCommunity() != null ? contest.getCommunity().getImageUrl() : null,
				registeredCount,
				puzzleCount,
				status);
	}
}
