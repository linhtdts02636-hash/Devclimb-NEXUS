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
import java.util.Objects;

@Service
public class PostService {

    private static final int MAX_TITLE_LENGTH = 255;

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
    public PostDto createPost(Integer communityId, CurrentUser currentUser, String title, String content) {
        requireAuthenticated(currentUser);
        Community community = findActiveCommunity(communityId);

        String trimmedContent = requireContent(content);
        String trimmedTitle = validateTitle(title);
        User author = findAuthor(currentUser);

        Post post = new Post();
        post.setAuthor(author);
        post.setCommunity(community);
        post.setTitle(trimmedTitle);
        post.setContent(trimmedContent);
        post.setCreatedAt(Instant.now());

        return PostDto.from(postRepository.save(post));
    }

    @Transactional
    public PostDto updatePost(Integer postId, CurrentUser currentUser, String title, String content) {
        requireAuthenticated(currentUser);
        Post post = findPost(postId);
        requireAuthor(post, currentUser);

        String trimmedContent = requireContent(content);
        String trimmedTitle = validateTitle(title);

        // Không đánh dấu "đã chỉnh sửa" nếu nội dung gửi lên không thay đổi.
        if (Objects.equals(post.getTitle(), trimmedTitle)
                && Objects.equals(post.getContent(), trimmedContent)) {
            return PostDto.from(post);
        }

        post.setTitle(trimmedTitle);
        post.setContent(trimmedContent);
        post.setUpdatedAt(Instant.now());

        return PostDto.from(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Integer postId, CurrentUser currentUser) {
        requireAuthenticated(currentUser);
        Post post = findPost(postId);
        requireAuthor(post, currentUser);
        postRepository.delete(post);
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

    private void requireAuthenticated(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHENTICATED",
                    "Bạn cần đăng nhập để thực hiện thao tác này."
            );
        }
    }

    private Post findPost(Integer postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "POST_NOT_FOUND",
                        "Bài viết không tồn tại."
                ));
    }

    private void requireAuthor(Post post, CurrentUser currentUser) {
        if (!post.getAuthor().getId().equals(currentUser.id())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "POST_FORBIDDEN",
                    "Bạn không có quyền thay đổi bài viết này."
            );
        }
    }

    private String requireContent(String content) {
        String trimmedContent = content == null ? "" : content.strip();
        if (trimmedContent.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "POST_CONTENT_EMPTY",
                    "Nội dung bài viết không được để trống."
            );
        }
        return trimmedContent;
    }

    private String validateTitle(String title) {
        // Tiêu đề là phần bổ sung, không bắt buộc; tiêu đề rỗng được lưu thành null.
        String trimmedTitle = title == null ? "" : title.strip();
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "POST_TITLE_TOO_LONG",
                    "Tiêu đề bài viết không được dài quá 255 ký tự."
            );
        }
        return trimmedTitle.isEmpty() ? null : trimmedTitle;
    }

    private User findAuthor(CurrentUser currentUser) {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHENTICATED",
                        "Người dùng không tồn tại."
                ));
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
