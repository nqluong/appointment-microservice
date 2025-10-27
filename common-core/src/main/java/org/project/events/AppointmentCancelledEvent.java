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
    UUID appointmentId;
    UUID slotId;
    LocalDateTime cancelledAt;
    String cancelledBy; // USER, DOCTOR, ADMIN
    LocalDate appointmentDate;

    UUID patientUserId;
    String patientName;
    String patientEmail;
    String patientPhone;

    UUID doctorUserId;
    String doctorName;
    String doctorEmail;
    String specialtyName;

    LocalTime startTime;
    LocalTime endTime;

    String reason;
    String transactionId;
    LocalDateTime timestamp;
}
