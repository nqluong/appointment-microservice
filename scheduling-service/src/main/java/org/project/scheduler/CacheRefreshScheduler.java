package org.project.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshScheduler {

    private final DoctorSlotRedisCache doctorSlotRedisCache;
    private final DoctorAvailableSlotRepository slotRepository;

    private static final int DAYS_TO_CHECK = 14;
    private static final int RECENT_UPDATE_MINUTES = 10;

    @Value("${cache.availability.refresh-rate-ms:300000}")
    private long refreshRateMs;

    @Value("${cache.availability.initial-delay-ms:60000}")
    private long initialDelayMs;

    @Scheduled(fixedRateString = "${cache.availability.refresh-rate-ms:300000}",
               initialDelayString = "${cache.availability.initial-delay-ms:60000}")
    public void checkAndRefreshStaleCache() {
        try {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(DAYS_TO_CHECK);

            // Lấy tất cả slots đã được cập nhật gần đây
            LocalDateTime recentUpdateThreshold = LocalDateTime.now().minusMinutes(RECENT_UPDATE_MINUTES);
            List<DoctorAvailableSlot> recentlyUpdatedSlots = slotRepository
                    .findRecentlyUpdatedSlots(recentUpdateThreshold, startDate, endDate);

            if (recentlyUpdatedSlots.isEmpty()) {
                log.debug("Không có slot nào được cập nhật trong {} phút qua", RECENT_UPDATE_MINUTES);
                return;
            }

            log.info("Phát hiện {} slot được cập nhật gần đây, bắt đầu refresh cache",
                    recentlyUpdatedSlots.size());

            // Nhóm theo doctorId và date để xử lý
            Map<String, List<DoctorAvailableSlot>> groupedSlots = recentlyUpdatedSlots.stream()
                    .collect(Collectors.groupingBy(slot -> 
                        slot.getDoctorId() + ":" + slot.getSlotDate()));

            int refreshedCount = 0;
            int skippedCount = 0;

            for (Map.Entry<String, List<DoctorAvailableSlot>> entry : groupedSlots.entrySet()) {
                String[] keys = entry.getKey().split(":");
                UUID doctorId = UUID.fromString(keys[0]);
                LocalDate date = LocalDate.parse(keys[1]);
                List<DoctorAvailableSlot> slots = entry.getValue();

                // Kiểm tra xem có cần refresh không
                if (shouldRefreshCache(doctorId, date, slots)) {
                    refreshDoctorCache(doctorId, date, slots);
                    refreshedCount++;
                } else {
                    skippedCount++;
                }
            }

            log.info("Hoàn thành refresh cache: {} ngày được cập nhật, {} ngày bỏ qua",
                    refreshedCount, skippedCount);

        } catch (Exception e) {
            log.error("Lỗi khi quét và refresh cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra xem cache có cần refresh không
     */
    private boolean shouldRefreshCache(UUID doctorId, LocalDate date, List<DoctorAvailableSlot> slots) {
        try {
            // Nếu cache chưa tồn tại cần refresh
            if (!doctorSlotRedisCache.isCacheExists(doctorId, date)) {
                log.debug("Cache chưa tồn tại cho doctor {} ngày {}", doctorId, date);
                return true;
            }

            // Lấy cache hiện tại
            DoctorAvailabilityCacheData cachedData = doctorSlotRedisCache.getDoctorAvailability(doctorId, date);
            if (cachedData == null) {
                return true;
            }

            // So sánh thời gian update
            LocalDateTime latestDbUpdate = slots.stream()
                    .map(DoctorAvailableSlot::getUpdatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            LocalDateTime cacheLastUpdate = cachedData.getLastUpdate();

            // Nếu DB mới hơn cache cần refresh
            if (latestDbUpdate != null && (cacheLastUpdate == null || latestDbUpdate.isAfter(cacheLastUpdate))) {
                log.debug("Phát hiện thay đổi cho doctor {} ngày {}: DB={}, Cache={}",
                        doctorId, date, latestDbUpdate, cacheLastUpdate);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra cache cho doctor {} ngày {}: {}",
                    doctorId, date, e.getMessage());
            return true;
        }
    }

    /**
     * Refresh cache cho doctor tại một ngày cụ thể
     */
    private void refreshDoctorCache(UUID doctorId, LocalDate date, List<DoctorAvailableSlot> slots) {
        try {
            if (slots.isEmpty()) {
                log.debug("Không có slot nào cho doctor {} ngày {}", doctorId, date);
                return;
            }

            // Convert sang TimeSlot DTO
            List<TimeSlot> timeSlots = slots.stream()
                    .map(this::convertToTimeSlot)
                    .collect(Collectors.toList());

            // Cache trực tiếp
            doctorSlotRedisCache.cacheDoctorAvailability(doctorId, date, timeSlots);

            log.debug("Đã refresh cache cho doctor {} ngày {} - {} slots",
                    doctorId, date, timeSlots.size());

        } catch (Exception e) {
            log.error("Lỗi khi refresh cache cho doctor {} ngày {}: {}",
                    doctorId, date, e.getMessage(), e);
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

}
