package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.LoginRequest;
import org.project.dto.request.RegisterRequest;
import org.project.dto.response.LoginResponse;
import org.project.dto.response.TokenResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.User;
import org.project.security.jwt.service.TokenService;
import org.project.service.AuthenticationManager;
import org.project.service.RoleManagementService;
import org.project.service.UserAuthenticationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandardAuthenticationManager implements AuthenticationManager {

    UserAuthenticationService userAuthenticationService;
    TokenService tokenService;
    RoleManagementService roleManagementService;

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            User user = authenticateUser(loginRequest);
            List<String> roles = getUserRoles(user);
            TokenResponse tokenResponse = generateTokens(user, roles);

            return buildLoginResponse(user, roles, tokenResponse);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Authentication failed", e);
        }
    }

    private User authenticateUser(LoginRequest loginRequest) {
        return userAuthenticationService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
    }

    private List<String> getUserRoles(User user) {
        return roleManagementService.getUserRoles(user.getId());
    }

    private TokenResponse generateTokens(User user, List<String> roles) {
        return tokenService.generateTokens(user.getId(), user.getUsername(), user.getEmail(),roles);
    }

    private LoginResponse buildLoginResponse(User user, List<String> roles,TokenResponse tokenResponse) {
        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .email(user.getEmail())
                .userId(user.getId())
                .userRoles(roles)
                .build();
    }
}
