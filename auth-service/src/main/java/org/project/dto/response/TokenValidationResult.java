package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.TokenType;
import org.project.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenValidationResult {
    String tokenHash;
    UUID userId;
    User user;
    LocalDateTime expirationTime;
    TokenType tokenType;

}
