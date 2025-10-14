package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_available_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorAvailableSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "doctor_user_id", nullable = false)
    UUID doctorId;

    @NotNull(message = "Slot date is required")
    @Column(name = "slot_date", nullable = false)
    LocalDate slotDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @Column(name = "is_available", nullable = false)
    boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    Long version;

    @Column(name = "reserved_by")
    UUID reservedBy;

    @Column(name = "reserved_at")
    LocalDateTime reservedAt;

}
