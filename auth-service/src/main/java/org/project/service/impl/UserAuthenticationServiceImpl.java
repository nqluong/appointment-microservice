package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.AssignRoleRequest;
import org.project.dto.request.RegisterRequest;
import org.project.events.UserRegisteredEvent;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.User;
import org.project.producer.KafkaProducerService;
import org.project.repository.UserRepository;
import org.project.service.RoleManagementService;
import org.project.service.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final RoleManagementService roleManagementService;

    @Value("${app.default-role}")
    private String defaultRole;

    @Override
    public User authenticateUser(String username, String password) {
        User user = userRepository.findActiveUserByUsernameWithRoles(username)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        return user;
    }


    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isEmailVerified(true)
                .build();

        User savedUser = userRepository.save(newUser);

        try {
            AssignRoleRequest roleRequest = AssignRoleRequest.builder()
                    .userId(savedUser.getId())
                    .roleId(UUID.fromString(defaultRole))
                    .build();
            roleManagementService.assignRoleToUser(roleRequest, savedUser.getId());
            log.info("Assigned default role to user: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to assign default role to user: {}", savedUser.getId(), e);
            // Không throw exception, để user vẫn được tạo
        }

        String[] nameParts = splitFullName(request.getFullName());

        // Gửi event để tạo user profile
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .build();

        kafkaProducerService.sendUserRegisteredEvent(event);

    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        String trimmedName = fullName.trim();
        int lastSpaceIndex = trimmedName.lastIndexOf(' ');

        if (lastSpaceIndex == -1) {
            return new String[]{trimmedName, ""};
        }

        String firstName = trimmedName.substring(0, lastSpaceIndex).trim();
        String lastName = trimmedName.substring(lastSpaceIndex + 1).trim();

        return new String[]{firstName, lastName};
    }
}
