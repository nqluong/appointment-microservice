package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.AssignRoleRequest;
import org.project.dto.request.UpdateRoleExpirationRequest;
import org.project.dto.response.RoleInfo;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.repository.UserRepository;
import org.project.repository.UserRoleJdbcRepository;
import org.project.service.RoleManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoleManagementServiceImpl implements RoleManagementService {

    UserRoleJdbcRepository userRoleRepositoryJdbc;
    UserRepository userRepository;

    @Override
    public List<String> getUserRoles(UUID userId) {
        return userRoleRepositoryJdbc.getUserRoleNames(userId);
    }

    @Override
    public void assignRoleToUser(AssignRoleRequest request, UUID assignedBy) {
        if(!userRepository.existsById(request.getUserId())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if(userRoleRepositoryJdbc.hasActiveRole(request.getUserId(), request.getRoleId())) {
            throw new CustomException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }

        if(request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_EXPIRATION_DATE);
        }
        try {
            if(userRoleRepositoryJdbc.hasInactiveRole(request.getUserId(), request.getRoleId())) {
                //Mở lại nếu có role đã bị deactivate
                userRoleRepositoryJdbc.reactivateUserRole(
                        request.getUserId(),
                        request.getRoleId(),
                        assignedBy,
                        request.getExpiresAt()
                );
            } else {
                // Nếu chưa có, tạo mới
                userRoleRepositoryJdbc.assignRoleToUser(
                        request.getUserId(),
                        request.getRoleId(),
                        assignedBy,
                        request.getExpiresAt()
                );
            }
        }catch (Exception e) {
            throw new CustomException(ErrorCode.ROLE_ASSIGNMENT_FAILED);
        }
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(UUID userId, UUID roleId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!userRoleRepositoryJdbc.hasActiveRole(userId, roleId)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        try {
            userRoleRepositoryJdbc.deactivateUserRole(userId, roleId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ROLE_REVOCATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void revokeAllUserRoles(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        try {
            userRoleRepositoryJdbc.deactivateAllUserRoles(userId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ROLE_REVOCATION_FAILED);
        }
    }

    @Override
    public List<RoleInfo> getAvailableRoles() {
        return userRoleRepositoryJdbc.getAvailableRoles();
    }

    @Override
    @Transactional
    public void updateRoleExpiration(UpdateRoleExpirationRequest request) {
        if (!userRepository.existsById(request.getUserId())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!userRoleRepositoryJdbc.hasActiveRole(request.getUserId(), request.getRoleId())) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        if (request.getNewExpiresAt() != null && request.getNewExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_EXPIRATION_DATE);
        }

        try {
            userRoleRepositoryJdbc.updateRoleExpiration(
                    request.getUserId(),
                    request.getRoleId(),
                    request.getNewExpiresAt()
            );

        } catch (Exception e) {
            throw new CustomException(ErrorCode.ROLE_UPDATE_FAILED);
        }
    }

    @Override
    public boolean userHasRole(UUID userId, String roleName) {
        List<String> userRoles = getUserRoles(userId);
        return userRoles.contains(roleName);
    }
}
