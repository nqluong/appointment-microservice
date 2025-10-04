package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.PageResponse;
import org.project.dto.request.DoctorScheduleCreateRequest;
import org.project.dto.request.DoctorScheduleUpdateRequest;
import org.project.dto.request.DoctorSearchRequest;
import org.project.dto.response.DoctorScheduleResponse;
import org.project.dto.response.DoctorSearchResponse;
import org.project.service.DoctorScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class DoctorScheduleController {
    private final DoctorScheduleService doctorScheduleService;

//    @PostMapping("/doctors")
//    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<DoctorScheduleResponse> createDoctorSchedule(
//            @Valid @RequestBody DoctorScheduleCreateRequest request) {
//
//        DoctorScheduleResponse response = doctorScheduleService.createDoctorSchedule(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

//    @GetMapping("/doctors/{doctorId}")
//    public ResponseEntity<DoctorScheduleResponse> getDoctorSchedule(
//            @PathVariable UUID doctorId) {
//
//        DoctorScheduleResponse response = doctorScheduleService.getDoctorSchedule(doctorId);
//
//        return ResponseEntity.ok(response);
//    }

//    @PutMapping("/doctors/{doctorId}")
//   // @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<DoctorScheduleResponse> updateDoctorSchedule(
//            @PathVariable UUID doctorId,
//            @Valid @RequestBody DoctorScheduleUpdateRequest request) {
//
//        DoctorScheduleResponse response = doctorScheduleService.updateDoctorSchedule(doctorId, request);
//
//        return ResponseEntity.ok(response);
//    }

    @DeleteMapping("/doctors/{doctorId}")
   // @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctorSchedule(@PathVariable UUID doctorId) {
        log.info("Deleting schedule for doctor: {}", doctorId);

        doctorScheduleService.deleteDoctorSchedule(doctorId);

        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/doctors/search")
//    public ResponseEntity<PageResponse<DoctorSearchResponse>> searchDoctors(
//            @RequestParam(required = false) UUID specialtyId,
//            @RequestParam(required = false) String specialtyName,
//            @RequestParam(required = false) String doctorName,
//            @RequestParam(required = false) String availableDate,
//            @RequestParam(required = false) String preferredStartTime,
//            @RequestParam(required = false) String preferredEndTime,
//            @RequestParam(required = false) Boolean isApproved,
//            @RequestParam(required = false) Integer minExperience,
//            @RequestParam(required = false) Integer maxExperience,
//            @RequestParam(required = false) String qualification,
//            @RequestParam(required = false) BigDecimal minConsultationFee,
//            @RequestParam(required = false) BigDecimal maxConsultationFee,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDirection) {
//
//        log.debug("Searching doctors with filters - specialty: {}, name: {}, date: {}",
//                specialtyId, doctorName, availableDate);
//
//        DoctorSearchRequest request = DoctorSearchRequest.builder()
//                .specialtyId(specialtyId)
//                .doctorName(doctorName)
//                .specialtyName(specialtyName)
//                .availableDate(availableDate != null ? LocalDate.parse(availableDate) : null)
//                .preferredStartTime(preferredStartTime != null ? LocalTime.parse(preferredStartTime) : null)
//                .preferredEndTime(preferredEndTime != null ? LocalTime.parse(preferredEndTime) : null)
//                .isApproved(isApproved)
//                .minExperience(minExperience)
//                .maxExperience(maxExperience)
//                .qualification(qualification)
//                .minConsultationFee(minConsultationFee)
//                .maxConsultationFee(maxConsultationFee)
//                .page(page)
//                .size(size)
//                .sortBy(sortBy)
//                .sortDirection(sortDirection)
//                .build();
//
//        PageResponse<DoctorSearchResponse> response = doctorScheduleService.searchDoctors(request);
//
//        return ResponseEntity.ok(response);
//    }
}
