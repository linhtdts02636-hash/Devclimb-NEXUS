package com.devclimb.contest;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.devclimb.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

// Thực thể ghi nhận thông tin thí sinh tham gia cuộc thi (khớp cấu trúc bảng user_contest trong mirrorSchema.sql)
@Entity
@Table(name = "user_contest")
public class UserContest {

	@EmbeddedId
	private UserContestId id;

	@MapsId("userId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@MapsId("contestId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cont_id", nullable = false)
	private Contest contest;

	@Generated(event = EventType.INSERT)
	@Column(name = "ucon_joinedat", insertable = false, updatable = false)
	private Instant joinedAt;

	protected UserContest() {
	}

	public UserContest(User user, Contest contest) {
		this.user = user;
		this.contest = contest;
		if (user != null && contest != null) {
			this.id = new UserContestId(user.getId(), contest.getId());
		}
	}

	public UserContestId getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Contest getContest() {
		return contest;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
