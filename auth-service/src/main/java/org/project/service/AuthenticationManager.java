package org.project.service;

import org.project.dto.request.LoginRequest;
import org.project.dto.response.LoginResponse;

public interface AuthenticationManager {
    LoginResponse authenticate(LoginRequest loginRequest);
}
