package org.project.service;

import org.project.dto.response.DoctorValidationResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface MedicalProfileService {
    @Transactional(readOnly = true)
    MedicalProfileResponse getMedicalProfileByUserId(UUID userId);

    @Transactional(readOnly = true)
    DoctorValidationResponse validateDoctorForAppointment(UUID doctorId);
}
