package org.project.service;

import org.project.enums.TokenType;
import org.project.model.User;

import java.time.LocalDateTime;

public interface TokenInvalidator {
    void invalidateToken(String tokenHash, LocalDateTime expiration, User user, TokenType type);
}
