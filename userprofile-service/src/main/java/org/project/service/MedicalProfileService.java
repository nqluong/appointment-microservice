package org.project.service;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface MedicalProfileService {
    @Transactional(readOnly = true)
    MedicalProfileResponse getMedicalProfileByUserId(UUID userId);

    @Transactional(readOnly = true)
    DoctorResponse validateDoctorForAppointment(UUID doctorId);
    
    /**
     * Lấy danh sách bác sĩ với phân trang cho internal API
     */
    @Transactional(readOnly = true)
    PageResponse<DoctorResponse> getDoctors(Pageable pageable);
    
    /**
     * Lấy danh sách bác sĩ theo chuyên khoa với phân trang cho internal API
     */
    @Transactional(readOnly = true)
    PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, Pageable pageable);
}
