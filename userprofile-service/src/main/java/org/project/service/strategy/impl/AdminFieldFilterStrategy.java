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
public class AdminFieldFilterStrategy implements FieldFilterStrategy {

    private static final Set<String> ALLOWED_USER_PROFILE_FIELDS = Set.of(
            "firstName", "lastName", "dateOfBirth", "gender", "address", "phone"
    );

    private static final Set<String> ALLOWED_MEDICAL_PROFILE_FIELDS = Set.of(
            "bloodType", "allergies", "medicalHistory",
            "emergencyContactName", "emergencyContactPhone",
            "licenseNumber", "qualification", "yearsOfExperience",
            "consultationFee", "bio", "specialtyId", "isDoctorApproved"
    );

    @Override
    public ProfileUpdateRequest filterFields(ProfileUpdateRequest request) {
        return request;
    }

    @Override
    public boolean supports(Set<String> roles) {
        return roles.contains("ADMIN");
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
