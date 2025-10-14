package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.DoctorValidationResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.project.exception.CustomException;
import org.project.repository.MedicalProfileRepository;
import org.project.service.MedicalProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicalProfileServiceImpl implements MedicalProfileService {
    MedicalProfileRepository medicalProfileRepository;

    @Transactional(readOnly = true)
    @Override
    public MedicalProfileResponse getMedicalProfileByUserId(UUID userId) {
        MedicalProfileResponse profile = medicalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(
                        "Medical profile not found for user: " + userId));

        return profile;
    }

    @Transactional(readOnly = true)
    @Override
    public DoctorValidationResponse validateDoctorForAppointment(UUID doctorId) {
        MedicalProfileResponse profile = medicalProfileRepository.findByUserId(doctorId).orElse(null);

        if (profile == null) {
            return DoctorValidationResponse.builder()
                    .valid(false)
                    .approved(false)
                    .hasLicense(false)
                    .message("Doctor does not have medical profile")
                    .consultationFee(null)
                    .build();
        }

        if (!profile.isDoctorApproved()) {
            return DoctorValidationResponse.builder()
                    .valid(true)
                    .approved(false)
                    .hasLicense(profile.getLicenseNumber() != null)
                    .message("Doctor is not approved yet")
                    .consultationFee(null)
                    .build();
        }

        if (profile.getLicenseNumber() == null || profile.getLicenseNumber().trim().isEmpty()) {
            return DoctorValidationResponse.builder()
                    .valid(true)
                    .approved(true)
                    .hasLicense(false)
                    .message("Doctor does not have license number")
                    .consultationFee(profile.getConsultationFee())
                    .build();
        }

        if (profile.getConsultationFee() == null) {
            return DoctorValidationResponse.builder()
                    .valid(true)
                    .approved(true)
                    .hasLicense(true)
                    .message("Doctor has not set consultation fee")
                    .consultationFee(null)
                    .build();
        }

        return DoctorValidationResponse.builder()
                .valid(true)
                .approved(true)
                .hasLicense(true)
                .message("Doctor validation successful")
                .consultationFee(profile.getConsultationFee())
                .build();
    }
}
