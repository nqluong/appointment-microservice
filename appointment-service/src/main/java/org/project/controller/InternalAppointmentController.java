package org.project.controller;

import java.util.UUID;

import org.project.dto.response.AppointmentResponse;
import org.project.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/appointments")
@RequiredArgsConstructor
@Slf4j
public class InternalAppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Internal API để payment service lấy thông tin appointment
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable UUID appointmentId) {
        log.info("Internal API: Get appointment {}", appointmentId);
        AppointmentResponse response = appointmentService.getAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }
}
