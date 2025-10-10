package org.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.service.AppointmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/appointments")
public class InternalAppointmentController {
    private final AppointmentService appointmentService;


}
