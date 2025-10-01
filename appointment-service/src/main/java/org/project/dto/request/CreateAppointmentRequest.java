package org.project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAppointmentRequest {
    @NotNull(message = "Doctor ID cannot be blank")
    UUID doctorId;

    @NotNull(message = "Slot ID cannot be blank")
    UUID slotId;

    @NotNull(message = "Patient ID cannot be blank")
    UUID patientId;

    @Size(max = 500, message = "Notes must not exceed 500 characters.")
    String notes;
}
