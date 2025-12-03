package org.project.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.config.PaymentKafkaTopics;
import org.project.events.PaymentCompletedEvent;
import org.project.events.PaymentFailedEvent;
import org.project.events.PaymentRefundProcessedEvent;
import org.project.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentKafkaTopics topics;

    // Publish sự kiện thanh toán hoàn thành
    public void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .paymentDate(payment.getPaymentDate())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(topics.getPaymentCompleted(),
                payment.getAppointmentId().toString(), event);

        log.info("Published PaymentCompletedEvent for appointment: {}",
                payment.getAppointmentId());
    }

    // Publish sự kiện thanh toán thất bại
    public void publishPaymentFailed(Payment payment, String reason, boolean confirmedFailure) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .transactionId(payment.getTransactionId())
                .reason(reason)
                .failedService("payment-service")
                .timestamp(LocalDateTime.now())
                .confirmedFailure(confirmedFailure)
                .build();

        kafkaTemplate.send(topics.getPaymentFailed(),
                payment.getAppointmentId().toString(), event);

        log.info("Published PaymentFailedEvent for appointment: {}, confirmedFailure: {}",
                payment.getAppointmentId(), confirmedFailure);
    }

    // Publish sự kiện hoàn tiền thành công
    public void publishRefundProcessedSuccess(PaymentRefundProcessedEvent event) {
        kafkaTemplate.send(topics.getPaymentRefundProcessed(),
                event.getAppointmentId().toString(), event);

        log.info("Published successful PaymentRefundProcessedEvent for appointment: {}",
                event.getAppointmentId());
    }

    // Publish sự kiện hoàn tiền thất bại
    public void publishRefundProcessedFailure(PaymentRefundProcessedEvent event) {
        kafkaTemplate.send(topics.getPaymentRefundProcessed(),
                event.getAppointmentId().toString(), event);

        log.error("Published failed PaymentRefundProcessedEvent for appointment: {}",
                event.getAppointmentId());
    }
}
