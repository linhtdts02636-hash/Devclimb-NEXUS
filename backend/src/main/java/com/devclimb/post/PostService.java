package com.devclimb.post;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.security.CurrentUser;
import com.devclimb.community.entity.Community;
import com.devclimb.community.repository.CommunityRepository;
import com.devclimb.community.repository.UserCommunityRepository;
import com.devclimb.user.User;
import com.devclimb.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       CommunityRepository communityRepository,
                       UserCommunityRepository userCommunityRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostDto createPost(Integer communityId, CurrentUser currentUser, String content) {
        if (currentUser == null) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHENTICATED",
                    "Bạn cần đăng nhập để đăng bài."
            );
        }

        Community community = findActiveCommunity(communityId);

        String trimmedContent = content == null ? "" : content.strip();
        if (trimmedContent.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "POST_CONTENT_EMPTY",
                    "Nội dung bài viết không được để trống."
            );
        }

        if (userCommunityRepository.countMembership(currentUser.id(), communityId) == 0) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "NOT_COMMUNITY_MEMBER",
                    "Bạn phải là thành viên của cộng đồng mới có thể đăng bài."
            );
        }

        User author = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHENTICATED",
                        "Người dùng không tồn tại."
                ));

        Post post = new Post();
        post.setAuthor(author);
        post.setCommunity(community);
        post.setContent(trimmedContent);
        post.setCreatedAt(Instant.now());

        Post savedPost = postRepository.save(post);
        return PostDto.from(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getPosts(Integer communityId, CurrentUser currentUser) {
        Community community = findActiveCommunity(communityId);

        if (!community.isPublicCommunity()) {
            if (currentUser == null) {
                throw new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHENTICATED",
                        "Bạn cần đăng nhập để xem cộng đồng này."
                );
            }
            if (userCommunityRepository.countMembership(currentUser.id(), communityId) == 0) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "NOT_COMMUNITY_MEMBER",
                        "Bạn không phải là thành viên của cộng đồng này."
                );
            }
        }

        return postRepository.findFeed(communityId).stream()
                .map(PostDto::from)
                .toList();
    }

    private Community findActiveCommunity(Integer communityId) {
        return communityRepository.findById(communityId)
                .filter(Community::isActive)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "COMMUNITY_NOT_FOUND",
                        "Cộng đồng không tồn tại."
                ));
    }
}
