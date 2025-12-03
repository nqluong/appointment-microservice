package org.project.listener;

import java.time.LocalDateTime;

import org.project.client.UserProfileServiceClient;
import org.project.config.AuthKafkaTopics;
import org.project.dto.response.UserProfileResponse;
import org.project.events.AppointmentCreatedEvent;
import org.project.events.PatientValidatedEvent;
import org.project.events.ValidationFailedEvent;
import org.project.model.User;
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
    UserProfileServiceClient userProfileServiceClient;
    AuthKafkaTopics topics;

//    @KafkaListener(
//            topics = "#{@authKafkaTopics.appointmentCreated}",
//            groupId = "auth-service",
//            concurrency = "3"  // 3 thread xử lý song song từ 3 partition
//    )
//    @Transactional
//    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
//        try {
//            User patient = userRepository.findById(event.getPatientUserId())
//                    .orElseThrow(() -> new RuntimeException("Patient không tồn tại"));
//
//            UserProfileResponse patientProfile = userProfileServiceClient
//                    .getUserProfile(event.getPatientUserId());
//
//            String patientFullName;
//            String patientPhone = null;
//
//            if (patientProfile != null) {
//                patientFullName = String.format("%s %s",
//                        patientProfile.getFirstName(),
//                        patientProfile.getLastName()).trim();
//                patientPhone = patientProfile.getPhone();
//            } else {
//                patientFullName = patient.getUsername();
//            }
//
//            PatientValidatedEvent validatedEvent = PatientValidatedEvent.builder()
//                    .sagaId(event.getSagaId())
//                    .appointmentId(event.getAppointmentId())
//                    .patientUserId(event.getPatientUserId())
//                    .patientEmail(patient.getEmail())
//                    .patientName(patientFullName)
//                    .patientPhone(patientPhone)
//                    .doctorUserId(event.getDoctorUserId())
//                    .timestamp(LocalDateTime.now())
//                    .build();
//
//            // Dùng appointmentId làm key để event của cùng appointment vào cùng partition
//            String partitionKey = event.getAppointmentId().toString();
//            kafkaTemplate.send(topics.getPatientValidated(), partitionKey, validatedEvent);
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy patient info", e);
//            publishValidationFailed(event, "Lỗi hệ thống khi lấy thông tin patient");
//        }
//    }

    private void publishValidationFailed(AppointmentCreatedEvent event, String reason) {
        ValidationFailedEvent failedEvent = ValidationFailedEvent.builder()
                .sagaId(event.getSagaId().toString())
                .appointmentId(event.getAppointmentId())
                .reason(reason)
                .failedService("auth-service")
                .timestamp(LocalDateTime.now())
                .build();

        String partitionKey = event.getAppointmentId().toString();
        kafkaTemplate.send(topics.getValidationFailed(), partitionKey, failedEvent);
    }
}
