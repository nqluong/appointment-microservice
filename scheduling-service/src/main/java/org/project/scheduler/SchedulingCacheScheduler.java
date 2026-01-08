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
            cleanupExpiredSlots();
        } catch (Exception ex) {
            log.error("Lỗi khi khởi tạo scheduling cache scheduler: {}", ex.getMessage(), ex);
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredSlots() {
        try {

            LocalDate today = LocalDate.now();
            String basePattern = "doctor:availability:*";

            Set<String> allKeys = redisCacheService.keys(basePattern);

            if (allKeys == null || allKeys.isEmpty()) {
                log.info("Không tìm thấy cache key nào để dọn dẹp");
                return;
            }

            log.info("Tìm thấy {} cache key để kiểm tra", allKeys.size());

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
                    log.warn("Không thể phân tích cache key: {} - {}", key, e.getMessage());
                }
            }

            if (!expiredKeys.isEmpty()) {
                long deletedCount = redisCacheService.delete(expiredKeys);
                log.info("Hoàn thành dọn dẹp: đã xóa {} cache hết hạn từ {} key",
                        deletedCount, expiredKeys.size());
            } else {
                log.info("Không tìm thấy slot hết hạn nào để dọn dẹp");
            }

        } catch (Exception ex) {
            log.error("Lỗi trong công việc dọn dẹp: {}", ex.getMessage(), ex);
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 * * * *")
    public void monitorQueueSize() {
        try {
            String queueKey = "doctor_availability_cache_queue";
            Long queueSize = redisCacheService.getListSize(queueKey);

            if (queueSize != null && queueSize > 0) {
                log.info("Kích thước queue cache: {} doctor ID đang chờ xử lý", queueSize);

                if (queueSize > 100) {
                    log.warn("CẢNH BÁO: Queue cache có {} mục đang chờ - có thể bị chậm xử lý",
                            queueSize);
                }
            }
        } catch (Exception ex) {
            log.error("Lỗi khi giám sát kích thước queue: {}", ex.getMessage(), ex);
        }
    }
}
