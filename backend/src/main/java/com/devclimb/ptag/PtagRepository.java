package com.devclimb.ptag;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PtagRepository extends JpaRepository<Ptag, Integer> {

	List<Ptag> findByNameIn(Collection<String> names);

	// Hai request tạo puzzle cùng lúc có thể cùng tạo một tag. Upsert giúp tránh
	// lỗi unique constraint trên ptag_name.
	@Modifying
	@Query(value = """
			INSERT INTO ptag (ptag_name)
			VALUES (:name)
			ON CONFLICT (ptag_name) DO NOTHING
			""", nativeQuery = true)
	void insertIfAbsent(@Param("name") String name);

	// Đếm số puzzle cho từng tag bằng một query aggregate duy nhất.
	// LEFT JOIN để tag chưa gắn puzzle nào vẫn xuất hiện với count = 0.
	@Query(value = """
			SELECT p.ptag_name, COUNT(pp.puzz_id)
			FROM ptag p
			LEFT JOIN ptag_puzzle pp ON pp.ptag_id = p.ptag_id
			GROUP BY p.ptag_id, p.ptag_name
			ORDER BY COUNT(pp.puzz_id) DESC, p.ptag_name ASC
			""", nativeQuery = true)
	List<Object[]> findTagUsage();
}
