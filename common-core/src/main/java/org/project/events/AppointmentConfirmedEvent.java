package org.project.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

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
    String doctorPhone;
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
