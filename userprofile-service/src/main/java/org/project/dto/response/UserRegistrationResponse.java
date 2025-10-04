package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationResponse {
    UUID userId;
    String username;
    String email;
    String firstName;
    String lastName;
    String userType;
    boolean isActive;
    boolean isEmailVerified;
    boolean isDoctorApproved;
    LocalDateTime createdAt;
}
