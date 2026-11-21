package com.devclimb.role;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// perm_name lưu tên hằng số, nên đổi tên hằng số phải kèm migration dữ liệu. Để bảo vệ một tính năng mới, thêm một
// hằng số ở đây rồi gọi CommunityAccessService.requirePermission(...) ở điểm vào của tính năng đó.
public enum CommunityPermission {

	PIN_POSTS("Pin posts", "Pin and unpin posts at the top of the community."),
	REMOVE_CONTENT("Remove posts and comments", "Take down content that breaks the rules."),
	CREATE_PUZZLES("Create puzzles", "Add problems to the community library."),
	RUN_CONTESTS("Run contests", "Create and edit contests."),
	MANAGE_MEMBERS("Manage members", "Assign roles and remove members."),
	ATTACH_FILES("Attach files", "Attach files to posts. Files are not available yet."),
	COMMENT("Comment", "Comment on posts. Comments are not available yet.");

	private final String label;
	private final String description;

	CommunityPermission(String label, String description) {
		this.label = label;
		this.description = description;
	}

	// Có bất kỳ quyền nào trong nhóm này thì được coi là người điều hành; thẻ Moderators trên trang cộng đồng liệt kê họ.
	public static final Set<CommunityPermission> MODERATION = Collections.unmodifiableSet(
			EnumSet.of(PIN_POSTS, REMOVE_CONTENT, MANAGE_MEMBERS));

	public static List<String> namesOf(Collection<CommunityPermission> permissions) {
		return permissions.stream().sorted().map(Enum::name).toList();
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
}
