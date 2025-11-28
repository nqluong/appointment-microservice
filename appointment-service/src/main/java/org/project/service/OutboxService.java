package org.project.service;
import org.project.model.OutboxEvent;

import java.util.UUID;

public interface OutboxService {

    void saveEvent(String eventId, String aggregateType, UUID aggregateId,
                   String eventType, Object eventPayload);

    void markAsProcessed(String outboxEventId);

    void markAsFailed(String outboxEventId, String errorMessage);

    OutboxEvent getEvent(String outboxEventId);
}
