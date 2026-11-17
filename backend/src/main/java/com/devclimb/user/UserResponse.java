package com.devclimb.user;

public record UserResponse(Integer id, String email, String name, String imageUrl, boolean admin) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getImageUrl(), user.isAdmin());
	}
}
