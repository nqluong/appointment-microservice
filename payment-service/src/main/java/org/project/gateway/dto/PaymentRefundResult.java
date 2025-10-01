package org.project.gateway.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRefundResult {
    boolean success;
    String refundTransactionId;
    String gatewayRefundId;
    BigDecimal refundAmount;
    PaymentStatus status;
    String responseCode;
    String message;
    LocalDateTime refundDate;
    String rawResponse;
    String errorCode;
}
