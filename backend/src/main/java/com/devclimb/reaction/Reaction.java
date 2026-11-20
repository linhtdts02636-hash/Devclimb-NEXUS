package com.devclimb.reaction;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

// Ánh xạ đúng bảng reaction có sẵn; nhánh này không tạo thêm schema hay cột mới.
@Entity
@Table(name = "reaction")
public class Reaction {

	@EmbeddedId
	private ReactionId id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "reac_type", nullable = false)
	private ReactionType type;

	// reac_createdat do PostgreSQL sinh; upsert chỉ đổi loại reaction.
	@Generated(event = EventType.INSERT)
	@Column(name = "reac_createdat", insertable = false, updatable = false, nullable = false)
	private Instant createdAt;

	protected Reaction() {
	}

	public Reaction(Integer postId, Integer userId, ReactionType type) {
		this.id = new ReactionId(postId, userId);
		this.type = type;
	}

	public ReactionId getId() {
		return id;
	}

	public ReactionType getType() {
		return type;
	}
}
