package com.devclimb.ptag;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PtagRepository extends JpaRepository<Ptag, Integer> {

	List<Ptag> findByNameIn(Collection<String> names);

	// Puzzle creation can race with another request creating the same tag. The
	// upsert keeps that race from turning into a database constraint exception.
	@Modifying
	@Query(value = """
			INSERT INTO ptag (ptag_name)
			VALUES (:name)
			ON CONFLICT (ptag_name) DO NOTHING
			""", nativeQuery = true)
	void insertIfAbsent(@Param("name") String name);
}
