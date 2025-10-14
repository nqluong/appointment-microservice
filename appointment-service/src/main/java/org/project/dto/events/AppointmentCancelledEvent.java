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
public class AppointmentCancelledEvent {
    String sagaId;
    UUID appointmentId;
    UUID slotId;
    String reason;
    LocalDateTime timestamp;
}
