package org.project.dto.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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
    UUID reservedBy;
    UUID doctorUserId;
    LocalDateTime timestamp;
}
