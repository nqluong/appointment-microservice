package org.project.security.jwt.generator;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessTokenGenerator {

    @Value("${jwt.signer-key}")
    String jwtSecret;

    public String generate(UUID userId, String username, String email,List<String> roles, Long expiration) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWTClaimsSet claimsSet = buildAccessTokenClaims(userId, username, email, roles, expiration);

            return createSignedToken(header, claimsSet);
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating access token", e);
        }
    }

    private JWTClaimsSet buildAccessTokenClaims(UUID userId, String username, String email, List<String> roles, Long expiration) {
        return new JWTClaimsSet.Builder()
                .subject(username)
                .issuer("appointment")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(expiration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .build();
    }

    private String createSignedToken(JWSHeader header, JWTClaimsSet claimsSet) throws JOSEException {
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
        return jwsObject.serialize();
    }
}
