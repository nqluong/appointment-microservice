package org.project.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAbsenceRequest {
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
