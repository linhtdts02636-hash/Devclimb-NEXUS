package com.devclimb.community;

import java.time.Instant;
import java.util.List;

import com.devclimb.role.CommunityPermission;

// permissions là quyền của chính người xem, chỉ được điền khi tra cứu một cộng đồng; các API danh sách để trống.
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
		Instant createdAt,
		List<String> permissions) {

	// Dùng cho các biểu thức constructor JPQL trong CommunityRepository, vốn không nạp được permissions.
	public CommunityDTO(Integer id, String name, String imageUrl, String bannerUrl, String shoutout, boolean isPublic,
			long memberCount, boolean isMember, boolean isOwner, Instant createdAt) {
		this(id, name, imageUrl, bannerUrl, shoutout, isPublic, memberCount, isMember, isOwner, createdAt, List.of());
	}

	public static CommunityDTO of(Community community, long memberCount, boolean isMember, boolean isOwner) {
		return new CommunityDTO(community.getId(), community.getName(), community.getImageUrl(),
				community.getBannerUrl(), community.getShoutout(), community.isPublic(), memberCount, isMember,
				isOwner, community.getCreatedAt());
	}

	public CommunityDTO withPermissions(List<String> permissions) {
		return new CommunityDTO(id, name, imageUrl, bannerUrl, shoutout, isPublic, memberCount, isMember, isOwner,
				createdAt, permissions);
	}

	public CommunityDTO withAllPermissions() {
		return withPermissions(CommunityPermission.namesOf(List.of(CommunityPermission.values())));
	}
}
