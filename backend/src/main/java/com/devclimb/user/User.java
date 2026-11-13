package com.devclimb.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer id;

	@Column(name = "user_email", nullable = false, unique = true, length = 50)
	private String email;

	@Column(name = "user_name", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "user_firebaseuid", nullable = false, unique = true, length = 300)
	private String firebaseUid;

	@Column(name = "user_isadmin", nullable = false)
	private boolean admin;

	@Column(name = "user_image")
	private String imageUrl;

	@Column(name = "user_bio")
	private String biography;

	@Column(name = "user_isactive", nullable = false)
	private boolean active;

	@Column(name = "user_createdat", insertable = false, updatable = false)
	private Instant createdAt;

	protected User() {
	}

	public User(String email, String name, String firebaseUid, String imageUrl) {
		this.email = email;
		this.name = name;
		this.firebaseUid = firebaseUid;
		this.imageUrl = imageUrl;
		this.admin = false;
		this.active = true;
	}

	public Integer getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getFirebaseUid() {
		return firebaseUid;
	}

	public boolean isAdmin() {
		return admin;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getBiography() {
		return biography;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
