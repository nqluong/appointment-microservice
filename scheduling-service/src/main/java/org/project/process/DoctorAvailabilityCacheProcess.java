package org.project.process;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.RedisCacheService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
public class DoctorAvailabilityCacheProcess implements Runnable {

    private final RedisCacheService redisCacheService;
    private final DoctorAvailableSlotRepository slotRepository;
    
    // Redis queue key where userprofile-service pushes doctor IDs
    private static final String QUEUE_KEY = "doctor_availability_cache_queue";
    private static final String CACHE_PREFIX = "doctor:availability:";
    private static final int CACHE_TTL_DAYS = 1; // Cache for 1 day
    private static final int DAYS_TO_CACHE = 14; // Cache next 14 days of availability
    private static final long POLL_TIMEOUT_MS = 2000; // Wait 2 seconds for queue items
    private static final long ERROR_RETRY_DELAY_MS = 1000;
    
    private volatile boolean running = true;

    public DoctorAvailabilityCacheProcess(RedisCacheService redisCacheService,
                                          DoctorAvailableSlotRepository slotRepository) {
        this.redisCacheService = redisCacheService;
        this.slotRepository = slotRepository;
    }

    @Override
    public void run() {
        log.info("Doctor availability cache worker started - Thread: {}", Thread.currentThread().getName());
        
        try {
            while (running) {
                try {
                    Object doctorIdObj = redisCacheService.rightPop(QUEUE_KEY, POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                    if (doctorIdObj != null) {
                        String doctorIdStr = doctorIdObj.toString();

                        long startTime = System.currentTimeMillis();
                        processDoctorAvailability(doctorIdStr);
                        long duration = System.currentTimeMillis() - startTime;
                        
                        log.info("Cached availability for doctorId: {} in {}ms", doctorIdStr, duration);
                    }

                } catch (Exception e) {
                    log.error("Error processing availability: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(ERROR_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Fatal error in cache worker: {}", e.getMessage(), e);
        }

        log.info("Doctor availability cache worker stopped - Thread: {}", Thread.currentThread().getName());
    }


    private void processDoctorAvailability(String doctorIdStr) {
        try {
            UUID doctorId = UUID.fromString(doctorIdStr);

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(DAYS_TO_CACHE);

            List<DoctorAvailableSlot> slots = slotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, startDate, endDate);

            if (slots.isEmpty()) {
                log.debug("No availability slots found for doctorId: {}", doctorId);
                return;
            }

            log.debug("Found {} slots for doctorId: {}", slots.size(), doctorId);

            // Group slots by date for efficient caching
            Map<LocalDate, List<DoctorAvailableSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(DoctorAvailableSlot::getSlotDate));

            // Cache each day's slots separately
            int cachedDays = 0;
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                List<DoctorAvailableSlot> dailySlots = slotsByDate.getOrDefault(
                        currentDate, Collections.emptyList());

                cacheDailySlots(doctorId, currentDate, dailySlots);
                
                if (!dailySlots.isEmpty()) {
                    cachedDays++;
                }

                currentDate = currentDate.plusDays(1);
            }

            log.debug("Cached availability for {} days for doctorId: {}", cachedDays, doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor ID format: {}", doctorIdStr);
        } catch (Exception e) {
            log.error("Error processing doctor availability for doctorId: {}", doctorIdStr, e);
        }
    }


    private void cacheDailySlots(UUID doctorId, LocalDate date, List<DoctorAvailableSlot> dailySlots) {
        try {
            List<TimeSlot> slots = dailySlots.stream()
                    .map(this::convertToTimeSlot)
                    .collect(Collectors.toList());

            DoctorAvailabilityCacheData cacheData = DoctorAvailabilityCacheData.builder()
                    .doctorId(doctorId)
                    .date(date.toString())
                    .slots(slots)
                    .totalSlots(slots.size())
                    .build();

            // Cache key format: doctor:availability:{doctorId}:{date}
            String cacheKey = CACHE_PREFIX + doctorId + ":" + date;
            redisCacheService.set(cacheKey, cacheData, CACHE_TTL_DAYS, TimeUnit.DAYS);
            
            log.trace("Cached {} slots for doctorId: {} on date: {}", slots.size(), doctorId, date);

        } catch (Exception e) {
            log.error("Error caching daily slots for doctorId: {} on date: {}", doctorId, date, e);
        }
    }


    private TimeSlot convertToTimeSlot(DoctorAvailableSlot slot) {
        return TimeSlot.builder()
                .slotId(slot.getId())
                .startTime(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .isAvailable(slot.isAvailable())
                .build();
    }


    public void stop() {
        log.info("Stopping cache worker - Thread: {}", Thread.currentThread().getName());
        running = false;
    }
}
