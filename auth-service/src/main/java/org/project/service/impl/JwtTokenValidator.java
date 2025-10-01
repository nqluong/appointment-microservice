package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.TokenValidationResult;
import org.project.enums.TokenType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.User;
import org.project.repository.InvalidatedTokenRepository;
import org.project.repository.UserRepository;
import org.project.security.jwt.service.TokenService;
import org.project.service.TokenValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenValidator implements TokenValidator {
    TokenService tokenService;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserRepository userRepository;

    @Override
    public TokenValidationResult validateToken(String token, TokenType type) {
        validateTokenFormat(token);
        validateTokenExpiration(token);
        validateTokenType(token, type);

        String tokenHash = tokenService.hashToken(token);
        validateTokenNotInvalidated(tokenHash, type);

        UUID userId = tokenService.getUserIdFromToken(token);
        User user = getUserFromToken(userId);
        LocalDateTime expirationTime = tokenService.getExpirationTimeFromToken(token);

        return TokenValidationResult.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .user(user)
                .expirationTime(expirationTime)
                .tokenType(type)
                .build();
    }

    private void validateTokenFormat(String token) {
        if (!tokenService.validateToken(token)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "Invalid token format");
        }
    }

    private void validateTokenExpiration(String token) {
        if (tokenService.isTokenExpired(token)) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED, "Token has expired");
        }
    }

    private void validateTokenType(String token, TokenType expectedTokenType) {
        String tokenType = tokenService.getTokenType(token);
        String expectedType = expectedTokenType == TokenType.REFRESH_TOKEN ? "REFRESH" : "ACCESS";

        if (!expectedType.equalsIgnoreCase(tokenType)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID,
                    String.format("Expected %s token but got %s", expectedType.toLowerCase(), tokenType));
        }
    }

    private void validateTokenNotInvalidated(String tokenHash, TokenType tokenType) {
        if (invalidatedTokenRepository.existsByTokenHash(tokenHash)) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED,
                    String.format("%s token has been invalidated", tokenType.name()));
        }
    }

    private User getUserFromToken(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,
                        "User not found for the provided token"));
    }
}
