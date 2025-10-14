package org.project.service;

import org.project.dto.response.UserBasicInfoResponse;
import org.project.dto.response.UserIdsResponse;
import org.project.dto.response.UserValidationResponse;
import org.project.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserIdsResponse getUserIdsByRole(String roleName);

    User findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);


    boolean hasRole(UUID userId, String roleName);

    boolean hasAllRoles(UUID userId, List<String> roleNames);

    boolean hasAnyRole(UUID userId, List<String> roleNames);

    @Transactional(readOnly = true)
    UserValidationResponse validateUserWithRole(UUID userId, String requiredRole);

    @Transactional(readOnly = true)
    UserBasicInfoResponse getUserBasicInfo(UUID userId);

    boolean isActive(UUID userId);
}
