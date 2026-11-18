package com.devclimb.submission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

	private final SubmissionService submissionService;

	public SubmissionController(SubmissionService submissionService) {
		this.submissionService = submissionService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SubmissionResult submit(
			@Valid @RequestBody SubmitCodeRequest request,
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser) {
		return submissionService.submit(request, firebaseUser);
	}
}
