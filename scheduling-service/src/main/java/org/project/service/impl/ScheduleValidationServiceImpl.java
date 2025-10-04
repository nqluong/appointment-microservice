package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.ScheduleEntryRequest;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.ScheduleValidationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScheduleValidationServiceImpl implements ScheduleValidationService {

    @Override
    public void validateScheduleEntries(List<ScheduleEntryRequest> scheduleEntries) {
        if (scheduleEntries == null || scheduleEntries.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Integer> dayOfWeeks = new HashSet<>();

        for (ScheduleEntryRequest entry : scheduleEntries) {
            validateScheduleEntry(entry);

            // Kiểm tra ngày trùng lặp
            if (!dayOfWeeks.add(entry.getDayOfWeek())) {
                log.warn("Phát hiện ngày trong tuần bị trùng lặp: {}", entry.getDayOfWeek());
                throw new CustomException(ErrorCode.DUPLICATE_SCHEDULE_DAY);
            }
        }
    }

    @Override
    public void validateScheduleEntry(ScheduleEntryRequest entry) {
        // Validate khoảng thời gian
        if (entry.getStartTime().isAfter(entry.getEndTime()) ||
                entry.getStartTime().equals(entry.getEndTime())) {
            log.warn("Khoảng thời gian không hợp lệ: {} - {}", entry.getStartTime(), entry.getEndTime());
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Validate thời gian làm
        LocalTime earliestStart = LocalTime.of(6, 0);
        LocalTime latestEnd = LocalTime.of(23, 0);

        if (entry.getStartTime().isBefore(earliestStart) ||
                entry.getEndTime().isAfter(latestEnd)) {
            log.warn("Giờ làm việc nằm ngoài phạm vi cho phép: {} - {}",
                    entry.getStartTime(), entry.getEndTime());
            throw new CustomException(ErrorCode.INVALID_WORKING_HOURS);
        }

        // Validate thời lượng của 1 lịch khám
        if (entry.getSlotDuration() != null) {
            long totalMinutes = Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes();
            if (entry.getSlotDuration() > totalMinutes) {
                log.warn("Thời lượng khung giờ {} vượt quá tổng thời gian làm việc {} phút",
                        entry.getSlotDuration(), totalMinutes);
                throw new CustomException(ErrorCode.INVALID_SLOT_DURATION);
            }
        }
    }
}
