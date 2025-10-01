package org.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorScheduleCreateRequest {
    @NotNull(message = "Doctor ID is required")
    UUID doctorId;

    @Valid
    @NotEmpty(message = "At least one schedule entry is required")
    List<ScheduleEntryRequest> scheduleEntries;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes;

    @NotBlank(message = "Timezone is required")
    String timezone;
}
