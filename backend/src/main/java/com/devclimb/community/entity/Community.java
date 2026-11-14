package com.devclimb.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "community")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comm_id")
    private Integer id;

    @Column(name = "comm_name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "comm_image", columnDefinition = "text")
    private String image;

    @Column(name = "comm_banner", columnDefinition = "text")
    private String banner;

    @Column(name = "comm_shoutout", columnDefinition = "text")
    private String shoutout;

    @Column(name = "comm_ispublic", nullable = false)
    private boolean publicCommunity;

    @Column(name = "comm_isactive", nullable = false)
    private boolean active;

    @Column(name = "comm_createdat", nullable = false)
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getShoutout() {
        return shoutout;
    }

    public void setShoutout(String shoutout) {
        this.shoutout = shoutout;
    }

    public boolean isPublicCommunity() {
        return publicCommunity;
    }

    public void setPublicCommunity(boolean publicCommunity) {
        this.publicCommunity = publicCommunity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
