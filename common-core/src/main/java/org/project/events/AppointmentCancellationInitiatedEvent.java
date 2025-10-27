package org.project.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentCancellationInitiatedEvent {
    UUID appointmentId;
    UUID userId;
    UUID doctorId;
    String reason;
    String cancelledBy;
    LocalDate appointmentDate;
    String appointmentTime;
    LocalDateTime initiatedAt;
    String message;
}
