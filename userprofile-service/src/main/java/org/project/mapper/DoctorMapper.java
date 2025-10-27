package org.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.project.dto.response.DoctorResponse;
import org.project.repository.DoctorProjection;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorMapper {


    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "qualification", source = "qualification")
    @Mapping(target = "consultationFee", source = "consultationFee")
    @Mapping(target = "yearsOfExperience", source = "yearsOfExperience")
    @Mapping(target = "specialtyName", source = "specialtyName")
    DoctorResponse projectionToResponse(DoctorProjection projection);

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
}
