package org.project.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentCreatedEvent {
    String sagaId;
    UUID appointmentId;
    UUID doctorUserId;
    UUID patientUserId;
    UUID slotId;
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    String notes;
    LocalDateTime timestamp;
}
