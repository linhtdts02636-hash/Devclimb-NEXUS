package com.devclimb.contest;

import java.time.Instant;

import com.devclimb.community.Community;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Thực thể đại diện cho cuộc thi (bảng contest trong mirrorSchema.sql)
@Entity
@Table(name = "contest")
public class Contest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cont_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comm_id", nullable = false)
	private Community community;

	@Column(name = "cont_title", nullable = false, length = 250)
	private String title;

	@Column(name = "cont_content", nullable = false)
	private String content;

	@Column(name = "cont_banner")
	private String banner;

	@Column(name = "cont_start", nullable = false)
	private Instant startTime;

	@Column(name = "cont_end", nullable = false)
	private Instant endTime;

	protected Contest() {
	}

	public Contest(
			Community community,
			String title,
			String content,
			String banner,
			Instant startTime,
			Instant endTime) {
		this.community = community;
		this.title = title;
		this.content = content;
		this.banner = banner;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Integer getId() {
		return id;
	}

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}
}
