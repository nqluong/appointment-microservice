package org.project.security.jwt.converter;

import org.project.security.jwt.principal.JwtUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {
    private final JwtUserPrincipal userPrincipal;

    public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, JwtUserPrincipal userPrincipal) {
        super(jwt, authorities, userPrincipal.getUsername());
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Object getPrincipal() {
        return userPrincipal;
    }

    public JwtUserPrincipal getUserPrincipal() {
        return userPrincipal;
    }
}
