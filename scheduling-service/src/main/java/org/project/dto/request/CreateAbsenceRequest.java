package org.project.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAbsenceRequest {
    @NotNull(message = "Doctor user ID is required")
    UUID doctorUserId;

    @NotNull(message = "Absence date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate absenceDate;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    String reason;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    String notes;
}
