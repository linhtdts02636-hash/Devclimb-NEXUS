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
		return HttpMethod.OPTIONS.matches(request.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			reject(response, ErrorCode.MISSING_TOKEN);
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
