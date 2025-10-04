package org.project.service;



import org.project.dto.request.ScheduleEntryRequest;

import java.util.List;

public interface ScheduleValidationService {

    void validateScheduleEntries(List<ScheduleEntryRequest> scheduleEntries);

    void validateScheduleEntry(ScheduleEntryRequest entry);
}
