package org.project.service;

import org.project.dto.request.*;
import org.project.dto.response.*;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    VerifyTokenResponse verifyToken(VerifyTokenRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    PasswordResetResponse resetPassword(PasswordResetRequest request);

    boolean validateResetToken(String token);
}
