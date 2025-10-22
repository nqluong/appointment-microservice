package org.project.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheInvalidationService {
    RedisCacheService redisCacheService;

    private static final String CACHE_PREFIX = "doctor:availability:";
    private static final int CACHE_TTL = 1;

    public void updateSlotInCache(DoctorAvailableSlot slot) {
        try {
            String cacheKey = CACHE_PREFIX + slot.getDoctorId() + ":" + slot.getSlotDate();

            // Lấy cache hiện tại
            DoctorAvailabilityCacheData cacheData = redisCacheService.get(cacheKey, DoctorAvailabilityCacheData.class);

            if (cacheData == null) {
                log.info("Cache không tồn tại cho key: {}, bỏ qua update", cacheKey);
                return;
            }

            boolean updated = updateSlotStatus(cacheData, slot);

            if (updated) {
                redisCacheService.set(cacheKey, cacheData, CACHE_TTL, TimeUnit.DAYS);
                log.info("Đã cập nhật cache cho slot {} của bác sĩ {} ngày {}",
                        slot.getId(), slot.getDoctorId(), slot.getSlotDate());
            }

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật cache cho slot {}: {}", slot.getId(), e.getMessage(), e);
        }
    }

    public void invalidateCache(UUID doctorId, LocalDate date) {
        try {
            String cacheKey = CACHE_PREFIX + doctorId + ":" + date;
            redisCacheService.delete(cacheKey);
            log.info("Đã xóa cache cho bác sĩ {} ngày {}", doctorId, date);
        } catch (Exception e) {
            log.error("Lỗi khi xóa cache: {}", e.getMessage(), e);
        }
    }

    public void invalidateCacheRange(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                invalidateCache(doctorId, currentDate);
                currentDate = currentDate.plusDays(1);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa cache range: {}", e.getMessage(), e);
        }
    }

    private boolean updateSlotStatus(DoctorAvailabilityCacheData cacheData, DoctorAvailableSlot slot) {
        UUID slotId = slot.getId();
        boolean newStatus = slot.isAvailable();

        TimeSlot targetSlot = cacheData.getSlots().stream()
                .filter(s -> s.getSlotId().equals(slotId))
                .findFirst()
                .orElse(null);

        if (targetSlot != null) {
            targetSlot.setAvailable(newStatus);

            return true;
        }
        return false;
    }
}
