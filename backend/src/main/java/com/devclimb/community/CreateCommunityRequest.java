package com.devclimb.community;

public record CreateCommunityRequest(String name, String shoutout, boolean isPublic, String imageUrl, String bannerUrl) {
}
