package org.project.service;

import org.project.dto.response.UserIdsResponse;
import org.project.model.User;

import java.util.UUID;

public interface UserService {
    UserIdsResponse getUserIdsByRole(String roleName);

    User findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
