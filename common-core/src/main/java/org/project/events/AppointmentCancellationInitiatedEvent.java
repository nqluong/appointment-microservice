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
    // Tracking
    UUID eventId;
    UUID sagaId;
    
    UUID appointmentId;
    UUID patientUserId;
    UUID doctorUserId;
    UUID slotId;
    LocalDate appointmentDate;
    
    String reason;
    String cancelledBy;  // USER, DOCTOR, SYSTEM
    
    LocalDateTime initiatedAt;
}
