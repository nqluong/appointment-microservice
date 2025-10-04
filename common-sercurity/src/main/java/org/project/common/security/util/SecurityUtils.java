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
    /**
     * Lấy Authentication hiện tại
     */
    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return authentication;
    }

    /**
     * Lấy Principal hiện tại
     */
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

    /**
     * Lấy userId hiện tại
     */
    public UUID getCurrentUserId() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        log.debug("Current userId: {}", principal.getUserId());
        return principal.getUserId();
    }

    /**
     * Lấy username hiện tại
     */
    public String getCurrentUsername() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        return principal.getUsername();
    }

    /**
     * Lấy email hiện tại
     */
    public String getCurrentEmail() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        return principal.getEmail();
    }

    /**
     * Lấy danh sách roles của user hiện tại
     */
    public List<String> getCurrentUserRoles() {
        GatewayUserPrincipal principal = getCurrentUserPrincipal();
        List<String> roles = principal.getRoles();
        log.debug("Current user roles: {}", roles);
        return roles;
    }

    /**
     * Kiểm tra user có role cụ thể không
     */
    public boolean hasRole(String role) {
        try {
            Authentication authentication = getAuthentication();
            String roleWithPrefix = "ROLE_" + role.toUpperCase();

            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));

            log.debug("User has role '{}': {}", role, hasRole);
            return hasRole;
        } catch (CustomException e) {
            return false;
        }
    }

    /**
     * Kiểm tra user có bất kỳ role nào trong danh sách không
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra user có tất cả các roles không
     */
    public boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Kiểm tra user hiện tại có phải admin không
     */
    public boolean isCurrentUserAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Validate user access: chỉ admin hoặc chính user đó mới được truy cập
     */
    public void validateUserAccess(UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();

        log.debug("Validating access: currentUserId={}, targetUserId={}",
                currentUserId, targetUserId);

        // Admin có quyền truy cập tất cả
        if (isCurrentUserAdmin()) {
            log.info("Admin access granted for user: {} by admin: {}",
                    targetUserId, currentUserId);
            return;
        }

        // User chỉ được truy cập thông tin của chính mình
        if (currentUserId.equals(targetUserId)) {
            log.info("Self access granted for user: {}", targetUserId);
            return;
        }

        log.warn("Access denied: User {} attempted to access user {}",
                currentUserId, targetUserId);
        throw new CustomException(ErrorCode.FORBIDDEN);
    }

    /**
     * Yêu cầu user phải có role cụ thể
     */
    public void requireRole(String role) {
        if (!hasRole(role)) {
            log.warn("Access denied: User does not have required role: {}", role);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * Yêu cầu user phải có ít nhất một trong các roles
     */
    public void requireAnyRole(String... roles) {
        if (!hasAnyRole(roles)) {
            log.warn("Access denied: User does not have any of required roles: {}",
                    String.join(", ", roles));
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * Yêu cầu user phải có tất cả các roles
     */
    public void requireAllRoles(String... roles) {
        if (!hasAllRoles(roles)) {
            log.warn("Access denied: User does not have all required roles: {}",
                    String.join(", ", roles));
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
