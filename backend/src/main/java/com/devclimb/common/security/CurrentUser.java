package com.devclimb.common.security;

import org.springframework.security.core.Authentication;

public record CurrentUser(Integer id, String name, String avatarUrl) {

    public static CurrentUser from(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }
        return null;
    }
}
