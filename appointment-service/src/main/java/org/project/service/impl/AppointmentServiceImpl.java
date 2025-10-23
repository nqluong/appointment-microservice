package org.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.project.client.AuthServiceClient;
import org.project.client.PaymentServiceClient;
import org.project.client.SchedulingServiceClient;
import org.project.client.UserProfileServiceClient;
import org.project.config.AppointmentKafkaTopics;
import org.project.dto.PageResponse;
import org.project.dto.events.AppointmentCreatedEvent;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.AppointmentDtoResponse;
import org.project.dto.response.AppointmentInternalResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.PaymentUrlResponse;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.UserValidationResponse;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentType;
import org.project.enums.SagaStatus;
import org.project.enums.Status;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.mapper.AppointmentMapper;
import org.project.mapper.PageMapper;
import org.project.model.Appointment;
import org.project.model.AppointmentSagaState;
import org.project.repository.AppointmentRepository;
import org.project.repository.SagaStateRepository;
import org.project.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentServiceImpl implements AppointmentService {

    AppointmentRepository appointmentRepository;
    SagaStateRepository sagaStateRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    SchedulingServiceClient schedulingServiceClient;
    AppointmentKafkaTopics topics;
    AuthServiceClient authServiceClient;
    UserProfileServiceClient userProfileServiceClient;
    PaymentServiceClient paymentServiceClient;

    PageMapper pageMapper;
    AppointmentMapper appointmentMapper;
    
    Executor validationExecutor;

    @Override
    public PageResponse<AppointmentDtoResponse> getUserAppointmentsByStatus(
            UUID patientId,
            List<Status> statuses,
            Pageable pageable) {

        Page<Appointment> appointments = appointmentRepository
                .findByUserIdAndStatusIn(patientId, statuses, pageable);

        return pageMapper.toPageResponse(appointments, appointmentMapper::toDto);
    }

    @Override
    public PageResponse<AppointmentDtoResponse> getDoctorAppointmentsByStatus(
            UUID doctorId,
            List<Status> statuses,
            Pageable pageable) {

        Page<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndStatusIn(doctorId, statuses, pageable);

        return pageMapper.toPageResponse(appointments,appointmentMapper::toDto);
    }

    @Transactional
    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Bắt đầu tạo appointment: sagaId={}", sagaId);

        DoctorResponse doctorValidation = validateInParallel(request.getPatientId(), request.getDoctorId());

        SlotDetailsResponse slotDetails = schedulingServiceClient.getSlotDetails(request.getSlotId());
        
        if (slotDetails == null) {
            log.error("Slot không tồn tại: {}", request.getSlotId());
            throw new CustomException(ErrorCode.SLOT_NOT_FOUND);
        }
        
        if (!slotDetails.isAvailable()) {
            log.error("Slot đã được đặt: {}", request.getSlotId());
            throw new CustomException(ErrorCode.SLOT_NOT_AVAILABLE);
        }
        
        // Validate slot thuộc đúng doctor
        if (!slotDetails.getDoctorId().equals(request.getDoctorId())) {
            log.error("Slot không thuộc bác sĩ này");
            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
        }
        
        // Validate slot chưa qua thời gian
        LocalDateTime slotDateTime = LocalDateTime.of(slotDetails.getSlotDate(), slotDetails.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            log.error("Slot đã qua thời gian");
            throw new CustomException(ErrorCode.SLOT_IN_PAST);
        }

        boolean hasOverlapping = appointmentRepository.existsOverlappingAppointment(
                request.getPatientId(),
                slotDetails.getSlotDate(),
                slotDetails.getStartTime(),
                slotDetails.getEndTime()
        );
        
        if (hasOverlapping) {
            log.error("Patient đã có lịch hẹn trùng thời gian");
            throw new CustomException(ErrorCode.PATIENT_OVERLAPPING_APPOINTMENT);
        }

        SlotReservationRequest reservationRequest = SlotReservationRequest.builder()
                .slotId(request.getSlotId())
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .build();
        
        SlotReservationResponse reservationResponse = schedulingServiceClient.reserveSlot(reservationRequest);
        
        if (!reservationResponse.isSuccess()) {
            log.error("Không thể reserve slot: {}", reservationResponse.getMessage());
            throw new CustomException(ErrorCode.SLOT_ALREADY_BOOKED);
        }

        BigDecimal consultationFee = doctorValidation.getConsultationFee();
        if (consultationFee == null) {
            log.error("Doctor {} không có consultation fee", request.getDoctorId());
            throw new CustomException(ErrorCode.CONSULTATION_FEE_NOT_FOUND);
        }


        Appointment appointment = Appointment.builder()
                .doctorUserId(request.getDoctorId())
                .doctorName(doctorValidation.getFullName())
                .doctorPhone(doctorValidation.getPhone())
                .specialtyName(doctorValidation.getSpecialtyName())
                .patientUserId(request.getPatientId())
                .slotId(request.getSlotId())
                .appointmentDate(slotDetails.getSlotDate())
                .startTime(slotDetails.getStartTime())
                .endTime(slotDetails.getEndTime())
                .consultationFee(consultationFee)
                .notes(request.getNotes())
                .status(Status.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Đã tạo appointment: id={}", appointment.getId());

        AppointmentSagaState sagaState = AppointmentSagaState.builder()
                .sagaId(sagaId)
                .appointmentId(appointment.getId())
                .status(SagaStatus.DOCTOR_VALIDATED)
                .currentStep("DOCTOR_VALIDATED")
                .createdAt(LocalDateTime.now())
                .build();

        sagaStateRepository.save(sagaState);

        // Publish AppointmentCreatedEvent để auth-service có thể lấy thông tin patient
        AppointmentCreatedEvent event = AppointmentCreatedEvent.builder()
                .sagaId(sagaId)
                .appointmentId(appointment.getId())
                .doctorUserId(request.getDoctorId())
                .patientUserId(request.getPatientId())
                .slotId(request.getSlotId())
                .appointmentDate(slotDetails.getSlotDate())
                .startTime(slotDetails.getStartTime())
                .endTime(slotDetails.getEndTime())
                .notes(request.getNotes())
                .timestamp(LocalDateTime.now())
                .build();

        String partitionKey = appointment.getId().toString();
        kafkaTemplate.send(topics.getAppointmentCreated(), partitionKey, event);

        log.info("Đã publish AppointmentCreatedEvent để auth-service lấy thông tin patient: sagaId={}, appointmentId={}, partitionKey={}",
                sagaId, appointment.getId(), partitionKey);

        PaymentUrlResponse paymentUrlResponse = createPaymentUrl(appointment.getId(), appointment.getConsultationFee());

        AppointmentResponse response = getAppointment(appointment.getId());
        response.setPaymentUrl(paymentUrlResponse.getPaymentUrl());
        response.setPaymentId(paymentUrlResponse.getPaymentId());

        return response;
    }


    private PaymentUrlResponse createPaymentUrl(UUID appointmentId, BigDecimal consultationFee) {
        try {
            CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                    .appointmentId(appointmentId)
                    .paymentType(PaymentType.FULL)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .consultationFee(consultationFee)
                    .notes("Thanh toán phí khám bệnh")
                    .build();

            log.info("Tạo payment URL cho appointment: {}, consultationFee: {}", appointmentId, consultationFee);
            PaymentUrlResponse paymentUrlResponse = paymentServiceClient.createPayment(paymentRequest);

            log.info("Đã tạo payment URL thành công cho appointment: {}", appointmentId);
            return paymentUrlResponse;

        } catch (Exception e) {
            log.error("Lỗi khi tạo payment URL cho appointment: {}", appointmentId, e);
            return PaymentUrlResponse.builder()
                    .paymentUrl(null)
                    .message("Không thể tạo URL thanh toán. Vui lòng thử lại sau.")
                    .build();
        }
    }

    @Override
    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

        return AppointmentResponse.builder()
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctorUserId())
                .doctorName(appointment.getDoctorName())
                .doctorPhone(appointment.getDoctorPhone())
                .specialtyName(appointment.getSpecialtyName())
                .patientId(appointment.getPatientUserId())
                .patientName(appointment.getPatientName())
                .patientEmail(appointment.getPatientEmail())
                .patientPhone(appointment.getPatientPhone())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .consultationFee(appointment.getConsultationFee())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .doctorNotes(appointment.getDoctorNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    @Override
    public boolean existsOverlappingAppointment(UUID patientId, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {
        return appointmentRepository.existsOverlappingAppointment(patientId, appointmentDate, startTime, endTime);
    }

    @Override
    public long countPendingAppointmentsByPatient(UUID patientId) {
        return appointmentRepository.countPendingAppointmentsByPatient(patientId);
    }

    @Override
    public PageResponse<AppointmentResponse> getAppointments(UUID userId, Status status, Pageable pageable) {
//        try {
//            if (userId != null) {
//                validateUserExists(userId);
//            }
//
//            Page<Appointment> appointmentsPage = getAppointmentsPage(userId, status, pageable);
//
//           return pageMapper.toPageResponse(appointmentsPage, appointmentMapper::toResponse);
//
//        } catch (CustomException e) {
//            log.error("Failed to get appointments: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error while getting appointments", e);
//            throw new CustomException(ErrorCode.APPOINTMENT_FETCH_FAILED);
//        }
        return null;
    }

//    @Override
//    @Transactional
//    public AppointmentResponse updateAppointmentStatus(UUID appointmentId, Status newStatus) {
//        try {
//            // Lấy appointment với lock
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//
//            validateStatusTransition(appointment.getStatus(), newStatus);
//
//            // Cập nhật status
//            appointment.setStatus(newStatus);
//            appointment = appointmentRepository.save(appointment);
//
//            handleStatusSideEffects(appointment, newStatus);
//
//            log.info("Successfully updated appointment {} status to {}", appointmentId, newStatus);
//            return appointmentMapper.toResponse(appointment);
//
//        } catch (CustomException e) {
//            log.error("Failed to update appointment status: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error while updating appointment status", e);
//            throw new CustomException(ErrorCode.APPOINTMENT_STATUS_UPDATE_FAILED);
//        }
//    }
//
//    @Transactional
//    @Override
//    public AppointmentResponse completeAppointment(UUID appointmentId) {
//        try {
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//
//            // Validate có thể complete không
//            validateAppointmentCanBeCompleted(appointment);
//
//            // Cập nhật status thành COMPLETED
//            appointment.setStatus(Status.COMPLETED);
//            appointment = appointmentRepository.save(appointment);
//
//
//            log.info("Successfully completed appointment {}", appointmentId);
//            return appointmentMapper.toResponse(appointment);
//
//        } catch (CustomException e) {
//            log.error("Failed to complete appointment: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error while completing appointment", e);
//            throw new CustomException(ErrorCode.APPOINTMENT_COMPLETION_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public AppointmentResponse cancelAppointment(UUID appointmentId, String reason) {
//        try {
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//
//            // Validate có thể cancel không
//            validateAppointmentCanBeCancelled(appointment);
//
//            appointment.setStatus(Status.CANCELLED);
//            if (reason != null && !reason.trim().isEmpty()) {
//                String currentNotes = appointment.getNotes();
//                String cancelReason = "CANCELLED: " + reason.trim();
//                appointment.setNotes(currentNotes == null ? cancelReason : currentNotes + " | " + cancelReason);
//            }
//
//            appointment = appointmentRepository.save(appointment);
//
//            // Giải phóng slot để có thể book lại
//            if (appointment.getSlot() != null) {
//                slotStatusService.releaseSlot(appointment.getSlot().getId());
//            }
//
//            log.info("Successfully cancelled appointment {}", appointmentId);
//            return appointmentMapper.toResponse(appointment);
//
//        } catch (CustomException e) {
//            log.error("Failed to cancel appointment: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error while cancelling appointment", e);
//            throw new CustomException(ErrorCode.APPOINTMENT_CANCELLATION_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public AppointmentResponse startExamination(UUID appointmentId) {
//        try {
//            log.info("Bắt đầu quá trình khám bệnh cho cuộc hẹn {}", appointmentId);
//
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//
//            validateAppointmentCanStartExamination(appointment);
//
//            appointment.setStatus(Status.IN_PROGRESS);
//            appointment = appointmentRepository.save(appointment);
//
//            return appointmentMapper.toResponse(appointment);
//
//        } catch (CustomException e) {
//            log.error("Không thể bắt đầu khám bệnh: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Lỗi không mong đợi khi bắt đầu khám bệnh cho cuộc hẹn {}", appointmentId, e);
//            throw new CustomException(ErrorCode.EXAMINATION_START_FAILED);
//        }
//    }

//    @Override
//    @Transactional
//    public MedicalRecordResponse completeAppointmentWithMedicalRecord(CreateMedicalRecordRequest request) {
//        try {
//            UUID appointmentId = request.getAppointmentId();
//
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//
//            validateAppointmentCanBeCompletedWithMedicalRecord(appointment);
//
//            MedicalRecordResponse medicalRecordResponse = medicalRecordService.createMedicalRecord(request);
//
//            appointment.setStatus(Status.COMPLETED);
//
//            if (request.getDoctorNotes() != null && !request.getDoctorNotes().trim().isEmpty()) {
//                appointment.setDoctorNotes(request.getDoctorNotes());
//            }
//
//            appointment = appointmentRepository.save(appointment);
//
//            return medicalRecordResponse;
//
//        } catch (CustomException e) {
//            log.error("Không thể hoàn thành cuộc hẹn với hồ sơ bệnh án: {} - {}",
//                    e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Lỗi không mong đợi khi hoàn thành cuộc hẹn với hồ sơ bệnh án", e);
//            throw new CustomException(ErrorCode.APPOINTMENT_COMPLETION_WITH_MEDICAL_RECORD_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public MedicalRecordResponse updateMedicalRecordForAppointment(UUID appointmentId, UpdateMedicalRecordRequest request) {
//        try {
//            log.info("Cập nhật hồ sơ bệnh án cho cuộc hẹn {}", appointmentId);
//
//            Appointment appointment = getAppointmentWithLock(appointmentId);
//            validateAppointmentForMedicalRecordUpdate(appointment);
//
//            if (!medicalRecordService.hasMedicalRecord(appointmentId)) {
//                throw new CustomException(ErrorCode.MEDICAL_RECORD_NOT_FOUND);
//            }
//
//            MedicalRecordResponse currentRecord = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
//
//            MedicalRecordResponse updatedRecord = medicalRecordService.updateMedicalRecord(
//                    currentRecord.getId(), request);
//
//            // Cập nhật doctor notes trong appointment nếu có
//            if (request.getDoctorNotes() != null && !request.getDoctorNotes().trim().isEmpty()) {
//                appointment.setDoctorNotes(request.getDoctorNotes());
//                appointmentRepository.save(appointment);
//            }
//
//            return updatedRecord;
//
//        } catch (CustomException e) {
//            log.error("Không thể cập nhật hồ sơ bệnh án: {} - {}", e.getErrorCode().getCode(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Lỗi không mong đợi khi cập nhật hồ sơ bệnh án cho cuộc hẹn {}", appointmentId, e);
//            throw new CustomException(ErrorCode.MEDICAL_RECORD_UPDATE_FAILED);
//        }
//    }


    @Override
    public List<AppointmentInternalResponse> getAffectedFullDay(UUID doctorId, LocalDate date) {
        return List.of();
    }

    @Override
    public List<AppointmentInternalResponse> getAffectedByTimeRange(UUID doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return List.of();
    }

    private void validateAppointmentCanStartExamination(Appointment appointment) {
        if (appointment.getStatus() != Status.CONFIRMED) {
            throw new CustomException(ErrorCode.APPOINTMENT_NOT_CONFIRMED);
        }
    }

    /**
     * Kiểm tra appointment có thể hoàn thành với hồ sơ bệnh án không
     * Chỉ appointment ở trạng thái IN_PROGRESS mới có thể hoàn thành
     */
//    private void validateAppointmentCanBeCompletedWithMedicalRecord(Appointment appointment) {
//        if (appointment.getStatus() != Status.IN_PROGRESS) {
//            log.warn("Cuộc hẹn {} không thể hoàn thành - trạng thái hiện tại: {}",
//                    appointment.getId(), appointment.getStatus());
//            throw new CustomException(ErrorCode.APPOINTMENT_NOT_IN_PROGRESS);
//        }
//
//        // Kiểm tra xem đã có medical record chưa
//        if (medicalRecordService.hasMedicalRecord(appointment.getId())) {
//            log.warn("Cuộc hẹn {} đã có hồ sơ bệnh án", appointment.getId());
//            throw new CustomException(ErrorCode.MEDICAL_RECORD_ALREADY_EXISTS);
//        }
//    }

    /**
     * Kiểm tra appointment có thể cập nhật hồ sơ bệnh án không
     * Chỉ appointment đã COMPLETED mới có thể cập nhật medical record
     */
//    private void validateAppointmentForMedicalRecordUpdate(Appointment appointment) {
//        if (appointment.getStatus() != Status.COMPLETED) {
//            log.warn("Không thể cập nhật hồ sơ bệnh án cho cuộc hẹn {} - trạng thái hiện tại: {}",
//                    appointment.getId(), appointment.getStatus());
//            throw new CustomException(ErrorCode.APPOINTMENT_NOT_COMPLETED);
//        }
//    }
//
//
//    //Lấy và lock slot để tránh concurrent access
//    private DoctorAvailableSlot getAndLockSlot(UUID slotId) {
//        return slotRepository.findByIdWithLock(slotId)
//                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));
//    }
//
//     //Lấy thông tin user
//    private User getUser(UUID userId, ErrorCode errorCode) {
//        return userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(errorCode));
//    }


//    //Tạo entity Appointment từ request
//    private Appointment createAppointmentEntity(CreateAppointmentRequest request,
//                                                User doctor, User patient, DoctorAvailableSlot slot) {
//        return Appointment.builder()
//                .doctor(doctor)
//                .patient(patient)
//                .slot(slot)
//                .consultationFee(doctor.getMedicalProfile().getConsultationFee())
//                .appointmentDate(slot.getSlotDate())
//                .status(Status.PENDING)
//                .notes(request.getNotes())
//                .build();
//    }
    private Appointment getAppointmentWithLock(UUID appointmentId) {
        return appointmentRepository.findByIdWithLock(appointmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }


    private DoctorResponse validateInParallel(UUID patientId, UUID doctorId) {

        CompletableFuture<Void> patientValidation = CompletableFuture.runAsync(() -> 
            validatePatientSync(patientId), validationExecutor
        );
        
        CompletableFuture<DoctorResponse> doctorValidation = CompletableFuture.supplyAsync(() ->
            validateDoctorSync(doctorId), validationExecutor
        );
        
        try {
            CompletableFuture.allOf(patientValidation, doctorValidation).join();
            return doctorValidation.get();
            
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof CustomException) {
                throw (CustomException) cause;
            }
            log.error("Validation failed with unexpected error", e);
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private void validatePatientSync(UUID patientId) {
        UserValidationResponse validation = authServiceClient.validateUser(patientId, "PATIENT");
        
        if (!validation.isValid()) {
            log.error("Patient không tồn tại: {}", patientId);
            throw new CustomException(ErrorCode.PATIENT_NOT_FOUND);
        }
        
        if (!validation.isActive()) {
            log.error("Patient không active: {}", patientId);
            throw new CustomException(ErrorCode.PATIENT_INACTIVE);
        }
        
        if (!validation.isHasRole()) {
            log.error("User không có role PATIENT: {}", patientId);
            throw new CustomException(ErrorCode.PATIENT_NO_ROLE);
        }
        
        log.info("Patient {} đã được validate thành công", patientId);
    }


    private DoctorResponse validateDoctorSync(UUID doctorId) {
        // Validate user tồn tại và active
        UserValidationResponse validation = authServiceClient.validateUser(doctorId, "DOCTOR");
        
        if (!validation.isValid()) {
            log.error("Doctor không tồn tại: {}", doctorId);
            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
        }
        
        if (!validation.isActive()) {
            log.error("Doctor không active: {}", doctorId);
            throw new CustomException(ErrorCode.DOCTOR_INACTIVE);
        }
        
        if (!validation.isHasRole()) {
            log.error("User không có role DOCTOR: {}", doctorId);
            throw new CustomException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        DoctorResponse doctorValidation = userProfileServiceClient.validateDoctor(doctorId);
        
        if (!doctorValidation.getApproved()) {
            log.error("Doctor chưa được approve: {}", doctorId);
            throw new CustomException(ErrorCode.DOCTOR_NOT_APPROVED);
        }
        
        log.info("Doctor {} đã được validate thành công với consultationFee: {}", 
                doctorId, doctorValidation.getConsultationFee());
        
        return doctorValidation;
    }

    private void validateStatusTransition(Status currentStatus, Status newStatus) {
        if (currentStatus == newStatus) {
            throw new CustomException(ErrorCode.APPOINTMENT_STATUS_ALREADY_SET);
        }

        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == Status.CONFIRMED || newStatus == Status.CANCELLED;
            case CONFIRMED -> newStatus == Status.COMPLETED || newStatus == Status.CANCELLED;
            case COMPLETED -> false;
            case CANCELLED -> false;
            default -> false;
        };

        if (!isValidTransition) {
            log.warn("Invalid status transition from {} to {}", currentStatus, newStatus);
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void validateAppointmentCanBeCompleted(Appointment appointment) {
        // Chỉ appointment CONFIRMED mới có thể complete
        if (appointment.getStatus() != Status.CONFIRMED) {
            throw new CustomException(ErrorCode.APPOINTMENT_NOT_CONFIRMED);
        }

        // - Kiểm tra appointment date đã qua chưa

        if (appointment.getDoctorNotes() == null || appointment.getDoctorNotes().trim().isEmpty()) {
            throw new CustomException(ErrorCode.DOCTOR_NOTES_REQUIRED);
        }
    }

    private void validateAppointmentCanBeCancelled(Appointment appointment) {
        if (appointment.getStatus() == Status.COMPLETED) {
            throw new CustomException(ErrorCode.APPOINTMENT_ALREADY_COMPLETED);
        }

        if (appointment.getStatus() == Status.CANCELLED) {
            throw new CustomException(ErrorCode.APPOINTMENT_ALREADY_CANCELLED);
        }
    }

//    private void handleStatusSideEffects(Appointment appointment, Status newStatus) {
//        switch (newStatus) {
//            case CONFIRMED -> {
//                log.debug("Appointment {} confirmed, sending notifications", appointment.getId());
//            }
//            case COMPLETED -> {
//                log.debug("Appointment {} completed, slot released", appointment.getId());
//            }
//            case CANCELLED -> {
//                if (appointment.getSlot() != null) {
//                    slotStatusService.releaseSlot(appointment.getSlot().getId());
//                }
//                log.debug("Appointment {} cancelled, slot released", appointment.getId());
//            }
//            default -> {
//                // No side effects for other statuses
//            }
//        }
//    }
//
//    private void validateUserExists(UUID userId) {
//        if (!userRepository.existsById(userId)) {
//            throw new CustomException(ErrorCode.USER_NOT_FOUND);
//        }
//    }

//    private Page<Appointment> getAppointmentsPage(UUID userId, Status status, Pageable pageable) {
//
//        if (userId == null) {
//            log.debug("Fetching all appointments for admin with status filter: {}", status);
//            return appointmentRepository.findAllAppointmentsByStatus(status, pageable);
//        } else {
//            log.debug("Fetching appointments for user {} with status filter: {}", userId, status);
//            return appointmentRepository.findAppointmentsByUserIdAndStatus(userId, status, pageable);
//        }
//    }

}
