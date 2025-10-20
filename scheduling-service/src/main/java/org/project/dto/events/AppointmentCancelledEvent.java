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
    // Appointment info
    UUID appointmentId;
    UUID slotId;
    
    // Patient info
    UUID patientUserId;
    String patientName;
    String patientEmail;
    String patientPhone;
    
    // Doctor info
    UUID doctorUserId;
    String doctorName;
    String doctorEmail;
    String specialtyName;
    
    // Appointment details
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    
    // Cancellation info
    String reason;
    String transactionId;
    
    // Metadata
    String sagaId;
    LocalDateTime timestamp;
}
