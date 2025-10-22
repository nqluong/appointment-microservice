package org.project.mapper;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.dto.response.AvailableSlotInfo;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.project.model.DoctorAvailableSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class DoctorAvailabilityMapper2 {
    private DoctorAvailabilityMapper2() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static DoctorWithSlotsResponse toWithSlotsResponse(DoctorResponse doctorResponse) {
        if (doctorResponse == null) {
            return null;
        }

        return DoctorWithSlotsResponse.builder()
                .userId(String.valueOf(doctorResponse.getUserId()))
                .fullName(doctorResponse.getFullName())
                .avatarUrl(doctorResponse.getAvatarUrl())
                .qualification(doctorResponse.getQualification())
                .consultationFee(doctorResponse.getConsultationFee())
                .yearsOfExperience(doctorResponse.getYearsOfExperience())
                .gender(doctorResponse.getGender())
                .phone(doctorResponse.getPhone())
                .specialtyName(doctorResponse.getSpecialtyName())
                .availableSlots(new ArrayList<>())
                .build();
    }


    public static AvailableSlotInfo toAvailableSlotInfo(DoctorAvailableSlot dbSlot) {
        if (dbSlot == null) {
            return null;
        }

        try {
            return AvailableSlotInfo.builder()
                    .slotId(String.valueOf(dbSlot.getId()))
                    .slotDate(dbSlot.getSlotDate())
                    .startTime(dbSlot.getStartTime())
                    .endTime(dbSlot.getEndTime())
                    .isAvailable(dbSlot.isAvailable())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi chuyển đổi DoctorAvailableSlot sang AvailableSlotInfo: {}", e.getMessage());
            return null;
        }
    }

    public static List<AvailableSlotInfo> toAvailableSlotInfoList(List<DoctorAvailableSlot> dbSlots) {
        if (dbSlots == null || dbSlots.isEmpty()) {
            return Collections.emptyList();
        }

        return dbSlots.stream()
                .map(DoctorAvailabilityMapper2::toAvailableSlotInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<AvailableSlotInfo> toAvailableSlotInfoList(DoctorAvailabilityCacheData cacheData) {
        if (cacheData == null || cacheData.getSlots() == null) {
            return Collections.emptyList();
        }

        LocalDate date;
        try {
            date = LocalDate.parse(cacheData.getDate());
        } catch (DateTimeParseException e) {
            log.error("Lỗi parse ngày từ cache: {}", cacheData.getDate());
            return Collections.emptyList();
        }

        return cacheData.getSlots().stream()
                .map(timeSlot -> toAvailableSlotInfo(timeSlot, date))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static AvailableSlotInfo toAvailableSlotInfo(TimeSlot timeSlot, LocalDate date) {
        if (timeSlot == null || date == null) {
            return null;
        }

        try {
            return AvailableSlotInfo.builder()
                    .slotId(String.valueOf(timeSlot.getSlotId()))
                    .slotDate(date)
                    .startTime(LocalTime.parse(timeSlot.getStartTime()))
                    .endTime(LocalTime.parse(timeSlot.getEndTime()))
                    .isAvailable(timeSlot.isAvailable())
                    .build();
        } catch (DateTimeParseException e) {
            log.error("Lỗi parse thời gian từ TimeSlot: {} - {}",
                    timeSlot.getStartTime(), timeSlot.getEndTime());
            return null;
        }
    }

    public static TimeSlot toTimeSlot(AvailableSlotInfo slotInfo) {
        if (slotInfo == null) {
            return null;
        }

        try {
            return TimeSlot.builder()
                    .slotId(UUID.fromString(slotInfo.getSlotId()))
                    .startTime(slotInfo.getStartTime().toString())
                    .endTime(slotInfo.getEndTime().toString())
                    .isAvailable(Boolean.TRUE.equals(slotInfo.getIsAvailable()))
                    .build();
        } catch (Exception e) {
            log.error("Lỗi chuyển đổi AvailableSlotInfo sang TimeSlot: {}", e.getMessage());
            return null;
        }
    }

    public static List<TimeSlot> toTimeSlots(List<AvailableSlotInfo> slotInfos) {
        if (slotInfos == null || slotInfos.isEmpty()) {
            return Collections.emptyList();
        }

        return slotInfos.stream()
                .map(DoctorAvailabilityMapper2::toTimeSlot)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static DoctorAvailabilityCacheData toCacheData(
            UUID doctorId,
            LocalDate date,
            List<AvailableSlotInfo> slotInfos) {

        if (doctorId == null || date == null) {
            return null;
        }

        List<TimeSlot> timeSlots = slotInfos != null
                ? toTimeSlots(slotInfos)
                : Collections.emptyList();

        return DoctorAvailabilityCacheData.builder()
                .doctorId(doctorId)
                .date(date.toString())
                .slots(timeSlots)
                .build();
    }

}
