package org.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.project.dto.response.AppointmentDtoResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.model.Appointment;


@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(source = "id", target = "appointmentId")
    @Mapping(source = "doctorUserId", target = "doctorId")
    @Mapping(source = "patientUserId", target = "patientId")
    AppointmentDtoResponse toDto(Appointment appointment);

//    @Mapping(source = "id", target = "appointmentId")
//    @Mapping(source = "doctor.id", target = "doctorId")
//    @Mapping(source = "doctor", target = "doctorName", qualifiedByName = "buildFullName")
//    @Mapping(source = "doctor", target = "specialtyName", qualifiedByName = "getSpecialtyName")
//    @Mapping(source = "patient.id", target = "patientId")
//    @Mapping(source = "patient", target = "patientName", qualifiedByName = "buildFullName")
//    @Mapping(source = "slot.slotDate", target = "appointmentDate")
//    @Mapping(source = "slot.startTime", target = "startTime")
//    @Mapping(source = "slot.endTime", target = "endTime")
//    AppointmentResponse toResponse(Appointment appointment);
//
//    @Named("buildFullName")
//    default String buildFullName(User user) {
//        if (user == null || user.getUserProfile() == null) {
//            return "";
//        }
//
//        String firstName = user.getUserProfile().getFirstName();
//        String lastName = user.getUserProfile().getLastName();
//
//        if (firstName != null && lastName != null) {
//            String cleanedLastName = lastName.replace("BS.", "").trim();
//            return "BS. " + firstName + " " + cleanedLastName;
//        }
//        return "BS. " + user.getUsername();
//    }
//
//    @Named("getSpecialtyName")
//    default String getSpecialtyName(User doctor) {
//        if (doctor == null ||
//                doctor.getMedicalProfile() == null ||
//                doctor.getMedicalProfile().getSpecialty() == null) {
//            return null;
//        }
//        return doctor.getMedicalProfile().getSpecialty().getName();
//    }
}
