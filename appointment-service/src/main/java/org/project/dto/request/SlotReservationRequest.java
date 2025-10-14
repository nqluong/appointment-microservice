package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlotReservationRequest {
    UUID slotId;
    UUID doctorId;
    UUID patientId;
    String idempotencyKey;
}
