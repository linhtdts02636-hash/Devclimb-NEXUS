package com.devclimb.upload;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.devclimb.common.config.R2Properties;
import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.user.User;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class ImageUploadService {

	public static final String COMMUNITY_IMAGE_FOLDER = "communities/";

	private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp");
	private static final long MAXIMUM_IMAGE_BYTES = 2L * 1024 * 1024;
	private static final long MAXIMUM_BANNER_BYTES = 5L * 1024 * 1024;
	private static final Duration UPLOAD_LINK_LIFETIME = Duration.ofMinutes(5);

	private final S3Presigner presigner;
	private final R2Properties r2Properties;

	public ImageUploadService(S3Presigner presigner, R2Properties r2Properties) {
		this.presigner = presigner;
		this.r2Properties = r2Properties;
	}

	public ImageUploadDTO createCommunityImageUpload(User user, ImageUploadRequest request) {
		String extension = request.contentType() == null ? null : EXTENSION_BY_CONTENT_TYPE.get(request.contentType());
		long maximumBytes = request.purpose() == ImageUploadRequest.Purpose.BANNER ? MAXIMUM_BANNER_BYTES : MAXIMUM_IMAGE_BYTES;
		if (extension == null || request.size() <= 0 || request.size() > maximumBytes) {
			throw new ApiException(ErrorCode.UPLOAD_REJECTED);
		}

		String objectKey = COMMUNITY_IMAGE_FOLDER + user.getId() + "/" + UUID.randomUUID() + "." + extension;
		PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(r2Properties.bucket())
				.key(objectKey)
				.contentType(request.contentType())
				.contentLength(request.size())
				.build();
		String uploadUrl = presigner.presignPutObject(PutObjectPresignRequest.builder()
				.signatureDuration(UPLOAD_LINK_LIFETIME)
				.putObjectRequest(putRequest)
				.build()).url().toString();
		return new ImageUploadDTO(uploadUrl, r2Properties.publicUrlOf(objectKey));
	}
}
