package org.project.consumer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.project.dto.request.PaymentRefundRequest;
import org.project.dto.response.PaymentRefundResponse;
import org.project.enums.RefundType;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.PaymentRefundProcessedEvent;
import org.project.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentCancellationConsumer {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "appointment-cancellation-initiated", groupId = "payment-service", concurrency = "3")
    public void handleAppointmentCancellation(AppointmentCancellationInitiatedEvent event) {
        log.info("Received appointment cancellation event: {}", event);

        try {
            // Tạo refund request với policy-based refund
            PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                    .appointmentId(event.getAppointmentId())
                    .refundType(RefundType.POLICY_BASED)
                    .appointmentDate(event.getAppointmentDate())
                    .reason(event.getReason())
                    .build();

            // Thực hiện refund
            PaymentRefundResponse refundResponse = paymentService.refundPayment(refundRequest);

            // Gửi event thành công
            PaymentRefundProcessedEvent refundEvent = PaymentRefundProcessedEvent.builder()
                    .appointmentId(event.getAppointmentId())
                    .paymentId(refundResponse.getPaymentId())
                    .userId(event.getUserId())
                    .success(true)
                    .refundAmount(refundResponse.getRefundAmount())
                    .refundType("POLICY_BASED") // Default refund type
                    .reason(event.getReason())
                    .processedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("payment.refund.processed", refundEvent);
            log.info("Successfully processed refund for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process refund for appointment: {}", event.getAppointmentId(), e);

            // Gửi event thất bại
            PaymentRefundProcessedEvent refundEvent = PaymentRefundProcessedEvent.builder()
                    .appointmentId(event.getAppointmentId())
                    .userId(event.getUserId())
                    .success(false)
                    .refundAmount(BigDecimal.ZERO)
                    .reason(event.getReason())
                    .processedAt(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();

            kafkaTemplate.send("payment.refund.processed", refundEvent);
        }
    }
}
