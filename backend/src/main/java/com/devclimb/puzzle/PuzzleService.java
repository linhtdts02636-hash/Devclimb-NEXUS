package com.devclimb.puzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.devclimb.auth.AuthenticationService;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.community.Community;
import com.devclimb.community.CommunityRepository;
import com.devclimb.community.UserCommunityId;
import com.devclimb.community.UserCommunityRepository;
import com.devclimb.contest.Contest;
import com.devclimb.contest.ContestRepository;
import com.devclimb.contest.PuzzleContest;
import com.devclimb.contest.PuzzleContestRepository;
import com.devclimb.language.Language;
import com.devclimb.language.LanguageRepository;
import com.devclimb.language.PuzzleLanguage;
import com.devclimb.language.PuzzleLanguageRepository;
import com.devclimb.ptag.Ptag;
import com.devclimb.ptag.PtagPuzzle;
import com.devclimb.ptag.PtagPuzzleRepository;
import com.devclimb.ptag.PtagRepository;
import com.devclimb.testcase.Testcase;
import com.devclimb.testcase.TestcaseRepository;
import com.devclimb.user.User;

import jakarta.persistence.EntityManager;

@Service
public class PuzzleService {

	private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
	private static final int MAXIMUM_TAGS = 20;
	private static final int MAXIMUM_TAG_LENGTH = 50;

	private final PuzzleRepository puzzleRepository;
	private final PuzzleLanguageRepository puzzleLanguageRepository;
	private final PtagPuzzleRepository ptagPuzzleRepository;
	private final PtagRepository ptagRepository;
	private final TestcaseRepository testcaseRepository;
	private final LanguageRepository languageRepository;
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final ContestRepository contestRepository;
	private final PuzzleContestRepository puzzleContestRepository;
	private final AuthenticationService authenticationService;
	private final FunctionHeaderValidator functionHeaderValidator;
	private final TransactionTemplate transactionTemplate;
	private final EntityManager entityManager;

	public PuzzleService(
			PuzzleRepository puzzleRepository,
			PuzzleLanguageRepository puzzleLanguageRepository,
			PtagPuzzleRepository ptagPuzzleRepository,
			PtagRepository ptagRepository,
			TestcaseRepository testcaseRepository,
			LanguageRepository languageRepository,
			CommunityRepository communityRepository,
			UserCommunityRepository userCommunityRepository,
			ContestRepository contestRepository,
			PuzzleContestRepository puzzleContestRepository,
			AuthenticationService authenticationService,
			FunctionHeaderValidator functionHeaderValidator,
			PlatformTransactionManager transactionManager,
			EntityManager entityManager) {
		this.puzzleRepository = puzzleRepository;
		this.puzzleLanguageRepository = puzzleLanguageRepository;
		this.ptagPuzzleRepository = ptagPuzzleRepository;
		this.ptagRepository = ptagRepository;
		this.testcaseRepository = testcaseRepository;
		this.languageRepository = languageRepository;
		this.communityRepository = communityRepository;
		this.userCommunityRepository = userCommunityRepository;
		this.contestRepository = contestRepository;
		this.puzzleContestRepository = puzzleContestRepository;
		this.authenticationService = authenticationService;
		this.functionHeaderValidator = functionHeaderValidator;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.entityManager = entityManager;
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
		List<String> excludedNames = new ArrayList<>();
		Set<String> requiredLanguages = new LinkedHashSet<>();
		Set<String> optionalLanguages = new LinkedHashSet<>();
		Set<String> excludedLanguages = new LinkedHashSet<>();
		Set<String> requiredTags = new LinkedHashSet<>();
		Set<String> optionalTags = new LinkedHashSet<>();
		Set<String> excludedTags = new LinkedHashSet<>();

		String normalizedQuery = StringUtils.hasText(query) ? query.trim() : "";
		normalizedQuery = normalizedQuery.replaceAll("\\s*\\|\\|\\s*", "||");
		for (String token : normalizedQuery.split("\\s+")) {
			if (token.isEmpty()) {
				continue;
			}
			// Xử lý tiền tố loại trừ '-' (-:ngôn ngữ, -#tag, -tên) và bộ lọc thông thường
			if (token.startsWith("-:") && token.length() > 2) {
				excludedLanguages.add(token.substring(2).trim().toLowerCase(Locale.ROOT));
			} else if (token.startsWith("-#") && token.length() > 2) {
				excludedTags.add(token.substring(2).trim().toLowerCase(Locale.ROOT));
			} else if (token.startsWith("-") && token.length() > 1) {
				excludedNames.add(token.substring(1).trim().toLowerCase(Locale.ROOT));
			} else if (token.startsWith(":") && token.length() > 1) {
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
				String.join(",", excludedNames),
				String.join(",", excludedLanguages),
				String.join(",", excludedTags),
				PageRequest.of(page, size));
		return toDTOs(puzzles);
	}

	// Lấy danh sách puzzle của một cộng đồng theo tên (handle).
	// Nếu cộng đồng ở chế độ riêng tư, chỉ cho phép thành viên truy cập.
	@Transactional(readOnly = true)
	public List<PuzzleDTO> findByCommunityName(AuthenticatedFirebaseUser firebaseUser, String communityName) {
		User user = authenticationService.findCurrentUser(firebaseUser);
		Community community = communityRepository.findByNameAndActiveTrue(communityName)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));

		if (!community.isPublic() && !userCommunityRepository.existsById(new UserCommunityId(user.getId(), community.getId()))) {
			throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
		}

		List<Puzzle> puzzles = puzzleRepository.findByCommunityId(community.getId());
		return toDTOs(puzzles);
	}

	// Chuyển đổi danh sách Puzzle entity sang PuzzleDTO, kèm nạp trước ngôn ngữ và tag tương ứng.
	private List<PuzzleDTO> toDTOs(List<Puzzle> puzzles) {
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

	public PuzzleDTO createPuzzle(CreatePuzzleRequest request, AuthenticatedFirebaseUser firebaseUser) {
		User author = authenticationService.findCurrentUser(firebaseUser);
		Community community = communityRepository.findById(request.communityId())
				.filter(Community::isActive)
				.orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
		requireMembership(author.getId(), community.getId());
		if (request.contestId() != null) {
			Contest contest = contestRepository.findByIdWithCommunity(request.contestId())
					.orElseThrow(() -> new ApiException(ErrorCode.CONTEST_NOT_FOUND));
			if (!contest.getCommunity().getId().equals(community.getId())) {
				throw new ApiException(ErrorCode.CONTEST_COMMUNITY_MISMATCH);
			}
		}

		Map<Integer, Language> languages = loadLanguages(request.languages());
		List<FunctionHeaderValidator.LanguageHeader> headers = request.languages().stream()
				.map(language -> new FunctionHeaderValidator.LanguageHeader(
						languages.get(language.languageId()),
						language.functionHeader().trim()))
				.toList();
		// Cố tình gọi Judge0 ra ngoài transaction bên dưới: network call có thể chậm
		// hoặc timeout, không nên giữ DB transaction mở trong lúc chờ.
		functionHeaderValidator.validate(headers);

		List<String> tagNames = normalizeTags(request.tags());

		return Objects.requireNonNull(transactionTemplate.execute(status -> persistPuzzle(
				author.getId(),
				community.getId(),
				request,
				tagNames)), "Created puzzle is required.");
	}

	private void requireMembership(Integer userId, Integer communityId) {
		// Tạm thời chỉ kiểm tra membership. Sau này nếu thêm role, chỉ cần thay
		// method này mà không phải sửa phần còn lại của luồng tạo puzzle.
		if (!userCommunityRepository.existsById(new UserCommunityId(userId, communityId))) {
			throw new ApiException(ErrorCode.NOT_A_MEMBER);
		}
	}

	private Map<Integer, Language> loadLanguages(List<CreatePuzzleRequest.PuzzleLanguageRequest> requested) {
		Set<Integer> languageIds = new LinkedHashSet<>();
		for (CreatePuzzleRequest.PuzzleLanguageRequest language : requested) {
			if (!languageIds.add(language.languageId())) {
				throw new ApiException(ErrorCode.PUZZLE_LANGUAGE_INVALID);
			}
		}

		Map<Integer, Language> languages = languageRepository.findAllById(languageIds).stream()
				.collect(Collectors.toMap(Language::getId, Function.identity()));
		if (languages.size() != languageIds.size()) {
			throw new ApiException(ErrorCode.PUZZLE_LANGUAGE_INVALID);
		}
		return languages;
	}

	private PuzzleDTO persistPuzzle(
			Integer authorId,
			Integer communityId,
			CreatePuzzleRequest request,
			List<String> tagNames) {
		User author = entityManager.getReference(User.class, authorId);
		Community community = entityManager.getReference(Community.class, communityId);
		Puzzle puzzle = new Puzzle(
				author,
				community,
				request.difficulty(),
				request.title().trim(),
				request.content().trim());
		puzzleRepository.saveAndFlush(puzzle);

		List<PuzzleLanguage> puzzleLanguages = request.languages().stream()
				.map(language -> new PuzzleLanguage(
						puzzle,
						entityManager.getReference(Language.class, language.languageId()),
						language.functionHeader().trim()))
				.toList();
		puzzleLanguageRepository.saveAll(puzzleLanguages);

		List<Testcase> testcases = request.testcases().stream()
				.map(testcase -> new Testcase(puzzle, testcase.input(), testcase.output()))
				.toList();
		testcaseRepository.saveAll(testcases);

		List<Ptag> tags = attachTags(puzzle, tagNames);
		if (request.contestId() != null) {
			Contest contest = entityManager.getReference(Contest.class, request.contestId());
			puzzleContestRepository.save(new PuzzleContest(puzzle, contest, request.contestScore()));
		}
		return PuzzleDTO.from(
				puzzle,
				puzzleLanguages,
				tags.stream().map(Ptag::getName).toList());
	}

	private List<Ptag> attachTags(Puzzle puzzle, List<String> tagNames) {
		if (tagNames.isEmpty()) {
			return List.of();
		}

		// Upsert trước để hai người cùng tạo một tag mới không đụng unique
		// constraint trên ptag_name.
		for (String tagName : tagNames) {
			ptagRepository.insertIfAbsent(tagName);
		}
		List<Ptag> tags = ptagRepository.findByNameIn(tagNames);
		ptagPuzzleRepository.saveAll(tags.stream()
				.map(tag -> new PtagPuzzle(tag, puzzle))
				.toList());
		return tags;
	}

	private List<String> normalizeTags(List<String> rawTags) {
		if (rawTags == null) {
			return List.of();
		}

		Set<String> normalized = new LinkedHashSet<>();
		for (String rawTag : rawTags) {
			String tag = normalizeTag(rawTag);
			if (tag.isEmpty()) {
				continue;
			}
			if (tag.length() > MAXIMUM_TAG_LENGTH) {
				throw new ApiException(ErrorCode.INVALID_REQUEST);
			}
			normalized.add(tag);
			if (normalized.size() > MAXIMUM_TAGS) {
				throw new ApiException(ErrorCode.INVALID_REQUEST);
			}
		}
		return List.copyOf(normalized);
	}

	private String normalizeTag(String rawTag) {
		if (rawTag == null) {
			return "";
		}
		return rawTag.trim()
				.toLowerCase(Locale.ROOT)
				.replaceAll("\\s+", "-")
				.replaceAll("-+", "-")
				.replaceAll("^-|-$", "");
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
