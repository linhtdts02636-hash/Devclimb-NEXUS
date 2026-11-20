package com.devclimb.contest;

// Projection chứa thông tin thí sinh và điểm số từ truy vấn cơ sở dữ liệu
public interface ContestantView {

	Integer getUserId();

	String getUserName();

	String getUserImage();

	Boolean getIsAdmin();

	Long getPoints();
}
