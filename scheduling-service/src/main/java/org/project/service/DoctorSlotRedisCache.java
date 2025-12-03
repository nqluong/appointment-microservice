package org.project.service;

import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DoctorSlotRedisCache {
    void cacheDoctorAvailability(UUID doctorId, LocalDate date, List<TimeSlot> slots);

    DoctorAvailabilityCacheData getDoctorAvailability(UUID doctorId, LocalDate date);

    void updateSlotAvailability(UUID doctorId, LocalDate slotDate, UUID slotId, boolean isAvailable);

    void evictDoctorAvailabilityCache(UUID doctorId, LocalDate slotDate);

    boolean isCacheExists(UUID doctorId, LocalDate date);
}
