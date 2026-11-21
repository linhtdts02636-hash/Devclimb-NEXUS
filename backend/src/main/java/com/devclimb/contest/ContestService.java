package com.devclimb.contest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.community.Community;
import com.devclimb.community.CommunityRepository;
import com.devclimb.community.UserCommunityId;
import com.devclimb.community.UserCommunityRepository;
import com.devclimb.language.PuzzleLanguage;
import com.devclimb.language.PuzzleLanguageDTO;
import com.devclimb.language.PuzzleLanguageRepository;
import com.devclimb.ptag.PtagPuzzleRepository;
import com.devclimb.submission.SubmissionRepository;
import com.devclimb.user.User;

// Service xử lý nghiệp vụ và giao dịch cơ sở dữ liệu cho cuộc thi
@Service
public class ContestService {

	private final ContestRepository contestRepository;
	private final PuzzleContestRepository puzzleContestRepository;
	private final UserContestRepository userContestRepository;
	private final AuthenticationService authenticationService;
	private final PuzzleLanguageRepository puzzleLanguageRepository;
	private final PtagPuzzleRepository ptagPuzzleRepository;
	private final SubmissionRepository submissionRepository;
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;

	public ContestService(
			ContestRepository contestRepository,
			PuzzleContestRepository puzzleContestRepository,
			UserContestRepository userContestRepository,
			AuthenticationService authenticationService,
			PuzzleLanguageRepository puzzleLanguageRepository,
			PtagPuzzleRepository ptagPuzzleRepository,
			SubmissionRepository submissionRepository,
			CommunityRepository communityRepository,
			UserCommunityRepository userCommunityRepository) {
		this.contestRepository = contestRepository;
		this.puzzleContestRepository = puzzleContestRepository;
		this.userContestRepository = userContestRepository;
		this.authenticationService = authenticationService;
		this.puzzleLanguageRepository = puzzleLanguageRepository;
		this.ptagPuzzleRepository = ptagPuzzleRepository;
		this.submissionRepository = submissionRepository;
		this.communityRepository = communityRepository;
		this.userCommunityRepository = userCommunityRepository;
	}

	@Transactional(readOnly = true)
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
				.map(contest -> ContestDTO.from(
						contest,
						registeredCounts.getOrDefault(contest.getId(), 0L),
						puzzleCounts.getOrDefault(contest.getId(), 0L)))
				.toList();
	}

	@Transactional(readOnly = true)
	public ContestDTO getContest(Integer id) {
		Contest contest = contestRepository.findByIdWithCommunity(id)
				.orElseThrow(() -> new ApiException(ErrorCode.CONTEST_NOT_FOUND));
		long registeredCount = userContestRepository.countByIdContestId(contest.getId());
		long puzzleCount = puzzleContestRepository.countByIdContestId(contest.getId());
		return ContestDTO.from(contest, registeredCount, puzzleCount);
	}

	// Tạo cuộc thi mới thuộc về một cộng đồng
	@Transactional
	public ContestDTO createContest(CreateContestRequest request, AuthenticatedFirebaseUser firebaseUser) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = communityRepository.findById(request.communityId())
				.filter(Community::isActive)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		requireCommunityMember(user.getId(), community.getId());
		validateContestSchedule(request.startTime(), request.endTime());

		Contest contest = new Contest(
				community,
				request.title().trim(),
				request.content().trim(),
				normalizeBanner(request.banner()),
				request.startTime(),
				request.endTime());
		contestRepository.saveAndFlush(contest);
		return ContestDTO.from(contest, 0L, 0L);
	}

	// Cập nhật thông tin cuộc thi ở chế độ chỉnh sửa
	@Transactional
	public ContestDTO updateContest(Integer id, UpdateContestRequest request, AuthenticatedFirebaseUser firebaseUser) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Contest contest = contestRepository.findByIdWithCommunity(id)
				.orElseThrow(() -> new ApiException(ErrorCode.CONTEST_NOT_FOUND));
		requireCommunityMember(user.getId(), contest.getCommunity().getId());
		validateContestSchedule(request.startTime(), request.endTime());

		contest.setTitle(request.title().trim());
		contest.setContent(request.content().trim());
		contest.setBanner(normalizeBanner(request.banner()));
		contest.setStartTime(request.startTime());
		contest.setEndTime(request.endTime());
		contestRepository.saveAndFlush(contest);

		long registeredCount = userContestRepository.countByIdContestId(contest.getId());
		long puzzleCount = puzzleContestRepository.countByIdContestId(contest.getId());
		return ContestDTO.from(contest, registeredCount, puzzleCount);
	}

	// Đăng ký tham gia cuộc thi
	@Transactional
	public ContestantDTO joinContest(Integer id, AuthenticatedFirebaseUser firebaseUser) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Contest contest = contestRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.CONTEST_NOT_FOUND));

		if (Instant.now().isAfter(contest.getEndTime())) {
			throw new ApiException(ErrorCode.CONTEST_ENDED);
		}

		if (!userContestRepository.existsByIdUserIdAndIdContestId(user.getId(), id)) {
			userContestRepository.save(new UserContest(user, contest));
		}

		long points = userContestRepository.calculateUserContestPoints(user.getId(), id);
		return new ContestantDTO(user.getId(), user.getName(), user.getImageUrl(), user.isAdmin(), points);
	}

	// Lấy danh sách thí sinh và bảng xếp hạng điểm số của cuộc thi
	@Transactional(readOnly = true)
	public List<ContestantDTO> listContestants(Integer id) {
		if (!contestRepository.existsById(id)) {
			throw new ApiException(ErrorCode.CONTEST_NOT_FOUND);
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
	@Transactional(readOnly = true)
	public List<ContestPuzzleDTO> listContestPuzzles(Integer id, AuthenticatedFirebaseUser firebaseUser) {
		if (!contestRepository.existsById(id)) {
			throw new ApiException(ErrorCode.CONTEST_NOT_FOUND);
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

	private void requireCommunityMember(Integer userId, Integer communityId) {
		if (!userCommunityRepository.existsById(new UserCommunityId(userId, communityId))) {
			throw new ApiException(ErrorCode.NOT_A_MEMBER);
		}
	}

	private void validateContestSchedule(Instant startTime, Instant endTime) {
		if (!endTime.isAfter(startTime)) {
			throw new ApiException(ErrorCode.CONTEST_SCHEDULE_INVALID);
		}
	}

	private String normalizeBanner(String banner) {
		return banner != null && !banner.isBlank() ? banner.trim() : null;
	}
}
