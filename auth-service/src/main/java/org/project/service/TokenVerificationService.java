package org.project.service;

import org.project.dto.request.VerifyTokenRequest;
import org.project.dto.response.VerifyTokenResponse;

// Check verify token
public interface TokenVerificationService {
    VerifyTokenResponse verifyToken(VerifyTokenRequest token);
}
