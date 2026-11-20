package com.devclimb.post;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.community.Community;
import com.devclimb.community.CommunityRepository;
import com.devclimb.community.UserCommunityId;
import com.devclimb.community.UserCommunityRepository;
import com.devclimb.reaction.ReactionService;
import com.devclimb.reaction.ReactionSummaryDTO;
import com.devclimb.user.User;

@Service
public class PostService {

	private static final int MAXIMUM_TITLE_LENGTH = 255;

	private final PostRepository postRepository;
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final AuthenticationService authenticationService;
	private final ReactionService reactionService;

	public PostService(PostRepository postRepository, CommunityRepository communityRepository,
			UserCommunityRepository userCommunityRepository, AuthenticationService authenticationService,
			ReactionService reactionService) {
		this.postRepository = postRepository;
		this.communityRepository = communityRepository;
		this.userCommunityRepository = userCommunityRepository;
		this.authenticationService = authenticationService;
		this.reactionService = reactionService;
	}

	@Transactional
	public PostDTO create(AuthenticatedFirebaseUser firebaseUser, Integer communityId, CreatePostRequest request) {
		User author = authenticationService.findCurrentUser(firebaseUser);
		Community community = findActiveCommunity(communityId);
		requireMembership(author, communityId);
		Post post = new Post(author, community, validateTitle(request.title()), requireContent(request.content()));
		return PostDTO.of(postRepository.save(post));
	}

	@Transactional
	public PostDTO update(AuthenticatedFirebaseUser firebaseUser, Integer postId, UpdatePostRequest request) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Post post = findPost(postId);
		requireAuthor(post, user);

		String trimmedContent = requireContent(request.content());
		String trimmedTitle = validateTitle(request.title());

		// Không đánh dấu "đã chỉnh sửa" nếu nội dung gửi lên không thay đổi.
		if (Objects.equals(post.getTitle(), trimmedTitle)
				&& Objects.equals(post.getContent(), trimmedContent)) {
			return withReaction(PostDTO.of(post), user.getId());
		}

		post.update(trimmedTitle, trimmedContent);
		return withReaction(PostDTO.of(postRepository.save(post)), user.getId());
	}

	@Transactional
	public void delete(AuthenticatedFirebaseUser firebaseUser, Integer postId) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Post post = findPost(postId);
		requireAuthor(post, user);
		postRepository.delete(post);
	}

	@Transactional(readOnly = true)
	public List<PostDTO> listFeed(AuthenticatedFirebaseUser firebaseUser, Integer communityId) {
		// Guest chỉ xem được cộng đồng công khai; cộng đồng riêng tư vẫn giữ nguyên quy tắc membership.
		User user = firebaseUser == null ? null : authenticationService.findCurrentUser(firebaseUser);
		Community community = findActiveCommunity(communityId);

		if (!community.isPublic()) {
			if (user == null) {
				throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
			}
			requireMembership(user, communityId);
		}

		List<PostDTO> posts = postRepository.findFeed(communityId);
		if (posts.isEmpty()) {
			return posts;
		}

		Map<Integer, ReactionSummaryDTO> summaries = reactionService.summariesFor(
				posts.stream().map(PostDTO::id).toList(), user == null ? null : user.getId());
		return posts.stream()
				.map(post -> post.withReaction(summaries.getOrDefault(post.id(), ReactionSummaryDTO.empty())))
				.toList();
	}

	private PostDTO withReaction(PostDTO post, Integer userId) {
		return post.withReaction(reactionService.summaryFor(post.id(), userId));
	}

	private void requireMembership(User user, Integer communityId) {
		if (!userCommunityRepository.existsById(new UserCommunityId(user.getId(), communityId))) {
			throw new ApiException(ErrorCode.NOT_A_MEMBER);
		}
	}

	private Post findPost(Integer postId) {
		return postRepository.findById(postId)
				.orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
	}

	private void requireAuthor(Post post, User user) {
		if (!post.getAuthor().getId().equals(user.getId())) {
			throw new ApiException(ErrorCode.POST_FORBIDDEN);
		}
	}

	private String requireContent(String content) {
		String trimmedContent = content == null ? "" : content.strip();
		if (trimmedContent.isEmpty()) {
			throw new ApiException(ErrorCode.POST_CONTENT_EMPTY);
		}
		return trimmedContent;
	}

	private String validateTitle(String title) {
		// Tiêu đề là phần bổ sung, không bắt buộc; tiêu đề rỗng được lưu thành null.
		String trimmedTitle = title == null ? "" : title.strip();
		if (trimmedTitle.length() > MAXIMUM_TITLE_LENGTH) {
			throw new ApiException(ErrorCode.POST_TITLE_TOO_LONG);
		}
		return trimmedTitle.isEmpty() ? null : trimmedTitle;
	}

	private Community findActiveCommunity(Integer communityId) {
		return communityRepository.findById(communityId)
				.filter(Community::isActive)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
	}
}
