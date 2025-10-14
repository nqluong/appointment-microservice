package org.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "slot_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlotReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "slot_id", nullable = false)
    UUID slotId;

    @Column(name = "patient_id", nullable = false)
    UUID patientId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    String idempotencyKey;

    @Column(name = "reserved_at", nullable = false)
    LocalDateTime reservedAt;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    boolean active = true;

    @Column(name = "confirmed", nullable = false)
    boolean confirmed = false;

    @Column(name = "confirmed_at")
    LocalDateTime confirmedAt;

    @Column(name = "cancellation_reason", length = 500)
    String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
