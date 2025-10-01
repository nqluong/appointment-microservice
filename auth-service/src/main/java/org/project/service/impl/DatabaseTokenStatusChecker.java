package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.service.TokenStatusChecker;
import org.project.repository.InvalidatedTokenRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseTokenStatusChecker implements TokenStatusChecker {
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public boolean isTokenInvalidated(String tokenHash) {
        return invalidatedTokenRepository.isTokenInvalid(tokenHash, LocalDateTime.now());
    }

    @Override
    public boolean isTokenBlacklisted(String tokenHash) {
        return invalidatedTokenRepository.existsByTokenHash(tokenHash);
    }
}
