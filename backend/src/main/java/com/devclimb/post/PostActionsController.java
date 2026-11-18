package com.devclimb.post;

import com.devclimb.common.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostActionsController {

    private final PostService postService;

    public PostActionsController(PostService postService) {
        this.postService = postService;
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Integer postId,
            Authentication authentication,
            @RequestBody(required = false) PostRequest request
    ) {
        String title = request == null ? null : request.title();
        String content = request == null ? null : request.content();
        PostDto response = postService.updatePost(
                postId, CurrentUser.from(authentication), title, content);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Integer postId,
            Authentication authentication
    ) {
        postService.deletePost(postId, CurrentUser.from(authentication));
        return ResponseEntity.noContent().build();
    }
}
