package com.takeone.backend.websocket;

import com.takeone.backend.entity.Session;
import com.takeone.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final SessionService sessionService;

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token == null) {
                // Try from header if available (some clients support this)
                token = servletRequest.getServletRequest().getHeader("X-Session-Token");
            }

            if (token != null) {
                try {
                    Session session = sessionService.validateAndRefreshSession(token,
                            servletRequest.getServletRequest());
                    if (session != null && session.isValid() && session.getUser() != null) {
                        attributes.put("userId", session.getUser().getId());
                        log.debug("WebSocket handshake authenticated for user: {}", session.getUser().getId());
                        return true;
                    }
                } catch (Exception e) {
                    log.error("WebSocket auth failed: {}", e.getMessage());
                }
            }
        }
        log.warn("WebSocket handshake failed: Missing or invalid token");
        return false;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
                               @NotNull WebSocketHandler wsHandler, Exception exception) {
    }
}
