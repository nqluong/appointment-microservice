package org.project.security.jwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.TokenResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.security.jwt.generator.AccessTokenGenerator;
import org.project.security.jwt.generator.PasswordResetTokenGenerator;
import org.project.security.jwt.generator.RefreshTokenGenerator;
import org.project.security.jwt.validator.TokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    @Value("${jwt.signer-key}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

     private final AccessTokenGenerator accessTokenGenerator;
     private final RefreshTokenGenerator refreshTokenGenerator;
     private final PasswordResetTokenGenerator passwordResetTokenGenerator;
     private final TokenValidator tokenValidator;

    @Override
    public TokenResponse generateTokens(UUID userId, String username, String email, List<String> roles) {
        try {
            String accessToken = accessTokenGenerator.generate(userId, username, email,roles, accessTokenExpiration);
            String refreshToken = refreshTokenGenerator.generate(userId, refreshTokenExpiration);

            return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenValidator.validate(token);
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        return tokenValidator.getUserId(token);
    }

    @Override
    public LocalDateTime getExpirationTimeFromToken(String token) {
        return tokenValidator.getExpiretionTime(token);
    }

    @Override
    public String getTokenType(String token) {
        return tokenValidator.getTokenType(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return tokenValidator.isExpired(token);
    }

    @Override
    public String hashToken(String token) {
        return tokenValidator.hash(token);
    }

    @Override
    public String generatePasswordResetToken(UUID userId, String email, Long expirationMinutes) {
        try {
            return passwordResetTokenGenerator.generate(userId, email, expirationMinutes);
        } catch (Exception e) {
            log.error("Error generating password reset token for user: {}", userId, e);
            throw new CustomException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        try {
            return tokenValidator.validatePasswordResetToken(token);
        } catch (Exception e) {
            log.error("Error validating password reset token", e);
            return false;
        }
    }

    @Override
    public UUID getUserIdFromPasswordResetToken(String token) {
        return tokenValidator.getUserIdFromPasswordResetToken(token);
    }

    @Override
    public String getEmailFromPasswordResetToken(String token) {
        return tokenValidator.getEmailFromPasswordResetToken(token);
    }
}
