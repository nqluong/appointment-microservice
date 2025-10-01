package org.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDepositPaymentRequest {
    @NotBlank(message = "Return URL is required")
    String returnUrl;

    @NotBlank(message = "Cancel URL is required")
    String cancelUrl;
}
