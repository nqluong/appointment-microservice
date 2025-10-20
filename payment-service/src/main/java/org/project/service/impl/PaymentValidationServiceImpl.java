package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.CreatePaymentRequest;
import org.project.enums.PaymentStatus;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Payment;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentValidationService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentValidationServiceImpl implements PaymentValidationService {
    PaymentRepository paymentRepository;
//    AppointmentRepository appointmentRepository;

    @Override
    public void validateCreatePaymentRequest(CreatePaymentRequest request) {

        boolean hasPendingPayment = paymentRepository.existsByAppointmentIdAndPaymentStatusIn(
                request.getAppointmentId(),
                Arrays.asList(PaymentStatus.PENDING, PaymentStatus.PROCESSING)
        );

        if (hasPendingPayment) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED,
                    "There is already a pending payment for this appointment");
        }
    }

    @Override
    public void validatePaymentCancellation(Payment payment) {
        List<PaymentStatus> cancellableStatuses = Arrays.asList(
                PaymentStatus.PENDING,
                PaymentStatus.PROCESSING
        );

        if (!cancellableStatuses.contains(payment.getPaymentStatus())) {
            throw new CustomException(ErrorCode.PAYMENT_INVALID_STATUS,
                    "Cannot cancel payment with status: " + payment.getPaymentStatus());
        }
    }

    @Override
    public void validatePaymentStatusTransition(Payment payment, PaymentStatus newStatus) {
        PaymentStatus currentStatus = payment.getPaymentStatus();

        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> Arrays.asList(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED, PaymentStatus.FAILED)
                    .contains(newStatus);
            case PROCESSING -> Arrays.asList(PaymentStatus.COMPLETED, PaymentStatus.FAILED, PaymentStatus.CANCELLED)
                    .contains(newStatus);
            case COMPLETED -> PaymentStatus.REFUNDED.equals(newStatus);
            case FAILED, CANCELLED, REFUNDED -> false;
        };

        if (!isValidTransition) {
            throw new CustomException(ErrorCode.PAYMENT_INVALID_STATUS,
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }
}
