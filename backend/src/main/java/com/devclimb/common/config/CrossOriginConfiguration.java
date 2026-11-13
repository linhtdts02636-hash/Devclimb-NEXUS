package com.devclimb.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CrossOriginConfiguration {

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter(
			@Value("${application.cors.allowed-origin}") String allowedOrigin) {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(List.of(allowedOrigin));
		corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
		configurationSource.registerCorsConfiguration("/api/**", corsConfiguration);

		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(
				new CorsFilter(configurationSource));
		// Must run before the authentication filter so that 401 responses still carry CORS headers.
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
