package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorValidationResponse {
    boolean valid;
    boolean approved;
    boolean hasLicense;
    String message;
    BigDecimal consultationFee;
}
