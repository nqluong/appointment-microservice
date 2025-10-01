package org.project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleEntryRequest {
    @Min(value = 1, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    @Max(value = 7, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    @NotNull(message = "Day of week is required")
    Integer dayOfWeek;

    @NotNull(message = "Start time is required")
    LocalTime startTime;

    @NotNull(message = "End time is required")
    LocalTime endTime;

    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    Integer slotDuration;

    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 60, message = "Break duration cannot exceed 1 hour")
    Integer breakDuration;

    @Min(value = 1, message = "Max appointments per slot must be at least 1")
    Integer maxAppointmentsPerSlot;

    @Min(value = 1, message = "Max appointments per day must be at least 1")
    Integer maxAppointmentsPerDay;

    Boolean isActive;
}
