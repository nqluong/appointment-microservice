package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.RefreshTokenRequest;
import org.project.dto.response.TokenResponse;
import org.project.dto.response.TokenValidationResult;
import org.project.enums.TokenType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.security.jwt.service.TokenService;
import org.project.service.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandardTokenManager implements TokenManager {
    TokenValidator tokenValidator;
    TokenInvalidator tokenInvalidator;
    TokenService tokenService;
    UserAuthenticationService userAuthenticationService;
    RoleManagementService roleManagementService;

    @Override
    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        try {
            TokenValidationResult validationResult = validateRefreshToken(request.getRefreshToken());
            invalidateOldRefreshToken(validationResult);

            return generateNewTokens(validationResult);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Token refresh failed", e);
        }
    }

    private TokenValidationResult validateRefreshToken(String refreshToken) {
        return tokenValidator.validateToken(refreshToken, TokenType.REFRESH_TOKEN);
    }

    private void invalidateOldRefreshToken(TokenValidationResult validationResult) {
        tokenInvalidator.invalidateToken(
                validationResult.getTokenHash(),
                validationResult.getExpirationTime(),
                validationResult.getUser(),
                TokenType.REFRESH_TOKEN
        );
    }

    private TokenResponse generateNewTokens(TokenValidationResult validationResult) {
        List<String> roles = roleManagementService.getUserRoles(validationResult.getUserId());
        return tokenService.generateTokens(
                validationResult.getUserId(),
                validationResult.getUser().getUsername(),
                validationResult.getUser().getEmail(),
                roles
        );
    }
}
