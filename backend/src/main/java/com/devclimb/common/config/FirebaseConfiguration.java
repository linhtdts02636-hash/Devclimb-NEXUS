package com.devclimb.common.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.devclimb.common.security.FirebaseAuthenticationFilter;
import com.devclimb.user.UserRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfiguration {

	@Bean
	public FirebaseApp firebaseApp(@Value("${firebase.service-account-file}") Resource serviceAccountFile)
			throws IOException {
		// Spring test contexts can be created more than once in the same JVM.
		// Reuse the already initialized default app instead of failing on re-initialization.
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}

		try (InputStream serviceAccountStream = serviceAccountFile.getInputStream()) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
					.build();
			return FirebaseApp.initializeApp(options);
		}
	}

	@Bean
	public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
		return FirebaseAuth.getInstance(firebaseApp);
	}

	@Bean
	public FirebaseAuthenticationFilter firebaseAuthenticationFilter(
			FirebaseAuth firebaseAuth,
			UserRepository userRepository,
			@Value("${app.auth.dev-header.enabled:false}") boolean devHeaderEnabled) {
		return new FirebaseAuthenticationFilter(firebaseAuth, userRepository, devHeaderEnabled);
	}

	@Bean
	public FilterRegistrationBean<FirebaseAuthenticationFilter> firebaseAuthenticationFilterRegistration(
			FirebaseAuthenticationFilter firebaseAuthenticationFilter) {
		FilterRegistrationBean<FirebaseAuthenticationFilter> registration =
				new FilterRegistrationBean<>(firebaseAuthenticationFilter);
		// Filter được gắn vào SecurityFilterChain, tắt đăng ký tự động của servlet container để tránh chạy hai lần.
		registration.setEnabled(false);
		return registration;
	}
}
