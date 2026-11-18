package com.devclimb.common.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.devclimb.common.exception.ErrorCode;
import com.devclimb.user.User;
import com.devclimb.user.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedFirebaseUser";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final boolean CHECK_REVOKED = true;
	private static final String FIREBASE_CLAIM = "firebase";
	private static final String SIGN_IN_PROVIDER_CLAIM = "sign_in_provider";

	private final FirebaseAuth firebaseAuth;
	private final UserRepository userRepository;
	private final boolean devHeaderEnabled;

	public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth, UserRepository userRepository,
			boolean devHeaderEnabled) {
		this.firebaseAuth = firebaseAuth;
		this.userRepository = userRepository;
		this.devHeaderEnabled = devHeaderEnabled;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// Khi bật header dev/test thì filter này không tham gia xác thực.
		// Các request không thuộc /api cũng không cần xác thực Firebase.
		return devHeaderEnabled
				|| HttpMethod.OPTIONS.matches(request.getMethod())
				|| !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			// Các API auth bắt buộc phải có Firebase token; API công khai có thể chạy ẩn danh.
			if (request.getRequestURI().startsWith("/api/auth/")) {
				reject(response, ErrorCode.MISSING_TOKEN);
				return;
			}
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String idToken = authorizationHeader.substring(BEARER_PREFIX.length());
			FirebaseToken verifiedToken = firebaseAuth.verifyIdToken(idToken, CHECK_REVOKED);
			AuthenticatedFirebaseUser firebaseUser = toAuthenticatedUser(verifiedToken);

			// Giữ request attribute để các controller hiện có (đặc biệt CommunityController)
			// vẫn đọc được người dùng Firebase qua @RequestAttribute.
			request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, firebaseUser);

			// Đồng thời đưa người dùng trong database vào SecurityContext để các endpoint
			// dùng Authentication/CurrentUser hoạt động đúng.
			authenticate(request, firebaseUser);
		} catch (FirebaseAuthException exception) {
			logger.warn("Rejected Firebase token: " + exception.getAuthErrorCode());
			reject(response, ErrorCode.INVALID_TOKEN);
			return;
		} catch (IllegalArgumentException exception) {
			reject(response, ErrorCode.INVALID_TOKEN);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void authenticate(HttpServletRequest request, AuthenticatedFirebaseUser firebaseUser) {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			return;
		}

		// Token hợp lệ nhưng user chưa có trong database thì để request chạy tiếp.
		// /api/auth/session sẽ tự tạo user mới; các API bảo vệ khác sẽ nhận 401.
		userRepository.findByFirebaseUid(firebaseUser.firebaseUid())
				.filter(User::isActive)
				.ifPresent(user -> {
					CurrentUser principal = new CurrentUser(user.getId(), user.getName(), user.getImageUrl());
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(principal, null, List.of());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
	}

	private AuthenticatedFirebaseUser toAuthenticatedUser(FirebaseToken token) {
		return new AuthenticatedFirebaseUser(
				token.getUid(),
				token.getEmail(),
				token.getName(),
				token.getPicture(),
				token.isEmailVerified(),
				readSignInProvider(token));
	}

	private String readSignInProvider(FirebaseToken token) {
		Object firebaseClaim = token.getClaims().get(FIREBASE_CLAIM);
		if (firebaseClaim instanceof Map<?, ?> firebaseClaimValues) {
			return String.valueOf(firebaseClaimValues.get(SIGN_IN_PROVIDER_CLAIM));
		}
		return null;
	}

	private void reject(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"code\":\"" + errorCode.name() + "\",\"message\":\"" + errorCode.getMessage() + "\"}");
	}
}
