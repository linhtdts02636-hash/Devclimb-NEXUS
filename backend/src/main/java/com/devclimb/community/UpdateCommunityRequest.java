package com.devclimb.community;

public record UpdateCommunityRequest(String name, String shoutout, boolean isPublic, String imageUrl, String bannerUrl) {
}
