package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.LogoutRequest;
import org.project.dto.response.TokenValidationResult;
import org.project.enums.TokenType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.SessionManager;
import org.project.service.TokenInvalidator;
import org.project.service.TokenValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandardSessionManager implements SessionManager {
    TokenValidator tokenValidator;
    TokenInvalidator tokenInvalidator;

    @Override
    public void terminateSession(LogoutRequest request) {
        try {
            invalidateRefreshTokenIfPresent(request.getRefreshToken());
            invalidateAccessTokenIfPresent(request.getAccessToken());

            log.info("Session terminated successfully");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Session termination failed", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Logout failed");
        }
    }

    private void invalidateRefreshTokenIfPresent(String refreshToken) {
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            invalidateToken(refreshToken, TokenType.REFRESH_TOKEN);
        }
    }

    private void invalidateAccessTokenIfPresent(String accessToken) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            invalidateToken(accessToken, TokenType.ACCESS_TOKEN);
        }
    }

    private void invalidateToken(String token, TokenType tokenType) {
        TokenValidationResult validationResult = tokenValidator.validateToken(token, tokenType);
        tokenInvalidator.invalidateToken(
                validationResult.getTokenHash(),
                validationResult.getExpirationTime(),
                validationResult.getUser(),
                tokenType
        );
    }
}
