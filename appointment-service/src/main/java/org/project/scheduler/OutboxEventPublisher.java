package org.project.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.config.AppointmentKafkaTopics;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCancelledEvent;
import org.project.events.AppointmentConfirmedEvent;
import org.project.events.AppointmentCreatedEvent;
import org.project.model.OutboxEvent;
import org.project.repository.OutboxEventRepository;
import org.project.service.OutboxService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppointmentKafkaTopics topics;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 5;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByProcessedFalseOrderByCreatedAtAsc();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                log.error("Event exceeded max retries: eventId={}, retries={}",
                        event.getEventId(), event.getRetryCount());
                continue;
            }

            try {
                publishEvent(event);
                outboxService.markAsProcessed(event.getId().toString());

            } catch (Exception e) {
                log.error("Failed to publish event: eventId={}", event.getEventId(), e);
                outboxService.markAsFailed(event.getId().toString(), e.getMessage());
            }
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) throws Exception {
        String topic = getTopicForEventType(outboxEvent.getEventType());
        String key = outboxEvent.getAggregateId().toString();

        Object eventPayload = deserializePayload(
                outboxEvent.getPayload(),
                outboxEvent.getEventType()
        );

        kafkaTemplate.send(topic, key, eventPayload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed: eventId={}", outboxEvent.getEventId(), ex);
                        throw new RuntimeException("Kafka send failed", ex);
                    } else {
                        log.info("Published event: eventId={}, type={}, partition={}, offset={}",
                                outboxEvent.getEventId(),
                                outboxEvent.getEventType(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    private String getTopicForEventType(String eventType) {
        return switch (eventType) {
            case "APPOINTMENT_CREATED" -> topics.getAppointmentCreated();
            case "APPOINTMENT_CONFIRMED" -> topics.getAppointmentConfirmed();
            case "APPOINTMENT_CANCELLED" -> topics.getAppointmentCancelled();
            case "APPOINTMENT_CANCELLATION_INITIATED" -> topics.getAppointmentCancellationInitiated();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    private Object deserializePayload(String payload, String eventType) throws Exception {
        Class<?> eventClass = getEventClass(eventType);
        return objectMapper.readValue(payload, eventClass);
    }

    private Class<?> getEventClass(String eventType) {
        return switch (eventType) {
            case "APPOINTMENT_CREATED" -> AppointmentCreatedEvent.class;
            case "APPOINTMENT_CONFIRMED" -> AppointmentConfirmedEvent.class;
            case "APPOINTMENT_CANCELLED" -> AppointmentCancelledEvent.class;
            case "APPOINTMENT_CANCELLATION_INITIATED" -> AppointmentCancellationInitiatedEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    /**
     * Cleanup old processed events - run daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deleted = outboxEventRepository.deleteOldProcessedEvents(cutoffDate);
        log.info("Cleaned up {} old outbox events", deleted);
    }
}
