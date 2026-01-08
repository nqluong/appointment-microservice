package org.project.process;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
import org.project.service.RedisCacheService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DoctorAvailabilityCacheProcess implements Runnable {

    private final RedisCacheService redisCacheService;
    private final DoctorAvailableSlotRepository slotRepository;
    private final DoctorSlotRedisCache doctorSlotRedisCache;
    
    private static final String QUEUE_KEY = "doctor_availability_cache_queue";
    private static final int DAYS_TO_CACHE = 14;
    private static final long POLL_TIMEOUT_MS = 2000;
    private static final long ERROR_RETRY_DELAY_MS = 1000;
    
    private volatile boolean running = true;

    public DoctorAvailabilityCacheProcess(RedisCacheService redisCacheService,
                                          DoctorSlotRedisCache doctorSlotRedisCache,
                                          DoctorAvailableSlotRepository slotRepository) {
        this.redisCacheService = redisCacheService;
        this.doctorSlotRedisCache = doctorSlotRedisCache;
        this.slotRepository = slotRepository;
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    Object doctorIdObj = redisCacheService.rightPop(QUEUE_KEY, POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                    if (doctorIdObj != null) {
                        String doctorIdStr = doctorIdObj.toString();

                        long startTime = System.currentTimeMillis();
                        processDoctorAvailability(doctorIdStr);
                        long duration = System.currentTimeMillis() - startTime;
                        
                        log.info("Đã cache lịch khám cho doctorId: {} trong {}ms", doctorIdStr, duration);
                    }

                } catch (Exception e) {
                    log.error("Lỗi khi xử lý cache lịch khám: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(ERROR_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng trong cache worker: {}", e.getMessage(), e);
        }

        log.info("Cache worker đã dừng - Thread: {}", Thread.currentThread().getName());
    }


    private void processDoctorAvailability(String doctorIdStr) {
        try {
            UUID doctorId = UUID.fromString(doctorIdStr);

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(DAYS_TO_CACHE);

            List<DoctorAvailableSlot> slots = slotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, startDate, endDate);

            if (slots.isEmpty()) {
                return;
            }

            Map<LocalDate, List<DoctorAvailableSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(DoctorAvailableSlot::getSlotDate));


            // Cache chỉ những ngày có slots
            for (Map.Entry<LocalDate, List<DoctorAvailableSlot>> entry : slotsByDate.entrySet()) {
                LocalDate slotDate = entry.getKey();
                List<DoctorAvailableSlot> dailySlots = entry.getValue();

                if (!dailySlots.isEmpty()) {
                    cacheDailySlots(doctorId, slotDate, dailySlots);
                }
            }

        } catch (IllegalArgumentException e) {
            log.error("Định dạng doctor ID không hợp lệ: {}", doctorIdStr);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý availability cho doctorId: {}", doctorIdStr, e);
        }
    }


    private void cacheDailySlots(UUID doctorId, LocalDate date, List<DoctorAvailableSlot> dailySlots) {
        try {
            // Chỉ cache nếu có slot
            if (dailySlots.isEmpty()) {
                return;
            }

            List<TimeSlot> slots = dailySlots.stream()
                    .map(this::convertToTimeSlot)
                    .collect(Collectors.toList());

            doctorSlotRedisCache.cacheDoctorAvailability(doctorId, date, slots);

        } catch (Exception e) {
            e.printStackTrace(); // Print full stack trace
        }
    }


    private TimeSlot convertToTimeSlot(DoctorAvailableSlot slot) {
        return TimeSlot.builder()
                .slotId(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .lastUpdate(slot.getUpdatedAt())
                .build();
    }


    public void stop() {
        running = false;
    }
}
