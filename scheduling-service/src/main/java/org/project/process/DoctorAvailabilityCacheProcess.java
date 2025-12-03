package org.project.process;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
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
    private final DoctorSlotRedisCache doctorSlotRedisCache;
    
    // Redis queue key where userprofile-service pushes doctor IDs
    private static final String QUEUE_KEY = "doctor_availability_cache_queue";
    private static final String CACHE_PREFIX = "doctor:availability:";
    private static final int CACHE_TTL_DAYS = 1; // Cache for 1 day
    private static final int DAYS_TO_CACHE = 14; // Cache next 14 days of availability
    private static final long POLL_TIMEOUT_MS = 2000; // Wait 2 seconds for queue items
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

            log.debug("🔍 Đang tìm slots cho doctor {} từ {} đến {}", doctorId, startDate, endDate);

            List<DoctorAvailableSlot> slots = slotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, startDate, endDate);

            if (slots.isEmpty()) {
                log.debug("⚠️ Không tìm thấy slot nào cho doctorId: {} trong khoảng {} - {}", 
                        doctorId, startDate, endDate);
                return;
            }

            log.debug("📋 Tìm thấy {} slots cho doctorId: {}", slots.size(), doctorId);

            // Group slots by date for efficient caching
            Map<LocalDate, List<DoctorAvailableSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(DoctorAvailableSlot::getSlotDate));

            log.debug("📅 Slots được phân bổ trên {} ngày khác nhau", slotsByDate.size());

            // Cache chỉ những ngày có slots
            int cachedDays = 0;
            for (Map.Entry<LocalDate, List<DoctorAvailableSlot>> entry : slotsByDate.entrySet()) {
                LocalDate slotDate = entry.getKey();
                List<DoctorAvailableSlot> dailySlots = entry.getValue();

                if (!dailySlots.isEmpty()) {
                    log.trace("  → Ngày {}: {} slots", slotDate, dailySlots.size());
                    cacheDailySlots(doctorId, slotDate, dailySlots);
                    cachedDays++;
                }
            }

            log.info("✅ Đã cache {} ngày có slot cho doctorId: {} (tổng {} slots)", 
                    cachedDays, doctorId, slots.size());

        } catch (IllegalArgumentException e) {
            log.error("❌ Định dạng doctor ID không hợp lệ: {}", doctorIdStr);
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý availability cho doctorId: {}", doctorIdStr, e);
        }
    }


    private void cacheDailySlots(UUID doctorId, LocalDate date, List<DoctorAvailableSlot> dailySlots) {
        try {
            log.debug("📝 cacheDailySlots được gọi: doctorId={}, date={}, số slots={}", 
                    doctorId, date, dailySlots.size());

            // Chỉ cache nếu có slot
            if (dailySlots.isEmpty()) {
                log.trace("Bỏ qua cache cho doctorId: {} vào ngày: {} (không có slot)", doctorId, date);
                return;
            }

            List<TimeSlot> slots = dailySlots.stream()
                    .map(this::convertToTimeSlot)
                    .collect(Collectors.toList());

            log.debug("🔄 Đang gọi doctorSlotRedisCache.cacheDoctorAvailability với {} slots", slots.size());
            
            doctorSlotRedisCache.cacheDoctorAvailability(doctorId, date, slots);

            log.debug("✅ Hoàn thành cache {} slots cho doctorId: {} vào ngày: {}", slots.size(), doctorId, date);

        } catch (Exception e) {
            log.error("❌ Lỗi khi cache slots hàng ngày cho doctorId: {} vào ngày: {}", doctorId, date, e);
            e.printStackTrace(); // Print full stack trace
        }
    }


    private TimeSlot convertToTimeSlot(DoctorAvailableSlot slot) {
        return TimeSlot.builder()
                .slotId(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .build();
    }


    public void stop() {
        log.info("Stopping cache worker - Thread: {}", Thread.currentThread().getName());
        running = false;
    }
}
