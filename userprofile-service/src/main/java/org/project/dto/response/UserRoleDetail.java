package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleDetail {
    UUID id;
    UUID userId;
    UUID roleId;
    String roleName;
    LocalDateTime assignedAt;
    UUID assignedBy;
    boolean isActive;
    LocalDateTime expiresAt;
}
