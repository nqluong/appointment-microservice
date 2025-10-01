package org.project.service;

import org.project.dto.response.TokenValidationResult;
import org.project.enums.TokenType;

public interface TokenValidator {
    TokenValidationResult validateToken(String token, TokenType type);
}
