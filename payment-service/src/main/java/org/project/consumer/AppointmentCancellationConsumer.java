package org.project.consumer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.project.dto.request.PaymentRefundRequest;
import org.project.dto.response.PaymentRefundResponse;
import org.project.enums.RefundType;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.PaymentRefundProcessedEvent;
import org.project.service.PaymentService;
import org.project.service.RefundService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentCancellationConsumer {

    private final RefundService refundService;

    @KafkaListener(topics = "appointment-cancellation-initiated", groupId = "payment-service", concurrency = "3")
    public void handleAppointmentCancellation(AppointmentCancellationInitiatedEvent event) {
        log.info("Received appointment cancellation event: {}", event);

            refundService.processRefundForCancellation(event);
        }
}

