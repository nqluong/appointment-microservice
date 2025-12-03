package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.AuthServiceClient;
import org.project.client.SchedulingServiceClient;
import org.project.client.UserProfileServiceClient;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.UserValidationResponse;
import org.project.enums.Status;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Appointment;
import org.project.repository.AppointmentRepository;
import org.project.service.AppointmentValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentValidatorImpl implements AppointmentValidator {
    AppointmentRepository appointmentRepository;
    AuthServiceClient authServiceClient;
    SchedulingServiceClient schedulingServiceClient;
    UserProfileServiceClient userProfileServiceClient;
    Executor validationExecutor;

    @Override
    public void validatePatientInfo(CreateAppointmentRequest request) {
        boolean hasPatientId = request.getPatientId() != null;
        boolean hasGuestInfo = isValidGuestInfo(request);

        if (!hasPatientId && !hasGuestInfo) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Must provide patientId or complete guest info (name, phone, email)");
        }

        if (hasPatientId && hasGuestInfo) {
            log.warn("Request has both patientId and guest info, will use patientId");
        }
    }

    @Override
    public void validatePatient(UUID patientId) {
        UserValidationResponse validation = authServiceClient.validateUser(patientId, "PATIENT");

        if (!validation.isValid()) {
            throw new CustomException(ErrorCode.PATIENT_NOT_FOUND);
        }

        if (!validation.isActive()) {
            throw new CustomException(ErrorCode.PATIENT_INACTIVE);
        }

        if (!validation.isHasRole()) {
            throw new CustomException(ErrorCode.PATIENT_NO_ROLE);
        }

        log.info("Patient validated: {}", patientId);
    }

    @Override
    public DoctorResponse validateDoctor(UUID doctorId) {
        UserValidationResponse validation = authServiceClient.validateUser(doctorId, "DOCTOR");

        if (!validation.isValid()) {
            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        if (!validation.isActive()) {
            throw new CustomException(ErrorCode.DOCTOR_INACTIVE);
        }

        if (!validation.isHasRole()) {
            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        DoctorResponse doctor = userProfileServiceClient.validateDoctor(doctorId);

        if (!doctor.getApproved()) {
            throw new CustomException(ErrorCode.DOCTOR_NOT_APPROVED);
        }

        if (doctor.getConsultationFee() == null) {
            throw new CustomException(ErrorCode.CONSULTATION_FEE_NOT_FOUND);
        }

        log.info("Doctor validated: {}, fee: {}", doctorId, doctor.getConsultationFee());
        return doctor;
    }

    @Override
    public SlotDetailsResponse validateAndReserveSlot(UUID slotId, UUID doctorId, UUID patientId) {
        SlotDetailsResponse slot = schedulingServiceClient.getSlotDetails(slotId);

        if (slot == null) {
            throw new CustomException(ErrorCode.SLOT_NOT_FOUND);
        }

        if (!slot.isAvailable()) {
            throw new CustomException(ErrorCode.SLOT_NOT_AVAILABLE);
        }

        if (!slot.getDoctorId().equals(doctorId)) {
            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
        }

        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SLOT_IN_PAST);
        }

        // Reserve slot
        SlotReservationRequest request = SlotReservationRequest.builder()
                .slotId(slotId)
                .doctorId(doctorId)
                .patientId(patientId)
                .build();

        SlotReservationResponse response = schedulingServiceClient.reserveSlot(request);

        if (!response.isSuccess()) {
            throw new CustomException(ErrorCode.SLOT_ALREADY_BOOKED);
        }

        return slot;
    }

    @Override
    public void checkOverlappingAppointment(UUID patientId, LocalDate date,
                                            LocalTime startTime, LocalTime endTime) {
        boolean hasOverlap = appointmentRepository.existsOverlappingAppointment(
                patientId, date, startTime, endTime
        );

        if (hasOverlap) {
            throw new CustomException(ErrorCode.PATIENT_OVERLAPPING_APPOINTMENT);
        }
    }

    @Override
    public void validateCancellation(Appointment appointment) {
        if (appointment.getStatus() == Status.CANCELLED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Appointment already cancelled");
        }

        if (appointment.getStatus() == Status.CANCELLING) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Cancellation already in progress");
        }

        if (appointment.getStatus() == Status.COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Cannot cancel completed appointment");
        }
    }

    private boolean isValidGuestInfo(CreateAppointmentRequest request) {
        return request.getPatientName() != null && !request.getPatientName().trim().isEmpty()
                && request.getPatientPhone() != null && !request.getPatientPhone().trim().isEmpty()
                && request.getPatientEmail() != null && !request.getPatientEmail().trim().isEmpty();
    }

//    @Override
//    public void validatePatientInfo(CreateAppointmentRequest request) {
//        boolean hasPatientId = request.getPatientId() != null;
//        boolean hasGuestInfo = request.getPatientName() != null
//                && !request.getPatientName().trim().isEmpty()
//                && request.getPatientPhone() != null
//                && !request.getPatientPhone().trim().isEmpty()
//                && request.getPatientEmail() != null
//                && !request.getPatientEmail().trim().isEmpty();
//
//        if (!hasPatientId && !hasGuestInfo) {
//            log.error("Request thiếu thông tin patient: cần có patientId hoặc (patientName + patientPhone + patientEmail)");
//            throw new CustomException(ErrorCode.INVALID_REQUEST,
//                    "Cần cung cấp patientId hoặc đầy đủ thông tin (patientName, patientPhone, patientEmail)");
//        }
//
//        if (hasPatientId && hasGuestInfo) {
//            log.warn("Request có cả patientId và guest info, sẽ ưu tiên sử dụng patientId");
//        }
//    }

//    @Override
//    public DoctorResponse validateInParallel(UUID patientId, UUID doctorId) {
//        CompletableFuture<DoctorResponse> doctorValidation = CompletableFuture.supplyAsync(() ->
//                validateDoctorSync(doctorId), validationExecutor
//        );
//
//        CompletableFuture<Void> patientValidation = null;
//        if (patientId != null) {
//            patientValidation = CompletableFuture.runAsync(() ->
//                    validatePatientSync(patientId), validationExecutor
//            );
//        }
//
//        try {
//            if (patientValidation != null) {
//                CompletableFuture.allOf(patientValidation, doctorValidation).join();
//            } else {
//                doctorValidation.join();
//            }
//            return doctorValidation.get();
//
//        } catch (Exception e) {
//            if (e.getCause() instanceof CustomException customException) {
//                throw customException;
//            }
//            log.error("Validation failed with unexpected error", e);
//            throw new CustomException(ErrorCode.VALIDATION_FAILED);
//        }
//    }
//
//    private void validatePatientSync(UUID patientId) {
//        UserValidationResponse validation = authServiceClient.validateUser(patientId, "PATIENT");
//
//        if (!validation.isValid()) {
//            log.error("Patient không tồn tại: {}", patientId);
//            throw new CustomException(ErrorCode.PATIENT_NOT_FOUND);
//        }
//
//        if (!validation.isActive()) {
//            log.error("Patient không active: {}", patientId);
//            throw new CustomException(ErrorCode.PATIENT_INACTIVE);
//        }
//
//        if (!validation.isHasRole()) {
//            log.error("User không có role PATIENT: {}", patientId);
//            throw new CustomException(ErrorCode.PATIENT_NO_ROLE);
//        }
//
//        log.info("Patient {} đã được validate thành công", patientId);
//    }
//
//
//    private DoctorResponse validateDoctorSync(UUID doctorId) {
//        // Validate user tồn tại và active
//        UserValidationResponse validation = authServiceClient.validateUser(doctorId, "DOCTOR");
//
//        if (!validation.isValid()) {
//            log.error("Doctor không tồn tại: {}", doctorId);
//            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
//        }
//
//        if (!validation.isActive()) {
//            log.error("Doctor không active: {}", doctorId);
//            throw new CustomException(ErrorCode.DOCTOR_INACTIVE);
//        }
//
//        if (!validation.isHasRole()) {
//            log.error("User không có role DOCTOR: {}", doctorId);
//            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
//        }
//
//        DoctorResponse doctorValidation = userProfileServiceClient.validateDoctor(doctorId);
//
//        if (!doctorValidation.getApproved()) {
//            log.error("Doctor chưa được approve: {}", doctorId);
//            throw new CustomException(ErrorCode.DOCTOR_NOT_APPROVED);
//        }
//
//        log.info("Doctor {} đã được validate thành công với consultationFee: {}",
//                doctorId, doctorValidation.getConsultationFee());
//
//        return doctorValidation;
//    }
//
//
//
////    public SlotDetailsResponse validateAndReserveSlot(UUID slotId, UUID doctorId, UUID patientId) {
////        // Get slot details
////        SlotDetailsResponse slot = schedulingServiceClient.getSlotDetails(slotId);
////
////        if (slot == null) {
////            throw new CustomException(ErrorCode.SLOT_NOT_FOUND);
////        }
////
////        if (!slot.isAvailable()) {
////            throw new CustomException(ErrorCode.SLOT_NOT_AVAILABLE);
////        }
////
////        if (!slot.getDoctorId().equals(doctorId)) {
////            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
////        }
////
////        // Check if slot is in the past
////        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
////        if (slotDateTime.isBefore(LocalDateTime.now())) {
////            throw new CustomException(ErrorCode.SLOT_IN_PAST);
////        }
////
////        // Reserve slot
////        SlotReservationRequest reservationRequest = SlotReservationRequest.builder()
////                .slotId(slotId)
////                .doctorId(doctorId)
////                .patientId(patientId)
////                .build();
////
////        SlotReservationResponse reservation = schedulingServiceClient.reserveSlot(reservationRequest);
////
////        if (!reservation.isSuccess()) {
////            throw new CustomException(ErrorCode.SLOT_ALREADY_BOOKED);
////        }
////
////        return slot;
////    }
//
//    @Override
//    public void checkOverlappingAppointments(UUID patientId, LocalDate date,
//                                             LocalTime startTime, LocalTime endTime) {
//        boolean hasOverlapping = appointmentRepository.existsOverlappingAppointment(
//                patientId, date, startTime, endTime
//        );
//
//        if (hasOverlapping) {
//            throw new CustomException(ErrorCode.PATIENT_OVERLAPPING_APPOINTMENT);
//        }
//    }
//
//    @Override
//    public void validateCancellation(Appointment appointment) {
//        if (appointment.getStatus() == Status.CANCELLED) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST, "Appointment already cancelled");
//        }
//
//        if (appointment.getStatus() == Status.CANCELLING) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST,
//                    "Appointment cancellation is already in progress");
//        }
//
//        if (appointment.getStatus() == Status.COMPLETED) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST,
//                    "Cannot cancel completed appointment");
//        }
//    }
}
