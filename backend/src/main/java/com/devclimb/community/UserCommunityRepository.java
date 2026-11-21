package com.devclimb.community;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, UserCommunityId> {

	long countByIdCommunityId(Integer communityId);

	@Query("""
			select new com.devclimb.community.MemberDTO(u.id, u.name, u.imageUrl, uc.owner, uc.joinedAt)
			from UserCommunity uc join User u on u.id = uc.id.userId
			where uc.id.communityId = :communityId
			order by uc.owner desc, uc.joinedAt asc
			""")
	List<MemberDTO> findMembersOf(@Param("communityId") Integer communityId);

	@Query("select uc.id.userId from UserCommunity uc where uc.id.communityId = :communityId and uc.owner = true")
	Optional<Integer> findOwnerUserIdOf(@Param("communityId") Integer communityId);
}
