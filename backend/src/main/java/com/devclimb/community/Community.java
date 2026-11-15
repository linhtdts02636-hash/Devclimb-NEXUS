package com.devclimb.community;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "community")
public class Community {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comm_id")
	private Integer id;

	@Column(name = "comm_name", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "comm_image")
	private String imageUrl;

	@Column(name = "comm_banner")
	private String bannerUrl;

	@Column(name = "comm_shoutout")
	private String shoutout;

	@Column(name = "comm_ispublic", nullable = false)
	private boolean publicCommunity;

	@Column(name = "comm_isactive", nullable = false)
	private boolean active;

	@Generated(event = EventType.INSERT)
	@Column(name = "comm_createdat", insertable = false, updatable = false)
	private Instant createdAt;

	protected Community() {
	}

	public Community(String name, String shoutout, boolean publicCommunity, String imageUrl, String bannerUrl) {
		this.name = name;
		this.shoutout = shoutout;
		this.publicCommunity = publicCommunity;
		this.imageUrl = imageUrl;
		this.bannerUrl = bannerUrl;
		this.active = true;
	}

	public void update(String shoutout, boolean publicCommunity, String imageUrl, String bannerUrl) {
		this.shoutout = shoutout;
		this.publicCommunity = publicCommunity;
		this.imageUrl = imageUrl;
		this.bannerUrl = bannerUrl;
	}

	public void rename(String newName) {
		this.name = newName;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getBannerUrl() {
		return bannerUrl;
	}

	public String getShoutout() {
		return shoutout;
	}

	public boolean isPublic() {
		return publicCommunity;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
