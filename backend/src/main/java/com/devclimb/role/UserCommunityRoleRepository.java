package com.devclimb.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommunityRoleRepository extends JpaRepository<UserCommunityRole, UserCommunityRoleId> {

	List<UserCommunityRole> findByIdCommunityId(Integer communityId);
}
