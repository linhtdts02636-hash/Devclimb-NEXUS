package com.devclimb.community;

import java.time.Instant;

public record MemberDTO(Integer userId, String name, String imageUrl, boolean isOwner, Instant joinedAt) {
}
