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
    @NotNull(message = "Doctor ID không được bỏ trống")
    UUID doctorId;

    @NotNull(message = "Slot ID không được bỏ trống")
    UUID slotId;

    @NotNull(message = "Patient ID không được bỏ trống")
    UUID patientId;

    @Size(max = 500, message = "Notes không được vượt quá 500 ký tự")
    String notes;
}
