package com.devclimb.testcase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestcaseRepository extends JpaRepository<Testcase, Integer> {

	List<Testcase> findByPuzzle_IdOrderByIdAsc(Integer puzzleId);
}
