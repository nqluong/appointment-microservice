package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.PaymentServiceClient;
import org.project.dto.PageResponse;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.response.*;
import org.project.enums.SagaStatus;
import org.project.enums.Status;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCreatedEvent;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.mapper.AppointmentMapper;
import org.project.mapper.PageMapper;
import org.project.model.Appointment;
import org.project.model.AppointmentSagaState;
import org.project.producer.AppointmentEventProducer;
import org.project.repository.AppointmentRepository;
import org.project.repository.SagaStateRepository;
import org.project.service.AppointmentService;
import org.project.service.AppointmentValidator;
import org.project.service.OutboxService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService2 implements AppointmentService {

    AppointmentRepository appointmentRepository;
    SagaStateRepository sagaStateRepository;
    OutboxService outboxService;

    AppointmentValidator validator;
    AppointmentFactory factory;
    PaymentServiceClient paymentServiceClient;

    PageMapper pageMapper;
    AppointmentMapper appointmentMapper;
    Executor validationExecutor;
    AppointmentEventProducer eventProducer;

    @Override
    public PageResponse<AppointmentDtoResponse> getUserAppointmentsByStatus(
            UUID patientId, List<Status> statuses, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository
                .findByUserIdAndStatusIn(patientId, statuses, pageable);
        return pageMapper.toPageResponse(appointments, appointmentMapper::toDto);
    }

    @Override
    public PageResponse<AppointmentDtoResponse> getDoctorAppointmentsByStatus(
            UUID doctorId, List<Status> statuses, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndStatusIn(doctorId, statuses, pageable);
        return pageMapper.toPageResponse(appointments, appointmentMapper::toDto);
    }

    @Override
    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        return factory.toResponse(appointment);
    }

    @Override
    public boolean existsOverlappingAppointment(UUID patientId, LocalDate date,
                                                LocalTime startTime, LocalTime endTime) {
        return appointmentRepository.existsOverlappingAppointment(
                patientId, date, startTime, endTime);
    }

    @Override
    public long countPendingAppointmentsByPatient(UUID patientId) {
        return appointmentRepository.countPendingAppointmentsByPatient(patientId);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        UUID sagaId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        log.info("Creating appointment: sagaId={}, eventId={}", sagaId, eventId);

        // Validate
        validator.validatePatientInfo(request);

        // Determine booking type
        boolean isGuestBooking = request.getPatientId() == null;
        UUID effectivePatientId = isGuestBooking
                ? UUID.randomUUID()
                : request.getPatientId();

        log.info("Booking type: {}, patientId={}",
                isGuestBooking ? "GUEST" : "REGISTERED", effectivePatientId);

        // Validate in parallel
        DoctorResponse doctor = validateInParallel(
                isGuestBooking ? null : effectivePatientId,
                request.getDoctorId()
        );

        // Validate and reserve slot
        SlotDetailsResponse slot = validator.validateAndReserveSlot(
                request.getSlotId(),
                request.getDoctorId(),
                effectivePatientId
        );

        // Check overlapping (registered users only)
        if (!isGuestBooking) {
            validator.checkOverlappingAppointment(
                    effectivePatientId,
                    slot.getSlotDate(),
                    slot.getStartTime(),
                    slot.getEndTime()
            );
        }

        // Create appointment
        Appointment appointment = factory.createAppointment(
                request, effectivePatientId, doctor, slot, isGuestBooking
        );
        appointment = appointmentRepository.save(appointment);

        // Create saga state với sagaId
        createSagaState(sagaId.toString(), appointment.getId());
        
        // Create event với eventId và sagaId
        AppointmentCreatedEvent event = factory.createAppointmentCreatedEvent(
                eventId, sagaId, appointment, slot
        );

        outboxService.saveEvent(
                eventId.toString(),
                "APPOINTMENT",
                appointment.getId(),
                "APPOINTMENT_CREATED",
                event
        );
        
        final AppointmentCreatedEvent finalEvent = event;
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                       eventProducer.publishAppointmentCreated(finalEvent);
                    }
                }
        );
        // Create payment URL
        PaymentUrlResponse payment = createPaymentUrl(
                appointment.getId(),
                appointment.getConsultationFee()
        );

        // Build response
        AppointmentResponse response = factory.toResponse(appointment);
        response.setPaymentUrl(payment.getPaymentUrl());
        response.setPaymentId(payment.getPaymentId());

        log.info("Appointment created: id={}, type={}",
                appointment.getId(), isGuestBooking ? "GUEST" : "REGISTERED");

        return response;
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, String reason) {
        log.info("Cancelling appointment: id={}", appointmentId);

        Appointment appointment = findAppointment(appointmentId);
        validator.validateCancellation(appointment);

        // Update status
        appointment.setStatus(Status.CANCELLING);
        appointment.setNotes(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        UUID sagaId = getSagaId(appointmentId);
        UUID eventId = UUID.randomUUID();
        
        AppointmentCancellationInitiatedEvent event = factory.createCancellationInitiatedEvent(
                eventId, sagaId, appointment, reason, "USER"
        );

        outboxService.saveEvent(
                eventId.toString(),
                "APPOINTMENT",
                appointment.getId(),
                "APPOINTMENT_CANCELLATION_INITIATED",
                event
        );

        final AppointmentCancellationInitiatedEvent finalEvent = event;
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishAppointmentCancellationInitiated(finalEvent);
                    }
                }
        );
        
        log.info("Cancellation initiated: appointmentId={}, sagaId={}, eventId={}", 
                appointmentId, sagaId, eventId);
        return factory.toResponse(appointment);
    }

    @Override
    public void updateAppointmentRefundStatus(UUID appointmentId, boolean refundSuccess, BigDecimal refundAmount, String refundType) {

    }

    private UUID getSagaId(UUID appointmentId) {
        return sagaStateRepository.findByAppointmentId(appointmentId)
                .map(saga -> UUID.fromString(saga.getSagaId()))
                .orElse(null);
    }

    private DoctorResponse validateInParallel(UUID patientId, UUID doctorId) {
        CompletableFuture<DoctorResponse> doctorFuture = CompletableFuture
                .supplyAsync(() -> validator.validateDoctor(doctorId), validationExecutor);

        CompletableFuture<Void> patientFuture = null;
        if (patientId != null) {
            patientFuture = CompletableFuture
                    .runAsync(() -> validator.validatePatient(patientId), validationExecutor);
        }

        try {
            if (patientFuture != null) {
                CompletableFuture.allOf(patientFuture, doctorFuture).join();
            } else {
                doctorFuture.join();
            }
            return doctorFuture.get();

        } catch (Exception e) {
            if (e.getCause() instanceof CustomException ce) {
                throw ce;
            }
            log.error("Validation failed", e);
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private void createSagaState(String sagaId, UUID appointmentId) {
        AppointmentSagaState saga = AppointmentSagaState.builder()
                .sagaId(sagaId)
                .appointmentId(appointmentId)
                .status(SagaStatus.DOCTOR_VALIDATED)
                .currentStep("DOCTOR_VALIDATED")
                .createdAt(LocalDateTime.now())
                .build();
        sagaStateRepository.save(saga);
    }

    private PaymentUrlResponse createPaymentUrl(UUID appointmentId, java.math.BigDecimal fee) {
        try {
            var request = factory.createPaymentRequest(appointmentId, fee);
            return paymentServiceClient.createPayment(request);
        } catch (Exception e) {
            log.error("Failed to create payment URL: appointmentId={}", appointmentId, e);
            return PaymentUrlResponse.builder()
                    .paymentUrl(null)
                    .message("Cannot create payment. Please try again.")
                    .build();
        }
    }

    private Appointment findAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }

    @Override
    public PageResponse<AppointmentResponse> getAppointments(UUID userId, Status status,
                                                             Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AppointmentInternalResponse> getAffectedFullDay(UUID doctorId, LocalDate date) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AppointmentInternalResponse> getAffectedByTimeRange(
            UUID doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
