package com.devclimb.post;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {

	// Phân trang theo id giảm dần: id tăng theo thời gian tạo nên thứ tự này trùng với "mới nhất trước",
	// và con trỏ chỉ cần là id bài cuối cùng đã tải.
	@Query("""
			select new com.devclimb.post.PostDTO(p.id, p.community.id, p.author.id, p.author.name, p.author.imageUrl,
				p.title, p.content, p.createdAt, p.updatedAt, p.pinned)
			from Post p
			where p.community.id = :communityId and p.parent is null and p.pinned = false and p.id < :beforeId
			order by p.id desc
			""")
	List<PostDTO> findFeedPage(@Param("communityId") Integer communityId, @Param("beforeId") Integer beforeId,
			Pageable pageable);

	@Query("""
			select new com.devclimb.post.PostDTO(p.id, p.community.id, p.author.id, p.author.name, p.author.imageUrl,
				p.title, p.content, p.createdAt, p.updatedAt, p.pinned)
			from Post p
			where p.community.id = :communityId and p.parent is null and p.pinned = true
			order by p.id desc
			""")
	List<PostDTO> findPinned(@Param("communityId") Integer communityId);
}
