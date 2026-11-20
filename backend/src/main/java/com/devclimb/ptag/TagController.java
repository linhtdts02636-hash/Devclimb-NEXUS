package com.devclimb.ptag;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagController {

	private final PtagRepository ptagRepository;

	public TagController(PtagRepository ptagRepository) {
		this.ptagRepository = ptagRepository;
	}

	// Trả về toàn bộ tag kèm số puzzle đang dùng. Dữ liệu tag hiện còn nhỏ nên
	// chưa cần phân trang; nếu sau này tăng mạnh có thể thêm limit hoặc cache.
	@GetMapping
	public List<TagDTO> findTags() {
		return ptagRepository.findTagUsage().stream()
				.map(row -> new TagDTO((String) row[0], ((Number) row[1]).longValue()))
				.toList();
	}
}
