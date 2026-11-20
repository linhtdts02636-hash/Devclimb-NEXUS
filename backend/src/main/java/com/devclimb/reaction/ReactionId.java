package com.devclimb.reaction;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Khóa ghép theo cặp (post_id, user_id) để mỗi user chỉ có một reaction cho mỗi post.
@Embeddable
public record ReactionId(
		@Column(name = "post_id") Integer postId,
		@Column(name = "user_id") Integer userId) implements Serializable {
}
