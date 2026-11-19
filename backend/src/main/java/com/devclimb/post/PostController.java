package com.devclimb.post;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

@RestController
public class PostController {

	private static final String FIREBASE_USER = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE;

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@PostMapping("/api/communities/{communityId}/posts")
	@ResponseStatus(HttpStatus.CREATED)
	public PostDTO create(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer communityId, @RequestBody CreatePostRequest request) {
		return postService.create(firebaseUser, communityId, request);
	}

	@GetMapping("/api/communities/{communityId}/posts")
	public List<PostDTO> listFeed(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer communityId) {
		return postService.listFeed(firebaseUser, communityId);
	}

	@PutMapping("/api/posts/{postId}")
	public PostDTO update(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer postId, @RequestBody UpdatePostRequest request) {
		return postService.update(firebaseUser, postId, request);
	}

	@DeleteMapping("/api/posts/{postId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable Integer postId) {
		postService.delete(firebaseUser, postId);
	}
}
