package com.devclimb.submission;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

	// Lấy danh sách ID các bài toán mà người dùng đã giải thành công (ACCEPTED)
	@Query(value = "SELECT DISTINCT s.puzz_id FROM submission s WHERE s.user_id = :userId AND s.subm_status = 'ACCEPTED' AND s.puzz_id IN :puzzleIds", nativeQuery = true)
	Set<Integer> findSolvedPuzzleIds(@Param("userId") Integer userId, @Param("puzzleIds") Collection<Integer> puzzleIds);
}
