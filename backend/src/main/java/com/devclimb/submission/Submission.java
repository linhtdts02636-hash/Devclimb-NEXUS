package com.devclimb.submission;

import java.time.Instant;

import com.devclimb.puzzle.Puzzle;
import com.devclimb.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "submission")
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subm_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "puzz_id", nullable = false)
	private Puzzle puzzle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "subm_code", nullable = false, columnDefinition = "TEXT")
	private String code;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "subm_status", nullable = false)
	private SubmissionStatus status;

	@Column(name = "subm_time")
	private Integer time;

	@Column(name = "subm_date", insertable = false, updatable = false)
	private Instant submittedAt;

	protected Submission() {
	}

	public Submission(Puzzle puzzle, User user, String code, SubmissionStatus status, Integer time) {
		this.puzzle = puzzle;
		this.user = user;
		this.code = code;
		this.status = status;
		this.time = time;
	}

	public Integer getId() {
		return id;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public User getUser() {
		return user;
	}

	public String getCode() {
		return code;
	}

	public SubmissionStatus getStatus() {
		return status;
	}

	public Integer getTime() {
		return time;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}
}
