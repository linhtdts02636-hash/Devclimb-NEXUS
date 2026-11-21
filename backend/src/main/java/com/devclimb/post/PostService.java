package com.devclimb.post;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
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
import com.devclimb.role.CommunityAccessService;
import com.devclimb.role.CommunityPermission;
import com.devclimb.role.MemberRoleDTO;
import com.devclimb.role.RoleService;
import com.devclimb.user.User;

@Service
public class PostService {

	private static final int MAXIMUM_TITLE_LENGTH = 255;
	private static final int PAGE_SIZE = 20;

	private final PostRepository postRepository;
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final AuthenticationService authenticationService;
	private final CommunityAccessService communityAccessService;
	private final RoleService roleService;

	public PostService(PostRepository postRepository, CommunityRepository communityRepository,
			UserCommunityRepository userCommunityRepository, AuthenticationService authenticationService,
			CommunityAccessService communityAccessService, RoleService roleService) {
		this.postRepository = postRepository;
		this.communityRepository = communityRepository;
		this.userCommunityRepository = userCommunityRepository;
		this.authenticationService = authenticationService;
		this.communityAccessService = communityAccessService;
		this.roleService = roleService;
	}

	@Transactional
	public PostDTO create(AuthenticatedFirebaseUser firebaseUser, Integer communityId, CreatePostRequest request) {
		User author = authenticationService.findCurrentUser(firebaseUser);
		Community community = findActiveCommunity(communityId);
		requireMembership(author, communityId);
		Post post = new Post(author, community, validateTitle(request.title()), requireContent(request.content()));
		return withAuthorRole(PostDTO.of(postRepository.save(post)));
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
			return withAuthorRole(PostDTO.of(post));
		}

		post.update(trimmedTitle, trimmedContent);
		return withAuthorRole(PostDTO.of(postRepository.save(post)));
	}

	@Transactional
	public PostDTO setPinned(AuthenticatedFirebaseUser firebaseUser, Integer postId, boolean pinned) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Post post = findPost(postId);
		communityAccessService.requirePermission(user.getId(), post.getCommunity().getId(), CommunityPermission.PIN_POSTS);
		post.setPinned(pinned);
		return withAuthorRole(PostDTO.of(postRepository.save(post)));
	}

	@Transactional
	public void delete(AuthenticatedFirebaseUser firebaseUser, Integer postId) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Post post = findPost(postId);
		boolean isAuthor = post.getAuthor().getId().equals(user.getId());
		if (!isAuthor) {
			communityAccessService.requirePermission(user.getId(), post.getCommunity().getId(),
					CommunityPermission.REMOVE_CONTENT);
		}
		postRepository.delete(post);
	}

	@Transactional(readOnly = true)
	public PostPageDTO listFeed(AuthenticatedFirebaseUser firebaseUser, Integer communityId, Integer before) {
		requireReadable(firebaseUser, communityId);

		int beforeId = before == null ? Integer.MAX_VALUE : before;
		// Lấy dư một bài để biết còn trang sau hay không mà không cần đếm.
		List<PostDTO> fetched = postRepository.findFeedPage(communityId, beforeId, PageRequest.of(0, PAGE_SIZE + 1));
		boolean hasMore = fetched.size() > PAGE_SIZE;
		List<PostDTO> page = hasMore ? fetched.subList(0, PAGE_SIZE) : fetched;

		Integer nextCursor = hasMore ? page.get(page.size() - 1).id() : null;
		return new PostPageDTO(withAuthorRoles(page, communityId), nextCursor);
	}

	@Transactional(readOnly = true)
	public List<PostDTO> listPinned(AuthenticatedFirebaseUser firebaseUser, Integer communityId) {
		requireReadable(firebaseUser, communityId);
		return withAuthorRoles(postRepository.findPinned(communityId), communityId);
	}

	private void requireReadable(AuthenticatedFirebaseUser firebaseUser, Integer communityId) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = findActiveCommunity(communityId);
		if (!community.isPublic()) {
			requireMembership(user, communityId);
		}
	}

	private List<PostDTO> withAuthorRoles(List<PostDTO> posts, Integer communityId) {
		Map<Integer, MemberRoleDTO> displayRoles = roleService.displayRolesByMemberOf(communityId);
		return posts.stream().map(post -> post.withAuthorRole(displayRoles.get(post.author().userId()))).toList();
	}

	private PostDTO withAuthorRole(PostDTO post) {
		return post.withAuthorRole(
				roleService.displayRolesByMemberOf(post.communityId()).get(post.author().userId()));
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
