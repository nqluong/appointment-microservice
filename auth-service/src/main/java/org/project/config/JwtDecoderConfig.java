package org.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.repository.UserRepository;
import org.project.security.jwt.validator.TokenValidator;
import org.project.service.TokenStatusChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;

//Cau hinh jwt Decoder
@Configuration
@Slf4j
@RequiredArgsConstructor
public class JwtDecoderConfig {
    @Value("${jwt.signer-key}")
    String signerKey;

    private final TokenValidator tokenValidator;
    private final TokenStatusChecker tokenStatusChecker;
    private final UserRepository userRepository;

    @Bean
    public JwtDecoder baseJwtDecoder() {

        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return new BlacklistAwareJwtDecoder(baseJwtDecoder(), tokenValidator, tokenStatusChecker,userRepository);
    }
}
