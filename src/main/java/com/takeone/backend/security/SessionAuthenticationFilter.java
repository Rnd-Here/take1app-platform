package com.takeone.backend.security;

import com.takeone.backend.entity.Session;
import com.takeone.backend.entity.User;
import com.takeone.backend.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SESSION_TOKEN_HEADER = "X-Session-Token";
    private final SessionService sessionService;

    private static @org.checkerframework.checker.nullness.qual.NonNull UsernamePasswordAuthenticationToken getAuthenticationToken(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getUid(),
                user.getUsername(),
                user.getEmail(),
                user.getAccountType()
        );

        return new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                Collections.emptyList()
        );
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String sessionToken = extractSessionToken(request);

            if (StringUtils.hasText(sessionToken)) {
                // Validate session and set authentication
                Session session = sessionService.validateAndRefreshSession(sessionToken, request);

                if (session != null && session.isValid()) {
                    User user = session.getUser();

                    if (user != null && user.getIsActive()) {
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication = getAuthenticationToken(user);

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Session authenticated for user: {}", user.getUsername());
                    } else {
                        log.warn("User is inactive or null for session token");
                    }
                } else {
                    log.warn("Invalid or expired session token");
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Don't throw exception, let it pass to controller where 401 will be returned
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract session token from request headers
     * Supports both Authorization: Bearer <token> and X-Session-Token: <token>
     */
    private String extractSessionToken(HttpServletRequest request) {
        // Try Authorization header first
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // Try custom session token header
        String sessionToken = request.getHeader(SESSION_TOKEN_HEADER);
        if (StringUtils.hasText(sessionToken)) {
            return sessionToken;
        }

        return null;
    }
}