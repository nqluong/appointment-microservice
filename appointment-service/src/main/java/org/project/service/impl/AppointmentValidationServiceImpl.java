//package org.project.service.impl;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.project.appointment_project.appoinment.repository.AppointmentRepository;
//import org.project.appointment_project.appoinment.service.AppointmentValidationService;
//import org.project.appointment_project.common.exception.CustomException;
//import org.project.appointment_project.common.exception.ErrorCode;
//import org.project.appointment_project.schedule.model.DoctorAvailableSlot;
//import org.project.appointment_project.user.model.User;
//import org.project.appointment_project.user.model.UserRole;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class AppointmentValidationServiceImpl implements AppointmentValidationService {
//
//    AppointmentRepository appointmentRepository;
//
//    static int MAX_PENDING_APPOINTMENTS = 3;
//
//
//    //Validate slot có hợp lệ để đặt lịch không
//    @Override
//    public void validateSlotForBooking(DoctorAvailableSlot slot, UUID doctorId) {
//        log.debug("Validating slot {} for doctor {}", slot.getId(), doctorId);
//
//        // Kiểm tra slot có thuộc về bác sĩ không
//        if (!slot.getDoctor().getId().equals(doctorId)) {
//            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
//        }
//
//        // Kiểm tra slot có available không
//        if (!slot.isAvailable()) {
//            throw new CustomException(ErrorCode.SLOT_NOT_AVAILABLE);
//        }
//
//        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
//        if (slotDateTime.isBefore(LocalDateTime.now())) {
//            throw new CustomException(ErrorCode.SLOT_IN_PAST);
//        }
//    }
//
//
//    // Validate bệnh nhân có hợp lệ để đặt lịch không
//    @Override
//    public void validatePatientForBooking(User patient, DoctorAvailableSlot slot) {
//        log.debug("Validating patient {} for booking", patient.getId());
//
//        if (!patient.isActive()) {
//            throw new CustomException(ErrorCode.PATIENT_INACTIVE);
//        }
//
//        boolean hasPatientRole = patient.getUserRoles().stream()
//                .filter(UserRole::isActive)
//                .anyMatch(ur -> "PATIENT".equals(ur.getRole().getName()));
//
//        if (!hasPatientRole) {
//            throw new CustomException(ErrorCode.PATIENT_NO_ROLE);
//        }
//
//        validateNoOverlappingAppointment(patient.getId(), slot);
//
//        validatePendingAppointmentLimit(patient.getId());
//    }
//
//
//     //Validate bác sĩ có hợp lệ không
//    @Override
//    public void validateDoctorForBooking(User doctor) {
//        log.debug("Validating doctor {} for booking", doctor.getId());
//
//        if (!doctor.isActive()) {
//            throw new CustomException(ErrorCode.DOCTOR_INACTIVE);
//        }
//
//
//        boolean hasDoctorRole = doctor.getUserRoles().stream()
//                .filter(UserRole::isActive)
//                .anyMatch(ur -> "DOCTOR".equals(ur.getRole().getName()));
//
//        if (!hasDoctorRole) {
//            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
//        }
//
//
//        if (doctor.getMedicalProfile() == null || !doctor.getMedicalProfile().isDoctorApproved()) {
//            throw new CustomException(ErrorCode.DOCTOR_NOT_APPROVED);
//        }
//    }
//
//
//    // Kiểm tra bệnh nhân có lịch hẹn trùng thời gian không
//    private void validateNoOverlappingAppointment(UUID patientId, DoctorAvailableSlot slot) {
//        boolean hasOverlapping = appointmentRepository.existsOverlappingAppointment(
//                patientId,
//                slot.getSlotDate(),
//                slot.getStartTime(),
//                slot.getEndTime()
//        );
//
//        if (hasOverlapping) {
//            throw new CustomException(ErrorCode.PATIENT_OVERLAPPING_APPOINTMENT);
//        }
//    }
//
//
//     // Kiểm tra giới hạn số lượng lịch hẹn pending
//    private void validatePendingAppointmentLimit(UUID patientId) {
//        long pendingCount = appointmentRepository.countPendingAppointmentsByPatient(patientId);
//
//        if (pendingCount >= MAX_PENDING_APPOINTMENTS) {
//            throw new CustomException(ErrorCode.PATIENT_TOO_MANY_PENDING);
//        }
//    }
//}
