package org.project.service;

import org.project.dto.request.RefreshTokenRequest;
import org.project.dto.response.TokenResponse;

public interface TokenManager {
    TokenResponse refreshAccessToken(RefreshTokenRequest request);
}
