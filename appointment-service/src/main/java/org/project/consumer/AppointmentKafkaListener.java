package org.project.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.events.*;
import org.project.repository.OutboxEventRepository;
import org.project.service.AppointmentSagaEventHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentKafkaListener {
    AppointmentSagaEventHandler eventHandler;
    KafkaErrorHandler errorHandler;
    OutboxEventRepository outboxEventRepository;

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentCompleted}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    public void onPaymentCompleted(PaymentCompletedEvent event, Acknowledgment ack) {

        if (outboxEventRepository.existsByEventId(event.getEventId())) {
            log.info("Event already processed via outbox, skipping: eventId={}", event.getEventId());
            ack.acknowledge();
            return;
        }

        processEvent(event,
                () -> eventHandler.handlePaymentCompleted(event),
                "PaymentCompleted",
                ack);
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentFailed}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    public void onPaymentFailed(PaymentFailedEvent event, Acknowledgment ack) {
        processEvent(event,
                () -> eventHandler.handlePaymentFailed(event),
                "PaymentFailed",
                ack);
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.validationFailed}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    public void onValidationFailed(ValidationFailedEvent event, Acknowledgment ack) {
        processEvent(event,
                () -> eventHandler.handleValidationFailed(event),
                "ValidationFailed",
                ack);
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.patientValidated}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    public void onPatientValidated(PatientValidatedEvent event, Acknowledgment ack) {
        processEvent(event,
                () -> eventHandler.handlePatientValidated(event),
                "PatientValidated",
                ack);
    }

    @KafkaListener(
            topics = "payment.refund.processed",
            groupId = "appointment-service",
            concurrency = "3"
    )
    public void onRefundProcessed(PaymentRefundProcessedEvent event, Acknowledgment ack) {
        processEvent(event,
                () -> eventHandler.handleRefundProcessed(event),
                "RefundProcessed",
                ack);
    }

    private void processEvent(Object event, Runnable handler, String eventType, Acknowledgment ack) {
        try {
            log.info("Received {}: {}", eventType, event);
            handler.run();
            ack.acknowledge();
            log.debug("Successfully processed {}", eventType);
        } catch (Exception e) {
            log.error("Error processing {}: {}", eventType, event, e);
            errorHandler.handleError(event, e, eventType);
            ack.acknowledge();
        }
    }

    private String generateEventId(Object event) {
        return UUID.nameUUIDFromBytes(event.toString().getBytes()).toString();
    }
}
