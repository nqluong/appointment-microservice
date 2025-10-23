package org.project.service.impl;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.project.exception.CustomException;
import org.project.repository.MedicalProfileRepository;
import org.project.service.DoctorService;
import org.project.service.MedicalProfileService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicalProfileServiceImpl implements MedicalProfileService {
    MedicalProfileRepository medicalProfileRepository;
    DoctorService doctorService;

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
    public DoctorResponse validateDoctorForAppointment(UUID doctorId) {
        MedicalProfileResponse profile = medicalProfileRepository.findByUserId(doctorId)
                .orElseThrow(() -> new CustomException("Doctor not found: " + doctorId));

        // Chuyển đổi MedicalProfileResponse thành DoctorResponse
        return DoctorResponse.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFirstName() + " " + profile.getLastName())
                .email(null) // Email không có trong MedicalProfileResponse, có thể cần lấy từ user service
                .gender(profile.getGender() != null ? profile.getGender().toString() : null)
                .phone(profile.getPhone())
                .avatarUrl(profile.getUrlAvatar() != null ? profile.getUrlAvatar() : null)
                .qualification(profile.getQualification())
                .yearsOfExperience(profile.getYearsOfExperience())
                .consultationFee(profile.getConsultationFee())
                .specialtyName(profile.getSpecialtyName())
                .approved(profile.isDoctorApproved())
                .build();
    }

    @Override
    public PageResponse<DoctorResponse> getDoctors(Pageable pageable) {
        log.info("Lấy danh sách bác sĩ với phân trang cho internal API");
        return doctorService.getAllDoctors(pageable);
    }

    @Override
    public PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, Pageable pageable) {
        log.info("Lấy danh sách bác sĩ theo chuyên khoa {} với phân trang cho internal API", specialtyId);
        return doctorService.getDoctorsBySpecialtyId(specialtyId, pageable);
    }

}
