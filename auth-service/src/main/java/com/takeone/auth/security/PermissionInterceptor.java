package com.takeone.auth.security;

import com.takeone.auth.dto.AuthContext;
import com.takeone.auth.service.ProfileManagerAuthService;
import com.takeone.common.annotation.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Permission Interceptor
 * Automatically checks permissions for @RequirePermission annotated endpoints
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileManagerAuthService profileManagerAuthService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) throws Exception {
        
        // TODO: Implement permission checking
        // 1. Check if handler has @RequirePermission annotation
        // 2. Extract token from request header
        // 3. Get auth context from token
        // 4. If user is acting as manager:
        //    a. Get required permission from annotation
        //    b. Check if manager has this permission
        //    c. If not, throw UnauthorizedException
        // 5. Log activity for manager actions
        // 6. Return true to continue
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        
        if (annotation == null) {
            return true; // No permission check required
        }
        
        // Extract and validate token
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        AuthContext context = jwtTokenProvider.getAuthContext(token);
        
        // Check permissions for manager
        if (context.isActingAsManager()) {
            String requiredPermission = annotation.value();
            boolean hasPermission = profileManagerAuthService.hasPermission(
                context.getActualUserId(),
                context.getEffectiveUserId(),
                requiredPermission
            );
            
            if (!hasPermission) {
                log.warn("Manager {} lacks permission {} for talent {}", 
                    context.getActualUserId(), requiredPermission, context.getEffectiveUserId());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        
        return true;
    }
}
