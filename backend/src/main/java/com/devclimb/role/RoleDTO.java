package com.devclimb.role;

import java.util.List;

public record RoleDTO(
		Integer id,
		String name,
		String color,
		RoleType type,
		List<String> permissions,
		long memberCount) {
}
