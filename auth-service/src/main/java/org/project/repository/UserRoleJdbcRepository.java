package org.project.repository;


import org.project.dto.response.RoleInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserRoleJdbcRepository {

    List<String> getUserRoleNames(UUID userId);

    void assignRoleToUser(UUID userId, UUID roleId, UUID assignedBy, LocalDateTime expiresAt);

    void assignRoleToUserOnRegistration(UUID userId, UUID roleId);

    boolean hasInactiveRole(UUID userId, UUID roleId);

    void reactivateUserRole(UUID userId, UUID roleId, UUID assignedBy, LocalDateTime expiresAt);

    boolean hasActiveRole(UUID userId, UUID roleId);

    List<RoleInfo> getAvailableRoles();

    void deactivateUserRole(UUID userId, UUID roleId);

    void deactivateAllUserRoles(UUID userId);

    void updateRoleExpiration(UUID userId, UUID roleId, LocalDateTime newExpiresAt);
}
