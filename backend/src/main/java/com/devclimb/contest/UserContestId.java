package com.devclimb.contest;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Khóa phức hợp gồm user_id và cont_id cho bảng liên kết user_contest
@Embeddable
public class UserContestId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "cont_id")
	private Integer contestId;

	protected UserContestId() {
	}

	public UserContestId(Integer userId, Integer contestId) {
		this.userId = userId;
		this.contestId = contestId;
	}

	public Integer getUserId() {
		return userId;
	}

	public Integer getContestId() {
		return contestId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserContestId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId)
				&& Objects.equals(contestId, that.contestId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, contestId);
	}
}
