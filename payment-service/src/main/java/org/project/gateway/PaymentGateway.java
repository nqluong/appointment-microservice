package org.project.gateway;

import org.project.dto.request.PaymentCallbackRequest;
import org.project.enums.PaymentMethod;
import org.project.gateway.dto.*;
import org.project.model.Payment;

public interface PaymentGateway {

    PaymentGatewayResponse createPaymentUrl(Payment payment, PaymentGatewayRequest request);

    PaymentVerificationResult verifyPayment(PaymentCallbackRequest callbackRequest);

    PaymentQueryResult queryPaymentStatus(String transactionId, String transactionDate);

    PaymentRefundResult refundPayment(RefundRequest refundRequest);

    boolean supports(PaymentMethod paymentMethod);
}
