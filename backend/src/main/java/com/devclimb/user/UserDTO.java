package com.devclimb.user;

public record UserDTO(Integer id, String email, String name, String imageUrl, boolean admin) {

	public static UserDTO from(User user) {
		return new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getImageUrl(), user.isAdmin());
	}
}
