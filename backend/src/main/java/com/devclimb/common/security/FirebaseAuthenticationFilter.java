package com.devclimb.common.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.devclimb.common.exception.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedFirebaseUser";

	private static final String PUBLIC_SUBMISSION_PATH = ""; // For testing, if you don't want to go through auth to test an API, put it here
	private static final String COMMUNITY_PATH_PREFIX = "/api/communities/";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final boolean CHECK_REVOKED = true;
	private static final String FIREBASE_CLAIM = "firebase";
	private static final String SIGN_IN_PROVIDER_CLAIM = "sign_in_provider";

	private final FirebaseAuth firebaseAuth;

	public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
		this.firebaseAuth = firebaseAuth;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		return HttpMethod.OPTIONS.matches(request.getMethod())
				|| PUBLIC_SUBMISSION_PATH.equals(servletPath)
				|| (PUBLIC_SUBMISSION_PATH + "/").equals(servletPath);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		boolean publicRead = isPublicRead(request);
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		// Các API đọc công khai vẫn nhận token nếu có, nhưng không bắt buộc phải có token.
		if (authorizationHeader == null) {
			if (publicRead) {
				filterChain.doFilter(request, response);
				return;
			}
			reject(response, ErrorCode.MISSING_TOKEN);
			return;
		}

		if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
			reject(response, publicRead ? ErrorCode.INVALID_TOKEN : ErrorCode.MISSING_TOKEN);
			return;
		}

		try {
			String idToken = authorizationHeader.substring(BEARER_PREFIX.length());
			FirebaseToken verifiedToken = firebaseAuth.verifyIdToken(idToken, CHECK_REVOKED);
			request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, toAuthenticatedUser(verifiedToken));
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

	private boolean isPublicRead(HttpServletRequest request) {
		if (!HttpMethod.GET.matches(request.getMethod())) {
			return false;
		}

		String servletPath = request.getServletPath();
		if (!servletPath.startsWith(COMMUNITY_PATH_PREFIX)) {
			return false;
		}

		String remainder = servletPath.substring(COMMUNITY_PATH_PREFIX.length()).replaceAll("/+$", "");
		String[] segments = remainder.split("/");
		if (segments.length == 1) {
			// /api/communities/mine là danh sách cộng đồng của chính người dùng, vẫn phải đăng nhập.
			return !"mine".equals(segments[0]);
		}

		// /api/communities/{id}/posts là feed công khai của cộng đồng public.
		return segments.length == 2 && "posts".equals(segments[1]) && segments[0].matches("\\d+");
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
