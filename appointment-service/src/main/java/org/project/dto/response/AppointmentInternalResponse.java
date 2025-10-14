package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentInternalResponse {
    UUID appointmentId;
    UUID doctorId;
    UUID patientId;
    LocalDate appointmentDate;
    UUID slotId;
    BigDecimal consultationFee;
    String reason;
    String notes;
    String doctorNotes;
    Status status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
