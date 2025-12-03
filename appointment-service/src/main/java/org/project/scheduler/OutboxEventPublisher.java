package org.project.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OutboxEventPublisher {
    OutboxEventRepository outboxEventRepository;
    OutboxService outboxService;
    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentKafkaTopics topics;
    ObjectMapper objectMapper;

    static final int MAX_RETRY_COUNT = 5;
    static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByProcessedFalseOrderByCreatedAtAsc();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Đang phát {} sự kiện outbox đang chờ", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                log.error("Sự kiện vượt quá số lần thử tối đa: eventId={}, số lần thử={}",
                        event.getEventId(), event.getRetryCount());
                continue;
            }

            try {
                publishEvent(event);
                outboxService.markAsProcessed(event.getEventId());

            } catch (Exception e) {
                log.error("Lỗi khi phát sự kiện: eventId={}", event.getEventId(), e);
                outboxService.markAsFailed(event.getEventId(), e.getMessage());
            }
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) throws Exception {
        String topic = getTopicForEventType(outboxEvent.getEventType());
        String key = outboxEvent.getAggregateId().toString();

        // Chuyển đổi JsonNode thành Object event
        Object eventPayload = deserializePayload(
                outboxEvent.getPayload(),
                outboxEvent.getEventType()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, eventPayload);

        SendResult<String, Object> result = future.get();
        
        log.info("Đã phát sự kiện: eventId={}, loại={}, partition={}, offset={}",
                outboxEvent.getEventId(),
                outboxEvent.getEventType(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
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

    private Object deserializePayload(JsonNode payload, String eventType) throws Exception {
        Class<?> eventClass = getEventClass(eventType);
        return objectMapper.treeToValue(payload, eventClass);
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
     * Dọn dẹp các sự kiện đã xử lý cũ - chạy hàng ngày lúc 2 giờ sáng
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deleted = outboxEventRepository.deleteOldProcessedEvents(cutoffDate);
        log.info("Đã dọn dẹp {} sự kiện outbox cũ", deleted);
    }
}
