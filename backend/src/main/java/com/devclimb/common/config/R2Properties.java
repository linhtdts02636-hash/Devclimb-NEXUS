package com.devclimb.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
		String accountId,
		String accessKeyId,
		String secretAccessKey,
		String bucket,
		String publicBaseUrl) {

	public String publicUrlOf(String objectKey) {
		return publicBaseUrl.replaceAll("/+$", "") + "/" + objectKey;
	}
}
