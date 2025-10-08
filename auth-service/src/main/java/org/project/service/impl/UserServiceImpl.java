package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.project.dto.response.UserIdsResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.User;
import org.project.repository.UserRepository;
import org.project.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
}
