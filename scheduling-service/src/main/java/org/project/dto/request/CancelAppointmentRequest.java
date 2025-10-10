package org.project.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelAppointmentRequest {
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    String reason;
}
