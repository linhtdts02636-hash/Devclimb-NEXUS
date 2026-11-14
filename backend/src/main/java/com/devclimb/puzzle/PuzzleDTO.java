package com.devclimb.puzzle;

import java.time.Instant;

public record PuzzleDTO(
		Integer id,
		String title,
		String content,
		PuzzleDifficulty difficulty,
		Integer communityId,
		Integer authorId,
		String authorName,
		Instant createdAt) {

	public static PuzzleDTO from(Puzzle puzzle) {
		return new PuzzleDTO(
				puzzle.getId(),
				puzzle.getTitle(),
				puzzle.getContent(),
				puzzle.getDifficulty(),
				puzzle.getCommunityId(),
				puzzle.getAuthor().getId(),
				puzzle.getAuthor().getName(),
				puzzle.getCreatedAt());
	}
}
