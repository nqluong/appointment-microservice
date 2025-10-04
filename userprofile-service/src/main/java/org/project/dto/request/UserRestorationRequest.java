package org.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRestorationRequest {
    @NotNull(message = "User ID cannot be null")
    UUID userId;

    @NotNull(message = "Restored by user ID cannot be null")
    UUID restoredBy;

    String reason;
}
