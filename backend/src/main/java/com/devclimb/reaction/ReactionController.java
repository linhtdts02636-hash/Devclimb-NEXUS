package com.devclimb.reaction;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

@RestController
public class ReactionController {

	private static final String FIREBASE_USER = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE;

	private final ReactionService reactionService;

	public ReactionController(ReactionService reactionService) {
		this.reactionService = reactionService;
	}

	// Cả PUT và DELETE đều trả summary để frontend khớp lại số liệu ngay.
	@PutMapping("/api/posts/{postId}/reaction")
	public ReactionSummaryDTO setReaction(
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer postId, @RequestBody SetReactionRequest request) {
		return reactionService.setReaction(firebaseUser, postId, request);
	}

	@DeleteMapping("/api/posts/{postId}/reaction")
	public ReactionSummaryDTO removeReaction(
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer postId) {
		return reactionService.removeReaction(firebaseUser, postId);
	}
}
