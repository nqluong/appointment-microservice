package org.project.repository;

import java.time.LocalDate;
import java.util.UUID;

public interface SlotGenerationRepository {

    void generateSlotsForRange(UUID doctorId, LocalDate startDate, LocalDate endDate);

    long countAvailableSlots(UUID doctorId, LocalDate startDate, LocalDate endDate);
}
