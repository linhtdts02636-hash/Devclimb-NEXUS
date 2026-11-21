package com.devclimb.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	// Chỉ gọi với các tên dành riêng; chỉ mục duy nhất đảm bảo mỗi tên chỉ có một dòng.
	Optional<Role> findByNameIgnoreCase(String name);

	// Vai trò tuỳ chỉnh thuộc về cộng đồng thông qua dòng user_community_role của chủ cộng đồng, tạo cùng lúc với vai trò.
	@Query("""
			select r from Role r
			where lower(r.name) not in ('owner', 'member')
				and r.id in (select ucr.id.roleId from UserCommunityRole ucr where ucr.id.communityId = :communityId)
			order by lower(r.name)
			""")
	List<Role> findCustomRolesOf(@Param("communityId") Integer communityId);

	@Query("""
			select p from Role r join r.permissions p
			where r.id in (select ucr.id.roleId from UserCommunityRole ucr
				where ucr.id.userId = :userId and ucr.id.communityId = :communityId)
			""")
	List<CommunityPermission> findPermissionsHeldBy(@Param("userId") Integer userId,
			@Param("communityId") Integer communityId);

	// Các dòng permission và user_community_role bị xoá theo vai trò nhờ ON DELETE CASCADE.
	@Modifying
	@Query(nativeQuery = true, value = """
			DELETE FROM role
			WHERE lower(role_name) NOT IN ('owner', 'member')
				AND role_id IN (SELECT role_id FROM user_community_role WHERE comm_id = :communityId)
			""")
	void deleteCustomRolesOf(@Param("communityId") Integer communityId);
}
