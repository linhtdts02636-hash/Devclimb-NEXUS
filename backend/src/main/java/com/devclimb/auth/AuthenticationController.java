package com.devclimb.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;
import com.devclimb.user.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/session")
	public UserResponse signIn(
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser) {
		return UserResponse.from(authenticationService.signIn(firebaseUser));
	}

	@GetMapping("/me")
	public UserResponse currentUser(
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser) {
		return UserResponse.from(authenticationService.findCurrentUser(firebaseUser));
	}
}
