package com.devclimb.upload;

public record ImageUploadRequest(Purpose purpose, String contentType, long size) {

	public enum Purpose {
		IMAGE,
		BANNER,
		CONTEST_BANNER
	}
}
