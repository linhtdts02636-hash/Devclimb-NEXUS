package com.devclimb.contest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;
import com.devclimb.language.PuzzleLanguage;
import com.devclimb.language.PuzzleLanguageDTO;
import com.devclimb.language.PuzzleLanguageRepository;
import com.devclimb.ptag.PtagPuzzleRepository;
import com.devclimb.submission.SubmissionRepository;
import com.devclimb.user.User;

// Controller phục vụ các endpoint liên quan đến cuộc thi (/api/contests)
@RestController
@RequestMapping("/api/contests")
@Transactional(readOnly = true)
public class ContestController {

	private final ContestRepository contestRepository;
	private final PuzzleContestRepository puzzleContestRepository;
	private final UserContestRepository userContestRepository;
	private final AuthenticationService authenticationService;
	private final PuzzleLanguageRepository puzzleLanguageRepository;
	private final PtagPuzzleRepository ptagPuzzleRepository;
	private final SubmissionRepository submissionRepository;

	public ContestController(
			ContestRepository contestRepository,
			PuzzleContestRepository puzzleContestRepository,
			UserContestRepository userContestRepository,
			AuthenticationService authenticationService,
			PuzzleLanguageRepository puzzleLanguageRepository,
			PtagPuzzleRepository ptagPuzzleRepository,
			SubmissionRepository submissionRepository) {
		this.contestRepository = contestRepository;
		this.puzzleContestRepository = puzzleContestRepository;
		this.userContestRepository = userContestRepository;
		this.authenticationService = authenticationService;
		this.puzzleLanguageRepository = puzzleLanguageRepository;
		this.ptagPuzzleRepository = ptagPuzzleRepository;
		this.submissionRepository = submissionRepository;
	}

	@GetMapping
	public List<ContestDTO> listContests() {
		List<Contest> contests = contestRepository.findAllOrdered();
		if (contests.isEmpty()) {
			return List.of();
		}

		List<Integer> contestIds = contests.stream().map(Contest::getId).toList();
		Map<Integer, Long> registeredCounts = userContestRepository.countByContestIds(contestIds).stream()
				.collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));
		Map<Integer, Long> puzzleCounts = puzzleContestRepository.countByContestIds(contestIds).stream()
				.collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));

		return contests.stream()
				.map(contest -> {
					long registeredCount = registeredCounts.getOrDefault(contest.getId(), 0L);
					long puzzleCount = puzzleCounts.getOrDefault(contest.getId(), 0L);
					return ContestDTO.from(contest, registeredCount, puzzleCount);
				})
				.toList();
	}

	@GetMapping("/{id}")
	public ContestDTO getContest(@PathVariable Integer id) {
		Contest contest = contestRepository.findByIdWithCommunity(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest not found."));
		long registeredCount = userContestRepository.countByIdContestId(contest.getId());
		long puzzleCount = puzzleContestRepository.countByIdContestId(contest.getId());
		return ContestDTO.from(contest, registeredCount, puzzleCount);
	}

	// Đăng ký tham gia cuộc thi
	@PostMapping("/{id}/contestants")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public ContestantDTO joinContest(
			@PathVariable Integer id,
			@RequestAttribute(FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedFirebaseUser firebaseUser) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Contest contest = contestRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest not found."));

		if (Instant.now().isAfter(contest.getEndTime())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contest has already ended.");
		}

		if (!userContestRepository.existsByIdUserIdAndIdContestId(user.getId(), id)) {
			UserContest userContest = new UserContest(user, contest);
			userContestRepository.save(userContest);
		}

		long points = userContestRepository.calculateUserContestPoints(user.getId(), id);
		return new ContestantDTO(user.getId(), user.getName(), user.getImageUrl(), user.isAdmin(), points);
	}

	// Lấy danh sách thí sinh và bảng xếp hạng điểm số của cuộc thi
	@GetMapping("/{id}/contestants")
	public List<ContestantDTO> listContestants(@PathVariable Integer id) {
		if (!contestRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest not found.");
		}

		return userContestRepository.findContestantsByContestId(id).stream()
				.map(row -> new ContestantDTO(
						row.getUserId(),
						row.getUserName(),
						row.getUserImage(),
						Boolean.TRUE.equals(row.getIsAdmin()),
						row.getPoints() != null ? row.getPoints() : 0L))
				.toList();
	}

	// Lấy danh sách bài toán thuộc cuộc thi kèm số điểm tương ứng và trạng thái đã giải của người dùng
	@GetMapping("/{id}/puzzles")
	public List<ContestPuzzleDTO> listContestPuzzles(
			@PathVariable Integer id,
			@RequestAttribute(name = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE, required = false) AuthenticatedFirebaseUser firebaseUser) {
		if (!contestRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest not found.");
		}

		List<PuzzleContest> puzzleContests = puzzleContestRepository.findByContestIdWithPuzzle(id);
		if (puzzleContests.isEmpty()) {
			return List.of();
		}

		List<Integer> puzzleIds = puzzleContests.stream()
				.map(pc -> pc.getPuzzle().getId())
				.toList();

		Map<Integer, List<PuzzleLanguageDTO>> languagesByPuzzle = new HashMap<>();
		for (PuzzleLanguage puzzleLanguage : puzzleLanguageRepository
				.findByPuzzle_IdInOrderByPuzzle_IdAscLanguage_IdAsc(puzzleIds)) {
			languagesByPuzzle.computeIfAbsent(puzzleLanguage.getPuzzle().getId(), key -> new ArrayList<>())
					.add(PuzzleLanguageDTO.from(puzzleLanguage));
		}

		Map<Integer, List<String>> tagsByPuzzle = new HashMap<>();
		for (Object[] row : ptagPuzzleRepository.findTagNamesByPuzzleIds(puzzleIds)) {
			tagsByPuzzle.computeIfAbsent((Integer) row[0], key -> new ArrayList<>())
					.add((String) row[1]);
		}

		Set<Integer> solvedPuzzleIds = Set.of();
		if (firebaseUser != null && !puzzleIds.isEmpty()) {
			try {
				User user = authenticationService.findCurrentUser(firebaseUser);
				if (user != null) {
					solvedPuzzleIds = submissionRepository.findSolvedPuzzleIds(user.getId(), puzzleIds);
				}
			} catch (Exception ex) {
				// Không để lỗi xác thực làm sập endpoint lấy danh sách bài toán
				solvedPuzzleIds = Set.of();
			}
		}

		final Set<Integer> finalSolvedPuzzleIds = solvedPuzzleIds;
		return puzzleContests.stream()
				.map(pc -> {
					Integer puzzleId = pc.getPuzzle().getId();
					boolean solved = finalSolvedPuzzleIds.contains(puzzleId);
					return ContestPuzzleDTO.from(
							pc,
							languagesByPuzzle.getOrDefault(puzzleId, List.of()),
							tagsByPuzzle.getOrDefault(puzzleId, List.of()),
							solved);
				})
				.toList();
	}
}
