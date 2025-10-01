package org.project.security.jwt.service;


import org.project.dto.response.TokenResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TokenService {
    TokenResponse generateTokens(UUID userId, String username, String email , List<String> roles);

    boolean validateToken(String token);

    UUID getUserIdFromToken(String token);
    String getTokenType(String token);
    LocalDateTime getExpirationTimeFromToken(String token);

    boolean isTokenExpired(String token);
    String hashToken(String token);

    String generatePasswordResetToken(UUID userId, String email, Long expirationMinutes);
    boolean validatePasswordResetToken(String token);
    UUID getUserIdFromPasswordResetToken(String token);
    String getEmailFromPasswordResetToken(String token);
}
