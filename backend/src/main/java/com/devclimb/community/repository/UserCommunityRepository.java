package com.devclimb.community.repository;

import com.devclimb.community.entity.UserCommunity;
import com.devclimb.community.entity.UserCommunityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, UserCommunityId> {

    @Query("""
            select count(uc)
            from UserCommunity uc
            where uc.id.userId = :userId
              and uc.id.communityId = :communityId
            """)
    long countMembership(@Param("userId") Integer userId,
                         @Param("communityId") Integer communityId);
}
