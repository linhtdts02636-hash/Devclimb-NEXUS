package com.devclimb.post;

import com.devclimb.common.security.CurrentUser;
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
    public ResponseEntity<PostDto> createPost(
            @PathVariable Integer communityId,
            Authentication authentication,
            @RequestBody(required = false) PostRequest request
    ) {
        String title = request == null ? null : request.title();
        String content = request == null ? null : request.content();
        PostDto response = postService.createPost(
                communityId, CurrentUser.from(authentication), title, content);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts(
            @PathVariable Integer communityId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                postService.getPosts(communityId, CurrentUser.from(authentication)));
    }
}
