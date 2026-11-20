package com.devclimb.reaction;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, ReactionId> {

	// Mỗi cặp (post_id, user_id) chỉ có một reaction; đổi loại thì cập nhật tại chỗ và giữ nguyên thời điểm tạo.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = """
			INSERT INTO reaction (post_id, user_id, reac_type)
			VALUES (:postId, :userId, CAST(:type AS reac_type_enum))
			ON CONFLICT (post_id, user_id)
			DO UPDATE SET reac_type = EXCLUDED.reac_type
			""", nativeQuery = true)
	void upsert(@Param("postId") Integer postId, @Param("userId") Integer userId, @Param("type") String type);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "DELETE FROM reaction WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
	void deleteByPostAndUser(@Param("postId") Integer postId, @Param("userId") Integer userId);

	// Gom số lượng của nhiều post trong một câu lệnh để feed không phải truy vấn từng bài.
	// Dùng native query vì Hibernate biến enum literal trong JPQL thành một kiểu PostgreSQL không tồn tại.
	@Query(value = """
			SELECT post_id,
				SUM(CASE WHEN reac_type = 'LIKE' THEN 1 ELSE 0 END) AS upvotes,
				SUM(CASE WHEN reac_type = 'DISLIKE' THEN 1 ELSE 0 END) AS downvotes
			FROM reaction
			WHERE post_id IN (:postIds)
			GROUP BY post_id
			""", nativeQuery = true)
	List<Object[]> countByPostIds(@Param("postIds") Collection<Integer> postIds);

	@Query("""
			select r
			from Reaction r
			where r.id.userId = :userId and r.id.postId in :postIds
			""")
	List<Reaction> findByUserIdAndPostIds(@Param("userId") Integer userId,
			@Param("postIds") Collection<Integer> postIds);
}
