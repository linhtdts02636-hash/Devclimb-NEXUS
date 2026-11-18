package com.devclimb.community;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_community")
public class UserCommunity {

	@EmbeddedId
	private UserCommunityId id;

	@Column(name = "ucom_isowner", nullable = false)
	private boolean owner;

	@Generated(event = EventType.INSERT)
	@Column(name = "ucom_joinedat", insertable = false, updatable = false)
	private Instant joinedAt;

	protected UserCommunity() {
	}

	public UserCommunity(Integer userId, Integer communityId, boolean owner) {
		this.id = new UserCommunityId(userId, communityId);
		this.owner = owner;
	}

	public UserCommunityId getId() {
		return id;
	}

	public boolean isOwner() {
		return owner;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
