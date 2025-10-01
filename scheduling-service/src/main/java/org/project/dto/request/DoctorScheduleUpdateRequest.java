package org.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorScheduleUpdateRequest {
    @Valid
    @NotEmpty(message = "At least one schedule entry is required")
    List<ScheduleEntryRequest> scheduleEntries;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes;

    String timezone;
}
