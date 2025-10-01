package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRefundResponse {
    UUID paymentId;
    String refundTransactionId;
    String gatewayRefundId;
    BigDecimal refundAmount;
    BigDecimal totalRefundedAmount;
    BigDecimal remainingAmount;
    PaymentStatus paymentStatus;
    String message;
    LocalDateTime refundDate;
    boolean success;
    BigDecimal refundPercentage;
}
