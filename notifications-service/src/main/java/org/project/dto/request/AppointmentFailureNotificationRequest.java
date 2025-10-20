package org.project.dto.request;

import java.time.LocalDateTime;

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
public class AppointmentFailureNotificationRequest {
    String patientEmail;
    String patientName;
    String doctorName;
    String appointmentId;
    LocalDateTime appointmentDate;
    String reason;
    String transactionId;
    LocalDateTime failureTime;
}

