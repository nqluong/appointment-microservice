package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.dto.response.UserIdsResponse;
import org.project.dto.response.UserValidationResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.User;
import org.project.model.UserRole;
import org.project.repository.UserRepository;
import org.project.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    @Override
    public UserIdsResponse getUserIdsByRole(String roleName) {
        List<UUID> userIds = userRepository.findUserIdsByRoleName(roleName);

        return UserIdsResponse.builder()
                .userIds(userIds)
                .total(userIds.size())
                .build();
    }

    @Override
    public User findByUserId(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public boolean hasRole(UUID userId, String roleName) {
        return userRepository.findById(userId)
                .map(user -> user.getUserRoles().stream()
                        .anyMatch(userRole -> roleName.equalsIgnoreCase(userRole.getRole().getName())))
                .orElse(false);
    }

    @Override
    public boolean hasAllRoles(UUID userId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> {
                    Set<String> userRoles = user.getUserRoles().stream()
                            .map(userRole -> userRole.getRole().getName().toUpperCase())
                            .collect(Collectors.toSet());

                    return roleNames.stream()
                            .map(String::toUpperCase)
                            .allMatch(userRoles::contains);
                })
                .orElse(false);
    }

    @Override
    public boolean hasAnyRole(UUID userId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> {
                    Set<String> userRoles = user.getUserRoles().stream()
                            .map(userRole -> userRole.getRole().getName().toUpperCase())
                            .collect(Collectors.toSet());
                    
                    return roleNames.stream()
                            .map(String::toUpperCase)
                            .anyMatch(userRoles::contains);
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    @Override
    public UserValidationResponse validateUserWithRole(UUID userId, String requiredRole) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return UserValidationResponse.builder()
                    .valid(false)
                    .active(false)
                    .hasRole(false)
                    .message("User not found")
                    .build();
        }

        if (!user.isActive()) {
            return UserValidationResponse.builder()
                    .valid(true)
                    .active(false)
                    .hasRole(false)
                    .message("User is inactive")
                    .build();
        }

        boolean hasRole = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .anyMatch(ur -> requiredRole.equals(ur.getRole().getName()));

        if (!hasRole) {
            return UserValidationResponse.builder()
                    .valid(true)
                    .active(true)
                    .hasRole(false)
                    .message("User does not have required role: " + requiredRole)
                    .build();
        }

        return UserValidationResponse.builder()
                .valid(true)
                .active(true)
                .hasRole(true)
                .message("User validation successful")
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public UserBasicInfoResponse getUserBasicInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserBasicInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getUsername())
                .active(user.isActive())
                .build();
    }

    @Override
    public boolean isActive(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.isActive();
    }
}
