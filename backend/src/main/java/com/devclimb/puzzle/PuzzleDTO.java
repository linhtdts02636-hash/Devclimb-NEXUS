package com.devclimb.puzzle;

import java.time.Instant;
import java.util.List;

public record PuzzleDTO(
		Integer id,
		String title,
		String content,
		PuzzleDifficulty difficulty,
		Integer communityId,
		Integer authorId,
		String authorName,
		Instant createdAt,
		List<PuzzleLanguageDTO> languages,
		List<String> tags) {

	public static PuzzleDTO from(Puzzle puzzle) {
		return from(puzzle, List.of(), List.of());
	}

	public static PuzzleDTO from(Puzzle puzzle, List<PuzzleLanguage> languages) {
		return from(puzzle, languages, List.of());
	}

	public static PuzzleDTO from(Puzzle puzzle, List<PuzzleLanguage> languages, List<String> tags) {
		return new PuzzleDTO(
				puzzle.getId(),
				puzzle.getTitle(),
				puzzle.getContent(),
				puzzle.getDifficulty(),
				puzzle.getCommunityId(),
				puzzle.getAuthor().getId(),
				puzzle.getAuthor().getName(),
				puzzle.getCreatedAt(),
				languages.stream()
						.map(PuzzleLanguageDTO::from)
						.toList(),
				tags);
	}
}
