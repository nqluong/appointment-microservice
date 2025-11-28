package org.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox_events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    String eventId;

    @Column(name = "aggregate_type", nullable = false)
    String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    // JSONB payload
    @Column(columnDefinition = "jsonb", nullable = false)
    String payload;

    @Column(nullable = false)
    Boolean processed = false;

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "retry_count")
    Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    @Version
    Long version;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
}
