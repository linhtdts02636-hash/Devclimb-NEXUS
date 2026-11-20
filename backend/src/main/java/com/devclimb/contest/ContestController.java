package com.devclimb.contest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

import jakarta.validation.Valid;

// Controller phục vụ các endpoint liên quan đến cuộc thi (/api/contests)
@RestController
@RequestMapping("/api/contests")
public class ContestController {

	private static final String FIREBASE_USER = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE;

	private final ContestService contestService;

	public ContestController(ContestService contestService) {
		this.contestService = contestService;
	}

	@GetMapping
	public List<ContestDTO> listContests() {
		return contestService.listContests();
	}

	@GetMapping("/{id}")
	public ContestDTO getContest(@PathVariable Integer id) {
		return contestService.getContest(id);
	}

	// Tạo cuộc thi mới thuộc về một cộng đồng
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ContestDTO createContest(
			@Valid @RequestBody CreateContestRequest request,
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser) {
		return contestService.createContest(request, firebaseUser);
	}

	// Cập nhật thông tin cuộc thi ở chế độ chỉnh sửa
	@PatchMapping("/{id}")
	public ContestDTO updateContest(
			@PathVariable Integer id,
			@Valid @RequestBody UpdateContestRequest request,
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser) {
		return contestService.updateContest(id, request, firebaseUser);
	}

	// Đăng ký tham gia cuộc thi
	@PostMapping("/{id}/contestants")
	@ResponseStatus(HttpStatus.CREATED)
	public ContestantDTO joinContest(
			@PathVariable Integer id,
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser) {
		return contestService.joinContest(id, firebaseUser);
	}

	// Lấy danh sách thí sinh và bảng xếp hạng điểm số của cuộc thi
	@GetMapping("/{id}/contestants")
	public List<ContestantDTO> listContestants(@PathVariable Integer id) {
		return contestService.listContestants(id);
	}

	// Lấy danh sách bài toán thuộc cuộc thi kèm số điểm tương ứng và trạng thái đã giải của người dùng
	@GetMapping("/{id}/puzzles")
	public List<ContestPuzzleDTO> listContestPuzzles(
			@PathVariable Integer id,
			@RequestAttribute(name = FIREBASE_USER, required = false) AuthenticatedFirebaseUser firebaseUser) {
		return contestService.listContestPuzzles(id, firebaseUser);
	}
}
