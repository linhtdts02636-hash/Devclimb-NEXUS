package com.devclimb.common.security;

public record AuthenticatedFirebaseUser(
		String firebaseUid,
		String email,
		String displayName,
		String pictureUrl,
		boolean emailVerified,
		String signInProvider) {

	private static final String PASSWORD_SIGN_IN_PROVIDER = "password";

	public boolean signedInWithPassword() {
		return PASSWORD_SIGN_IN_PROVIDER.equals(signInProvider);
	}
}
