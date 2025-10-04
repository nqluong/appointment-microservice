//package org.project.mapper;
//
//import lombok.RequiredArgsConstructor;
//
//import org.project.dto.response.DoctorSearchResponse;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class DoctorSearchMapper {
//
//    private final DoctorScheduleMapper doctorScheduleMapper;
//
//    public DoctorSearchResponse toDoctorSearchResponse(User user, List<DoctorSchedule> schedules) {
//        return DoctorSearchResponse.builder()
//                .id(user.getId())
//                .fullName(user.getUserProfile().getFirstName() + " " + user.getUserProfile().getLastName())
//                .email(user.getEmail())
//                .phoneNumber(user.getUserProfile().getPhone())
//                .profilePhotoUrl(user.getUserProfile().getAvatarUrl())
//                .specialty(user.getMedicalProfile() != null && user.getMedicalProfile().getSpecialty() != null ?
//                        toSpecialtyResponse(user.getMedicalProfile().getSpecialty()) : null)
//                .qualification(user.getMedicalProfile() != null ? user.getMedicalProfile().getQualification() : null)
//                .yearsOfExperience(user.getMedicalProfile() != null ? user.getMedicalProfile().getYearsOfExperience() : null)
//                .consultationFee(user.getMedicalProfile() != null ? user.getMedicalProfile().getConsultationFee() : null)
//                .bio(user.getMedicalProfile() != null ? user.getMedicalProfile().getBio() : null)
//                .isDoctorApproved(user.getMedicalProfile() != null ? user.getMedicalProfile().isDoctorApproved() : false)
//                .weeklySchedule(doctorScheduleMapper.toScheduleEntryResponseList(schedules))
//                .build();
//    }
//
//    public SpecialtyResponse toSpecialtyResponse(Specialty specialty) {
//        if (specialty == null) {
//            return null;
//        }
//
//        return SpecialtyResponse.builder()
//                .specialtyId(specialty.getId())
//                .name(specialty.getName())
//                .description(specialty.getDescription())
//                .build();
//    }
//}
