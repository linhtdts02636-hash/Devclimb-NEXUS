package com.devclimb.post;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.devclimb.community.Community;
import com.devclimb.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comm_id", nullable = false)
	private Community community;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_parentid")
	private Post parent;

	@Column(name = "post_title", length = 255)
	private String title;

	@Column(name = "post_content", nullable = false, columnDefinition = "text")
	private String content;

	@Generated(event = EventType.INSERT)
	@Column(name = "post_createdat", insertable = false, updatable = false, nullable = false)
	private Instant createdAt;

	@Column(name = "post_updatedat")
	private Instant updatedAt;

	protected Post() {
	}

	public Post(User author, Community community, String title, String content) {
		this.author = author;
		this.community = community;
		this.title = title;
		this.content = content;
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
		this.updatedAt = Instant.now();
	}

	public Integer getId() {
		return id;
	}

	public User getAuthor() {
		return author;
	}

	public Community getCommunity() {
		return community;
	}

	public Post getParent() {
		return parent;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
