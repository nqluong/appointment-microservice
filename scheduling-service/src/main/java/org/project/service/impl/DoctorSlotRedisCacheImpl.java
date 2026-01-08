package org.project.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.service.DoctorSlotRedisCache;
import org.project.service.RedisCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorSlotRedisCacheImpl implements DoctorSlotRedisCache {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheService redisCacheService;

    private static final String CACHE_KEY_PREFIX = "doctor:availability:";
    private static final String SLOT_FIELD_PREFIX = "slot:";
    private static final String METADATA_FIELD = "metadata";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    public void cacheDoctorAvailability(UUID doctorId, LocalDate date, List<TimeSlot> slots) {

        
        try {
            // Không cache nếu không có slot
            if (slots == null || slots.isEmpty()) {
                log.debug("Bỏ qua cache cho doctor {} vào ngày {} (không có slot)", doctorId, date);
                return;
            }

            String cacheKey = buildCacheKey(doctorId, date);

            Map<String, Object> hashData = new HashMap<>();

            // Tìm thời gian cập nhật mới nhất từ tất cả slots
            java.time.LocalDateTime latestUpdate = slots.stream()
                    .map(TimeSlot::getLastUpdate)
                    .filter(Objects::nonNull)
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null);

            // Lưu metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("doctorId", doctorId.toString());
            metadata.put("date", date.toString());
            metadata.put("totalSlots", slots.size());
            metadata.put("cachedAt", System.currentTimeMillis());
            if (latestUpdate != null) {
                metadata.put("lastUpdate", latestUpdate.toString());
            }
            hashData.put(METADATA_FIELD, metadata);

            // Lưu từng slot
            int slotCount = 0;
            for (TimeSlot slot : slots) {
                String hashField = SLOT_FIELD_PREFIX + slot.getSlotId();
                hashData.put(hashField, slot);
                slotCount++;
                log.trace("  → Slot {}: {} - {} (available: {})", 
                        slotCount, slot.getStartTime(), slot.getEndTime(), slot.isAvailable());
            }

            // Put all vào hash
            redisTemplate.opsForHash().putAll(cacheKey, hashData);
            
            Boolean expireResult = redisTemplate.expire(cacheKey, CACHE_TTL);

            // Verify cache
            Long hashSize = redisTemplate.opsForHash().size(cacheKey);
            log.info("Đã cache {} slots cho doctor {} vào ngày {} (key: {}, hash size: {})",
                    slots.size(), doctorId, date, cacheKey, hashSize);

        } catch (Exception e) {
            log.error("Lỗi khi cache availability cho doctor {} vào ngày {}: {}",
                    doctorId, date, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    public DoctorAvailabilityCacheData getDoctorAvailability(UUID doctorId, LocalDate date) {
        try {
            String cacheKey = buildCacheKey(doctorId, date);

            Map<Object, Object> hashData = redisTemplate.opsForHash().entries(cacheKey);

            if (hashData.isEmpty()) {
                log.debug("Cache miss for doctor {} on {}", doctorId, date);
                return null;
            }

            // Lấy metadata
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) hashData.get(METADATA_FIELD);

            if (metadata == null) {
                log.warn("Metadata not found in cache for doctor {} on {}", doctorId, date);
                return null;
            }

            // Lấy các slots
            List<TimeSlot> slots = hashData.entrySet().stream()
                    .filter(entry -> ((String) entry.getKey()).startsWith(SLOT_FIELD_PREFIX))
                    .map(entry -> (TimeSlot) entry.getValue())
                    .sorted(Comparator.comparing(TimeSlot::getStartTime))
                    .collect(Collectors.toList());

            log.debug("Cache hit for doctor {} on {} with {} slots", doctorId, date, slots.size());

            // Parse lastUpdate nếu có
            java.time.LocalDateTime lastUpdate = null;
            if (metadata.containsKey("lastUpdate")) {
                try {
                    lastUpdate = java.time.LocalDateTime.parse((String) metadata.get("lastUpdate"));
                } catch (Exception e) {
                    log.warn("Failed to parse lastUpdate from cache: {}", e.getMessage());
                }
            }

            return DoctorAvailabilityCacheData.builder()
                    .doctorId(doctorId)
                    .date(date)
                    .slots(slots)
                    .totalSlots(slots.size())
                    .cachedAt(((Number) metadata.get("cachedAt")).longValue())
                    .lastUpdate(lastUpdate)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get doctor availability from cache for doctor {} on {}: {}",
                    doctorId, date, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void updateSlotAvailability(UUID doctorId, LocalDate slotDate, UUID slotId, boolean isAvailable) {
        try {
            String cacheKey = buildCacheKey(doctorId, slotDate);
            String hashField = SLOT_FIELD_PREFIX + slotId;

            // Kiểm tra cache key có tồn tại không
            if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))) {
                log.debug("Cache key {} not found, skipping update", cacheKey);
                return;
            }

            // Lấy slot hiện tại
            Object slotData = redisTemplate.opsForHash().get(cacheKey, hashField);

            if (slotData != null) {
                TimeSlot slot = (TimeSlot) slotData;
                slot.setAvailable(isAvailable);

                // Update chỉ field này trong hash - O(1) operation
                redisTemplate.opsForHash().put(cacheKey, hashField, slot);

                log.debug("Updated slot {} availability to {} in cache for doctor {} on {}",
                        slotId, isAvailable, doctorId, slotDate);
            } else {
                log.debug("Slot {} not found in cache key {}", slotId, cacheKey);
            }

        } catch (Exception e) {
            log.error("Failed to update slot {} availability in cache: {}", slotId, e.getMessage(), e);
            // Fallback: evict cache nếu update thất bại
            evictDoctorAvailabilityCache(doctorId, slotDate);
        }
    }

    @Override
    public void evictDoctorAvailabilityCache(UUID doctorId, LocalDate slotDate) {
        try {
            String cacheKey = buildCacheKey(doctorId, slotDate);
            Boolean deleted = redisTemplate.delete(cacheKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Evicted cache for doctor {} on {}", doctorId, slotDate);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache for doctor {} on {}: {}",
                    doctorId, slotDate, e.getMessage(), e);
        }
    }

    @Override
    public boolean isCacheExists(UUID doctorId, LocalDate date) {
        try {
            String cacheKey = buildCacheKey(doctorId, date);
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        } catch (Exception e) {
            log.error("Failed to check cache existence: {}", e.getMessage(), e);
            return false;
        }
    }

    private String buildCacheKey(UUID doctorId, LocalDate date) {
        return CACHE_KEY_PREFIX + doctorId + ":" + date;
    }
}
