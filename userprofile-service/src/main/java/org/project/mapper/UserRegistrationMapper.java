//package org.project.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.project.appointment_project.user.dto.request.BaseUserRegistrationRequest;
//import org.project.appointment_project.user.dto.request.DoctorRegistrationRequest;
//import org.project.appointment_project.user.dto.request.PatientRegistrationRequest;
//import org.project.appointment_project.user.dto.response.UserRegistrationResponse;
//import org.project.appointment_project.user.model.MedicalProfile;
//import org.project.appointment_project.user.model.User;
//import org.project.appointment_project.user.model.UserProfile;
//
//@Mapper(componentModel = "spring")
//public interface UserRegistrationMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "passwordHash", ignore = true)
//    @Mapping(target = "isActive", constant = "true")
//    @Mapping(target = "isEmailVerified", constant = "true")
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "userRoles", ignore = true)
//    @Mapping(target = "userProfile", ignore = true)
//    @Mapping(target = "medicalProfile", ignore = true)
//    @Mapping(target = "passwordResetTokens", ignore = true)
//    User toUser(BaseUserRegistrationRequest request);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "avatarUrl", ignore = true)
//    UserProfile toUserProfile(BaseUserRegistrationRequest request);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "licenseNumber", ignore = true)
//    @Mapping(target = "specialty", ignore = true)
//    @Mapping(target = "qualification", ignore = true)
//    @Mapping(target = "yearsOfExperience", ignore = true)
//    @Mapping(target = "consultationFee", ignore = true)
//    @Mapping(target = "bio", ignore = true)
//    @Mapping(target = "isDoctorApproved", constant = "false")
//    MedicalProfile toPatientMedicalProfile(PatientRegistrationRequest request);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "bloodType", ignore = true)
//    @Mapping(target = "allergies", ignore = true)
//    @Mapping(target = "medicalHistory", ignore = true)
//    @Mapping(target = "emergencyContactName", ignore = true)
//    @Mapping(target = "emergencyContactPhone", ignore = true)
//    @Mapping(target = "isDoctorApproved", constant = "false")
//    @Mapping(target = "specialty", ignore = true)
//    MedicalProfile toDoctorMedicalProfile(DoctorRegistrationRequest request);
//
//    @Mapping(target = "userId", source = "user.id")
//    @Mapping(target = "username", source = "user.username")
//    @Mapping(target = "email", source = "user.email")
//    @Mapping(target = "firstName", source = "userProfile.firstName")
//    @Mapping(target = "lastName", source = "userProfile.lastName")
//    @Mapping(target = "isActive", source = "user.active")
//    @Mapping(target = "isEmailVerified", source = "user.emailVerified")
//    @Mapping(target = "createdAt", source = "user.createdAt")
//    @Mapping(target = "isDoctorApproved", source = "medicalProfile.doctorApproved")
//    @Mapping(target = "userType", source = "userType")
//    UserRegistrationResponse toRegistrationResponse(User user, UserProfile userProfile,
//                                                    MedicalProfile medicalProfile,
//                                                    String userType);
//
//}
