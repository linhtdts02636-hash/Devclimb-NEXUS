package com.devclimb.community;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.config.R2Properties;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.role.CommunityAccessService;
import com.devclimb.role.MemberRoleDTO;
import com.devclimb.role.RoleService;
import com.devclimb.upload.ImageUploadService;
import com.devclimb.user.User;

@Service
public class CommunityService {

	private static final int MINIMUM_NAME_LENGTH = 3;
	private static final int MAXIMUM_NAME_LENGTH = 50;
	private static final int MAXIMUM_SHOUTOUT_LENGTH = 300;
	private static final int MAXIMUM_LOOKUP_IDS = 20;
	private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final AuthenticationService authenticationService;
	private final R2Properties r2Properties;
	private final RoleService roleService;
	private final CommunityAccessService communityAccessService;

	public CommunityService(CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository,
			AuthenticationService authenticationService, R2Properties r2Properties, RoleService roleService,
			CommunityAccessService communityAccessService) {
		this.communityRepository = communityRepository;
		this.userCommunityRepository = userCommunityRepository;
		this.authenticationService = authenticationService;
		this.r2Properties = r2Properties;
		this.roleService = roleService;
		this.communityAccessService = communityAccessService;
	}

	@Transactional
	public CommunityDTO create(AuthenticatedFirebaseUser firebaseUser, CreateCommunityRequest request) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		String name = normalizeName(request.name());
		if (communityRepository.existsByName(name)) {
			throw new ApiException(ErrorCode.COMMUNITY_NAME_TAKEN);
		}

		Community community = new Community(name, cleanShoutout(request.shoutout()), request.isPublic(),
				cleanImageUrl(request.imageUrl()), cleanImageUrl(request.bannerUrl()));
		try {
			communityRepository.saveAndFlush(community);
		} catch (DataIntegrityViolationException exception) {
			// Hai người tạo cùng một tên gần như cùng lúc: bản ghi thứ hai vi phạm ràng buộc UNIQUE.
			throw new ApiException(ErrorCode.COMMUNITY_NAME_TAKEN);
		}
		userCommunityRepository.save(new UserCommunity(user.getId(), community.getId(), true));
		roleService.assignOwnerRole(user.getId(), community.getId());
		return CommunityDTO.of(community, 1, true, true).withAllPermissions();
	}

	@Transactional(readOnly = true)
	public CommunityDTO get(AuthenticatedFirebaseUser firebaseUser, String name) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		CommunityDTO community = requireVisibleCommunityView(name, user);
		return community.withPermissions(communityAccessService.permissionNamesOf(user.getId(), community.id()));
	}

	@Transactional(readOnly = true)
	public List<CommunityDTO> listByIds(AuthenticatedFirebaseUser firebaseUser, List<Integer> ids) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		if (ids.isEmpty()) {
			return List.of();
		}
		if (ids.size() > MAXIMUM_LOOKUP_IDS) {
			throw new ApiException(ErrorCode.TOO_MANY_IDS);
		}
		return communityRepository.findViewableByIds(ids, user.getId());
	}

	@Transactional(readOnly = true)
	public List<CommunityDTO> listJoined(AuthenticatedFirebaseUser firebaseUser) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		return communityRepository.findJoinedBy(user.getId());
	}

	@Transactional(readOnly = true)
	public List<MemberDTO> listMembers(AuthenticatedFirebaseUser firebaseUser, String name) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		CommunityDTO community = requireVisibleCommunityView(name, user);
		Map<Integer, List<MemberRoleDTO>> rolesByMember = roleService.rolesByMemberOf(community.id());
		Set<Integer> moderatorUserIds = roleService.moderatorUserIdsOf(community.id());
		return userCommunityRepository.findMembersOf(community.id()).stream()
				.map(member -> member.withRoles(rolesByMember.getOrDefault(member.userId(), List.of()),
						moderatorUserIds.contains(member.userId())))
				.toList();
	}

	@Transactional
	public CommunityDTO update(AuthenticatedFirebaseUser firebaseUser, String name, UpdateCommunityRequest request) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireOwnedCommunity(name, user);
		if (request.name() != null) {
			renameIfChanged(community, normalizeName(request.name()));
		}
		community.update(cleanShoutout(request.shoutout()), request.isPublic(), cleanImageUrl(request.imageUrl()),
				cleanImageUrl(request.bannerUrl()));
		return CommunityDTO.of(community, userCommunityRepository.countByIdCommunityId(community.getId()), true, true)
				.withAllPermissions();
	}

	// Link cũ dùng tên trước khi đổi sẽ không còn tìm thấy cộng đồng (quyết định có chủ đích: không lưu lịch sử tên).
	private void renameIfChanged(Community community, String newName) {
		if (newName.equals(community.getName())) {
			return;
		}
		if (communityRepository.existsByName(newName)) {
			throw new ApiException(ErrorCode.COMMUNITY_NAME_TAKEN);
		}
		community.rename(newName);
		try {
			communityRepository.flush();
		} catch (DataIntegrityViolationException exception) {
			throw new ApiException(ErrorCode.COMMUNITY_NAME_TAKEN);
		}
	}

	@Transactional
	public void delete(AuthenticatedFirebaseUser firebaseUser, String name, DeleteCommunityRequest request) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireOwnedCommunity(name, user);
		if (request == null || !community.getName().equals(request.confirmName())) {
			throw new ApiException(ErrorCode.DELETE_CONFIRMATION_MISMATCH);
		}

		Integer communityId = community.getId();
		communityRepository.deleteAppealsOf(communityId);
		communityRepository.deleteWarningsOf(communityId);
		communityRepository.deleteSubmissionsOf(communityId);
		communityRepository.deleteAchievementsOf(communityId);
		communityRepository.deleteContestsOf(communityId);
		communityRepository.deletePuzzlesOf(communityId);
		roleService.deleteCustomRolesOf(communityId);
		communityRepository.deleteCommunityRow(communityId);
	}

	@Transactional
	public CommunityDTO join(AuthenticatedFirebaseUser firebaseUser, String name) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(name, user);
		if (findMembership(user, community).isPresent()) {
			throw new ApiException(ErrorCode.ALREADY_MEMBER);
		}
		// Cộng đồng riêng tư đã bị requireVisibleCommunity chặn, nên tới đây chắc chắn là cộng đồng công khai.
		userCommunityRepository.save(new UserCommunity(user.getId(), community.getId(), false));
		roleService.assignDefaultRole(user.getId(), community.getId());
		return CommunityDTO.of(community, userCommunityRepository.countByIdCommunityId(community.getId()), true, false);
	}

	@Transactional
	public void leave(AuthenticatedFirebaseUser firebaseUser, String name) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(name, user);
		UserCommunity membership = findMembership(user, community)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_A_MEMBER));
		if (membership.isOwner()) {
			throw new ApiException(ErrorCode.OWNER_CANNOT_LEAVE);
		}
		userCommunityRepository.delete(membership);
	}

	private Optional<UserCommunity> findMembership(User user, Community community) {
		return userCommunityRepository.findById(new UserCommunityId(user.getId(), community.getId()));
	}

	// Cộng đồng riêng tư trả về "không tồn tại" cho người ngoài để không lộ việc nó có tồn tại.
	private Community requireVisibleCommunity(String name, User user) {
		Community community = communityRepository.findByNameAndActiveTrue(name)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		if (!community.isPublic() && findMembership(user, community).isEmpty()) {
			throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
		}
		return community;
	}

	// Cùng quy tắc với requireVisibleCommunity nhưng chỉ tốn một truy vấn và không cần thực thể.
	private CommunityDTO requireVisibleCommunityView(String name, User user) {
		CommunityDTO community = communityRepository.findViewableByName(name, user.getId())
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		if (!community.isPublic() && !community.isMember()) {
			throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
		}
		return community;
	}

	private Community requireOwnedCommunity(String name, User user) {
		Community community = requireVisibleCommunity(name, user);
		boolean isOwner = findMembership(user, community).map(UserCommunity::isOwner).orElse(false);
		if (!isOwner) {
			throw new ApiException(ErrorCode.COMMUNITY_FORBIDDEN);
		}
		return community;
	}

	private String normalizeName(String rawName) {
		String name = rawName == null ? "" : rawName.trim().toLowerCase(Locale.ROOT);
		boolean hasValidLength = name.length() >= MINIMUM_NAME_LENGTH && name.length() <= MAXIMUM_NAME_LENGTH;
		if (!hasValidLength || !VALID_NAME.matcher(name).matches()) {
			throw new ApiException(ErrorCode.COMMUNITY_NAME_INVALID);
		}
		return name;
	}

	private String cleanShoutout(String shoutout) {
		if (!StringUtils.hasText(shoutout)) {
			return null;
		}
		String trimmedShoutout = shoutout.trim();
		if (trimmedShoutout.codePointCount(0, trimmedShoutout.length()) > MAXIMUM_SHOUTOUT_LENGTH) {
			throw new ApiException(ErrorCode.COMMUNITY_SHOUTOUT_TOO_LONG);
		}
		return trimmedShoutout;
	}

	// Chỉ nhận ảnh do chính hệ thống cấp link upload, để không ai gắn được URL tùy ý vào trang cộng đồng.
	private String cleanImageUrl(String imageUrl) {
		if (!StringUtils.hasText(imageUrl)) {
			return null;
		}
		if (!imageUrl.startsWith(r2Properties.publicUrlOf(ImageUploadService.COMMUNITY_IMAGE_FOLDER))) {
			throw new ApiException(ErrorCode.IMAGE_URL_INVALID);
		}
		return imageUrl;
	}
}
