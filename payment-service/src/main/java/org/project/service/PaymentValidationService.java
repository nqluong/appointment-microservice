package org.project.service;


import org.project.dto.request.CreatePaymentRequest;
import org.project.enums.PaymentStatus;
import org.project.model.Payment;

public interface PaymentValidationService {

    void validateCreatePaymentRequest(CreatePaymentRequest request);

    void validatePaymentCancellation(Payment payment);

    void validatePaymentStatusTransition(Payment payment, PaymentStatus newStatus);
}
