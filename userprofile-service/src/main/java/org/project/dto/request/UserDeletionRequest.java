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
public class UserDeletionRequest {
    @NotNull(message = "User ID cannot be null")
    UUID userId;

    @NotNull(message = "Deleted by user ID cannot be null")
    UUID deletedBy;

    String reason;
    boolean hardDelete = false;
}
