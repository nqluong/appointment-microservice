package org.project.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCompletedEvent {
    UUID paymentId;
    UUID appointmentId;
    BigDecimal amount;
    String paymentType;
    String paymentMethod;
    String transactionId;
    String gatewayTransactionId;
    LocalDateTime paymentDate;
    LocalDateTime timestamp;
}

