package org.project.security.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.security.annotation.RequireOwnershipOrAdmin;
import org.project.utils.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAspect {
    private final SecurityUtils securityUtils;

    @Before("@annotation(requireOwnershipOrAdmin)")
    public void validateOwnershipOrAdmin(JoinPoint joinPoint, RequireOwnershipOrAdmin requireOwnershipOrAdmin) {
        Object[] args = joinPoint.getArgs();
        String userIdParamName = requireOwnershipOrAdmin.userIdParam();
        String[] allowedRoles = requireOwnershipOrAdmin.allowedRoles();

        UUID currentUserId = securityUtils.getCurrentUserId();

        // Nếu là admin, cho phép truy cập tất cả
        if (securityUtils.isCurrentUserAdmin()) {
            log.info("Admin access granted for user: {}", currentUserId);
            return;
        }

        /// Tìm UUID từ parameters dựa trên tên parameter
        UUID targetUserId = extractUserIdFromParameters(joinPoint, userIdParamName);
        if (targetUserId == null) {
            log.warn("Non-admin user {} attempted to access without specifying userId", currentUserId);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // Kiểm tra nếu là đúng của user truyền vào
        if (!currentUserId.equals(targetUserId)) {
            log.warn("User {} attempted to access profile of user {}", currentUserId, targetUserId);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // Kiểm tra nếu có role phù hợp
        boolean hasAllowedRole = Arrays.stream(allowedRoles)
                .anyMatch(securityUtils::hasRole);

        if (!hasAllowedRole) {
            log.warn("User {} does not have required role for this operation", currentUserId);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        log.info("Access granted for user: {} to modify their own profile", currentUserId);
    }

    private UUID extractUserIdFromParameters(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // Check @RequestParam annotation
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String paramName = requestParam.value().isEmpty() ?
                        requestParam.name().isEmpty() ?
                                parameter.getName() : requestParam.name()
                        : requestParam.value();

                if (parameterName.equals(paramName) && args[i] instanceof UUID) {
                    return (UUID) args[i];
                }
            }

            // Check @PathVariable annotation
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String paramName = pathVariable.value().isEmpty() ?
                        pathVariable.name().isEmpty() ?
                                parameter.getName() : pathVariable.name()
                        : pathVariable.value();

                if (parameterName.equals(paramName) && args[i] instanceof UUID) {
                    return (UUID) args[i];
                }
            }

            // Fallback: check by parameter name
            if (parameterName.equals(parameter.getName()) && args[i] instanceof UUID) {
                return (UUID) args[i];
            }
        }

        return null;
    }
}
