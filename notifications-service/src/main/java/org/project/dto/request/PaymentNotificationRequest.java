package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentNotificationRequest {
    String patientEmail;
    String patientName;
    String doctorName;
    String appointmentId;
    LocalDateTime appointmentDate;
    BigDecimal paymentAmount;
    String transactionId;
    LocalDateTime paymentDate;
    String paymentType;

    // Thêm các trường bổ sung nếu cần
    String doctorSpecialty;
    String clinicAddress;
    String notes;
}
