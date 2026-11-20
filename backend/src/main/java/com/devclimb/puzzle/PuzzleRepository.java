package com.devclimb.puzzle;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PuzzleRepository extends JpaRepository<Puzzle, Integer> {

	@Query(value = """
			SELECT p.*
			FROM puzzle p
			WHERE (:difficulty = '' OR p.puzz_difficulty::text = :difficulty)
			  AND (:name = '' OR p.puzz_title ILIKE CONCAT('%', :name, '%'))
			  AND (
			    :languagesAll = '' OR (
			      SELECT COUNT(DISTINCT lower(l.lang_name))
			      FROM puzzle_language pl
			      JOIN language l ON l.lang_id = pl.lang_id
			      WHERE pl.puzz_id = p.puzz_id
			        AND lower(l.lang_name) = ANY(string_to_array(:languagesAll, ','))
			    ) = cardinality(string_to_array(:languagesAll, ','))
			  )
			  AND (
			    :languagesAny = '' OR EXISTS (
			      SELECT 1
			      FROM puzzle_language pl
			      JOIN language l ON l.lang_id = pl.lang_id
			      WHERE pl.puzz_id = p.puzz_id
			        AND lower(l.lang_name) = ANY(string_to_array(:languagesAny, ','))
			    )
			  )
			  AND (
			    :tagsAll = '' OR (
			      SELECT COUNT(DISTINCT lower(t.ptag_name))
			      FROM ptag_puzzle ptp
			      JOIN ptag t ON t.ptag_id = ptp.ptag_id
			      WHERE ptp.puzz_id = p.puzz_id
			        AND lower(t.ptag_name) = ANY(string_to_array(:tagsAll, ','))
			    ) = cardinality(string_to_array(:tagsAll, ','))
			  )
			  AND (
			    :tagsAny = '' OR EXISTS (
			      SELECT 1
			      FROM ptag_puzzle ptp
			      JOIN ptag t ON t.ptag_id = ptp.ptag_id
			      WHERE ptp.puzz_id = p.puzz_id
			        AND lower(t.ptag_name) = ANY(string_to_array(:tagsAny, ','))
			    )
			  )
			  AND (
			    :namesExcluded = '' OR NOT EXISTS (
			      SELECT 1
			      FROM unnest(string_to_array(:namesExcluded, ',')) AS ex(word)
			      WHERE ex.word <> '' AND p.puzz_title ILIKE CONCAT('%', ex.word, '%')
			    )
			  )
			  AND (
			    :languagesExcluded = '' OR NOT EXISTS (
			      SELECT 1
			      FROM puzzle_language pl
			      JOIN language l ON l.lang_id = pl.lang_id
			      WHERE pl.puzz_id = p.puzz_id
			        AND lower(l.lang_name) = ANY(string_to_array(:languagesExcluded, ','))
			    )
			  )
			  AND (
			    :tagsExcluded = '' OR NOT EXISTS (
			      SELECT 1
			      FROM ptag_puzzle ptp
			      JOIN ptag t ON t.ptag_id = ptp.ptag_id
			      WHERE ptp.puzz_id = p.puzz_id
			        AND lower(t.ptag_name) = ANY(string_to_array(:tagsExcluded, ','))
			    )
			  )
			ORDER BY p.puzz_createdat DESC NULLS LAST, p.puzz_id DESC
			""", nativeQuery = true)
	List<Puzzle> search(
			@Param("name") String name,
			@Param("difficulty") String difficulty,
			@Param("languagesAll") String languagesAll,
			@Param("languagesAny") String languagesAny,
			@Param("tagsAll") String tagsAll,
			@Param("tagsAny") String tagsAny,
			@Param("namesExcluded") String namesExcluded,
			@Param("languagesExcluded") String languagesExcluded,
			@Param("tagsExcluded") String tagsExcluded,
			Pageable pageable);

	// Lấy danh sách puzzle thuộc về một cộng đồng, ưu tiên puzzle mới tạo nhất
	@Query("""
			SELECT p
			FROM Puzzle p
			WHERE p.community.id = :communityId
			ORDER BY p.createdAt DESC NULLS LAST, p.id DESC
			""")
	List<Puzzle> findByCommunityId(@Param("communityId") Integer communityId);
}
