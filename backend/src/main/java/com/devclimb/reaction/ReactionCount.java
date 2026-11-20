package com.devclimb.reaction;

// Một hàng kết quả của truy vấn đếm gộp theo post_id.
public record ReactionCount(Integer postId, Long upvotes, Long downvotes) {
}
