package com.devclimb.contest;

import java.time.Instant;
import java.util.List;

import com.devclimb.language.PuzzleLanguageDTO;
import com.devclimb.puzzle.Puzzle;
import com.devclimb.puzzle.PuzzleDifficulty;

// DTO biểu diễn bài toán trong cuộc thi kèm số điểm đạt được khi giải thành công
public record ContestPuzzleDTO(
		Integer id,
		String title,
		String content,
		PuzzleDifficulty difficulty,
		Integer points,
		Integer communityId,
		String communityName,
		String communityImageUrl,
		Integer authorId,
		String authorName,
		Instant createdAt,
		List<PuzzleLanguageDTO> languages,
		List<String> tags,
		boolean solved) {

	public static ContestPuzzleDTO from(
			PuzzleContest puzzleContest,
			List<PuzzleLanguageDTO> languages,
			List<String> tags,
			boolean solved) {
		Puzzle puzzle = puzzleContest.getPuzzle();
		return new ContestPuzzleDTO(
				puzzle.getId(),
				puzzle.getTitle(),
				puzzle.getContent(),
				puzzle.getDifficulty(),
				puzzleContest.getScore(),
				puzzle.getCommunity() != null ? puzzle.getCommunity().getId() : null,
				puzzle.getCommunity() != null ? puzzle.getCommunity().getName() : null,
				puzzle.getCommunity() != null ? puzzle.getCommunity().getImageUrl() : null,
				puzzle.getAuthor() != null ? puzzle.getAuthor().getId() : null,
				puzzle.getAuthor() != null ? puzzle.getAuthor().getName() : null,
				puzzle.getCreatedAt(),
				languages,
				tags,
				solved);
	}
}
