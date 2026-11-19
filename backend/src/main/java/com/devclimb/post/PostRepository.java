package com.devclimb.post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {

	@Query("""
			select new com.devclimb.post.PostDTO(p.id, p.community.id, p.author.id, p.author.name, p.author.imageUrl,
				p.title, p.content, p.createdAt, p.updatedAt)
			from Post p
			where p.community.id = :communityId and p.parent is null
			order by p.createdAt desc, p.id desc
			""")
	List<PostDTO> findFeed(@Param("communityId") Integer communityId);
}
