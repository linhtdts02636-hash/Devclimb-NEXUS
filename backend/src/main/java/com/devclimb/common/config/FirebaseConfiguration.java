package com.devclimb.common.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
public class FirebaseConfiguration {

	@Bean
	public FirebaseApp firebaseApp(@Value("${firebase.service-account-file}") Resource serviceAccountFile)
			throws IOException {
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
}
