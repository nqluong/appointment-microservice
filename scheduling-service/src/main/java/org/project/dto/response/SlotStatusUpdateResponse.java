package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotStatusUpdateResponse {

    UUID slotId;
    UUID doctorId;
    LocalDate slotDate;
    LocalTime startTime;
    LocalTime endTime;
    Boolean isAvailable;
    String message;
    LocalDateTime updatedAt;

}
