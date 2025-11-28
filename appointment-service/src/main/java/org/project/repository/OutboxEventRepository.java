package org.project.repository;

import org.project.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByProcessedFalseOrderByCreatedAtAsc();

    boolean existsByEventId(String eventId);

    Optional<OutboxEvent> findByEventId(String eventId);

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.processed = true AND e.processedAt < :cutoffDate")
    int deleteOldProcessedEvents(LocalDateTime cutoffDate);

    List<OutboxEvent> findByProcessedFalseAndRetryCountGreaterThan(int retryThreshold);
}
