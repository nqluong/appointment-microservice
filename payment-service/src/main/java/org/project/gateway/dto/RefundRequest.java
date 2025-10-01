package org.project.gateway.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundRequest {
    String originalTransactionId;
    String refundTransactionId;
    String gatewayTransactionId;
    BigDecimal refundAmount;
    BigDecimal originalAmount;
    String reason;
    String transactionDate;
    String customerIp;
    String orderInfo;
}
