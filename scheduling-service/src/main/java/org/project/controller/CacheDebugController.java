package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.cache.TimeSlot;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
import org.project.service.RedisCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheDebugController {

    private final DoctorSlotRedisCache doctorSlotRedisCache;
    private final DoctorAvailableSlotRepository slotRepository;
    private final RedisCacheService redisCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/test-cache/{doctorId}")
    public ResponseEntity<Map<String, Object>> testCache(@PathVariable UUID doctorId) {
        log.info("🧪 Testing cache for doctor: {}", doctorId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Lấy slots từ DB
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(14);
            
            List<DoctorAvailableSlot> slots = slotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, startDate, endDate);
            
            result.put("slotsFromDB", slots.size());
            log.info("📋 Found {} slots from DB", slots.size());
            
            if (slots.isEmpty()) {
                result.put("error", "No slots found in DB");
                return ResponseEntity.ok(result);
            }
            
            // 2. Group by date
            Map<LocalDate, List<DoctorAvailableSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(DoctorAvailableSlot::getSlotDate));
            
            result.put("daysWithSlots", slotsByDate.size());
            log.info("📅 Slots distributed across {} days", slotsByDate.size());
            
            // 3. Test cache một ngày
            LocalDate testDate = slotsByDate.keySet().iterator().next();
            List<DoctorAvailableSlot> dailySlots = slotsByDate.get(testDate);
            
            List<TimeSlot> timeSlots = dailySlots.stream()
                    .map(slot -> TimeSlot.builder()
                            .slotId(slot.getId())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .isAvailable(slot.isAvailable())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("🔄 Testing cache for date: {} with {} slots", testDate, timeSlots.size());
            doctorSlotRedisCache.cacheDoctorAvailability(doctorId, testDate, timeSlots);
            
            // 4. Verify cache
            String cacheKey = "doctor:availability:" + doctorId + ":" + testDate;
            Boolean exists = redisTemplate.hasKey(cacheKey);
            Long hashSize = exists ? redisTemplate.opsForHash().size(cacheKey) : 0L;
            
            result.put("testDate", testDate.toString());
            result.put("slotsForTestDate", timeSlots.size());
            result.put("cacheKey", cacheKey);
            result.put("cacheExists", exists);
            result.put("cacheHashSize", hashSize);
            
            log.info("✅ Cache test completed. Exists: {}, Hash size: {}", exists, hashSize);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error testing cache", e);
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/check/{doctorId}/{date}")
    public ResponseEntity<Map<String, Object>> checkCache(
            @PathVariable UUID doctorId,
            @PathVariable String date) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            String cacheKey = "doctor:availability:" + doctorId + ":" + localDate;
            
            Boolean exists = redisTemplate.hasKey(cacheKey);
            result.put("cacheKey", cacheKey);
            result.put("exists", exists);
            
            if (Boolean.TRUE.equals(exists)) {
                Long hashSize = redisTemplate.opsForHash().size(cacheKey);
                Map<Object, Object> hashData = redisTemplate.opsForHash().entries(cacheKey);
                
                result.put("hashSize", hashSize);
                result.put("fields", hashData.keySet());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error checking cache", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/push-to-queue/{doctorId}")
    public ResponseEntity<String> pushToQueue(@PathVariable UUID doctorId) {
        try {
            redisCacheService.leftPush("doctor_availability_cache_queue", doctorId.toString());
            return ResponseEntity.ok("Pushed doctor " + doctorId + " to queue");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
