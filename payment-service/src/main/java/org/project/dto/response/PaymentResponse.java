package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentStatus;
import org.project.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    UUID id;
    UUID appointmentId;
    BigDecimal amount;
    PaymentType paymentType;
    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    String transactionId;
    String gatewayTransactionId;
    String notes;
    LocalDateTime paymentDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
