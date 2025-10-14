package org.project.listener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.AppointmentValidationClient;
import org.project.client.UserProfileServiceClient;
import org.project.config.AuthKafkaTopics;
import org.project.dto.events.PatientValidatedEvent;
import org.project.dto.events.SlotReservedEvent;
import org.project.dto.events.ValidationFailedEvent;
import org.project.dto.response.UserProfileResponse;
import org.project.model.User;
import org.project.model.UserRole;
import org.project.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
            topics = "#{@authKafkaTopics.slotReserved}",
            groupId = "auth-service"
    )
    @Transactional
    public void handleSlotReserved(SlotReservedEvent event) {
        log.info("Nhận SlotReservedEvent để validate patient: sagaId={}", event.getSagaId());

        try {
            User patient = userRepository.findById(event.getReservedBy())
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

            boolean hasOverlapping = appointmentValidationClient
                    .checkOverlappingAppointment(event.getReservedBy(), event.getAppointmentDate(), event.getStartTime(), event.getEndTime());

            if (hasOverlapping) {
                publishValidationFailed(event, "Patient đã có lịch hẹn trùng giờ");
                return;
            }

            // Check pending limit
            int pendingCount = appointmentValidationClient
                    .countPendingAppointments(event.getReservedBy());

            if (pendingCount >= 3) {
                publishValidationFailed(event, "Patient đã có quá nhiều lịch hẹn pending");
                return;
            }

            UserProfileResponse patientProfile = userProfileServiceClient
                    .getUserProfile(event.getReservedBy());

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
                    .patientUserId(event.getReservedBy())
                    .patientEmail(patient.getEmail())
                    .patientName(patientFullName)
                    .patientPhone(patientPhone)
                    .doctorUserId(event.getDoctorUserId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(topics.getPatientValidated(), event.getSagaId(), validatedEvent);

            log.info("Patient đã được validate: userId={}", event.getReservedBy());

        } catch (Exception e) {
            log.error("Lỗi khi validate patient");
            publishValidationFailed(event, "Lỗi hệ thống khi validate patient");
        }
    }

    private void publishValidationFailed(SlotReservedEvent event, String reason) {
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
