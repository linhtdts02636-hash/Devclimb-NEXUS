package com.devclimb.contest;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Khóa phức hợp gồm puzz_id và cont_id cho bảng liên kết puzzle_contest
@Embeddable
public class PuzzleContestId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "puzz_id")
	private Integer puzzleId;

	@Column(name = "cont_id")
	private Integer contestId;

	protected PuzzleContestId() {
	}

	public PuzzleContestId(Integer puzzleId, Integer contestId) {
		this.puzzleId = puzzleId;
		this.contestId = contestId;
	}

	public Integer getPuzzleId() {
		return puzzleId;
	}

	public Integer getContestId() {
		return contestId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PuzzleContestId that)) {
			return false;
		}
		return Objects.equals(puzzleId, that.puzzleId)
				&& Objects.equals(contestId, that.contestId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(puzzleId, contestId);
	}
}
