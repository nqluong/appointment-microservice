package org.project.service;


import org.project.dto.request.RegisterRequest;
import org.project.model.User;

public interface UserAuthenticationService {
    User authenticateUser(String username, String password);

    void registerUser(RegisterRequest request);
}
