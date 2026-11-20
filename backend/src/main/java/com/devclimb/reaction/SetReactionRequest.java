package com.devclimb.reaction;

// Body PUT chỉ nhận type; sai enum thì Spring trả INVALID_REQUEST.
public record SetReactionRequest(ReactionType type) {
}
