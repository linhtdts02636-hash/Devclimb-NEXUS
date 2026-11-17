package com.devclimb.community;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Integer> {

	Optional<Community> findByNameAndActiveTrue(String name);

	boolean existsByName(String name);

	// Một câu lệnh lấy cộng đồng, số thành viên và quan hệ của người xem; mỗi lần gọi tới Aiven tốn ~77 ms nên gộp lại.
	@Query("""
			select new com.devclimb.community.CommunityDTO(c.id, c.name, c.imageUrl, c.bannerUrl, c.shoutout,
				c.publicCommunity, (select count(m) from UserCommunity m where m.id.communityId = c.id),
				(case when uc.id.userId is not null then true else false end),
				(case when uc.owner = true then true else false end), c.createdAt)
			from Community c left join UserCommunity uc on uc.id.communityId = c.id and uc.id.userId = :userId
			where c.name = :name and c.active = true
			""")
	Optional<CommunityDTO> findViewableByName(@Param("name") String name, @Param("userId") Integer userId);

	// Tra cứu hàng loạt theo id (danh sách "Recent" của thanh bên lưu id nên vẫn đúng sau khi cộng đồng đổi tên).
	// Cộng đồng riêng tư mà người xem không phải thành viên bị loại, giống quy tắc của findViewableByName.
	@Query("""
			select new com.devclimb.community.CommunityDTO(c.id, c.name, c.imageUrl, c.bannerUrl, c.shoutout,
				c.publicCommunity, (select count(m) from UserCommunity m where m.id.communityId = c.id),
				(case when uc.id.userId is not null then true else false end),
				(case when uc.owner = true then true else false end), c.createdAt)
			from Community c left join UserCommunity uc on uc.id.communityId = c.id and uc.id.userId = :userId
			where c.id in :ids and c.active = true and (c.publicCommunity = true or uc.id.userId is not null)
			""")
	List<CommunityDTO> findViewableByIds(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);

	@Query("""
			select new com.devclimb.community.CommunityDTO(c.id, c.name, c.imageUrl, c.bannerUrl, c.shoutout,
				c.publicCommunity, (select count(m) from UserCommunity m where m.id.communityId = c.id), true,
				uc.owner, c.createdAt)
			from Community c join UserCommunity uc on uc.id.communityId = c.id
			where uc.id.userId = :userId and c.active = true
			order by uc.joinedAt desc
			""")
	List<CommunityDTO> findJoinedBy(@Param("userId") Integer userId);

	// Các bảng dưới đây không có ON DELETE CASCADE tới community nên phải xóa tay, theo đúng thứ tự khóa ngoại.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(nativeQuery = true, value = "DELETE FROM appeal WHERE warn_id IN (SELECT warn_id FROM warn WHERE comm_id = :communityId)")
	void deleteAppealsOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM warn WHERE comm_id = :communityId")
	void deleteWarningsOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM submission WHERE puzz_id IN (SELECT puzz_id FROM puzzle WHERE comm_id = :communityId)")
	void deleteSubmissionsOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM achievement WHERE cont_id IN (SELECT cont_id FROM contest WHERE comm_id = :communityId)")
	void deleteAchievementsOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM contest WHERE comm_id = :communityId")
	void deleteContestsOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM puzzle WHERE comm_id = :communityId")
	void deletePuzzlesOf(@Param("communityId") Integer communityId);

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM community WHERE comm_id = :communityId")
	void deleteCommunityRow(@Param("communityId") Integer communityId);
}
