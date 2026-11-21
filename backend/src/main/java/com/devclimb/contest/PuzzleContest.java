package com.devclimb.contest;

import com.devclimb.puzzle.Puzzle;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

// Thực thể liên kết bài tập và cuộc thi (bảng puzzle_contest trong mirrorSchema.sql)
@Entity
@Table(name = "puzzle_contest")
public class PuzzleContest {

	@EmbeddedId
	private PuzzleContestId id;

	@MapsId("puzzleId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "puzz_id", nullable = false)
	private Puzzle puzzle;

	@MapsId("contestId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cont_id", nullable = false)
	private Contest contest;

	@Column(name = "puco_score", nullable = false)
	private Integer score = 100;

	protected PuzzleContest() {
	}

	public PuzzleContest(Puzzle puzzle, Contest contest, Integer score) {
		this.puzzle = puzzle;
		this.contest = contest;
		this.score = score != null ? score : 100;
		if (puzzle != null && contest != null) {
			this.id = new PuzzleContestId(puzzle.getId(), contest.getId());
		}
	}

	public PuzzleContestId getId() {
		return id;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public Contest getContest() {
		return contest;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}
}
