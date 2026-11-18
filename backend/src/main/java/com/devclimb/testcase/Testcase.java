package com.devclimb.testcase;

import com.devclimb.puzzle.Puzzle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "testcase")
public class Testcase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "test_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "puzz_id", nullable = false)
	private Puzzle puzzle;

	@Column(name = "test_input", nullable = false, columnDefinition = "TEXT")
	private String input;

	@Column(name = "test_output", nullable = false, columnDefinition = "TEXT")
	private String output;

	protected Testcase() {
	}

	public Testcase(Puzzle puzzle, String input, String output) {
		this.puzzle = puzzle;
		this.input = input;
		this.output = output;
	}

	public Integer getId() {
		return id;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}
}
