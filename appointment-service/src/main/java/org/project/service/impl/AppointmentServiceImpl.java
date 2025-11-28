//package org.project.service.impl;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//import org.project.client.AuthServiceClient;
//import org.project.client.PaymentServiceClient;
//import org.project.client.SchedulingServiceClient;
//import org.project.client.UserProfileServiceClient;
//import org.project.config.AppointmentKafkaTopics;
//import org.project.dto.PageResponse;
//import org.project.dto.request.CreateAppointmentRequest;
//import org.project.dto.request.CreatePaymentRequest;
//import org.project.dto.request.SlotReservationRequest;
//import org.project.dto.response.AppointmentDtoResponse;
//import org.project.dto.response.AppointmentInternalResponse;
//import org.project.dto.response.AppointmentResponse;
//import org.project.dto.response.DoctorResponse;
//import org.project.dto.response.PaymentUrlResponse;
//import org.project.dto.response.SlotDetailsResponse;
//import org.project.dto.response.SlotReservationResponse;
//import org.project.dto.response.UserValidationResponse;
//import org.project.enums.PaymentMethod;
//import org.project.enums.PaymentType;
//import org.project.enums.SagaStatus;
//import org.project.enums.Status;
//import org.project.events.AppointmentCreatedEvent;
//import org.project.exception.CustomException;
//import org.project.exception.ErrorCode;
//import org.project.mapper.AppointmentMapper;
//import org.project.mapper.PageMapper;
//import org.project.model.Appointment;
//import org.project.model.AppointmentSagaState;
//import org.project.producer.AppointmentEventProducer;
//import org.project.repository.AppointmentRepository;
//import org.project.repository.SagaStateRepository;
//import org.project.service.AppointmentService;
//import org.project.service.AppointmentValidator;
//import org.project.service.OutboxService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class AppointmentServiceImpl implements AppointmentService {
//
//    AppointmentRepository appointmentRepository;
//    SagaStateRepository sagaStateRepository;
//    OutboxService outboxService;
//    AppointmentValidator validator;
//    AppointmentFactory factory;;
//    SchedulingServiceClient schedulingServiceClient;
//    AppointmentKafkaTopics topics;
//    AuthServiceClient authServiceClient;
//    UserProfileServiceClient userProfileServiceClient;
//    PaymentServiceClient paymentServiceClient;
//    AppointmentEventProducer appointmentEventProducer;
//
//    PageMapper pageMapper;
//    AppointmentMapper appointmentMapper;
//
//    Executor validationExecutor;
//
//    @Override
//    public PageResponse<AppointmentDtoResponse> getUserAppointmentsByStatus(
//            UUID patientId,
//            List<Status> statuses,
//            Pageable pageable) {
//
//        Page<Appointment> appointments = appointmentRepository
//                .findByUserIdAndStatusIn(patientId, statuses, pageable);
//
//        return pageMapper.toPageResponse(appointments, appointmentMapper::toDto);
//    }
//
//    @Override
//    public PageResponse<AppointmentDtoResponse> getDoctorAppointmentsByStatus(
//            UUID doctorId,
//            List<Status> statuses,
//            Pageable pageable) {
//
//        Page<Appointment> appointments = appointmentRepository
//                .findByDoctorIdAndStatusIn(doctorId, statuses, pageable);
//
//        return pageMapper.toPageResponse(appointments,appointmentMapper::toDto);
//    }
//
//    @Transactional
//    @Override
//    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
//        String sagaId = UUID.randomUUID().toString();
//        log.info("Bắt đầu tạo appointment: sagaId={}", sagaId);
//        validatePatientInfo(request);
//
//        boolean isGuestBooking = request.getPatientId() == null;
//        UUID effectivePatientId;
//        String patientName;
//        String patientEmail;
//        String patientPhone;
//
//        if (isGuestBooking) {
//            effectivePatientId = UUID.randomUUID();
//            patientName = request.getPatientName();
//            patientEmail = request.getPatientEmail();
//            patientPhone = request.getPatientPhone();
//            log.info("Guest booking: tạo patientId mới={}, name={}, phone={}",
//                    effectivePatientId, patientName, patientPhone);
//        } else {
//            effectivePatientId = request.getPatientId();
//            patientName = request.getPatientName();
//            patientEmail = request.getPatientEmail();
//            patientPhone = request.getPatientPhone();
//            log.info("Registered user booking: patientId={}", effectivePatientId);
//        }
//
//        DoctorResponse doctorValidation = validateInParallel(
//                isGuestBooking ? null : effectivePatientId,
//                request.getDoctorId()
//        );
//
//        SlotDetailsResponse slotDetails = schedulingServiceClient.getSlotDetails(request.getSlotId());
//
//        if (slotDetails == null) {
//            log.error("Slot không tồn tại: {}", request.getSlotId());
//            throw new CustomException(ErrorCode.SLOT_NOT_FOUND);
//        }
//
//        if (!slotDetails.isAvailable()) {
//            log.error("Slot đã được đặt: {}", request.getSlotId());
//            throw new CustomException(ErrorCode.SLOT_NOT_AVAILABLE);
//        }
//
//        // Validate slot thuộc đúng doctor
//        if (!slotDetails.getDoctorId().equals(request.getDoctorId())) {
//            log.error("Slot không thuộc bác sĩ này");
//            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
//        }
//
//        // Validate slot chưa qua thời gian
//        LocalDateTime slotDateTime = LocalDateTime.of(slotDetails.getSlotDate(), slotDetails.getStartTime());
//        if (slotDateTime.isBefore(LocalDateTime.now())) {
//            log.error("Slot đã qua thời gian");
//            throw new CustomException(ErrorCode.SLOT_IN_PAST);
//        }
//
//        // Chỉ check overlapping cho registered user, guest không check
//        if (!isGuestBooking) {
//            boolean hasOverlapping = appointmentRepository.existsOverlappingAppointment(
//                    effectivePatientId,
//                    slotDetails.getSlotDate(),
//                    slotDetails.getStartTime(),
//                    slotDetails.getEndTime()
//            );
//
//            if (hasOverlapping) {
//                log.error("Patient đã có lịch hẹn trùng thời gian");
//                throw new CustomException(ErrorCode.PATIENT_OVERLAPPING_APPOINTMENT);
//            }
//        }
//
//        SlotReservationRequest reservationRequest = SlotReservationRequest.builder()
//                .slotId(request.getSlotId())
//                .doctorId(request.getDoctorId())
//                .patientId(effectivePatientId)
//                .build();
//
//        SlotReservationResponse reservationResponse = schedulingServiceClient.reserveSlot(reservationRequest);
//
//        if (!reservationResponse.isSuccess()) {
//            log.error("Không thể reserve slot: {}", reservationResponse.getMessage());
//            throw new CustomException(ErrorCode.SLOT_ALREADY_BOOKED);
//        }
//
//        BigDecimal consultationFee = doctorValidation.getConsultationFee();
//        if (consultationFee == null) {
//            log.error("Doctor {} không có consultation fee", request.getDoctorId());
//            throw new CustomException(ErrorCode.CONSULTATION_FEE_NOT_FOUND);
//        }
//
//
//        Appointment appointment = Appointment.builder()
//                .doctorUserId(request.getDoctorId())
//                .doctorName(doctorValidation.getFullName())
//                .doctorPhone(doctorValidation.getPhone())
//                .specialtyName(doctorValidation.getSpecialtyName())
//                .patientUserId(effectivePatientId)
//                .patientName(patientName)
//                .patientEmail(patientEmail)
//                .patientPhone(patientPhone)
//                .slotId(request.getSlotId())
//                .appointmentDate(slotDetails.getSlotDate())
//                .startTime(slotDetails.getStartTime())
//                .endTime(slotDetails.getEndTime())
//                .consultationFee(consultationFee)
//                .notes(buildNotesWithBookingType(request.getNotes(), isGuestBooking))
//                .status(Status.PENDING)
//                .build();
//
//        appointment = appointmentRepository.save(appointment);
//        log.info("Đã tạo appointment: id={}, type={}",
//                appointment.getId(), isGuestBooking ? "GUEST" : "REGISTERED");
//
//        AppointmentSagaState sagaState = AppointmentSagaState.builder()
//                .sagaId(sagaId)
//                .appointmentId(appointment.getId())
//                .status(SagaStatus.DOCTOR_VALIDATED)
//                .currentStep("DOCTOR_VALIDATED")
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        sagaStateRepository.save(sagaState);
//
//
//        if (!isGuestBooking) {
//            AppointmentCreatedEvent event = AppointmentCreatedEvent.builder()
//                    .sagaId(sagaId)
//                    .appointmentId(appointment.getId())
//                    .doctorUserId(request.getDoctorId())
//                    .patientUserId(effectivePatientId)
//                    .slotId(request.getSlotId())
//                    .appointmentDate(slotDetails.getSlotDate())
//                    .startTime(slotDetails.getStartTime())
//                    .endTime(slotDetails.getEndTime())
//                    .notes(request.getNotes())
//                    .timestamp(LocalDateTime.now())
//                    .build();
//
//            String partitionKey = appointment.getId().toString();
//            kafkaTemplate.send(topics.getAppointmentCreated(), partitionKey, event);
//
//            log.info("Đã publish AppointmentCreatedEvent để auth-service lấy thông tin patient: sagaId={}, appointmentId={}, partitionKey={}",
//                    sagaId, appointment.getId(), partitionKey);
//        } else {
//            log.info("Guest booking - bỏ qua publish AppointmentCreatedEvent");
//        }
//
//        PaymentUrlResponse paymentUrlResponse = createPaymentUrl(appointment.getId(), appointment.getConsultationFee());
//
//        AppointmentResponse response = getAppointment(appointment.getId());
//        response.setPaymentUrl(paymentUrlResponse.getPaymentUrl());
//        response.setPaymentId(paymentUrlResponse.getPaymentId());
//
//        return response;
//    }
//
//
//    private PaymentUrlResponse createPaymentUrl(UUID appointmentId, BigDecimal consultationFee) {
//        try {
//            CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
//                    .appointmentId(appointmentId)
//                    .paymentType(PaymentType.FULL)
//                    .paymentMethod(PaymentMethod.VNPAY)
//                    .consultationFee(consultationFee)
//                    .notes("Thanh toán phí khám bệnh")
//                    .build();
//
//            log.info("Tạo payment URL cho appointment: {}, consultationFee: {}", appointmentId, consultationFee);
//            PaymentUrlResponse paymentUrlResponse = paymentServiceClient.createPayment(paymentRequest);
//
//            log.info("Đã tạo payment URL thành công cho appointment: {}", appointmentId);
//            return paymentUrlResponse;
//
//        } catch (Exception e) {
//            log.error("Lỗi khi tạo payment URL cho appointment: {}", appointmentId, e);
//            return PaymentUrlResponse.builder()
//                    .paymentUrl(null)
//                    .message("Không thể tạo thanh toán. Vui lòng thử lại sau.")
//                    .build();
//        }
//    }
//
//    @Override
//    public AppointmentResponse getAppointment(UUID appointmentId) {
//        Appointment appointment = appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
//
//        return AppointmentResponse.builder()
//                .appointmentId(appointment.getId())
//                .doctorId(appointment.getDoctorUserId())
//                .doctorName(appointment.getDoctorName())
//                .doctorPhone(appointment.getDoctorPhone())
//                .specialtyName(appointment.getSpecialtyName())
//                .patientId(appointment.getPatientUserId())
//                .patientName(appointment.getPatientName())
//                .patientEmail(appointment.getPatientEmail())
//                .patientPhone(appointment.getPatientPhone())
//                .appointmentDate(appointment.getAppointmentDate())
//                .startTime(appointment.getStartTime())
//                .endTime(appointment.getEndTime())
//                .consultationFee(appointment.getConsultationFee())
//                .status(appointment.getStatus())
//                .notes(appointment.getNotes())
//                .doctorNotes(appointment.getDoctorNotes())
//                .createdAt(appointment.getCreatedAt())
//                .updatedAt(appointment.getUpdatedAt())
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public AppointmentResponse cancelAppointment(UUID appointmentId, String reason) {
//        Appointment appointment = appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
//
//        if (appointment.getStatus() == Status.CANCELLED) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST, "Appointment already cancelled");
//        }
//
//        if (appointment.getStatus() == Status.CANCELLING) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST, "Appointment cancellation is already in progress");
//        }
//
//        if (appointment.getStatus() == Status.COMPLETED) {
//            throw new CustomException(ErrorCode.INVALID_REQUEST, "Cannot cancel completed appointment");
//        }
//
//        appointment.setStatus(Status.CANCELLING);
//        appointment.setNotes(reason);
//        appointment.setUpdatedAt(LocalDateTime.now());
//
//        appointmentRepository.save(appointment);
//
//        try {
//            appointmentEventProducer.publishAppointmentCancellationInitiated(
//                    appointmentId,
//                    appointment.getPatientUserId(),
//                    appointment.getDoctorUserId(),
//                    appointment.getSlotId(),
//                    reason,
//                    "USER",
//                    appointment.getAppointmentDate(),
//                    appointment.getStartTime().toString()
//            );
//        } catch (Exception e) {
//            log.error("Failed to publish cancellation initiated event for appointment: {}", appointmentId, e);
//        }
//
//        return getAppointment(appointmentId);
//    }
//
//    @Override
//    @Transactional
//    public void updateAppointmentRefundStatus(UUID appointmentId, boolean refundSuccess,
//                                            BigDecimal refundAmount, String refundType) {
//        Appointment appointment = appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
//
//        appointment.setUpdatedAt(LocalDateTime.now());
//
//        // Nếu refund thành công và appointment đang ở trạng thái CANCELLING, chuyển sang CANCELLED
//        if (refundSuccess && appointment.getStatus() == Status.CANCELLING) {
//            appointment.setStatus(Status.CANCELLED);
//            appointmentRepository.save(appointment);
//
//            // Publish event cancelled để scheduling service có thể release slot
//            try {
//                appointmentEventProducer.publishAppointmentCancelled(
//                        appointmentId,
//                        appointment.getPatientUserId(),
//                        appointment.getDoctorUserId(),
//                        appointment.getSlotId(),
//                        appointment.getNotes(),
//                        "SYSTEM",
//                        appointment.getAppointmentDate(),
//                        appointment.getStartTime()
//                );
//                log.info("Published appointment cancelled event after successful refund for appointment: {}", appointmentId);
//            } catch (Exception e) {
//                log.error("Failed to publish appointment cancelled event after refund for appointment: {}", appointmentId, e);
//            }
//        } else {
//            appointmentRepository.save(appointment);
//        }
//
//        log.info("Updated appointment {} refund status: success={}, amount={}, type={}",
//                appointmentId, refundSuccess, refundAmount, refundType);
//    }
//
//    @Override
//    public boolean existsOverlappingAppointment(UUID patientId, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {
//        return appointmentRepository.existsOverlappingAppointment(patientId, appointmentDate, startTime, endTime);
//    }
//
//    @Override
//    public long countPendingAppointmentsByPatient(UUID patientId) {
//        return appointmentRepository.countPendingAppointmentsByPatient(patientId);
//    }
//
//    @Override
//    public PageResponse<AppointmentResponse> getAppointments(UUID userId, Status status, Pageable pageable) {
//        return null;
//    }
//
//    @Override
//    public List<AppointmentInternalResponse> getAffectedFullDay(UUID doctorId, LocalDate date) {
//        return List.of();
//    }
//
//    @Override
//    public List<AppointmentInternalResponse> getAffectedByTimeRange(UUID doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
//        return List.of();
//    }
//
//    private DoctorResponse validateInParallel(UUID patientId, UUID doctorId) {
//        CompletableFuture<DoctorResponse> doctorValidation = CompletableFuture.supplyAsync(() ->
//            validateDoctorSync(doctorId), validationExecutor
//        );
//
//        CompletableFuture<Void> patientValidation = null;
//        if (patientId != null) {
//            patientValidation = CompletableFuture.runAsync(() ->
//                validatePatientSync(patientId), validationExecutor
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
//    /**
//     * Validate thông tin patient trong request
//     * Phải có patientId HOẶC (patientName + patientPhone + patientEmail)
//     */
//    private void validatePatientInfo(CreateAppointmentRequest request) {
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
//
//
//    private String buildNotesWithBookingType(String originalNotes, boolean isGuestBooking) {
//        String bookingTypeMarker = isGuestBooking ? "[GUEST_BOOKING]" : "[REGISTERED_USER]";
//        if (originalNotes == null || originalNotes.trim().isEmpty()) {
//            return bookingTypeMarker;
//        }
//        return bookingTypeMarker + " " + originalNotes;
//    }
//
//}
