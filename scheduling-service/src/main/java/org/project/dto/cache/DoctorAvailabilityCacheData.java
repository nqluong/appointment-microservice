package org.project.dto.cache;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityCacheData {
    private UUID doctorId;
    private LocalDate date; // LocalDate as String (yyyy-MM-dd)
    private List<TimeSlot> slots;
    private int totalSlots;
    private long cachedAt; // Timestamp when cached
}

