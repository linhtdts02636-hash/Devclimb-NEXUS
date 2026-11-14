package com.devclimb.post;

import com.devclimb.attachment.Attachment;
import com.devclimb.attachment.AttachmentDto;

import java.time.Instant;
import java.util.List;

public record PostDto(
        Integer id,
        Integer communityId,
        AuthorDto author,
        String content,
        List<AttachmentDto> attachments,
        Instant createdAt
) {

    public static PostDto from(Post post) {
        List<AttachmentDto> attachmentDtos = post.getAttachments() == null
                ? List.of()
                : post.getAttachments().stream()
                        .map(PostDto::toAttachmentDto)
                        .toList();

        return new PostDto(
                post.getId(),
                post.getCommunity().getId(),
                new AuthorDto(
                        post.getAuthor().getId(),
                        post.getAuthor().getName(),
                        post.getAuthor().getImageUrl()
                ),
                post.getContent(),
                attachmentDtos,
                post.getCreatedAt()
        );
    }

    private static AttachmentDto toAttachmentDto(Attachment attachment) {
        return new AttachmentDto(
                attachment.getId(),
                attachment.getType().name(),
                attachment.getUrl()
        );
    }
}
