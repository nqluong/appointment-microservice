package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "doctor_user_id", nullable = false)
    UUID doctorUserId;

    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    @Column(name = "day_of_week", nullable = false)
    Integer dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    @Column(name = "slot_duration")
    Integer slotDuration = 30;

    @Column(name = "break_duration")
    Integer breakDuration = 5;

    @Column(name = "max_appointments_per_day")
    Integer maxAppointmentsPerDay;

    @Column(name = "max_appointments_per_slot")
    Integer maxAppointmentsPerSlot = 1;

    @Column(name = "is_active", nullable = false)
    boolean isActive = true;

    @Column(name = "notes")
    String notes;

    @NotNull(message = "Timezone is required")
    @Column(name = "timezone")
    String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
