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
public class AppointmentCancelledEvent {
    UUID appointmentId;
    UUID slotId;

    UUID patientUserId;
    String patientName;
    String patientEmail;
    String patientPhone;

    UUID doctorUserId;
    String doctorName;
    String doctorEmail;
    String specialtyName;

    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;

    String reason;
    String transactionId;

    String sagaId;
    LocalDateTime timestamp;
}

