package org.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchSlotStatusRequest {

    @NotNull(message = "Slot ID is required")
    UUID slotId;

    @NotNull(message = "Available status is required")
    Boolean isAvailable;

    String reason;
}
