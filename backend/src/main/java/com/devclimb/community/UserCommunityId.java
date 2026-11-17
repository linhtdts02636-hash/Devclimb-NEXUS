package com.devclimb.community;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserCommunityId(
		@Column(name = "user_id") Integer userId,
		@Column(name = "comm_id") Integer communityId) implements Serializable {
}
