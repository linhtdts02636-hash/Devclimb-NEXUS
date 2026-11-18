package com.devclimb.upload;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

	private final AuthenticationService authenticationService;
	private final ImageUploadService imageUploadService;

	public UploadController(AuthenticationService authenticationService, ImageUploadService imageUploadService) {
		this.authenticationService = authenticationService;
		this.imageUploadService = imageUploadService;
	}

	@PostMapping("/community-image")
	public ImageUploadDTO createCommunityImageUpload(
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser,
			@RequestBody ImageUploadRequest request) {
		return imageUploadService.createCommunityImageUpload(authenticationService.findCurrentUser(firebaseUser), request);
	}
}
