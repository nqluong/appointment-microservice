package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserValidationResponse {
    boolean valid;
    boolean active;
    boolean hasRole;
    String message;
}
