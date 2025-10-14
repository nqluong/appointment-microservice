package org.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.dto.response.AppointmentInternalResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/appointments")
public class InternalAppointmentController {
    private final AppointmentService appointmentService;

//    @GetMapping("/affected/full-day")
//    public ResponseEntity<List<AppointmentInternalResponse>> getAffectedFullDay(
//            @RequestParam UUID doctorId,
//            @RequestParam LocalDate date
//            ){
//        List<AppointmentInternalResponse> responses = appointmentService.getAffectedFullDay(doctorId, date);
//        return ResponseEntity.ok(responses);
//    }
//    @GetMapping("/affected/full-day")
//    public ResponseEntity<List<AppointmentInternalResponse>> getAffectedByTimeRange(
//            @RequestParam UUID doctorId,
//            @RequestParam LocalDate date,
//            @RequestParam LocalTime startTime,
//            @RequestParam LocalTime endTime
//    ){
//
//        List<AppointmentInternalResponse> responses = appointmentService.getAffectedByTimeRange(doctorId, date, startTime, endTime);
//        return ResponseEntity.ok(responses);
//    }
    @GetMapping("/check-overlapping")
    public ResponseEntity<Boolean> checkOverlappingAppointment(
            @RequestParam UUID patientId,
            @RequestParam LocalDate appointmentDate,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {

        boolean hasOverlapping = appointmentService.existsOverlappingAppointment(patientId, appointmentDate, startTime, endTime);
        return ResponseEntity.ok(hasOverlapping);
    }

    @GetMapping("/count-pending/{patientId}")
    public ResponseEntity<Integer> countPendingAppointments(@PathVariable UUID patientId) {
        long count = appointmentService.countPendingAppointmentsByPatient(patientId);
        return ResponseEntity.ok((int) count);
    }

}
