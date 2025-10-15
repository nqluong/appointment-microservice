package org.project.listener;

import java.time.LocalDateTime;

import org.project.client.AppointmentValidationClient;
import org.project.client.UserProfileServiceClient;
import org.project.config.AuthKafkaTopics;
import org.project.dto.events.AppointmentCreatedEvent;
import org.project.dto.events.PatientValidatedEvent;
import org.project.dto.events.ValidationFailedEvent;
import org.project.dto.response.UserProfileResponse;
import org.project.model.User;
import org.project.model.UserRole;
import org.project.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientValidationListener {
    UserRepository userRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentValidationClient appointmentValidationClient;
    UserProfileServiceClient userProfileServiceClient;
    AuthKafkaTopics topics;

    @KafkaListener(
            topics = "#{@authKafkaTopics.appointmentCreated}",
            groupId = "auth-service"
    )
    @Transactional
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Nhận AppointmentCreatedEvent để validate patient: sagaId={}", event.getSagaId());

        try {
            User patient = userRepository.findById(event.getPatientUserId())
                    .orElseThrow(() -> new RuntimeException("Patient không tồn tại"));

            // Validate patient active
            if (!patient.isActive()) {
                publishValidationFailed(event, "Patient không hoạt động");
                return;
            }

            boolean hasPatientRole = patient.getUserRoles().stream()
                    .filter(UserRole::isActive)
                    .anyMatch(ur -> "PATIENT".equals(ur.getRole().getName()));

            if (!hasPatientRole) {
                publishValidationFailed(event, "User không có role PATIENT");
                return;
            }

            // Check pending limit
            int pendingCount = appointmentValidationClient
                    .countPendingAppointments(event.getPatientUserId());

            if (pendingCount >= 3) {
                publishValidationFailed(event, "Patient đã có quá nhiều lịch hẹn pending");
                return;
            }

            UserProfileResponse patientProfile = userProfileServiceClient
                    .getUserProfile(event.getPatientUserId());

            String patientFullName;
            String patientPhone = null;

            if (patientProfile != null) {
                patientFullName = String.format("%s %s",
                        patientProfile.getFirstName(),
                        patientProfile.getLastName()).trim();
                patientPhone = patientProfile.getPhone();
            } else {
                patientFullName = patient.getUsername();
            }

            // Publish success
            PatientValidatedEvent validatedEvent = PatientValidatedEvent.builder()
                    .sagaId(event.getSagaId())
                    .appointmentId(event.getAppointmentId())
                    .patientUserId(event.getPatientUserId())
                    .patientEmail(patient.getEmail())
                    .patientName(patientFullName)
                    .patientPhone(patientPhone)
                    .doctorUserId(event.getDoctorUserId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(topics.getPatientValidated(), event.getSagaId(), validatedEvent);

            log.info("Patient đã được validate: userId={}", event.getPatientUserId());

        } catch (Exception e) {
            log.error("Lỗi khi validate patient", e);
            publishValidationFailed(event, "Lỗi hệ thống khi validate patient");
        }
    }

    private void publishValidationFailed(AppointmentCreatedEvent event, String reason) {
        ValidationFailedEvent failedEvent = ValidationFailedEvent.builder()
                .sagaId(event.getSagaId())
                .appointmentId(event.getAppointmentId())
                .reason(reason)
                .failedService("auth-service")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(topics.getValidationFailed(), event.getSagaId(), failedEvent);
    }
}
