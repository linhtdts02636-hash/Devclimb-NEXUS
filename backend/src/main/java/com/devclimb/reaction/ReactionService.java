package com.devclimb.reaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.post.Post;
import com.devclimb.post.PostRepository;
import com.devclimb.user.User;

@Service
public class ReactionService {

	private final ReactionRepository reactionRepository;
	private final PostRepository postRepository;
	private final AuthenticationService authenticationService;

	public ReactionService(ReactionRepository reactionRepository, PostRepository postRepository,
			AuthenticationService authenticationService) {
		this.reactionRepository = reactionRepository;
		this.postRepository = postRepository;
		this.authenticationService = authenticationService;
	}

	@Transactional
	public ReactionSummaryDTO setReaction(AuthenticatedFirebaseUser firebaseUser, Integer postId,
			SetReactionRequest request) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		ReactionType type = request == null ? null : request.type();
		if (type == null) {
			throw new ApiException(ErrorCode.INVALID_REQUEST);
		}

		requireReactablePost(postId);
		reactionRepository.upsert(postId, user.getId(), type.name());
		return summaryFor(postId, user.getId());
	}

	@Transactional
	public ReactionSummaryDTO removeReaction(AuthenticatedFirebaseUser firebaseUser, Integer postId) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		requireReactablePost(postId);
		// Xoá idempotent: không có reaction thì vẫn trả về summary bình thường.
		reactionRepository.deleteByPostAndUser(postId, user.getId());
		return summaryFor(postId, user.getId());
	}

	@Transactional(readOnly = true)
	public ReactionSummaryDTO summaryFor(Integer postId, Integer userId) {
		return summariesFor(List.of(postId), userId).getOrDefault(postId, ReactionSummaryDTO.empty());
	}

	@Transactional(readOnly = true)
	public Map<Integer, ReactionSummaryDTO> summariesFor(Collection<Integer> postIds, Integer userId) {
		if (postIds == null || postIds.isEmpty()) {
			return Map.of();
		}

		Map<Integer, ReactionCount> counts = reactionRepository.countByPostIds(postIds).stream()
				.collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> new ReactionCount(
						((Number) row[0]).intValue(),
						row[1] == null ? 0L : ((Number) row[1]).longValue(),
						row[2] == null ? 0L : ((Number) row[2]).longValue())));
		Map<Integer, ReactionType> myReactions = userId == null ? Map.of()
				: reactionRepository.findByUserIdAndPostIds(userId, postIds).stream()
						.collect(Collectors.toMap(reaction -> reaction.getId().postId(), Reaction::getType));

		return postIds.stream().distinct().collect(Collectors.toMap(Function.identity(), postId -> {
			ReactionCount count = counts.get(postId);
			long upvotes = count == null || count.upvotes() == null ? 0 : count.upvotes();
			long downvotes = count == null || count.downvotes() == null ? 0 : count.downvotes();
			return new ReactionSummaryDTO(upvotes, downvotes, myReactions.get(postId));
		}));
	}

	// Reaction chỉ áp dụng cho bài post gốc; comment/reply cũng là post nhưng có parent nên bị từ chối.
	private void requireReactablePost(Integer postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
		if (post.getParent() != null) {
			throw new ApiException(ErrorCode.POST_NOT_FOUND);
		}
	}
}
