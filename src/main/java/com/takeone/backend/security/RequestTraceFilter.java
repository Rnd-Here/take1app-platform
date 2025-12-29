package com.takeone.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    // MDC Keys
    private static final String MDC_APP_NAME = "appName";
    private static final String MDC_ENDPOINT = "endpoint";
    private static final String MDC_TRACE_ID = "traceId";
    private final String appName;

    public RequestTraceFilter(@Value("${spring.application.name:take-one-app-backend}") String appName) {
        this.appName = appName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);
        String endpoint = request.getRequestURI();

        if (traceId == null || traceId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Security Error: Missing " + TRACE_ID_HEADER + " header\"}");
            return;
        }

        try {
            MDC.put(MDC_APP_NAME, appName);
            MDC.put(MDC_ENDPOINT, endpoint);
            MDC.put(MDC_TRACE_ID, traceId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
