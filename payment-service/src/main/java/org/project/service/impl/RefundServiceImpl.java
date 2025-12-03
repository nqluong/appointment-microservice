package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.PaymentRefundRequest;
import org.project.dto.response.PaymentRefundResponse;
import org.project.enums.RefundType;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.PaymentRefundProcessedEvent;
import org.project.publisher.PaymentEventPublisher;
import org.project.service.PaymentService;
import org.project.service.RefundService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundServiceImpl implements RefundService {
    PaymentService paymentService;
    PaymentEventPublisher eventPublisher;

    @Override
    public void processRefundForCancellation(AppointmentCancellationInitiatedEvent event) {
        log.info("Processing refund for cancelled appointment: {}", event.getAppointmentId());

        try {
            PaymentRefundRequest refundRequest = buildRefundRequest(event);

            PaymentRefundResponse refundResponse = paymentService.refundPayment(refundRequest);

            publishSuccessEvent(event, refundResponse);

            log.info("Successfully processed refund for appointment: {}, amount: {}",
                    event.getAppointmentId(), refundResponse.getRefundAmount());

        } catch (Exception e) {
            log.error("Failed to process refund for appointment: {}",
                    event.getAppointmentId(), e);

            publishFailureEvent(event, e.getMessage());
        }
    }

    private PaymentRefundRequest buildRefundRequest(AppointmentCancellationInitiatedEvent event) {
        return PaymentRefundRequest.builder()
                .appointmentId(event.getAppointmentId())
                .refundType(RefundType.POLICY_BASED)
                .appointmentDate(event.getAppointmentDate())
                .reason(event.getReason())
                .build();
    }

    private void publishSuccessEvent(AppointmentCancellationInitiatedEvent cancellationEvent,
                                     PaymentRefundResponse refundResponse) {
        PaymentRefundProcessedEvent event = PaymentRefundProcessedEvent.builder()
                .appointmentId(cancellationEvent.getAppointmentId())
                .paymentId(refundResponse.getPaymentId())
                .success(true)
                .refundAmount(refundResponse.getRefundAmount())
                .refundType(RefundType.POLICY_BASED.name())
                .reason(cancellationEvent.getReason())
                .processedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishRefundProcessedSuccess(event);
    }


    private void publishFailureEvent(AppointmentCancellationInitiatedEvent cancellationEvent,
                                     String errorMessage) {
        PaymentRefundProcessedEvent event = PaymentRefundProcessedEvent.builder()
                .appointmentId(cancellationEvent.getAppointmentId())
                .success(false)
                .refundAmount(BigDecimal.ZERO)
                .refundType(RefundType.POLICY_BASED.name())
                .reason(cancellationEvent.getReason())
                .errorMessage(errorMessage)
                .processedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishRefundProcessedFailure(event);
    }
}
