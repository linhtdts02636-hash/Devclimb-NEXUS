package com.devclimb.reaction;

// Summary mặc định dùng cho bài chưa có reaction.
public record ReactionSummaryDTO(long upvotes, long downvotes, ReactionType myReaction) {

	private static final ReactionSummaryDTO EMPTY = new ReactionSummaryDTO(0, 0, null);

	public static ReactionSummaryDTO empty() {
		return EMPTY;
	}
}
