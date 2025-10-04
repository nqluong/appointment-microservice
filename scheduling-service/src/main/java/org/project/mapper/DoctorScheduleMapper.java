package org.project.mapper;


import org.project.dto.request.ScheduleEntryRequest;
import org.project.dto.response.DoctorScheduleResponse;
import org.project.dto.response.ScheduleEntryResponse;
import org.project.model.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorScheduleMapper {
    public DoctorSchedule toEntity(ScheduleEntryRequest request, String timezone, String notes) {
        //public DoctorSchedule toEntity(ScheduleEntryRequest request, User doctor, String timezone, String notes) {
        return DoctorSchedule.builder()
               // .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDuration(request.getSlotDuration() != null ? request.getSlotDuration() : 30)
                .breakDuration(request.getBreakDuration() != null ? request.getBreakDuration() : 5)
                .maxAppointmentsPerSlot(request.getMaxAppointmentsPerSlot() != null ? request.getMaxAppointmentsPerSlot() : 1)
                .maxAppointmentsPerDay(request.getMaxAppointmentsPerDay())
                .isActive(true)
                .timezone(timezone)
                .notes(notes)
                .build();
    }

    public List<DoctorSchedule> toEntityList(List<ScheduleEntryRequest> requests,  String timezone, String notes) {
//        public List<DoctorSchedule> toEntityList(List<ScheduleEntryRequest> requests, User doctor, String timezone, String notes) {
        return requests.stream()
                .map(request -> toEntity(request,  timezone, notes))
                .collect(Collectors.toList());
    }

    public ScheduleEntryResponse toScheduleEntryResponse(DoctorSchedule entity) {
        return ScheduleEntryResponse.builder()
                .id(entity.getId())
                .dayOfWeek(entity.getDayOfWeek())
                .dayName(getDayName(entity.getDayOfWeek()))
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .slotDuration(entity.getSlotDuration())
                .breakDuration(entity.getBreakDuration())
                .maxAppointmentsPerSlot(entity.getMaxAppointmentsPerSlot())
                .maxAppointmentsPerDay(entity.getMaxAppointmentsPerDay())
                .isActive(entity.isActive())
                .build();
    }

    public List<ScheduleEntryResponse> toScheduleEntryResponseList(List<DoctorSchedule> entities) {
        return entities.stream()
                .map(this::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

//    public DoctorScheduleResponse toDoctorScheduleResponse(User doctor, List<DoctorSchedule> schedules) {
//        return DoctorScheduleResponse.builder()
//                .doctorId(doctor.getId())
//                .doctorName(doctor.getUserProfile().getFirstName() + " " + doctor.getUserProfile().getLastName())
//                .scheduleEntries(toScheduleEntryResponseList(schedules))
//                .timezone(extractTimezone(schedules))
//                .notes(extractNotes(schedules))
//                .createdAt(extractCreatedAt(schedules))
//                .updatedAt(extractUpdatedAt(schedules))
//                .build();
//    }

    private String extractTimezone(List<DoctorSchedule> schedules) {
        return schedules.isEmpty() ? null : schedules.get(0).getTimezone();
    }

    private String extractNotes(List<DoctorSchedule> schedules) {
        return schedules.isEmpty() ? null : schedules.get(0).getNotes();
    }

    private LocalDateTime extractCreatedAt(List<DoctorSchedule> schedules) {
        return schedules.isEmpty() ? null : schedules.get(0).getCreatedAt();
    }

    private LocalDateTime extractUpdatedAt(List<DoctorSchedule> schedules) {
        return schedules.isEmpty() ? null : schedules.get(0).getUpdatedAt();
    }

    private String getDayName(Integer dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Unknown";
        };
    }

    public DoctorSchedule updateEntity(DoctorSchedule existingSchedule, ScheduleEntryRequest request,
                                       String timezone, String notes) {
        existingSchedule.setStartTime(request.getStartTime());
        existingSchedule.setEndTime(request.getEndTime());
        existingSchedule.setSlotDuration(request.getSlotDuration() != null ? request.getSlotDuration() : 30);
        existingSchedule.setBreakDuration(request.getBreakDuration() != null ? request.getBreakDuration() : 5);
        existingSchedule.setMaxAppointmentsPerSlot(request.getMaxAppointmentsPerSlot() != null ?
                request.getMaxAppointmentsPerSlot() : 1);
        existingSchedule.setMaxAppointmentsPerDay(request.getMaxAppointmentsPerDay());
        existingSchedule.setTimezone(timezone);
        existingSchedule.setNotes(notes);

        // Update isActive
        if (request.getIsActive() != null) {
            existingSchedule.setActive(request.getIsActive());
        }

        return existingSchedule;
    }
}
