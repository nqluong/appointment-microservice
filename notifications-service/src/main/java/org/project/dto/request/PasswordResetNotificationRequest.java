package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetNotificationRequest {
    String email;
    String userName;
    String resetToken;
    String resetUrl;
    int expiryTime;
}
