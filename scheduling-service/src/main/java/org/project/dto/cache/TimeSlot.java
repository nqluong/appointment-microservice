package org.project.dto.cache;

import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    private UUID slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;
}

