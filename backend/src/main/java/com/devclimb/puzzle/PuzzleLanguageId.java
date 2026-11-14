package com.devclimb.puzzle;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PuzzleLanguageId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "puzz_id")
	private Integer puzzleId;

	@Column(name = "lang_id")
	private Integer languageId;

	protected PuzzleLanguageId() {
	}

	public PuzzleLanguageId(Integer puzzleId, Integer languageId) {
		this.puzzleId = puzzleId;
		this.languageId = languageId;
	}

	public Integer getPuzzleId() {
		return puzzleId;
	}

	public Integer getLanguageId() {
		return languageId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PuzzleLanguageId that)) {
			return false;
		}
		return Objects.equals(puzzleId, that.puzzleId)
				&& Objects.equals(languageId, that.languageId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(puzzleId, languageId);
	}
}
