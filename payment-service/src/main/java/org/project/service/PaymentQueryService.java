package org.project.service;

import org.project.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentQueryService {

    PaymentResponse queryPaymentStatus(UUID paymentId);

    PaymentResponse queryPaymentStatus(String transactionId);

    void processProcessingPayments();
}
