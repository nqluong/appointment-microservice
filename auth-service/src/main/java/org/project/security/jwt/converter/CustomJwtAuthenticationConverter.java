package org.project.security.jwt.converter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.security.jwt.principal.JwtUserPrincipal;
import org.project.security.jwt.service.JwtClaimsExtractor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

//Chuyen doi jwt thanh authentication
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    JwtClaimsExtractor jwtClaimsExtractor;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            JwtUserPrincipal principal = jwtClaimsExtractor.extractPrincipal(jwt);
            Collection<GrantedAuthority> authorities = jwtClaimsExtractor.extractAuthorities(jwt);

            return new CustomJwtAuthenticationToken(jwt, authorities, principal);
        } catch (Exception e) {
            log.error("Error converting JWT to Authentication: {}", e.getMessage(), e);
            return new JwtAuthenticationToken(jwt);
        }
    }

}
