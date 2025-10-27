package org.project.events;

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
public class SlotReservedEvent {
    String sagaId;
    UUID slotId;
    UUID appointmentId;
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    UUID reservedBy;
    UUID doctorUserId;
    LocalDateTime timestamp;
}
