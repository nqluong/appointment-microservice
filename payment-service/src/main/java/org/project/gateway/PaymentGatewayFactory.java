package org.project.gateway;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentGatewayFactory {

    List<PaymentGateway> paymentGateways;

    public PaymentGateway getGateway(PaymentMethod paymentMethod) {
        return paymentGateways.stream()
                .filter(gateway -> gateway.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_PROCESSING_FAILED));
    }
}
