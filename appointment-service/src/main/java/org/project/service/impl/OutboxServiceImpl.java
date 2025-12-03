package org.project.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.config.AppointmentKafkaTopics;
import org.project.dto.response.SlotDetailsResponse;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCreatedEvent;
import org.project.model.Appointment;
import org.project.model.OutboxEvent;
import org.project.repository.OutboxEventRepository;
import org.project.service.OutboxService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OutboxServiceImpl implements OutboxService {
    OutboxEventRepository outboxEventRepository;
    ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void saveEvent(String eventId, String aggregateType, UUID aggregateId,
                          String eventType, Object eventPayload) {

        if (outboxEventRepository.existsByEventId(eventId)) {
            log.debug("Sự kiện đã tồn tại trong outbox, bỏ qua: eventId={}", eventId);
            return;
        }

        try {
            // Chuyển đổi Object thành JsonNode để lưu vào JSONB
            JsonNode jsonNode = objectMapper.valueToTree(eventPayload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(eventId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(jsonNode)
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outboxEvent);

            log.info("Đã lưu vào outbox: eventId={}, loại={}, aggregate={}",
                    eventId, eventType, aggregateId);

        } catch (Exception e) {
            log.error("Lỗi khi lưu vào outbox: eventId={}, loại={}",
                    eventId, eventType, e);
            throw new RuntimeException("Không thể lưu sự kiện vào outbox", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markAsProcessed(String outboxEventId) {
        outboxEventRepository.findByEventId(outboxEventId)
                .ifPresent(event -> {
                    event.setProcessed(true);
                    event.setProcessedAt(LocalDateTime.now());
                    outboxEventRepository.save(event);
                    log.debug("Đã đánh dấu đã xử lý: outboxId={}, eventId={}",
                            outboxEventId, event.getEventId());
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markAsFailed(String outboxEventId, String errorMessage) {
        outboxEventRepository.findByEventId(outboxEventId)
                .ifPresent(event -> {
                    event.setRetryCount(event.getRetryCount() + 1);
                    event.setErrorMessage(errorMessage);
                    outboxEventRepository.save(event);
                    log.warn("Đã đánh dấu thất bại: outboxId={}, eventId={}, số lần thử={}",
                            outboxEventId, event.getEventId(), event.getRetryCount());
                });
    }

    @Override
    public OutboxEvent getEvent(String outboxEventId) {
        return outboxEventRepository.findByEventId(outboxEventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện outbox: " + outboxEventId));
    }
}
