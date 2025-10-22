package org.project.dto.cache;

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
    private String startTime; 
    private String endTime;  
    private boolean isAvailable;
}

