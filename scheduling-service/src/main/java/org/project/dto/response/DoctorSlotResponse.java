package org.project.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSlotResponse {
    UUID slotId;
    UUID doctorId;
    LocalDate slotDate;
    LocalTime startTime;
    LocalTime endTime;
    boolean isAvailable;
}
