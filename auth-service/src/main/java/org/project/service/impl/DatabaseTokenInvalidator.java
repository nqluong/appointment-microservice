package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.service.TokenInvalidator;
import org.project.enums.TokenType;
import org.project.model.InvalidatedToken;
import org.project.model.User;
import org.project.repository.InvalidatedTokenRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseTokenInvalidator implements TokenInvalidator {
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public void invalidateToken(String tokenHash, LocalDateTime expiration, User user, TokenType type) {
        InvalidatedToken invalidatedToken = createInvalidatedToken(tokenHash, expiration, user, type);
        invalidatedTokenRepository.save(invalidatedToken);

        log.info("Successfully invalidated {} token for user: {}", type.name(), user.getId());
    }

    private InvalidatedToken createInvalidatedToken(String tokenHash, LocalDateTime expirationTime,
                                                    User user, TokenType tokenType) {
        return InvalidatedToken.builder()
                .tokenHash(tokenHash)
                .tokenType(tokenType)
                .blackListedAt(LocalDateTime.now())
                .expiresAt(expirationTime)
                .user(user)
                .build();
    }
}
