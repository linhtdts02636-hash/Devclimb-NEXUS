package com.devclimb.puzzle;

import java.time.Instant;

import com.devclimb.community.Community;
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
@Table(name = "puzzle")
public class Puzzle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "puzz_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comm_id", nullable = false)
	private Community community;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "puzz_difficulty", nullable = false)
	private PuzzleDifficulty difficulty = PuzzleDifficulty.MEDIUM;

	@Column(name = "puzz_title")
	private String title;

	@Column(name = "puzz_content", nullable = false)
	private String content;

	@Column(name = "puzz_createdat", insertable = false, updatable = false)
	private Instant createdAt;

	protected Puzzle() {
	}

	public Puzzle(
			User author,
			Community community,
			PuzzleDifficulty difficulty,
			String title,
			String content) {
		this.author = author;
		this.community = community;
		this.difficulty = difficulty;
		this.title = title;
		this.content = content;
	}

	public Integer getId() {
		return id;
	}

	public User getAuthor() {
		return author;
	}

	public Community getCommunity() {
		return community;
	}

	public PuzzleDifficulty getDifficulty() {
		return difficulty;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
