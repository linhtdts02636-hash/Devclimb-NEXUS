package com.devclimb.post;

import java.time.Instant;
import java.util.List;

import com.devclimb.reaction.ReactionSummaryDTO;

public record PostDTO(
		Integer id,
		Integer communityId,
		AuthorDTO author,
		String title,
		String content,
		List<String> images,
		Instant createdAt,
		Instant updatedAt,
		ReactionSummaryDTO reaction) {

	public PostDTO(Integer id, Integer communityId, Integer authorId, String authorName, String authorImageUrl,
			String title, String content, Instant createdAt, Instant updatedAt) {
		this(id, communityId, new AuthorDTO(authorId, authorName, authorImageUrl), title, content, List.of(),
				createdAt, updatedAt, ReactionSummaryDTO.empty());
	}

	public static PostDTO of(Post post) {
		return new PostDTO(post.getId(), post.getCommunity().getId(), AuthorDTO.of(post.getAuthor()),
				post.getTitle(), post.getContent(), List.of(), post.getCreatedAt(), post.getUpdatedAt(),
				ReactionSummaryDTO.empty());
	}

	// Trả bản sao có summary mà không sửa DTO gốc.
	public PostDTO withReaction(ReactionSummaryDTO reaction) {
		return new PostDTO(id, communityId, author, title, content, images, createdAt, updatedAt,
				reaction == null ? ReactionSummaryDTO.empty() : reaction);
	}
}
