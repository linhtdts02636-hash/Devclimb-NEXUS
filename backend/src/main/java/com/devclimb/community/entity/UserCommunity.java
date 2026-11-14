package com.devclimb.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_community")
public class UserCommunity {

    @EmbeddedId
    private UserCommunityId id;

    @Column(name = "ucom_joinedat", nullable = false)
    private Instant joinedAt;

    public UserCommunityId getId() {
        return id;
    }

    public void setId(UserCommunityId id) {
        this.id = id;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
