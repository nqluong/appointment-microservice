package org.project.service.impl;

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
            log.debug("Event already in outbox, skipping: eventId={}", eventId);
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(eventId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outboxEvent);

            log.info("Saved to outbox: eventId={}, type={}, aggregate={}",
                    eventId, eventType, aggregateId);

        } catch (Exception e) {
            log.error("Failed to save to outbox: eventId={}, type={}",
                    eventId, eventType, e);
            throw new RuntimeException("Failed to save event to outbox", e);
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
                    log.debug("Marked as processed: outboxId={}, eventId={}",
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
                    log.warn("Marked as failed: outboxId={}, eventId={}, retries={}",
                            outboxEventId, event.getEventId(), event.getRetryCount());
                });
    }

    @Override
    public OutboxEvent getEvent(String outboxEventId) {
        return outboxEventRepository.findByEventId(outboxEventId)
                .orElseThrow(() -> new RuntimeException("Outbox event not found: " + outboxEventId));
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
