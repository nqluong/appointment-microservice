//package org.project.service.impl;
//
//import jakarta.transaction.Transactional;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.project.dto.request.BaseUserRegistrationRequest;
//import org.project.dto.request.DoctorRegistrationRequest;
//import org.project.dto.request.PatientRegistrationRequest;
//import org.project.dto.response.UserRegistrationResponse;
//import org.project.exception.CustomException;
//import org.project.exception.ErrorCode;
//import org.project.model.MedicalProfile;
//import org.project.model.Specialty;
//import org.project.model.UserProfile;
//import org.project.repository.SpecialtyRepository;
//import org.project.service.UserRegistrationService;
//import org.project.service.UserRegistrationValidator;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class UserRegistrationServiceImpl implements UserRegistrationService {
//    SpecialtyRepository specialtyRepository;
//    UserRegistrationValidator userRegistrationValidator;
//    PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public UserRegistrationResponse registerPatient(PatientRegistrationRequest request) {
//        userRegistrationValidator.validatePatientRegistration(request);
//
//        try {
//            User user = createUser(request);
//            User savedUser = userRepository.save(user);
//
//            UserProfile userProfile = createUserProfile(request, savedUser);
//            MedicalProfile medicalProfile = createPatientMedicalProfile(request, savedUser);
//            assignRole(savedUser, "PATIENT");
//            return userRegistrationMapper.toRegistrationResponse(savedUser, userProfile, medicalProfile, "PATIENT");
//        }catch (Exception e){
//            log.error("Error during patient registration: {}", e.getMessage());
//            throw new CustomException(ErrorCode.REGISTRATION_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public UserRegistrationResponse registerDoctor(DoctorRegistrationRequest request) {
//        userRegistrationValidator.validateDoctorRegistration(request);
//
//        try {
//            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.SPECIALTY_NOT_FOUND, "Specialty not found with ID: " + request.getSpecialtyId()));
//
//
//            User user = createUser(request);
//            User savedUser = userRepository.save(user);
//
//            UserProfile userProfile = createUserProfile(request, savedUser);
//
//            MedicalProfile medicalProfile = createDoctorMedicalProfile(request, savedUser, specialty);
//            assignRole(savedUser, "DOCTOR");
//            log.info("Doctor registration completed successfully for user: {}", savedUser.getUsername());
//
//            return userRegistrationMapper.toRegistrationResponse(
//                    savedUser,
//                    userProfile,
//                    medicalProfile,
//                    "DOCTOR"
//            );
//
//        } catch (Exception e) {
//            log.error("Error during doctor registration: {}", e.getMessage());
//           throw new CustomException(ErrorCode.REGISTRATION_FAILED);
//        }
//    }
//
//    private User createUser(BaseUserRegistrationRequest request) {
//        User user = userRegistrationMapper.toUser(request);
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        return user;
//    }
//
//    private UserProfile createUserProfile(BaseUserRegistrationRequest request, User user) {
//        UserProfile userProfile = userRegistrationMapper.toUserProfile(request);
//        userProfile.setUser(user);
//        user.setUserProfile(userProfile);
//        return userProfile;
//    }
//
//    private MedicalProfile createPatientMedicalProfile(PatientRegistrationRequest request, User user) {
//        MedicalProfile medicalProfile = userRegistrationMapper.toPatientMedicalProfile(request);
//        medicalProfile.setUser(user);
//        user.setMedicalProfile(medicalProfile);
//        return medicalProfile;
//    }
//
//    private MedicalProfile createDoctorMedicalProfile(DoctorRegistrationRequest request, User user, Specialty specialty) {
//        MedicalProfile medicalProfile = userRegistrationMapper.toDoctorMedicalProfile(request);
//        medicalProfile.setUser(user);
//        medicalProfile.setLicenseNumber(request.getLicenseNumber());
//        medicalProfile.setSpecialty(specialty);
//        medicalProfile.setQualification(request.getQualification());
//        medicalProfile.setYearsOfExperience(request.getYearsOfExperience());
//        medicalProfile.setConsultationFee(request.getConsultationFee());
//        medicalProfile.setBio(request.getBio());
//        user.setMedicalProfile(medicalProfile);
//        return medicalProfile;
//    }
//
//    private void assignRole(User user, String roleName) {
//        Role role = roleRepository.findByName(roleName)
//                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND, "Role not found: " + roleName));
//        UserRole userRole = UserRole.builder()
//                .user(user)
//                .role(role)
//                .assignedAt(LocalDateTime.now())
//                .isActive(true)
//                .build();
////        userRoleRepositoryJdbc.assignRoleToUserOnRegistration(user.getId(), role.getId());
//        userRoleRepository.save(userRole);
//    }
//}
