package com.devclimb.role;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_community_role")
public class UserCommunityRole {

	@EmbeddedId
	private UserCommunityRoleId id;

	@Generated(event = EventType.INSERT)
	@Column(name = "ucrl_assignedat", insertable = false, updatable = false)
	private Instant assignedAt;

	protected UserCommunityRole() {
	}

	public UserCommunityRole(Integer userId, Integer communityId, Integer roleId) {
		this.id = new UserCommunityRoleId(userId, communityId, roleId);
	}

	public UserCommunityRoleId getId() {
		return id;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}
}
