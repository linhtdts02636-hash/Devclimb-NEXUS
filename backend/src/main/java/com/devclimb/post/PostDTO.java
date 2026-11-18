package com.devclimb.post;

import java.time.Instant;
import java.util.List;

public record PostDTO(
		Integer id,
		Integer communityId,
		AuthorDTO author,
		String title,
		String content,
		List<String> images,
		Instant createdAt,
		Instant updatedAt) {

	public PostDTO(Integer id, Integer communityId, Integer authorId, String authorName, String authorImageUrl,
			String title, String content, Instant createdAt, Instant updatedAt) {
		this(id, communityId, new AuthorDTO(authorId, authorName, authorImageUrl), title, content, List.of(),
				createdAt, updatedAt);
	}

	public static PostDTO of(Post post) {
		return new PostDTO(post.getId(), post.getCommunity().getId(), AuthorDTO.of(post.getAuthor()),
				post.getTitle(), post.getContent(), List.of(), post.getCreatedAt(), post.getUpdatedAt());
	}
}
