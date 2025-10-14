package org.project.common.security.util;

import lombok.extern.slf4j.Slf4j;
import org.project.common.security.token.GatewayAuthenticationToken;
import org.project.common.security.token.GatewayUserPrincipal;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SecurityUtils {

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return authentication;
    }

    public GatewayUserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = getAuthentication();

        if (authentication instanceof GatewayAuthenticationToken gatewayAuth) {
            return (GatewayUserPrincipal) gatewayAuth.getPrincipal();
        }

        // Fallback cho các loại authentication khác (nếu có)
        Object principal = authentication.getPrincipal();
        if (principal instanceof GatewayUserPrincipal gatewayPrincipal) {
            return gatewayPrincipal;
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    public UUID getCurrentUserId() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        return principal.getUserId();
    }

    public String getCurrentUsername() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        return principal.getUsername();
    }

    public String getCurrentEmail() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        return principal.getEmail();
    }

    public List<String> getCurrentUserRoles() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        List<String> roles = principal.getRoles();
        return roles;
    }

    public boolean hasRole(String role) {
        try {
            Authentication authentication = getAuthentication();
            String roleWithPrefix = "ROLE_" + role.toUpperCase();

            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));

            return hasRole;
        } catch (CustomException e) {
            return false;
        }
    }


    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCurrentUserAdmin() {
        return hasRole("ADMIN");
    }

    public void validateUserAccess(UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();

        // Admin có quyền truy cập tất cả
        if (isCurrentUserAdmin()) {
            return;
        }

        // User chỉ được truy cập thông tin của chính mình
        if (currentUserId.equals(targetUserId)) {
            return;
        }

        throw new CustomException(ErrorCode.FORBIDDEN);
    }

    public void requireRole(String role) {
        if (!hasRole(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public void requireAnyRole(String... roles) {
        if (!hasAnyRole(roles)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public void requireAllRoles(String... roles) {
        if (!hasAllRoles(roles)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
