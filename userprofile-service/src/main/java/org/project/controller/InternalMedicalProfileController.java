package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.DoctorValidationResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.project.service.MedicalProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/medical-profile")
@RequiredArgsConstructor
@Slf4j
public class InternalMedicalProfileController {
    private final MedicalProfileService medicalProfileService;

    /**
     * Get medical profile by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<MedicalProfileResponse> getMedicalProfileByUserId(
            @PathVariable UUID userId) {

        log.debug("Getting medical profile for user {}", userId);

        MedicalProfileResponse response = medicalProfileService.getMedicalProfileByUserId(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate doctor cho appointment booking
     * Trả về cả consultation fee luôn để tránh gọi thêm lần nữa
     */
    @GetMapping("/validate-doctor/{doctorId}")
    public ResponseEntity<DoctorValidationResponse> validateDoctor(
            @PathVariable UUID doctorId) {

        log.info("Validating doctor {} for appointment", doctorId);

        DoctorValidationResponse response = medicalProfileService.validateDoctorForAppointment(doctorId);

        return ResponseEntity.ok(response);
    }
}
