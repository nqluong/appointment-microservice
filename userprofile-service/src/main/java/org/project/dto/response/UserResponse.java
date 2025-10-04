package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    UUID userId;
    String username;
    String email;
    String firstName;
    String lastName;
    List<String> roles;
    boolean isActive;
    boolean isEmailVerified;
    LocalDateTime createdAt;
    LocalDateTime deletedAt;
    UUID deletedBy;
}
