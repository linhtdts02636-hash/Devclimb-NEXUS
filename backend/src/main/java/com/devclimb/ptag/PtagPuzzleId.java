package com.devclimb.ptag;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PtagPuzzleId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "ptag_id")
	private Integer ptagId;

	@Column(name = "puzz_id")
	private Integer puzzleId;

	protected PtagPuzzleId() {
	}

	public PtagPuzzleId(Integer ptagId, Integer puzzleId) {
		this.ptagId = ptagId;
		this.puzzleId = puzzleId;
	}

	public Integer getPtagId() {
		return ptagId;
	}

	public Integer getPuzzleId() {
		return puzzleId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PtagPuzzleId that)) {
			return false;
		}
		return Objects.equals(ptagId, that.ptagId)
				&& Objects.equals(puzzleId, that.puzzleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ptagId, puzzleId);
	}
}
