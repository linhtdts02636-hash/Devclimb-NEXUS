package com.devclimb.community;

import com.devclimb.community.entity.Community;

public record CommunityResponse(
        Integer id,
        String name,
        String slug,
        String image,
        boolean publicCommunity
) {

    public static CommunityResponse from(Community community) {
        return new CommunityResponse(
                community.getId(),
                community.getName(),
                CommunitySlug.from(community.getName()),
                community.getImage(),
                community.isPublicCommunity()
        );
    }
}
