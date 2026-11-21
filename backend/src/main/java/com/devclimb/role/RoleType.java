package com.devclimb.role;

import java.util.Locale;

public enum RoleType {

	OWNER,
	DEFAULT,
	CUSTOM;

	public static final String OWNER_ROLE_NAME = "Owner";
	public static final String DEFAULT_ROLE_NAME = "Member";

	public static RoleType of(Role role) {
		String lowerCaseName = role.getName().toLowerCase(Locale.ROOT);
		if (lowerCaseName.equals(OWNER_ROLE_NAME.toLowerCase(Locale.ROOT))) {
			return OWNER;
		}
		if (lowerCaseName.equals(DEFAULT_ROLE_NAME.toLowerCase(Locale.ROOT))) {
			return DEFAULT;
		}
		return CUSTOM;
	}
}
