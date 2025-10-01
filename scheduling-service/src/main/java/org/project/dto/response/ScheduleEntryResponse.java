package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleEntryResponse {
    UUID id;
    Integer dayOfWeek;
    String dayName;
    LocalTime startTime;
    LocalTime endTime;
    Integer slotDuration;
    Integer breakDuration;
    Integer maxAppointmentsPerSlot;
    Integer maxAppointmentsPerDay;
    Boolean isActive;
}
