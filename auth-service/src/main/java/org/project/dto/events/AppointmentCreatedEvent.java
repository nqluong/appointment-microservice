package org.project.dto.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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

