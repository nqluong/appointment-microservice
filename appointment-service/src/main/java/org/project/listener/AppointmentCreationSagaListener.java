package org.project.listener;

import java.time.LocalDateTime;

import org.project.config.AppointmentKafkaTopics;
import org.project.dto.events.AppointmentCancelledEvent;
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
import org.springframework.kafka.support.Acknowledgment;
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
public class AppointmentCreationSagaListener {
    AppointmentRepository appointmentRepository;
    SagaStateRepository sagaStateRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentKafkaTopics topics;


    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.validationFailed}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    @Transactional
    public void handleValidationFailed(ValidationFailedEvent event, Acknowledgment ack) {
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

        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .sagaId(event.getSagaId())
                .appointmentId(event.getAppointmentId())
                .slotId(appointment.getSlotId())
                .patientUserId(appointment.getPatientUserId())
                .patientName(appointment.getPatientName())
                .patientEmail(appointment.getPatientEmail())
                .patientPhone(appointment.getPatientPhone())
                .doctorUserId(appointment.getDoctorUserId())
                .doctorName(appointment.getDoctorName())
                .doctorPhone(appointment.getDoctorPhone())
                .specialtyName(appointment.getSpecialtyName())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .reason(event.getReason())
                .timestamp(LocalDateTime.now())
                .build();

        String partitionKey = event.getAppointmentId().toString();
        kafkaTemplate.send(topics.getAppointmentCancelled(), partitionKey, cancelEvent);

        sagaState.setStatus(SagaStatus.COMPENSATED);
        sagaStateRepository.save(sagaState);

        log.info("Đã compensate appointment: id={}, partitionKey={}", appointment.getId(), partitionKey);

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.patientValidated}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    @Transactional
    public void handlePatientValidated(PatientValidatedEvent event, Acknowledgment ack) {
        log.info("Nhận PatientValidatedEvent: sagaId={}", event.getSagaId());

        updateSagaState(event.getSagaId(), SagaStatus.COMPLETED, "COMPLETED");

        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

        appointment.setPatientName(event.getPatientName());
        appointment.setPatientEmail(event.getPatientEmail());
        appointment.setPatientPhone(event.getPatientPhone());

        appointmentRepository.save(appointment);

        log.info("Appointment đã được enrich đầy đủ thông tin patient: id={}", appointment.getId());

        ack.acknowledge();
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
