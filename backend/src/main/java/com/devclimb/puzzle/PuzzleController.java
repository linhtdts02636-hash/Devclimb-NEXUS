package com.devclimb.puzzle;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {

	private final PuzzleService puzzleService;

	public PuzzleController(PuzzleService puzzleService) {
		this.puzzleService = puzzleService;
	}

	@GetMapping
	public List<PuzzleDTO> findPuzzles(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String difficulty,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return puzzleService.findPuzzles(q, difficulty, page, size);
	}

	@GetMapping("/{id}")
	public PuzzleDTO findPuzzle(@PathVariable Integer id) {
		return puzzleService.findPuzzle(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PuzzleDTO createPuzzle(
			@Valid @RequestBody CreatePuzzleRequest request,
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser) {
		return puzzleService.createPuzzle(request, firebaseUser);
	}
}
