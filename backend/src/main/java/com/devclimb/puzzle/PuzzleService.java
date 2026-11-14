package com.devclimb.puzzle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.user.User;

@Service
public class PuzzleService {

	private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
	private static final int DEFAULT_COMMUNITY_ID = 1;

	private final PuzzleRepository puzzleRepository;
	private final AuthenticationService authenticationService;

	public PuzzleService(PuzzleRepository puzzleRepository, AuthenticationService authenticationService) {
		this.puzzleRepository = puzzleRepository;
		this.authenticationService = authenticationService;
	}

	@Transactional(readOnly = true)
	public List<PuzzleDTO> findPuzzles(String query, String difficulty, int page, int size) {
		if (page < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
		}
		if (!ALLOWED_PAGE_SIZES.contains(size)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be 10, 30, or 50.");
		}

		List<String> nameParts = new ArrayList<>();
		Set<String> requiredLanguages = new LinkedHashSet<>();
		Set<String> optionalLanguages = new LinkedHashSet<>();
		Set<String> requiredTags = new LinkedHashSet<>();
		Set<String> optionalTags = new LinkedHashSet<>();

		String normalizedQuery = StringUtils.hasText(query) ? query.trim() : "";
		normalizedQuery = normalizedQuery.replaceAll("\\s*\\|\\|\\s*", "||");
		for (String token : normalizedQuery.split("\\s+")) {
			if (token.isEmpty()) {
				continue;
			}
			if (token.startsWith(":") && token.length() > 1) {
				collectFilters(token.substring(1), ':', requiredLanguages, optionalLanguages);
			} else if (token.startsWith("#") && token.length() > 1) {
				collectFilters(token.substring(1), '#', requiredTags, optionalTags);
			} else {
				nameParts.add(token);
			}
		}

		return puzzleRepository.search(
				String.join(" ", nameParts),
				parseDifficulty(difficulty),
				String.join(",", requiredLanguages),
				String.join(",", optionalLanguages),
				String.join(",", requiredTags),
				String.join(",", optionalTags),
				PageRequest.of(page, size))
				.stream()
				.map(PuzzleDTO::from)
				.toList();
	}

	@Transactional
	public PuzzleDTO createPuzzle(CreatePuzzleRequest request, AuthenticatedFirebaseUser firebaseUser) {
		User author = authenticationService.findCurrentUser(firebaseUser);
		Puzzle puzzle = new Puzzle(
				author,
				DEFAULT_COMMUNITY_ID,
				request.difficulty(),
				request.title().trim(),
				request.content().trim());
		return PuzzleDTO.from(puzzleRepository.save(puzzle));
	}

	private void collectFilters(String value, char prefix, Set<String> required, Set<String> optional) {
		String[] alternatives = value.split("\\|\\|", -1);
		if (alternatives.length == 1) {
			required.add(value.trim().toLowerCase(Locale.ROOT));
			return;
		}
		for (String alternative : alternatives) {
			String candidate = alternative.trim();
			if (candidate.startsWith(String.valueOf(prefix))) {
				candidate = candidate.substring(1);
			}
			if (!candidate.isEmpty()) {
				optional.add(candidate.toLowerCase(Locale.ROOT));
			}
		}
	}

	private String parseDifficulty(String difficulty) {
		if (!StringUtils.hasText(difficulty)) {
			return "";
		}
		try {
			return PuzzleDifficulty.valueOf(difficulty.trim().toUpperCase(Locale.ROOT)).name();
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficulty must be EASY, MEDIUM, or HARD.");
		}
	}
}
