package org.project.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
import org.project.service.RedisCacheService;
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
    private final RedisCacheService redisCacheService;

    private static final String CACHE_PREFIX = "doctor:availability:";
    private static final String QUEUE_KEY = "doctor_availability_cache_queue";
    private static final int DAYS_TO_CHECK = 14;


    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    public void checkAndRefreshStaleCache() {

        try {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(DAYS_TO_CHECK);

            // Lấy tất cả slots đã được cập nhật trong vòng 10 phút qua
            LocalDateTime recentUpdateThreshold = LocalDateTime.now().minusMinutes(10);
            List<DoctorAvailableSlot> recentlyUpdatedSlots = slotRepository
                    .findRecentlyUpdatedSlots(recentUpdateThreshold, startDate, endDate);

            if (recentlyUpdatedSlots.isEmpty()) {
                log.debug("Không có slot nào được cập nhật gần đây");
                return;
            }

            // Nhóm theo doctorId và date
            Map<String, List<DoctorAvailableSlot>> groupedSlots = recentlyUpdatedSlots.stream()
                    .collect(Collectors.groupingBy(slot -> 
                        slot.getDoctorId() + ":" + slot.getSlotDate()));

            int refreshCount = 0;
            Set<UUID> processedDoctors = new HashSet<>();
            
            for (Map.Entry<String, List<DoctorAvailableSlot>> entry : groupedSlots.entrySet()) {
                String[] keys = entry.getKey().split(":");
                UUID doctorId = UUID.fromString(keys[0]);
                LocalDate date = LocalDate.parse(keys[1]);
                List<DoctorAvailableSlot> slots = entry.getValue();

                boolean shouldRefresh = false;
                String reason = "";

                boolean cacheExists = doctorSlotRedisCache.isCacheExists(doctorId, date);
                
                if (!cacheExists) {
                    shouldRefresh = true;
                    reason = "Cache chưa tồn tại, có slots mới cần được cache";
                    log.info("Phát hiện slots mới cho doctor {} ngày {} chưa có cache", doctorId, date);
                } else {
                    // Cache đã tồn tại kiểm tra xem có thay đổi không
                    DoctorAvailabilityCacheData cachedData = doctorSlotRedisCache.getDoctorAvailability(doctorId, date);
                    
                    if (cachedData != null) {
                        LocalDateTime latestDbUpdate = slots.stream()
                                .map(DoctorAvailableSlot::getUpdatedAt)
                                .max(LocalDateTime::compareTo)
                                .orElse(null);

                        LocalDateTime cacheLastUpdate = cachedData.getLastUpdate();

                        if (latestDbUpdate != null && (cacheLastUpdate == null || latestDbUpdate.isAfter(cacheLastUpdate))) {
                            shouldRefresh = true;
                            reason = String.format("Phát hiện thay đổi: DB=%s, Cache=%s", latestDbUpdate, cacheLastUpdate);
                        }
                    }
                }

                if (shouldRefresh && !processedDoctors.contains(doctorId)) {
                    log.info("Đẩy doctor {} vào queue để refresh cache. Lý do: {}", doctorId, reason);
                    redisCacheService.leftPush(QUEUE_KEY, doctorId.toString());
                    processedDoctors.add(doctorId);
                    refreshCount++;
                }
            }

            log.info("Hoàn thành quét cache: Đã đẩy {} doctor vào queue để refresh", refreshCount);

        } catch (Exception e) {
            log.error("Lỗi khi quét và refresh cache: {}", e.getMessage(), e);
        }
    }

}
