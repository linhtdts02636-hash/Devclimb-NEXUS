package com.devclimb.puzzle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleLanguageRepository extends JpaRepository<PuzzleLanguage, PuzzleLanguageId> {

	List<PuzzleLanguage> findByPuzzle_IdOrderByLanguage_IdAsc(Integer puzzleId);

	List<PuzzleLanguage> findByPuzzle_IdInOrderByPuzzle_IdAscLanguage_IdAsc(List<Integer> puzzleIds);
}
