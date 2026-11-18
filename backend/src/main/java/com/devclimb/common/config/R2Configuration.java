package com.devclimb.common.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Configuration {

	@Bean
	public S3Presigner r2Presigner(R2Properties properties) {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey());
		return S3Presigner.builder()
				.region(Region.of("auto"))
				.endpointOverride(URI.create("https://" + properties.accountId() + ".r2.cloudflarestorage.com"))
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.build();
	}
}
