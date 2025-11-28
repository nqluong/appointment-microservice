package org.project.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.events.PaymentCompletedEvent;
import org.project.events.PaymentFailedEvent;
import org.project.events.PaymentRefundProcessedEvent;
import org.project.service.AppointmentSagaEventHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentRetryListener {
    private final AppointmentSagaEventHandler eventHandler;
    private final KafkaErrorHandler errorHandler;
    private static final int MAX_RETRY_COUNT = 3;

    @KafkaListener(
            topics = "appointment-retry",
            groupId = "appointment-retry-group",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void handleRetry(Object event, Acknowledgment ack) {
        log.info("Processing retry event: {}", event);

        try {
            // Add delay before retry
            Thread.sleep(5000); // 5 second delay

            // Determine event type and route to handler
            if (event instanceof PaymentCompletedEvent) {
                eventHandler.handlePaymentCompleted((PaymentCompletedEvent) event);
            } else if (event instanceof PaymentFailedEvent) {
                eventHandler.handlePaymentFailed((PaymentFailedEvent) event);
            } else if (event instanceof PaymentRefundProcessedEvent) {
                eventHandler.handleRefundProcessed((PaymentRefundProcessedEvent) event);
            }

            ack.acknowledge();
            log.info("Successfully processed retry event");

        } catch (Exception e) {
            log.error("Retry failed, sending to DLQ", e);
            errorHandler.handleFatalError(event, e, "RETRY_FAILED");
            ack.acknowledge();
        }
    }
}
