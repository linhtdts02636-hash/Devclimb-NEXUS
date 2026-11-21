package com.devclimb.puzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.community.Community;
import com.devclimb.community.CommunityRepository;
import com.devclimb.role.CommunityAccessService;
import com.devclimb.role.CommunityPermission;
import com.devclimb.tag.PtagPuzzleRepository;
import com.devclimb.user.User;

@Service
public class PuzzleService {

	private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

	private final PuzzleRepository puzzleRepository;
	private final PuzzleLanguageRepository puzzleLanguageRepository;
	private final PtagPuzzleRepository ptagPuzzleRepository;
	private final CommunityRepository communityRepository;
	private final AuthenticationService authenticationService;
	private final CommunityAccessService communityAccessService;

	public PuzzleService(
			PuzzleRepository puzzleRepository,
			PuzzleLanguageRepository puzzleLanguageRepository,
			PtagPuzzleRepository ptagPuzzleRepository,
			CommunityRepository communityRepository,
			AuthenticationService authenticationService,
			CommunityAccessService communityAccessService) {
		this.communityAccessService = communityAccessService;
		this.puzzleRepository = puzzleRepository;
		this.puzzleLanguageRepository = puzzleLanguageRepository;
		this.ptagPuzzleRepository = ptagPuzzleRepository;
		this.communityRepository = communityRepository;
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

		List<Puzzle> puzzles = puzzleRepository.search(
				String.join(" ", nameParts),
				parseDifficulty(difficulty),
				String.join(",", requiredLanguages),
				String.join(",", optionalLanguages),
				String.join(",", requiredTags),
				String.join(",", optionalTags),
				PageRequest.of(page, size));
		if (puzzles.isEmpty()) {
			return List.of();
		}

		List<Integer> puzzleIds = puzzles.stream()
				.map(Puzzle::getId)
				.toList();
		Map<Integer, List<PuzzleLanguage>> languagesByPuzzle = new HashMap<>();
		for (PuzzleLanguage puzzleLanguage : puzzleLanguageRepository
				.findByPuzzle_IdInOrderByPuzzle_IdAscLanguage_IdAsc(puzzleIds)) {
			languagesByPuzzle.computeIfAbsent(puzzleLanguage.getPuzzle().getId(), key -> new ArrayList<>())
					.add(puzzleLanguage);
		}

		Map<Integer, List<String>> tagsByPuzzle = new HashMap<>();
		for (Object[] row : ptagPuzzleRepository.findTagNamesByPuzzleIds(puzzleIds)) {
			tagsByPuzzle.computeIfAbsent((Integer) row[0], key -> new ArrayList<>())
					.add((String) row[1]);
		}

		return puzzles.stream()
				.map(puzzle -> PuzzleDTO.from(
						puzzle,
						languagesByPuzzle.getOrDefault(puzzle.getId(), List.of()),
						tagsByPuzzle.getOrDefault(puzzle.getId(), List.of())))
				.toList();
	}

	@Transactional(readOnly = true)
	public PuzzleDTO findPuzzle(Integer id) {
		return puzzleRepository.findById(id)
				.map(puzzle -> PuzzleDTO.from(
						puzzle,
						puzzleLanguageRepository.findByPuzzle_IdOrderByLanguage_IdAsc(id),
						ptagPuzzleRepository.findTagNamesByPuzzleIds(List.of(id)).stream()
								.map(row -> (String) row[1])
								.toList()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle not found."));
	}

	@Transactional
	public PuzzleDTO createPuzzle(CreatePuzzleRequest request, AuthenticatedFirebaseUser firebaseUser) {
		User author = authenticationService.findCurrentUser(firebaseUser);
		Community community = communityRepository.findById(request.communityId())
				.filter(Community::isActive)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		communityAccessService.requirePermission(author.getId(), community.getId(), CommunityPermission.CREATE_PUZZLES);
		Puzzle puzzle = new Puzzle(
				author,
				community,
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
