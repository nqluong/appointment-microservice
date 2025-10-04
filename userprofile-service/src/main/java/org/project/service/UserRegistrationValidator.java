package org.project.service;

import org.project.dto.request.DoctorRegistrationRequest;
import org.project.dto.request.PatientRegistrationRequest;

public interface UserRegistrationValidator {
    void validatePatientRegistration(PatientRegistrationRequest request);
    void validateDoctorRegistration(DoctorRegistrationRequest request);
}
