package org.project.dto.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

