package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlotDetailsResponse {
    UUID slotId;
    UUID doctorId;
    LocalDate slotDate;
    LocalTime startTime;
    LocalTime endTime;
    BigDecimal consultationFee;
    boolean available;
}
