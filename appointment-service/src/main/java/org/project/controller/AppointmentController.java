package org.project.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.common.security.util.SecurityUtils;
import org.project.dto.PageResponse;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.response.AppointmentDtoResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.enums.Status;
import org.project.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final SecurityUtils securityUtils;

    @GetMapping("/users/{userId}")
    public ResponseEntity<PageResponse<AppointmentDtoResponse>> getUserAppointmentsByStatus(
            @PathVariable UUID userId,
            @RequestParam List<Status> statuses,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "appointmentDate") String sortBy) {

        securityUtils.validateUserAccess(userId);
                Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<AppointmentDtoResponse> appointments = appointmentService
                .getUserAppointmentsByStatus(userId, statuses, pageable);

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AppointmentDtoResponse>> getDoctorAppointmentsByStatus(
            @PathVariable UUID doctorId,
            @RequestParam List<Status> statuses,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "appointmentDate") String sortBy) {

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<AppointmentDtoResponse> appointments = appointmentService
                .getDoctorAppointmentsByStatus(doctorId, statuses, pageable);

        return ResponseEntity.ok(appointments);
    }

//    @PostMapping
//    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
//        AppointmentResponse appointmentResponse = appointmentService.createAppointment(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentResponse);
//    }
//
//    @GetMapping
//    //@RequireOwnershipOrAdmin
//    public ResponseEntity<PageResponse<AppointmentResponse>> getAppointments(
//            @RequestParam(required = false) UUID userId,
//            @RequestParam(required = false) Status status,
//            @RequestParam(defaultValue = "0") @Min(0) Integer page,
//            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
//            @RequestParam(defaultValue = "DESC") String sortDirection,
//            @RequestParam(defaultValue = "appointmentDate") String sortBy) {
//
//        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
//                ? Sort.Direction.ASC
//                : Sort.Direction.DESC;
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//
//        PageResponse<AppointmentResponse> response = appointmentService.getAppointments(userId, status, pageable);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/{appointmentId}/status")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
//            @PathVariable UUID appointmentId,
//            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
//
//        log.info("Admin updating appointment {} status to {}", appointmentId, request.getStatus());
//
//        AppointmentResponse response = appointmentService.updateAppointmentStatus(
//                appointmentId,
//                request.getStatus()
//        );
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @PutMapping("/{appointmentId}/complete")
//    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<AppointmentResponse> completeAppointment(
//            @PathVariable UUID appointmentId) {
//
//        log.info("Completing appointment {}", appointmentId);
//
//        AppointmentResponse response = appointmentService.completeAppointment(appointmentId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/{appointmentId}/cancel")
//    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<AppointmentResponse> cancelAppointment(
//            @PathVariable UUID appointmentId,
//            @Valid @RequestBody CancelAppointmentRequest request) {
//
//        log.info("Cancelling appointment {} with reason: {}", appointmentId, request.getReason());
//
//        AppointmentResponse response = appointmentService.cancelAppointment(
//                appointmentId,
//                request.getReason()
//        );
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @PutMapping("/{appointmentId}/start-examination")
//    @PreAuthorize("hasRole('DOCTOR')")
//    public ResponseEntity<AppointmentResponse> startExamination(@PathVariable UUID appointmentId) {
//        AppointmentResponse response = appointmentService.startExamination(appointmentId);
//        return ResponseEntity.ok(response);
//    }
//
//
//    @PostMapping("/{appointmentId}/complete-with-medical-record")
//    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
//    public ResponseEntity<MedicalRecordResponse> completeAppointmentWithMedicalRecord(
//            @PathVariable UUID appointmentId,
//            @Valid @RequestBody CreateMedicalRecordRequest request) {
//
//        request.setAppointmentId(appointmentId);
//
//        MedicalRecordResponse response = appointmentService.completeAppointmentWithMedicalRecord(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//
//    @PutMapping("/{appointmentId}/update-medical-record")
//    @PreAuthorize("hasRole('DOCTOR')")
//    public ResponseEntity<MedicalRecordResponse> updateMedicalRecordForAppointment(
//            @PathVariable UUID appointmentId,
//            @Valid @RequestBody UpdateMedicalRecordRequest request) {
//
//        MedicalRecordResponse response = appointmentService.updateMedicalRecordForAppointment(appointmentId, request);
//        return ResponseEntity.ok(response);
//    }
}
