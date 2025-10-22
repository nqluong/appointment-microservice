package org.project.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.request.DoctorAvailabilityFilter;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.project.service.DoctorAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/doctor-slots/public")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @GetMapping("/doctors")
    public ResponseEntity<PageResponse<DoctorWithSlotsResponse>> getDoctorsWithSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) List<UUID> doctorIds,
            @RequestParam(required = false) List<UUID> specialtyIds,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Boolean hasAvailableSlots,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false, defaultValue = "true") Boolean useCache,
            @RequestParam(required = false, defaultValue = "true") Boolean useParallelProcessing,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .doctorIds(doctorIds)
                .specialtyIds(specialtyIds)
                .doctorName(doctorName)
                .isAvailable(isAvailable)
                .hasAvailableSlots(hasAvailableSlots)
                .maxResults(maxResults)
                .useCache(useCache)
                .useParallelProcessing(useParallelProcessing)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        try {
            PageResponse<DoctorWithSlotsResponse> response = doctorAvailabilityService
                    .getDoctorsWithAvailableSlots(filter);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/doctors/specialty/{specialtyId}")
    public ResponseEntity<PageResponse<DoctorWithSlotsResponse>> getDoctorsBySpecialtyWithSlots(
            @PathVariable UUID specialtyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Boolean hasAvailableSlots,
            @RequestParam(required = false, defaultValue = "true") Boolean useCache,
            @RequestParam(required = false, defaultValue = "false") Boolean useParallelProcessing,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {


        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .specialtyIds(List.of(specialtyId))
                .isAvailable(isAvailable)
                .hasAvailableSlots(hasAvailableSlots)
                .useCache(useCache)
                .useParallelProcessing(useParallelProcessing)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        try {
            PageResponse<DoctorWithSlotsResponse> response = doctorAvailabilityService
                    .getDoctorsWithAvailableSlots(filter);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<DoctorWithSlotsResponse> getDoctorSlots(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false, defaultValue = "true") Boolean useCache) {

        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .isAvailable(isAvailable)
                .useCache(useCache)
                .build();

        try {
            // Sử dụng method cũ nhưng với filter
            DoctorWithSlotsResponse response = doctorAvailabilityService.getDoctorAvailableSlots(
                    doctorId,
                    filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now(),
                    filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now().plusDays(7));

            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
