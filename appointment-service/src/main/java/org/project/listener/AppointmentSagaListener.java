package org.project.listener;

import java.time.LocalDateTime;

import org.project.config.AppointmentKafkaTopics;
import org.project.dto.events.AppointmentCancelledEvent;
import org.project.dto.events.DoctorValidatedEvent;
import org.project.dto.events.PatientValidatedEvent;
import org.project.dto.events.ValidationFailedEvent;
import org.project.enums.SagaStatus;
import org.project.enums.Status;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Appointment;
import org.project.model.AppointmentSagaState;
import org.project.repository.AppointmentRepository;
import org.project.repository.SagaStateRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSagaListener {
    AppointmentRepository appointmentRepository;
    SagaStateRepository sagaStateRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentKafkaTopics topics;

    // các lỗi validation để compensate
    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.validationFailed}",
            groupId = "appointment-service"
    )
    @Transactional
    public void handleValidationFailed(ValidationFailedEvent event) {
        log.error("Validation failed: sagaId={}, reason={}",
                event.getSagaId(), event.getReason());

        // Cập nhật saga state
        AppointmentSagaState sagaState = sagaStateRepository.findById(event.getSagaId())
                .orElseThrow();
        sagaState.setStatus(SagaStatus.COMPENSATING);
        sagaState.setFailureReason(event.getReason());
        sagaStateRepository.save(sagaState);

        // Cancel appointment
        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                .orElseThrow();
        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);

        // Publish compensation event để release slot
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .sagaId(event.getSagaId())
                .appointmentId(event.getAppointmentId())
                .slotId(appointment.getSlotId())
                .reason(event.getReason())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(topics.getAppointmentCancelled(), event.getSagaId(), cancelEvent);

        sagaState.setStatus(SagaStatus.COMPENSATED);
        sagaStateRepository.save(sagaState);

        log.info("Đã compensate appointment: id={}", appointment.getId());
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.patientValidated}",
            groupId = "appointment-service"
    )
    @Transactional
    public void handlePatientValidated(PatientValidatedEvent event) {

        updateSagaState(event.getSagaId(), SagaStatus.PATIENT_VALIDATED, "PATIENT_VALIDATED");

        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

        appointment.setPatientName(event.getPatientName());
        appointment.setPatientEmail(event.getPatientEmail());
        appointment.setPatientPhone(event.getPatientPhone());

        appointmentRepository.save(appointment);

        log.info("Đã cập nhật thông tin patient vào appointment: {}", appointment.getId());
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.doctorValidated}",
            groupId = "appointment-service"
    )
    @Transactional
    public void handleDoctorValidated(DoctorValidatedEvent event) {
        log.info("Nhận DoctorValidatedEvent: sagaId={}", event.getSagaId());

        // Cập nhật saga state
        updateSagaState(event.getSagaId(), SagaStatus.COMPLETED, "COMPLETED");

        // CẬP NHẬT THÔNG TIN DOCTOR VÀ CONFIRM APPOINTMENT
        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

        appointment.setDoctorName(event.getDoctorName());
        appointment.setDoctorEmail(event.getDoctorEmail());
        appointment.setSpecialtyName(event.getSpecialtyName());
        appointment.setConsultationFee(event.getConsultationFee());
        appointment.setStatus(Status.CONFIRMED);

        appointmentRepository.save(appointment);

        log.info("Appointment đã được confirm với đầy đủ thông tin: id={}", appointment.getId());

    }

    private void updateSagaState(String sagaId, SagaStatus status, String step) {
        AppointmentSagaState sagaState = sagaStateRepository.findById(sagaId)
                .orElseThrow();
        sagaState.setStatus(status);
        sagaState.setCurrentStep(step);
        sagaState.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(sagaState);
    }

}
