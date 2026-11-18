package com.devclimb.common.security;

import com.devclimb.user.User;
import com.devclimb.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile({"dev", "test"})
@ConditionalOnProperty(prefix = "app.auth.dev-header", name = "enabled", havingValue = "true")
public class DevAuthenticationFilter extends OncePerRequestFilter {

    public static final String DEV_USER_HEADER = "X-Dev-User-Id";

    private final UserRepository userRepository;

    public DevAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(DEV_USER_HEADER);

        if (header != null && !header.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Integer userId = Integer.valueOf(header.trim());
                userRepository.findById(userId)
                        .filter(User::isActive)
                        .ifPresent(user -> authenticate(request, user));
            } catch (NumberFormatException ignored) {
                // Header dev không hợp lệ được coi như chưa đăng nhập.
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, User user) {
        CurrentUser principal = new CurrentUser(user.getId(), user.getName(), user.getImageUrl());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
