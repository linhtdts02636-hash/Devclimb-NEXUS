package com.devclimb.role;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.community.UserCommunity;
import com.devclimb.community.UserCommunityId;
import com.devclimb.community.UserCommunityRepository;

@Service
public class CommunityAccessService {

	private final UserCommunityRepository userCommunityRepository;
	private final RoleRepository roleRepository;

	public CommunityAccessService(UserCommunityRepository userCommunityRepository, RoleRepository roleRepository) {
		this.userCommunityRepository = userCommunityRepository;
		this.roleRepository = roleRepository;
	}

	// Chủ cộng đồng mặc nhiên có mọi quyền, kể cả quyền thêm sau này. Người ngoài cộng đồng không có quyền nào.
	@Transactional(readOnly = true)
	public Set<CommunityPermission> permissionsOf(Integer userId, Integer communityId) {
		Optional<UserCommunity> membership = userCommunityRepository.findById(new UserCommunityId(userId, communityId));
		if (membership.isEmpty()) {
			return EnumSet.noneOf(CommunityPermission.class);
		}
		if (membership.get().isOwner()) {
			return EnumSet.allOf(CommunityPermission.class);
		}
		Set<CommunityPermission> permissions = EnumSet.noneOf(CommunityPermission.class);
		permissions.addAll(roleRepository.findPermissionsHeldBy(userId, communityId));
		return permissions;
	}

	@Transactional(readOnly = true)
	public List<String> permissionNamesOf(Integer userId, Integer communityId) {
		return CommunityPermission.namesOf(permissionsOf(userId, communityId));
	}

	@Transactional(readOnly = true)
	public boolean hasPermission(Integer userId, Integer communityId, CommunityPermission permission) {
		return permissionsOf(userId, communityId).contains(permission);
	}

	@Transactional(readOnly = true)
	public void requirePermission(Integer userId, Integer communityId, CommunityPermission permission) {
		if (!hasPermission(userId, communityId, permission)) {
			throw new ApiException(ErrorCode.PERMISSION_DENIED);
		}
	}
}
