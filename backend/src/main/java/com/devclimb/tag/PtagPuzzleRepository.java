package com.devclimb.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PtagPuzzleRepository extends JpaRepository<PtagPuzzle, PtagPuzzleId> {

	@Query("""
			SELECT pp.puzzle.id, pp.ptag.name
			FROM PtagPuzzle pp
			WHERE pp.puzzle.id IN :puzzleIds
			ORDER BY pp.puzzle.id ASC, pp.ptag.name ASC
			""")
	List<Object[]> findTagNamesByPuzzleIds(@Param("puzzleIds") List<Integer> puzzleIds);
}
