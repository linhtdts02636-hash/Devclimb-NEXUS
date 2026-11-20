package com.devclimb.community;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;
import com.devclimb.puzzle.PuzzleDTO;
import com.devclimb.puzzle.PuzzleService;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

	private static final String FIREBASE_USER = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE;

	private final CommunityService communityService;
	private final PuzzleService puzzleService;

	public CommunityController(CommunityService communityService, PuzzleService puzzleService) {
		this.communityService = communityService;
		this.puzzleService = puzzleService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CommunityDTO create(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@RequestBody CreateCommunityRequest request) {
		return communityService.create(firebaseUser, request);
	}

	@GetMapping
	public List<CommunityDTO> listByIds(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@RequestParam List<Integer> ids) {
		return communityService.listByIds(firebaseUser, ids);
	}

	@GetMapping("/mine")
	public List<CommunityDTO> listJoined(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser) {
		return communityService.listJoined(firebaseUser);
	}

	@GetMapping("/{name}")
	public CommunityDTO get(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		return communityService.get(firebaseUser, name);
	}

	@PatchMapping("/{name}")
	public CommunityDTO update(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @RequestBody UpdateCommunityRequest request) {
		return communityService.update(firebaseUser, name, request);
	}

	@DeleteMapping("/{name}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @RequestBody DeleteCommunityRequest request) {
		communityService.delete(firebaseUser, name, request);
	}

	@GetMapping("/{name}/members")
	public List<MemberDTO> listMembers(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		return communityService.listMembers(firebaseUser, name);
	}

	// Lấy danh sách puzzle thuộc về một cộng đồng theo tên
	@GetMapping("/{name}/puzzles")
	public List<PuzzleDTO> listPuzzles(
			@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		return puzzleService.findByCommunityName(firebaseUser, name);
	}

	@PostMapping("/{name}/members")
	public CommunityDTO join(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		return communityService.join(firebaseUser, name);
	}

	@DeleteMapping("/{name}/members/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void leave(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		communityService.leave(firebaseUser, name);
	}
}
