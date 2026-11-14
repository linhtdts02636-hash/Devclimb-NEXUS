package com.devclimb.post.controller;

import com.devclimb.common.security.CurrentUser;
import com.devclimb.post.dto.CreatePostRequest;
import com.devclimb.post.dto.PostResponse;
import com.devclimb.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communities/{communityId}/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Integer communityId,
            Authentication authentication,
            @RequestBody(required = false) CreatePostRequest request
    ) {
        String content = request == null ? null : request.content();
        PostResponse response = postService.createPost(communityId, currentUser(authentication), content);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPosts(
            @PathVariable Integer communityId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(postService.getPosts(communityId, currentUser(authentication)));
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }
        return null;
    }
}
