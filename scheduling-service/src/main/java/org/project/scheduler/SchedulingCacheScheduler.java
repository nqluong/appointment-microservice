package org.project.scheduler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.project.service.RedisCacheService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulingCacheScheduler {

    RedisCacheService redisCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            log.info("Initializing scheduling cache scheduler...");
            cleanupExpiredSlots();
            log.info("Scheduling cache scheduler initialized successfully");
        } catch (Exception ex) {
            log.error("Error initializing scheduling cache scheduler: {}", ex.getMessage(), ex);
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredSlots() {
        try {
            log.info("Starting cleanup of expired availability slots...");

            LocalDate today = LocalDate.now();
            String basePattern = "doctor:availability:*";

            Set<String> allKeys = redisCacheService.keys(basePattern);

            if (allKeys == null || allKeys.isEmpty()) {
                log.info("No availability cache keys found for cleanup");
                return;
            }

            log.info("Found {} availability cache keys to check", allKeys.size());

            List<String> expiredKeys = new ArrayList<>();
            for (String key : allKeys) {
                try {
                    String[] parts = key.split(":");
                    if (parts.length >= 4) {
                        String dateStr = parts[3];
                        LocalDate slotDate = LocalDate.parse(dateStr);

                        if (slotDate.isBefore(today)) {
                            expiredKeys.add(key);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse cache key: {} - {}", key, e.getMessage());
                }
            }

            if (!expiredKeys.isEmpty()) {
                long deletedCount = redisCacheService.delete(expiredKeys);
                log.info("Cleanup completed: removed {} expired slot caches from {} keys",
                        deletedCount, expiredKeys.size());
            } else {
                log.info("No expired slots found to cleanup");
            }

        } catch (Exception ex) {
            log.error("Error in cleanup job: {}", ex.getMessage(), ex);
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 * * * *")
    public void monitorQueueSize() {
        try {
            String queueKey = "doctor_availability_cache_queue";
            Long queueSize = redisCacheService.getListSize(queueKey);

            if (queueSize != null && queueSize > 0) {
                log.info("Availability cache queue size: {} pending doctor IDs", queueSize);

                if (queueSize > 100) {
                    log.warn("ALERT: Availability cache queue has {} pending items - may indicate processing delay",
                            queueSize);
                }
            }
        } catch (Exception ex) {
            log.error("Error monitoring queue size: {}", ex.getMessage(), ex);
        }
    }
}
