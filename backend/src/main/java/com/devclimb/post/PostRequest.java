package com.devclimb.post;

// title không bắt buộc để tương thích với các client cũ chỉ gửi content.
public record PostRequest(String title, String content) {
}
