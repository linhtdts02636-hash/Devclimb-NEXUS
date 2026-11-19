package com.devclimb.post;

import com.devclimb.user.User;

public record AuthorDTO(Integer userId, String name, String imageUrl) {

	public static AuthorDTO of(User user) {
		return new AuthorDTO(user.getId(), user.getName(), user.getImageUrl());
	}
}
