package com.devclimb.post;

import java.time.Instant;

public record PostDto(
        Integer id,
        Integer communityId,
        AuthorDto author,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt
) {

    public static PostDto from(Post post) {
        return new PostDto(
                post.getId(),
                post.getCommunity().getId(),
                new AuthorDto(
                        post.getAuthor().getId(),
                        post.getAuthor().getName(),
                        post.getAuthor().getImageUrl()
                ),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
