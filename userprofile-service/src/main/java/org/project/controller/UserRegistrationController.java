package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.project.dto.request.DoctorRegistrationRequest;
import org.project.dto.request.PatientRegistrationRequest;
import org.project.dto.response.UserRegistrationResponse;
import org.project.service.UserRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/register")
@RequiredArgsConstructor
public class UserRegistrationController {
//    private final UserRegistrationService userRegistrationService;
//
//    @PostMapping("/patient")
//    public ResponseEntity<UserRegistrationResponse> registerPatient(
//            @Valid @RequestBody PatientRegistrationRequest request) {
//
//        UserRegistrationResponse response = userRegistrationService.registerPatient(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @PostMapping("/doctor")
//    public ResponseEntity<UserRegistrationResponse> registerDoctor(
//            @Valid @RequestBody DoctorRegistrationRequest request) {
//
//        UserRegistrationResponse response = userRegistrationService.registerDoctor(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
}
