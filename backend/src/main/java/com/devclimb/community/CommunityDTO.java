package com.devclimb.community;

import java.time.Instant;

public record CommunityDTO(
		Integer id,
		String name,
		String imageUrl,
		String bannerUrl,
		String shoutout,
		boolean isPublic,
		long memberCount,
		boolean isMember,
		boolean isOwner,
		Instant createdAt) {

	public static CommunityDTO of(Community community, long memberCount, boolean isMember, boolean isOwner) {
		return new CommunityDTO(community.getId(), community.getName(), community.getImageUrl(),
				community.getBannerUrl(), community.getShoutout(), community.isPublic(), memberCount, isMember,
				isOwner, community.getCreatedAt());
	}
}
