package com.devclimb.post.dto;

import com.devclimb.post.entity.Attachment;
import com.devclimb.post.entity.Post;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Integer id,
        Integer communityId,
        AuthorResponse author,
        String content,
        List<AttachmentResponse> attachments,
        Instant createdAt
) {

    public static PostResponse from(Post post) {
        List<AttachmentResponse> attachmentResponses = post.getAttachments() == null
                ? List.of()
                : post.getAttachments().stream()
                        .map(PostResponse::toAttachmentResponse)
                        .toList();

        return new PostResponse(
                post.getId(),
                post.getCommunity().getId(),
                new AuthorResponse(
                        post.getAuthor().getId(),
                        post.getAuthor().getName(),
                        post.getAuthor().getImage()
                ),
                post.getContent(),
                attachmentResponses,
                post.getCreatedAt()
        );
    }

    private static AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getType().name(),
                attachment.getUrl()
        );
    }
}
