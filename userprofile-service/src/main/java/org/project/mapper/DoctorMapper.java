//package org.project.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.NullValuePropertyMappingStrategy;
//import org.mapstruct.ReportingPolicy;
//import org.project.dto.response.DoctorResponse;
//
//import java.util.List;
//
//@Mapper(
//        componentModel = "spring",
//        unmappedTargetPolicy = ReportingPolicy.IGNORE,
//        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
//)
//public interface DoctorMapper {
//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
//    @Mapping(target = "avatarUrl", source = "userProfile.avatarUrl")
//    @Mapping(target = "qualification", source = "medicalProfile.qualification")
//    @Mapping(target = "consultationFee", source = "medicalProfile.consultationFee")
//    @Mapping(target = "yearsOfExperience", source = "medicalProfile.yearsOfExperience")
//    @Mapping(target = "gender", expression = "java(getGenderString(user))")
//    @Mapping(target = "phone", source = "userProfile.phone")
//    @Mapping(target = "specialtyName", source = "medicalProfile.specialty.name")
//    DoctorResponse toResponse(User user);
//
//    List<DoctorResponse> toResponseList(List<User> users);
//
//    default String buildFullName(User user) {
//        if (user.getUserProfile() == null) {
//            return null;
//        }
//        String firstName = user.getUserProfile().getFirstName();
//        String lastName = user.getUserProfile().getLastName();
//
//        if (firstName != null && lastName != null) {
//            return firstName + " " + lastName;
//        } else if (firstName != null) {
//            return firstName;
//        } else if (lastName != null) {
//            return lastName;
//        }
//        return null;
//    }
//
//    default String getGenderString(User user) {
//        if (user.getUserProfile() == null || user.getUserProfile().getGender() == null) {
//            return null;
//        }
//        return user.getUserProfile().getGender().toString();
//    }
//}
