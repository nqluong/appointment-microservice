package org.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.*;
import org.project.dto.response.*;
import org.project.service.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;
    private final SessionManager sessionManager;
    private final TokenVerificationService tokenVerificationService;
    private final PasswordResetService passwordResetService;
    private final UserAuthenticationService userAuthenticationService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        return authenticationManager.authenticate(loginRequest);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        return tokenManager.refreshAccessToken(request);
    }

    @Override
    public void logout(LogoutRequest request) {
        sessionManager.terminateSession(request);
    }

    @Override
    public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
        return tokenVerificationService.verifyToken(request);
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        return passwordResetService.forgotPassword(request);
    }

    @Override
    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        return passwordResetService.passwordReset(request);
    }

    @Override
    public void register(RegisterRequest request) {
        userAuthenticationService.registerUser(request);
    }

    @Override
    public boolean validateResetToken(String token) {
        return passwordResetService.validateResetToken(token);
    }
}
