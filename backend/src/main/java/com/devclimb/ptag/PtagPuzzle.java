package com.devclimb.ptag;

import com.devclimb.puzzle.Puzzle;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "ptag_puzzle")
public class PtagPuzzle {

	@EmbeddedId
	private PtagPuzzleId id;

	@MapsId("ptagId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ptag_id", nullable = false)
	private Ptag ptag;

	@MapsId("puzzleId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "puzz_id", nullable = false)
	private Puzzle puzzle;

	protected PtagPuzzle() {
	}

	public PtagPuzzle(Ptag ptag, Puzzle puzzle) {
		this.ptag = ptag;
		this.puzzle = puzzle;
	}

	public PtagPuzzleId getId() {
		return id;
	}

	public Ptag getPtag() {
		return ptag;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}
}
