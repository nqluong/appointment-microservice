package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenInfo {
    UUID userId;
    String username;
    String email;
    List<String> roles;
    String tokenType;
    LocalDateTime expirationTime;
    boolean isExpired;
    boolean isInvalidated;
}
