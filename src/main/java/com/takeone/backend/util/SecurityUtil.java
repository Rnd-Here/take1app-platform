package com.takeone.backend.util;

import com.takeone.backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access current authenticated user information
 * Use this in your controllers and services to get the current user
 */
public class SecurityUtil {

    /**
     * Get current authenticated user principal
     *
     * @return UserPrincipal or null if not authenticated
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }

        return null;
    }

    /**
     * Get current authenticated user ID
     *
     * @return User ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get current authenticated user's Firebase UID
     *
     * @return Firebase UID or null if not authenticated
     */
    public static String getCurrentUserUid() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Get current authenticated username
     *
     * @return Username or null if not authenticated
     */
    public static String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Check if current user is authenticated
     *
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserPrincipal;
    }

    /**
     * Check if current user has specific account type
     */
    public static boolean hasAccountType(com.takeone.backend.entity.AccountType accountType) {
        UserPrincipal user = getCurrentUser();
        return user != null && accountType.equals(user.getAccountType());
    }
}