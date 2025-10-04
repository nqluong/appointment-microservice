package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.PageResponse;
import org.project.dto.request.CreateAbsenceRequest;
import org.project.dto.request.UpdateAbsenceRequest;
import org.project.dto.response.DoctorAbsenceResponse;
import org.project.service.DoctorAbsenceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor-absences")
@RequiredArgsConstructor
@Slf4j
public class DoctorAbsenceController {

    private final DoctorAbsenceService absenceService;

//    @PostMapping
//    @RequireOwnershipOrAdmin(userIdParam = "doctorUserId", allowedRoles = {"DOCTOR", "ADMIN"})
//    public ResponseEntity<DoctorAbsenceResponse> createAbsence(@Valid @RequestBody CreateAbsenceRequest request) {
//        DoctorAbsenceResponse createdAbsence = absenceService.createAbsence(request);
//        return new ResponseEntity<>(createdAbsence, HttpStatus.CREATED);
//    }

//    @PutMapping("/{absenceId}")
//    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<DoctorAbsenceResponse> updateAbsence(
//            @PathVariable UUID absenceId,
//            @Valid @RequestBody UpdateAbsenceRequest request) {
//        DoctorAbsenceResponse updatedAbsence = absenceService.updateAbsence(absenceId, request);
//        return ResponseEntity.ok(updatedAbsence);
//    }

    @GetMapping("/{absenceId}")
    public ResponseEntity<DoctorAbsenceResponse> getAbsenceById(@PathVariable UUID absenceId) {
        DoctorAbsenceResponse absence = absenceService.getAbsenceById(absenceId);
        return ResponseEntity.ok(absence);
    }

    @GetMapping("/doctor/{doctorUserId}")
    public ResponseEntity<PageResponse<DoctorAbsenceResponse>> getAbsencesByDoctor(
            @PathVariable UUID doctorUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DoctorAbsenceResponse> absences = absenceService.getAbsencesByDoctor(doctorUserId, pageable);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/doctor/{doctorUserId}/date-range")
    public ResponseEntity<List<DoctorAbsenceResponse>> getAbsencesInDateRange(
            @PathVariable UUID doctorUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DoctorAbsenceResponse> absences = absenceService.getAbsencesInDateRange(doctorUserId, startDate, endDate);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/doctor/{doctorUserId}/future")
    public ResponseEntity<List<DoctorAbsenceResponse>> getFutureAbsences(@PathVariable UUID doctorUserId) {
        List<DoctorAbsenceResponse> absences = absenceService.getFutureAbsences(doctorUserId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/doctor/{doctorUserId}/check")
    public ResponseEntity<Boolean> isDoctorAbsentOnDate(
            @PathVariable UUID doctorUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean isAbsent = absenceService.isDoctorAbsentOnDate(doctorUserId, date);
        return ResponseEntity.ok(isAbsent);
    }

    @DeleteMapping("/{absenceId}")
    //@PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteAbsence(@PathVariable UUID absenceId) {
        absenceService.deleteAbsence(absenceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup")
  //  @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> cleanupPastAbsences(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {
        int deletedCount = absenceService.cleanupPastAbsences(cutoffDate);
        return ResponseEntity.ok(deletedCount);
    }
}
