package com.devclimb.role;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.community.Community;
import com.devclimb.community.CommunityRepository;
import com.devclimb.community.UserCommunity;
import com.devclimb.community.UserCommunityId;
import com.devclimb.community.UserCommunityRepository;
import com.devclimb.user.User;

@Service
public class RoleService {

	private static final int MAXIMUM_CUSTOM_ROLES = 25;
	private static final int MAXIMUM_NAME_LENGTH = 50;
	// Ký tự điều khiển, ký tự định dạng cùng các chữ và ký hiệu trông như khoảng trắng (Hangul filler, Braille trống) mà trim() không loại được.
	private static final Pattern INVISIBLE_CHARACTERS = Pattern.compile("[\\p{C}\\p{Zl}\\p{Zp}\\u1160\\u115F\\u3164\\u2800]");
	private static final Pattern REPEATED_WHITESPACE = Pattern.compile("\\s+");
	private static final Pattern HEX_COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");

	// Owner ở trên cùng, rồi các vai trò tuỳ chỉnh từ nhiều đến ít quyền nhất, vai trò Member mặc định ở cuối.
	private static final Comparator<Role> DISPLAY_ORDER = Comparator
			.comparingInt((Role role) -> switch (RoleType.of(role)) {
				case OWNER -> 0;
				case CUSTOM -> 1;
				case DEFAULT -> 2;
			})
			.thenComparing(Comparator.comparingInt((Role role) -> role.getPermissions().size()).reversed())
			.thenComparing(role -> role.getName().toLowerCase(Locale.ROOT));

	private final RoleRepository roleRepository;
	private final UserCommunityRoleRepository userCommunityRoleRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final CommunityRepository communityRepository;
	private final CommunityAccessService communityAccessService;
	private final AuthenticationService authenticationService;

	public RoleService(RoleRepository roleRepository, UserCommunityRoleRepository userCommunityRoleRepository,
			UserCommunityRepository userCommunityRepository, CommunityRepository communityRepository,
			CommunityAccessService communityAccessService, AuthenticationService authenticationService) {
		this.roleRepository = roleRepository;
		this.userCommunityRoleRepository = userCommunityRoleRepository;
		this.userCommunityRepository = userCommunityRepository;
		this.communityRepository = communityRepository;
		this.communityAccessService = communityAccessService;
		this.authenticationService = authenticationService;
	}

	@Transactional
	public void assignOwnerRole(Integer userId, Integer communityId) {
		assignSystemRole(userId, communityId, RoleType.OWNER_ROLE_NAME);
	}

	@Transactional
	public void assignDefaultRole(Integer userId, Integer communityId) {
		assignSystemRole(userId, communityId, RoleType.DEFAULT_ROLE_NAME);
	}

	// Dòng vai trò tuỳ chỉnh không có khoá ngoại tới community nên phải xoá trước dòng community.
	@Transactional
	public void deleteCustomRolesOf(Integer communityId) {
		roleRepository.deleteCustomRolesOf(communityId);
	}

	@Transactional(readOnly = true)
	public List<RoleDTO> listRoles(AuthenticatedFirebaseUser firebaseUser, String communityName) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(communityName, user);
		Integer communityId = community.getId();

		List<Role> customRoles = roleRepository.findCustomRolesOf(communityId);
		Set<Integer> customRoleIds = customRoles.stream().map(Role::getId).collect(Collectors.toSet());
		Integer ownerUserId = findOwnerUserId(communityId);
		Map<Integer, Long> holderCounts = new HashMap<>();
		for (UserCommunityRole assignment : userCommunityRoleRepository.findByIdCommunityId(communityId)) {
			boolean isOwnersMarkerRow = assignment.getId().userId().equals(ownerUserId)
					&& customRoleIds.contains(assignment.getId().roleId());
			if (!isOwnersMarkerRow) {
				holderCounts.merge(assignment.getId().roleId(), 1L, Long::sum);
			}
		}

		List<Role> roles = new ArrayList<>(customRoles);
		roles.add(findSystemRole(RoleType.OWNER_ROLE_NAME));
		roles.add(findSystemRole(RoleType.DEFAULT_ROLE_NAME));
		roles.sort(DISPLAY_ORDER);
		return roles.stream().map(role -> toDto(role, holderCounts.getOrDefault(role.getId(), 0L))).toList();
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<MemberRoleDTO>> rolesByMemberOf(Integer communityId) {
		Integer ownerUserId = findOwnerUserId(communityId);
		List<UserCommunityRole> assignments = userCommunityRoleRepository.findByIdCommunityId(communityId);
		Map<Integer, Role> rolesById = roleRepository
				.findAllById(assignments.stream().map(assignment -> assignment.getId().roleId()).distinct().toList())
				.stream().collect(Collectors.toMap(Role::getId, Function.identity()));

		Map<Integer, List<MemberRoleDTO>> rolesByMember = new HashMap<>();
		for (UserCommunityRole assignment : assignments) {
			Role role = rolesById.get(assignment.getId().roleId());
			RoleType type = RoleType.of(role);
			if (type == RoleType.CUSTOM && assignment.getId().userId().equals(ownerUserId)) {
				continue;
			}
			rolesByMember.computeIfAbsent(assignment.getId().userId(), key -> new ArrayList<>())
					.add(new MemberRoleDTO(role.getId(), role.getName(), role.getColor(), type));
		}
		Comparator<MemberRoleDTO> displayOrder = Comparator.comparing(MemberRoleDTO::type)
				.thenComparing(memberRole -> memberRole.name().toLowerCase(Locale.ROOT));
		rolesByMember.values().forEach(memberRoles -> memberRoles.sort(displayOrder));
		return rolesByMember;
	}

	// Chủ cộng đồng cùng mọi người có vai trò cấp bất kỳ quyền điều hành nào.
	@Transactional(readOnly = true)
	public Set<Integer> moderatorUserIdsOf(Integer communityId) {
		Set<Integer> moderatorUserIds = new HashSet<>();
		moderatorUserIds.add(findOwnerUserId(communityId));
		List<UserCommunityRole> assignments = userCommunityRoleRepository.findByIdCommunityId(communityId);
		Map<Integer, Role> rolesById = roleRepository
				.findAllById(assignments.stream().map(assignment -> assignment.getId().roleId()).distinct().toList())
				.stream().collect(Collectors.toMap(Role::getId, Function.identity()));
		for (UserCommunityRole assignment : assignments) {
			Role role = rolesById.get(assignment.getId().roleId());
			if (role.getPermissions().stream().anyMatch(CommunityPermission.MODERATION::contains)) {
				moderatorUserIds.add(assignment.getId().userId());
			}
		}
		return moderatorUserIds;
	}

	// Vai trò hiện cạnh tên thành viên: Owner, nếu không thì vai trò tuỳ chỉnh có nhiều quyền nhất.
	// Người chỉ có vai trò Member mặc định thì không có mục nào.
	@Transactional(readOnly = true)
	public Map<Integer, MemberRoleDTO> displayRolesByMemberOf(Integer communityId) {
		Integer ownerUserId = findOwnerUserId(communityId);
		List<UserCommunityRole> assignments = userCommunityRoleRepository.findByIdCommunityId(communityId);
		Map<Integer, Role> rolesById = roleRepository
				.findAllById(assignments.stream().map(assignment -> assignment.getId().roleId()).distinct().toList())
				.stream().collect(Collectors.toMap(Role::getId, Function.identity()));

		Map<Integer, Role> strongestRoleByMember = new HashMap<>();
		for (UserCommunityRole assignment : assignments) {
			Role role = rolesById.get(assignment.getId().roleId());
			RoleType type = RoleType.of(role);
			boolean isOwnersMarkerRow = type == RoleType.CUSTOM && assignment.getId().userId().equals(ownerUserId);
			if (type == RoleType.DEFAULT || isOwnersMarkerRow) {
				continue;
			}
			strongestRoleByMember.merge(assignment.getId().userId(), role,
					(current, candidate) -> DISPLAY_ORDER.compare(current, candidate) <= 0 ? current : candidate);
		}
		return strongestRoleByMember.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
				entry -> new MemberRoleDTO(entry.getValue().getId(), entry.getValue().getName(),
						entry.getValue().getColor(), RoleType.of(entry.getValue()))));
	}

	@Transactional
	public RoleDTO createRole(AuthenticatedFirebaseUser firebaseUser, String communityName, SaveRoleRequest request) {
		User owner = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireOwnedCommunity(communityName, owner);
		List<Role> customRoles = roleRepository.findCustomRolesOf(community.getId());
		if (customRoles.size() >= MAXIMUM_CUSTOM_ROLES) {
			throw new ApiException(ErrorCode.ROLE_LIMIT_REACHED);
		}

		ValidatedRole validated = validate(request, customRoles, null);
		Role role = roleRepository.saveAndFlush(new Role(validated.name(), validated.color(), validated.permissions()));
		// Dòng đánh dấu của chủ cộng đồng là thứ gắn vai trò này với cộng đồng; dòng này bị ẩn khỏi danh sách và số đếm người giữ vai trò.
		userCommunityRoleRepository.save(new UserCommunityRole(owner.getId(), community.getId(), role.getId()));
		return toDto(role, 0);
	}

	@Transactional
	public RoleDTO updateRole(AuthenticatedFirebaseUser firebaseUser, String communityName, Integer roleId,
			SaveRoleRequest request) {
		User owner = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireOwnedCommunity(communityName, owner);
		Role role = requireCustomRoleOf(community, roleId);

		ValidatedRole validated = validate(request, roleRepository.findCustomRolesOf(community.getId()), role.getId());
		role.update(validated.name(), validated.color(), validated.permissions());
		long holderCount = userCommunityRoleRepository.findByIdCommunityId(community.getId()).stream()
				.filter(assignment -> assignment.getId().roleId().equals(roleId))
				.filter(assignment -> !assignment.getId().userId().equals(owner.getId())).count();
		return toDto(roleRepository.save(role), holderCount);
	}

	@Transactional
	public void deleteRole(AuthenticatedFirebaseUser firebaseUser, String communityName, Integer roleId) {
		User owner = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireOwnedCommunity(communityName, owner);
		roleRepository.delete(requireCustomRoleOf(community, roleId));
	}

	@Transactional
	public void assignRole(AuthenticatedFirebaseUser firebaseUser, String communityName, Integer targetUserId,
			Integer roleId) {
		User actor = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(communityName, actor);
		Set<CommunityPermission> actorPermissions = requireMemberManager(community, actor);
		requireChangeableMember(community, targetUserId);

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(ErrorCode.ROLE_NOT_FOUND));
		RoleType type = RoleType.of(role);
		if (type == RoleType.OWNER) {
			throw new ApiException(ErrorCode.ROLE_PROTECTED);
		}
		if (type == RoleType.DEFAULT) {
			return;
		}
		requireCustomRoleOf(community, roleId);
		requireWithin(actorPermissions, role.getPermissions());

		UserCommunityRoleId id = new UserCommunityRoleId(targetUserId, community.getId(), roleId);
		if (!userCommunityRoleRepository.existsById(id)) {
			userCommunityRoleRepository.save(new UserCommunityRole(targetUserId, community.getId(), roleId));
		}
	}

	@Transactional
	public void unassignRole(AuthenticatedFirebaseUser firebaseUser, String communityName, Integer targetUserId,
			Integer roleId) {
		User actor = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(communityName, actor);
		Set<CommunityPermission> actorPermissions = requireMemberManager(community, actor);
		requireChangeableMember(community, targetUserId);

		Role role = requireCustomRoleOf(community, roleId);
		requireWithin(actorPermissions, role.getPermissions());
		userCommunityRoleRepository.deleteById(new UserCommunityRoleId(targetUserId, community.getId(), roleId));
	}

	@Transactional
	public void removeMember(AuthenticatedFirebaseUser firebaseUser, String communityName, Integer targetUserId) {
		User actor = authenticationService.findCurrentUser(firebaseUser);
		Community community = requireVisibleCommunity(communityName, actor);
		Set<CommunityPermission> actorPermissions = requireMemberManager(community, actor);
		if (targetUserId.equals(actor.getId())) {
			throw new ApiException(ErrorCode.MEMBER_PROTECTED);
		}
		requireChangeableMember(community, targetUserId);
		requireWithin(actorPermissions, communityAccessService.permissionsOf(targetUserId, community.getId()));
		// Các dòng user_community_role bị xoá theo tư cách thành viên nhờ ON DELETE CASCADE.
		userCommunityRepository.deleteById(new UserCommunityId(targetUserId, community.getId()));
	}

	private void assignSystemRole(Integer userId, Integer communityId, String roleName) {
		Role role = findSystemRole(roleName);
		userCommunityRoleRepository.save(new UserCommunityRole(userId, communityId, role.getId()));
	}

	private Role findSystemRole(String roleName) {
		return roleRepository.findByNameIgnoreCase(roleName).orElseThrow(() -> new IllegalStateException(
				"System role '" + roleName + "' is missing. Run DOCS/migrations/community-roles.sql."));
	}

	private Integer findOwnerUserId(Integer communityId) {
		return userCommunityRepository.findOwnerUserIdOf(communityId)
				.orElseThrow(() -> new IllegalStateException("Community " + communityId + " has no owner."));
	}

	// Cộng đồng riêng tư trả về "không tồn tại" cho người ngoài để không lộ việc nó có tồn tại.
	private Community requireVisibleCommunity(String name, User user) {
		Community community = communityRepository.findByNameAndActiveTrue(name)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		boolean isMember = userCommunityRepository.existsById(new UserCommunityId(user.getId(), community.getId()));
		if (!community.isPublic() && !isMember) {
			throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
		}
		return community;
	}

	private Community requireOwnedCommunity(String name, User user) {
		Community community = requireVisibleCommunity(name, user);
		boolean isOwner = userCommunityRepository.findById(new UserCommunityId(user.getId(), community.getId()))
				.map(UserCommunity::isOwner).orElse(false);
		if (!isOwner) {
			throw new ApiException(ErrorCode.COMMUNITY_FORBIDDEN);
		}
		return community;
	}

	private Set<CommunityPermission> requireMemberManager(Community community, User actor) {
		Set<CommunityPermission> actorPermissions = communityAccessService.permissionsOf(actor.getId(), community.getId());
		if (!actorPermissions.contains(CommunityPermission.MANAGE_MEMBERS)) {
			throw new ApiException(ErrorCode.PERMISSION_DENIED);
		}
		return actorPermissions;
	}

	private void requireChangeableMember(Community community, Integer targetUserId) {
		UserCommunity membership = userCommunityRepository.findById(new UserCommunityId(targetUserId, community.getId()))
				.orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
		if (membership.isOwner()) {
			throw new ApiException(ErrorCode.MEMBER_PROTECTED);
		}
	}

	// Thiếu bước này, người có quyền Manage members có thể trao (hoặc gỡ) vai trò mạnh hơn chính họ.
	private void requireWithin(Set<CommunityPermission> actorPermissions, Set<CommunityPermission> targetPermissions) {
		if (!actorPermissions.containsAll(targetPermissions)) {
			throw new ApiException(ErrorCode.PERMISSION_DENIED);
		}
	}

	private Role requireCustomRoleOf(Community community, Integer roleId) {
		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(ErrorCode.ROLE_NOT_FOUND));
		if (RoleType.of(role) != RoleType.CUSTOM) {
			throw new ApiException(ErrorCode.ROLE_PROTECTED);
		}
		boolean belongsToCommunity = roleRepository.findCustomRolesOf(community.getId()).stream()
				.anyMatch(customRole -> customRole.getId().equals(roleId));
		if (!belongsToCommunity) {
			throw new ApiException(ErrorCode.ROLE_NOT_FOUND);
		}
		return role;
	}

	private ValidatedRole validate(SaveRoleRequest request, List<Role> communityCustomRoles, Integer ignoredRoleId) {
		if (request == null || request.name() == null || request.color() == null || request.permissions() == null) {
			throw new ApiException(ErrorCode.INVALID_REQUEST);
		}

		String name = cleanName(request.name());
		int nameLength = name.codePointCount(0, name.length());
		boolean isReservedName = name.equalsIgnoreCase(RoleType.OWNER_ROLE_NAME)
				|| name.equalsIgnoreCase(RoleType.DEFAULT_ROLE_NAME);
		if (nameLength == 0 || nameLength > MAXIMUM_NAME_LENGTH || isReservedName) {
			throw new ApiException(ErrorCode.ROLE_NAME_INVALID);
		}
		boolean nameTaken = communityCustomRoles.stream().filter(role -> !role.getId().equals(ignoredRoleId))
				.anyMatch(role -> role.getName().equalsIgnoreCase(name));
		if (nameTaken) {
			throw new ApiException(ErrorCode.ROLE_NAME_TAKEN);
		}

		String color = request.color().trim();
		if (!HEX_COLOR.matcher(color).matches()) {
			throw new ApiException(ErrorCode.ROLE_COLOR_INVALID);
		}

		Set<CommunityPermission> permissions = EnumSet.noneOf(CommunityPermission.class);
		for (String permissionName : request.permissions()) {
			try {
				permissions.add(CommunityPermission.valueOf(permissionName));
			} catch (IllegalArgumentException | NullPointerException exception) {
				throw new ApiException(ErrorCode.PERMISSION_UNKNOWN);
			}
		}
		return new ValidatedRole(name, color.toUpperCase(Locale.ROOT), permissions);
	}

	private String cleanName(String rawName) {
		String normalizedName = Normalizer.normalize(rawName, Normalizer.Form.NFKC);
		String visibleName = INVISIBLE_CHARACTERS.matcher(normalizedName).replaceAll("");
		return REPEATED_WHITESPACE.matcher(visibleName).replaceAll(" ").trim();
	}

	private RoleDTO toDto(Role role, long holderCount) {
		RoleType type = RoleType.of(role);
		Set<CommunityPermission> permissions = type == RoleType.OWNER ? EnumSet.allOf(CommunityPermission.class)
				: role.getPermissions();
		return new RoleDTO(role.getId(), role.getName(), role.getColor(), type,
				CommunityPermission.namesOf(permissions), holderCount);
	}

	private record ValidatedRole(String name, String color, Set<CommunityPermission> permissions) {
	}
}
