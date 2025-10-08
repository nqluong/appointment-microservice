package org.project.service.strategy.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.ProfileUpdateRequest;
import org.project.service.strategy.FieldFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorFieldFilterStrategy implements FieldFilterStrategy {

    static final Set<String> ALLOWED_USER_PROFILE_FIELDS = Set.of(
            "firstName", "lastName", "dateOfBirth", "gender", "address", "phone"
    );

    static final Set<String> ALLOWED_MEDICAL_PROFILE_FIELDS = Set.of(
            "bloodType", "allergies", "medicalHistory",
            "emergencyContactName", "emergencyContactPhone",
            "licenseNumber", "qualification", "yearsOfExperience",
            "consultationFee", "bio", "specialtyId"
    );

    @Override
    public ProfileUpdateRequest filterFields(ProfileUpdateRequest request) {
        return ProfileUpdateRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .phone(request.getPhone())
                // Medical profile fields
                .bloodType(request.getBloodType())
                .allergies(request.getAllergies())
                .medicalHistory(request.getMedicalHistory())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .licenseNumber(request.getLicenseNumber())
                .qualification(request.getQualification())
                .yearsOfExperience(request.getYearsOfExperience())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio())
                .specialtyId(request.getSpecialtyId())

                .isDoctorApproved(null) // Doctor không được update field này
                .build();
    }

    @Override
    public boolean supports(Set<String> roles) {
       return roles.contains("DOCTOR");
    }

    @Override
    public Set<String> getAllowedUserProfileFields() {
        return ALLOWED_USER_PROFILE_FIELDS;
    }

    @Override
    public Set<String> getAllowedMedicalProfileFields() {
        return ALLOWED_MEDICAL_PROFILE_FIELDS;
    }
}
