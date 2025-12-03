package org.project.service;



import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.request.PaymentCallbackRequest;
import org.project.dto.request.PaymentRefundRequest;
import org.project.dto.response.PaymentRefundResponse;
import org.project.dto.response.PaymentResponse;
import org.project.dto.response.PaymentUrlResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentUrlResponse createPayment(CreatePaymentRequest request, String customerIp);

    PaymentResponse processPaymentCallback(PaymentCallbackRequest callbackRequest);

    PaymentResponse getPaymentById(UUID paymentId);

    PaymentResponse cancelPayment(UUID paymentId);

    void handlePaymentSuccess(UUID paymentId);

    void handlePaymentFailure(UUID paymentId);

    PaymentResponse queryPaymentStatus(UUID paymentId);

    PaymentResponse queryPaymentStatus(String transactionId);

    void processProcessingPayments();

    PaymentRefundResponse refundPayment(PaymentRefundRequest request);

    PaymentResponse confirmPaymentProcessing(UUID paymentId);

    PaymentResponse confirmPaymentProcessingByTransactionId(String transactionId);

}
