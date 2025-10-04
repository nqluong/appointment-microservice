package org.project.service;


import org.project.dto.request.DoctorRegistrationRequest;
import org.project.dto.request.PatientRegistrationRequest;
import org.project.dto.response.UserRegistrationResponse;

public interface UserRegistrationService {
    UserRegistrationResponse registerPatient(PatientRegistrationRequest request);

    UserRegistrationResponse registerDoctor(DoctorRegistrationRequest request);
}
