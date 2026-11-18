package com.devclimb.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("""
            select p
            from Post p
            join fetch p.author
            where p.community.id = :communityId
              and p.parent is null
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFeed(@Param("communityId") Integer communityId);
}
