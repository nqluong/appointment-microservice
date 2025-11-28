package org.project.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentCancelledEvent {
    UUID eventId;
    UUID sagaId;
    
    UUID appointmentId;
    UUID slotId;
    UUID patientUserId;
    UUID doctorUserId;
    
    String reason;
    String cancelledBy;  // USER, DOCTOR, SYSTEM
    
    LocalDateTime cancelledAt;
}
