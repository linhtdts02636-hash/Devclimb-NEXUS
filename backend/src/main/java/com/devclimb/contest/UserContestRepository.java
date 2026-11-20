package com.devclimb.contest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Repository truy vấn bảng liên kết user_contest
public interface UserContestRepository extends JpaRepository<UserContest, UserContestId> {

	List<UserContest> findByIdContestId(Integer contestId);

	long countByIdContestId(Integer contestId);

	boolean existsByIdUserIdAndIdContestId(Integer userId, Integer contestId);

	// Đếm số lượng người tham gia theo danh sách id cuộc thi trong một câu truy vấn duy nhất
	@Query("SELECT uc.id.contestId, COUNT(uc) FROM UserContest uc WHERE uc.id.contestId IN :contestIds GROUP BY uc.id.contestId")
	List<Object[]> countByContestIds(@Param("contestIds") List<Integer> contestIds);

	// Lấy danh sách thí sinh kèm tổng điểm tính từ các bài tập đã nộp thành công (ACCEPTED) trong thời gian cuộc thi
	@Query(value = """
			SELECT 
				u.user_id AS userId,
				u.user_name AS userName,
				u.user_image AS userImage,
				u.user_isadmin AS isAdmin,
				COALESCE((
					SELECT SUM(pc.puco_score)
					FROM puzzle_contest pc
					JOIN contest c ON c.cont_id = pc.cont_id
					WHERE pc.cont_id = uc.cont_id
					  AND EXISTS (
						  SELECT 1 FROM submission s
						  WHERE s.user_id = uc.user_id
						    AND s.puzz_id = pc.puzz_id
						    AND s.subm_status = 'ACCEPTED'
						    AND s.subm_date >= c.cont_start
						    AND s.subm_date <= c.cont_end
					  )
				), 0) AS points
			FROM user_contest uc
			JOIN users u ON u.user_id = uc.user_id
			WHERE uc.cont_id = :contestId
			ORDER BY points DESC, u.user_name ASC
			""", nativeQuery = true)
	List<ContestantView> findContestantsByContestId(@Param("contestId") Integer contestId);

	// Tính điểm hiện tại của một người dùng cụ thể trong thời gian cuộc thi diễn ra
	@Query(value = """
			SELECT COALESCE((
				SELECT SUM(pc.puco_score)
				FROM puzzle_contest pc
				JOIN contest c ON c.cont_id = pc.cont_id
				WHERE pc.cont_id = :contestId
				  AND EXISTS (
					  SELECT 1 FROM submission s
					  WHERE s.user_id = :userId
					    AND s.puzz_id = pc.puzz_id
					    AND s.subm_status = 'ACCEPTED'
					    AND s.subm_date >= c.cont_start
					    AND s.subm_date <= c.cont_end
				  )
			), 0)
			""", nativeQuery = true)
	long calculateUserContestPoints(@Param("userId") Integer userId, @Param("contestId") Integer contestId);
}
