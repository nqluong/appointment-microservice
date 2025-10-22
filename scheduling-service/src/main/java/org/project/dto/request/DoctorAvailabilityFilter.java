package org.project.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
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
public class DoctorAvailabilityFilter {

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private List<UUID> doctorIds;
    private List<UUID> specialtyIds;
    private String doctorName;

    private Boolean isAvailable;
    private Boolean hasAvailableSlots;

    private Integer maxResults;
    private Boolean useCache;
    private Boolean useParallelProcessing;

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
