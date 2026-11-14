package com.devclimb.puzzle;

import com.devclimb.language.Language;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "puzzle_language")
public class PuzzleLanguage {

	@EmbeddedId
	private PuzzleLanguageId id;

	@MapsId("puzzleId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "puzz_id", nullable = false)
	private Puzzle puzzle;

	@MapsId("languageId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lang_id", nullable = false)
	private Language language;

	@Column(name = "pula_functionheader", nullable = false, columnDefinition = "TEXT")
	private String functionHeader;

	protected PuzzleLanguage() {
	}

	public PuzzleLanguage(Puzzle puzzle, Language language, String functionHeader) {
		this.puzzle = puzzle;
		this.language = language;
		this.functionHeader = functionHeader;
	}

	public PuzzleLanguageId getId() {
		return id;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public Language getLanguage() {
		return language;
	}

	public String getFunctionHeader() {
		return functionHeader;
	}
}
