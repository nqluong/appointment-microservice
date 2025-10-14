package org.project.dto.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientValidatedEvent {
    String sagaId;
    UUID appointmentId;
    UUID patientUserId;

    String patientName;
    String patientEmail;
    String patientPhone;
    UUID doctorUserId;
    LocalDateTime timestamp;
}
