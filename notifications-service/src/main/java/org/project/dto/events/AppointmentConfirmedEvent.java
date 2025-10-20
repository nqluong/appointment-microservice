package org.project.dto.events;

import java.math.BigDecimal;
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
public class AppointmentConfirmedEvent {
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
    BigDecimal consultationFee;
    String notes;

    UUID paymentId;
    BigDecimal paymentAmount;
    String paymentType;
    String transactionId;
    LocalDateTime paymentDate;

    String sagaId;
    LocalDateTime timestamp;
}

