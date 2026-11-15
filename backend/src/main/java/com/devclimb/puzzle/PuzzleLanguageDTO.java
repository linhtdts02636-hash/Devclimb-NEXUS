package com.devclimb.puzzle;

import com.devclimb.language.Language;

public record PuzzleLanguageDTO(
		Integer languageId,
		String languageName,
		Integer judge0Id,
		String functionHeader) {

	public static PuzzleLanguageDTO from(PuzzleLanguage puzzleLanguage) {
		Language language = puzzleLanguage.getLanguage();
		return new PuzzleLanguageDTO(
				language.getId(),
				language.getName(),
				language.getJudge0Id(),
				puzzleLanguage.getFunctionHeader());
	}
}
