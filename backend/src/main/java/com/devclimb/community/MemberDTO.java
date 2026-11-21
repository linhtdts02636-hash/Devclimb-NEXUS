package com.devclimb.community;

import java.time.Instant;
import java.util.List;

import com.devclimb.role.MemberRoleDTO;

public record MemberDTO(Integer userId, String name, String imageUrl, boolean isOwner, Instant joinedAt,
		List<MemberRoleDTO> roles, boolean isModerator) {

	// Dùng cho biểu thức constructor JPQL trong UserCommunityRepository; vai trò được gắn vào sau.
	public MemberDTO(Integer userId, String name, String imageUrl, boolean isOwner, Instant joinedAt) {
		this(userId, name, imageUrl, isOwner, joinedAt, List.of(), isOwner);
	}

	public MemberDTO withRoles(List<MemberRoleDTO> roles, boolean isModerator) {
		return new MemberDTO(userId, name, imageUrl, isOwner, joinedAt, roles, isModerator);
	}
}
