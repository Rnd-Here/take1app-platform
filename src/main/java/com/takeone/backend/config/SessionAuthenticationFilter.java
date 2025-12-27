package com.takeone.backend.config;

import com.takeone.backend.model.Session;
import com.takeone.backend.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<Session> sessionOpt = sessionService.findByRefreshToken(token);

            if (sessionOpt.isPresent() && sessionService.isSessionValid(sessionOpt.get())) {
                Session session = sessionOpt.get();
                // Update last accessed
                // In a real high-throughput app, we might check/update this async or less
                // frequently

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        session.getUser(),
                        null,
                        // Updated to AccountType
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + session.getUser().getAccountType())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
