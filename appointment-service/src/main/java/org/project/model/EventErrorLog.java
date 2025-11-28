package org.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_error_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String eventType;

    @Column(columnDefinition = "TEXT")
    String eventPayload;

    @Column(columnDefinition = "TEXT")
    String errorMessage;

    @Column(columnDefinition = "TEXT")
    String stackTrace;

    String category;

    Boolean resolved = false;

    LocalDateTime createdAt;

    LocalDateTime resolvedAt;
}
