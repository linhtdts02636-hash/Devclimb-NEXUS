package com.devclimb.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.devclimb.common.security.FirebaseAuthenticationFilter;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
public class AuthenticationFilterConfiguration {

	@Bean
	public FilterRegistrationBean<FirebaseAuthenticationFilter> firebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
		FilterRegistrationBean<FirebaseAuthenticationFilter> registration = new FilterRegistrationBean<>(
				new FirebaseAuthenticationFilter(firebaseAuth));
		// The servlet container normalises the path before matching, so encoded or ";param" variants cannot skip the filter.
		registration.addUrlPatterns("/api/*");
		return registration;
	}
}
