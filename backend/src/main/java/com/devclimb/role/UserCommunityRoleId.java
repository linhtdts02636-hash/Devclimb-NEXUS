package com.devclimb.role;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserCommunityRoleId(
		@Column(name = "user_id") Integer userId,
		@Column(name = "comm_id") Integer communityId,
		@Column(name = "role_id") Integer roleId) implements Serializable {
}
