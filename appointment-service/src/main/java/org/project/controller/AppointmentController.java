//package org.project.controller;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.project.appointment_project.appoinment.dto.request.CancelAppointmentRequest;
//import org.project.appointment_project.appoinment.dto.request.CreateAppointmentRequest;
//import org.project.appointment_project.appoinment.dto.request.UpdateAppointmentStatusRequest;
//import org.project.appointment_project.appoinment.dto.response.AppointmentResponse;
//import org.project.appointment_project.appoinment.enums.Status;
//import org.project.appointment_project.appoinment.service.AppointmentService;
//import org.project.appointment_project.common.dto.PageResponse;
//import org.project.appointment_project.common.security.annotation.RequireOwnershipOrAdmin;
//import org.project.appointment_project.medicalrecord.dto.request.CreateMedicalRecordRequest;
//import org.project.appointment_project.medicalrecord.dto.request.UpdateMedicalRecordRequest;
//import org.project.appointment_project.medicalrecord.dto.response.MedicalRecordResponse;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/appointments")
//@Slf4j
//public class AppointmentController {
//    private final AppointmentService appointmentService;
//
//    @PostMapping
//    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
//        AppointmentResponse appointmentResponse = appointmentService.createAppointment(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentResponse);
//    }
//
//    @GetMapping
//    @RequireOwnershipOrAdmin
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
//}
