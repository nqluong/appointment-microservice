package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyTokenResponse {
    boolean valid;
    String tokenType;
    String userId;
    String username;
    String email;
    List<String> roles;
    LocalDateTime expirationTime;
    LocalDateTime verificationTime;
    String message;

    public static VerifyTokenResponse valid(TokenInfo tokenInfo) {
        return VerifyTokenResponse.builder()
                .valid(true)
                .tokenType(tokenInfo.getTokenType())
                .userId(tokenInfo.getUserId().toString())
                .username(tokenInfo.getUsername())
                .email(tokenInfo.getEmail())
                .roles(tokenInfo.getRoles())
                .expirationTime(tokenInfo.getExpirationTime())
                .verificationTime(LocalDateTime.now())
                .message("Token is valid")
                .build();
    }

    public static VerifyTokenResponse invalid(String message) {
        return VerifyTokenResponse.builder()
                .valid(false)
                .verificationTime(LocalDateTime.now())
                .message(message)
                .build();
    }
}
