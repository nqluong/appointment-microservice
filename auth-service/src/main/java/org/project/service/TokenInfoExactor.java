package org.project.service;

import org.project.dto.response.TokenInfo;

// Lay thong tin tu token
public interface TokenInfoExactor {
    TokenInfo extractTokenInfo(String token);
}
