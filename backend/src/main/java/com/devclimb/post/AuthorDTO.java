package com.devclimb.post;

import com.devclimb.role.MemberRoleDTO;
import com.devclimb.user.User;

// role là vai trò cao nhất (không tính vai trò mặc định) của tác giả trong cộng đồng; null nếu chỉ có vai trò Member mặc định.
public record AuthorDTO(Integer userId, String name, String imageUrl, MemberRoleDTO role) {

	// Dùng cho biểu thức constructor JPQL trong PostRepository; vai trò được gắn vào sau.
	public AuthorDTO(Integer userId, String name, String imageUrl) {
		this(userId, name, imageUrl, null);
	}

	public static AuthorDTO of(User user) {
		return new AuthorDTO(user.getId(), user.getName(), user.getImageUrl());
	}

	public AuthorDTO withRole(MemberRoleDTO role) {
		return new AuthorDTO(userId, name, imageUrl, role);
	}
}
