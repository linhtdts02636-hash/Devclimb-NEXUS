package com.devclimb.contest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Repository truy vấn bảng liên kết puzzle_contest
public interface PuzzleContestRepository extends JpaRepository<PuzzleContest, PuzzleContestId> {

	List<PuzzleContest> findByIdContestId(Integer contestId);

	long countByIdContestId(Integer contestId);

	// Đếm số lượng bài toán theo danh sách id cuộc thi trong một câu truy vấn duy nhất
	@Query("SELECT pc.id.contestId, COUNT(pc) FROM PuzzleContest pc WHERE pc.id.contestId IN :contestIds GROUP BY pc.id.contestId")
	List<Object[]> countByContestIds(@Param("contestIds") List<Integer> contestIds);

	// Lấy danh sách liên kết bài toán của cuộc thi kèm thông tin Puzzle, Community và Author để tránh N+1 query
	@Query("SELECT pc FROM PuzzleContest pc JOIN FETCH pc.puzzle p JOIN FETCH p.community JOIN FETCH p.author WHERE pc.contest.id = :contestId ORDER BY pc.score ASC, p.id ASC")
	List<PuzzleContest> findByContestIdWithPuzzle(@Param("contestId") Integer contestId);
}
