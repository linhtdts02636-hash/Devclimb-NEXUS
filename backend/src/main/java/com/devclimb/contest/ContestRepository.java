package com.devclimb.contest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Repository truy vấn bảng contest
public interface ContestRepository extends JpaRepository<Contest, Integer> {

	List<Contest> findByCommunityId(Integer communityId);

	// Nạp trước thông tin cộng đồng (LEFT JOIN FETCH) để tránh lỗi LazyInitializationException và loại bỏ N+1 query
	@Query("SELECT c FROM Contest c LEFT JOIN FETCH c.community ORDER BY c.startTime DESC")
	List<Contest> findAllOrdered();

	// Lấy chi tiết cuộc thi kèm dữ liệu cộng đồng
	@Query("SELECT c FROM Contest c LEFT JOIN FETCH c.community WHERE c.id = :id")
	Optional<Contest> findByIdWithCommunity(@Param("id") Integer id);
}
