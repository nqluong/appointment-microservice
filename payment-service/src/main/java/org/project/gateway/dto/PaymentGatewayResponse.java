package org.project.gateway.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentGatewayResponse {
    boolean success;
    String paymentUrl;
    String transactionId;
    String message;
    String errorCode;
}
