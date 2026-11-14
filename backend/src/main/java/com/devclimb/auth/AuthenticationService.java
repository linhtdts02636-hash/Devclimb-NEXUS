package com.devclimb.auth;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.user.User;
import com.devclimb.user.UserRepository;

@Service
public class AuthenticationService {

	private static final int MAXIMUM_EMAIL_LENGTH = 50;
	private static final int MAXIMUM_NAME_LENGTH = 50;
	private static final int NAME_SUFFIX_DIGIT_COUNT = 4;
	private static final int NAME_SUFFIX_BOUND = 10_000;
	private static final int NAME_SUFFIX_LENGTH = 1 + NAME_SUFFIX_DIGIT_COUNT;
	private static final int MAXIMUM_NAME_ATTEMPTS = 20;
	private static final String FALLBACK_NAME = "climber";
	private static final Set<String> RESERVED_NAMES = Set.of(
			"admin", "administrator", "root", "system", "support", "moderator", "staff", "devclimb");
	private static final Pattern INVISIBLE_CHARACTERS = Pattern.compile("[\\p{C}\\p{Zl}\\p{Zp}]");
	private static final Pattern REPEATED_WHITESPACE = Pattern.compile("\\s+");

	private final UserRepository userRepository;
	private final SecureRandom randomNumberGenerator = new SecureRandom();

	public AuthenticationService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public User signIn(AuthenticatedFirebaseUser firebaseUser) {
		requireVerifiedEmail(firebaseUser);
		User user = userRepository.findByFirebaseUid(firebaseUser.firebaseUid())
				.orElseGet(() -> register(firebaseUser));
		requireActiveAccount(user);
		return user;
	}

	@Transactional(readOnly = true)
	public User findCurrentUser(AuthenticatedFirebaseUser firebaseUser) {
		requireVerifiedEmail(firebaseUser);
		User user = userRepository.findByFirebaseUid(firebaseUser.firebaseUid())
				.orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
		requireActiveAccount(user);
		return user;
	}

	private void requireVerifiedEmail(AuthenticatedFirebaseUser firebaseUser) {
		if (firebaseUser.signedInWithPassword() && !firebaseUser.emailVerified()) {
			throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
		}
	}

	private void requireActiveAccount(User user) {
		if (!user.isActive()) {
			throw new ApiException(ErrorCode.ACCOUNT_DISABLED);
		}
	}

	private User register(AuthenticatedFirebaseUser firebaseUser) {
		String email = normalizeEmail(firebaseUser.email());
		String name = generateUniqueName(firebaseUser.displayName(), email);
		return userRepository.save(new User(email, name, firebaseUser.firebaseUid(), keepSecureImageUrl(firebaseUser.pictureUrl())));
	}

	private String keepSecureImageUrl(String pictureUrl) {
		return pictureUrl != null && pictureUrl.startsWith("https://") ? pictureUrl : null;
	}

	private String normalizeEmail(String email) {
		if (!StringUtils.hasText(email)) {
			throw new ApiException(ErrorCode.EMAIL_UNAVAILABLE);
		}
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (normalizedEmail.length() > MAXIMUM_EMAIL_LENGTH) {
			throw new ApiException(ErrorCode.EMAIL_TOO_LONG);
		}
		return normalizedEmail;
	}

	private String generateUniqueName(String displayName, String email) {
		String baseName = sanitizeName(displayName);
		if (baseName.isEmpty()) {
			baseName = sanitizeName(emailLocalPart(email));
		}
		if (baseName.isEmpty()) {
			baseName = FALLBACK_NAME;
		}
		String firstCandidate = truncate(baseName, MAXIMUM_NAME_LENGTH);
		if (!isNameTaken(firstCandidate)) {
			return firstCandidate;
		}

		// user_name là UNIQUE trong schema nhưng tên hiển thị thì không, nên khi trùng sẽ thêm hậu tố số ngẫu nhiên.
		String suffixedBaseName = truncate(baseName, MAXIMUM_NAME_LENGTH - NAME_SUFFIX_LENGTH);
		for (int attempt = 0; attempt < MAXIMUM_NAME_ATTEMPTS; attempt++) {
			String candidate = suffixedBaseName + "-"
					+ String.format("%0" + NAME_SUFFIX_DIGIT_COUNT + "d", randomNumberGenerator.nextInt(NAME_SUFFIX_BOUND));
			if (!isNameTaken(candidate)) {
				return candidate;
			}
		}
		throw new ApiException(ErrorCode.ACCOUNT_CONFLICT);
	}

	private boolean isNameTaken(String name) {
		return RESERVED_NAMES.contains(name.toLowerCase(Locale.ROOT)) || userRepository.existsByNameIgnoreCase(name);
	}

	private String emailLocalPart(String email) {
		int separatorIndex = email.indexOf('@');
		return separatorIndex > 0 ? email.substring(0, separatorIndex) : email;
	}

	private String sanitizeName(String rawName) {
		if (rawName == null) {
			return "";
		}
		String normalizedName = Normalizer.normalize(rawName, Normalizer.Form.NFKC);
		String visibleName = INVISIBLE_CHARACTERS.matcher(normalizedName).replaceAll("");
		return REPEATED_WHITESPACE.matcher(visibleName).replaceAll(" ").trim();
	}

	private String truncate(String value, int maximumLength) {
		if (value.codePointCount(0, value.length()) <= maximumLength) {
			return value;
		}
		return value.substring(0, value.offsetByCodePoints(0, maximumLength)).stripTrailing();
	}
}
