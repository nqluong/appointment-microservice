package org.project.common.security.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.project.common.security.annotation.RequireOwnershipOrAdmin;
import org.project.common.security.util.SecurityUtils;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
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
            return;
        }

        if (allowedRoles.length > 0) {
            boolean hasAllowedRole = Arrays.stream(allowedRoles)
                    .anyMatch(securityUtils::hasRole);

            if (hasAllowedRole) {
                UUID targetUserId = extractUserIdFromParameters(joinPoint, userIdParamName);

                if (targetUserId == null) {
                    return;
                }
                if (currentUserId.equals(targetUserId)) {
                    return;
                }

                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }

        throw new CustomException(ErrorCode.FORBIDDEN);
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
