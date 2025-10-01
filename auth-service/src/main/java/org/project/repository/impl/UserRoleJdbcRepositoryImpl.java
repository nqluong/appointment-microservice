package org.project.repository.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.project.dto.response.RoleInfo;
import org.project.repository.UserRoleJdbcRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleJdbcRepositoryImpl implements UserRoleJdbcRepository {

    JdbcTemplate jdbcTemplate;

    private static final String GET_USER_ROLE_NAMES_SQL = """
            SELECT r.name 
            FROM user_roles ur 
            JOIN roles r ON ur.role_id = r.id 
            WHERE ur.user_id = ? 
              AND ur.is_active = true 
              AND r.is_active = true
              AND (ur.expires_at IS NULL OR ur.expires_at > CURRENT_TIMESTAMP)
            """;

    private static final String ASSIGN_ROLE_TO_USER_SQL = """
            INSERT INTO user_roles (id, user_id, role_id, assigned_by, assigned_at, is_active, expires_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, true, ?)
            """;

    private static final String ASSIGN_ROLE_ON_REGISTRATION_SQL = """
            INSERT INTO user_roles (id, user_id, role_id, assigned_at, is_active)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, true)
            """;

    private static final String HAS_ACTIVE_ROLE_SQL = """
            SELECT COUNT(*)
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id = ?
                AND ur.role_id = ?
                AND ur.is_active = true
                AND r.is_active = true
                AND (ur.expires_at IS NULL OR ur.expires_at > CURRENT_TIMESTAMP)
            """;

    private static final String HAS_INACTIVE_ROLE_SQL = """
            SELECT COUNT(*)
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id = ?
                AND ur.role_id = ?
                AND ur.is_active = false
                AND r.is_active = true
            """;

    private static final String REACTIVATE_USER_ROLE_SQL = """
            UPDATE user_roles 
            SET is_active = true,
                assigned_by = ?,
                assigned_at = CURRENT_TIMESTAMP,
                expires_at = ?
            WHERE user_id = ? 
                AND role_id = ? 
                AND is_active = false
            """;

    private static final String DEACTIVATE_USER_ROLE_SQL = """
            UPDATE user_roles 
            SET is_active = false 
            WHERE user_id = ? AND role_id = ? AND is_active = true
            """;

    private static final String DEACTIVATE_ALL_USER_ROLES_SQL = """
            UPDATE user_roles 
            SET is_active = false 
            WHERE user_id = ? AND is_active = true
            """;

    private static final String GET_AVAILABLE_ROLES_SQL = """
            SELECT id, name, description 
            FROM roles 
            WHERE is_active = true 
            ORDER BY name
            """;

    private static final String UPDATE_ROLE_EXPIRATION_SQL = """
            UPDATE user_roles 
            SET expires_at = ? 
            WHERE user_id = ? AND role_id = ? AND is_active = true
            """;


    // Lấy danh sách các quyền của user
    @Override
    public List<String> getUserRoleNames(UUID userId) {
        return jdbcTemplate.queryForList(GET_USER_ROLE_NAMES_SQL, String.class, userId);
    }

    //Cấp quyền cho user
    @Override
    public void assignRoleToUser(UUID userId, UUID roleId, UUID assignedBy, LocalDateTime expiresAt) {
        jdbcTemplate.update(ASSIGN_ROLE_TO_USER_SQL, UUID.randomUUID(), userId, roleId, assignedBy, expiresAt);
    }

    //Cấp quyền khi đăng ký
    @Override
    public void assignRoleToUserOnRegistration(UUID userId, UUID roleId) {
        jdbcTemplate.update(ASSIGN_ROLE_ON_REGISTRATION_SQL, UUID.randomUUID(), userId, roleId);
    }

    //Kiểm tra có quyền này không
    @Override
    public boolean hasActiveRole(UUID userId, UUID roleId) {
        Integer count = jdbcTemplate.queryForObject(HAS_ACTIVE_ROLE_SQL, Integer.class, userId, roleId);
        return count != null && count > 0;
    }

    //Kiểm tra có role bị deactivate không
    @Override
    public boolean hasInactiveRole(UUID userId, UUID roleId) {
        Integer count = jdbcTemplate.queryForObject(HAS_INACTIVE_ROLE_SQL, Integer.class, userId, roleId);
        return count != null && count > 0;
    }

    //Reactivate role đã bị deactivate
    @Override
    public void reactivateUserRole(UUID userId, UUID roleId, UUID assignedBy, LocalDateTime expiresAt) {
        jdbcTemplate.update(REACTIVATE_USER_ROLE_SQL, assignedBy, expiresAt, userId, roleId);
    }

    //Vô hiệu hóa quyền của user
    @Override
    public void deactivateUserRole(UUID userId, UUID roleId) {
        jdbcTemplate.update(DEACTIVATE_USER_ROLE_SQL, userId, roleId);
    }

    //Vô hiệu hóa tất cả quyền của user
    @Override
    public void deactivateAllUserRoles(UUID userId) {
        jdbcTemplate.update(DEACTIVATE_ALL_USER_ROLES_SQL, userId);
    }


    //Lấy danh sách các role có thể cấp
    @Override
    public List<RoleInfo> getAvailableRoles() {
        return jdbcTemplate.query(GET_AVAILABLE_ROLES_SQL, (rs, rowNum) -> RoleInfo.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .build());
    }

    //Cập nhật thời hạn quyền
    @Override
    public void updateRoleExpiration(UUID userId, UUID roleId, LocalDateTime newExpiresAt) {
        jdbcTemplate.update(UPDATE_ROLE_EXPIRATION_SQL, newExpiresAt, userId, roleId);
    }
}