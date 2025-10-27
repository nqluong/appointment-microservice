package org.project.listener;

import java.time.LocalDateTime;
import java.util.UUID;

import org.project.client.AuthServiceClient;
import org.project.config.UserProfileKafkaTopics;
import org.project.dto.response.MedicalProfileResponse;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.events.AppointmentCreatedEvent;
import org.project.events.DoctorValidatedEvent;
import org.project.events.ValidationFailedEvent;
import org.project.model.Specialty;
import org.project.model.UserProfile;
import org.project.repository.MedicalProfileRepository;
import org.project.repository.SpecialtyRepository;
import org.project.repository.UserProfileRepository;
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
public class DoctorValidationListener {
    MedicalProfileRepository medicalProfileRepository;
    UserProfileRepository userProfileRepository;
    AuthServiceClient authServiceClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    SpecialtyRepository specialtyRepository;
    private final UserProfileKafkaTopics topics;

    @KafkaListener(
            topics = "#{@userProfileKafkaTopics.appointmentCreated}",
            groupId = "userprofile-service",
            concurrency = "3"
    )
    @Transactional
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Nhận AppointmentCreatedEvent để validate doctor: sagaId={}, appointmentId={}", 
                event.getSagaId(), event.getAppointmentId());

        try {
            UUID doctorUserId = event.getDoctorUserId();

            UserBasicInfoResponse doctorUser = authServiceClient.getUserBasicInfo(doctorUserId);

            UserProfile doctorProfile = userProfileRepository.findByUserId(doctorUserId)
                    .orElse(null);

            String doctorFullName;
            String doctorPhone = null;

            if (doctorProfile != null) {
                doctorFullName = String.format("%s %s",
                        doctorProfile.getFirstName(),
                        doctorProfile.getLastName()).trim();
                doctorPhone = doctorProfile.getPhone();
            } else {
                doctorFullName = doctorUser.getFullName();
            }

            MedicalProfileResponse medicalProfile = medicalProfileRepository
                    .findByUserId(doctorUserId)
                    .orElseThrow(() -> new RuntimeException("Bác sĩ chưa có hồ sơ y khoa"));

            String specialtyName = specialtyRepository.findById(medicalProfile.getSpecialtyId())
                    .map(Specialty::getName)
                    .orElse("Không xác định");

            DoctorValidatedEvent validatedEvent = DoctorValidatedEvent.builder()
                    .sagaId(event.getSagaId())
                    .appointmentId(event.getAppointmentId())
                    .doctorUserId(doctorUserId)
                    .doctorEmail(doctorUser.getEmail())
                    .doctorName(doctorFullName)
                    .doctorPhone(doctorPhone)
                    .specialtyName(specialtyName)
                    .consultationFee(medicalProfile.getConsultationFee())
                    .timestamp(LocalDateTime.now())
                    .build();

            String partitionKey = event.getAppointmentId().toString();
            kafkaTemplate.send(topics.getDoctorValidated(), partitionKey, validatedEvent);


        } catch (Exception e) {
            log.error("Lỗi khi lấy doctor info", e);
            publishValidationFailed(event, "Lỗi hệ thống khi lấy thông tin doctor");
        }
    }

    private void publishValidationFailed(AppointmentCreatedEvent event, String reason) {
        ValidationFailedEvent failedEvent = ValidationFailedEvent.builder()
                .sagaId(event.getSagaId())
                .appointmentId(event.getAppointmentId())
                .reason(reason)
                .failedService("userprofile-service")
                .timestamp(LocalDateTime.now())
                .build();

        String partitionKey = event.getAppointmentId().toString();
        kafkaTemplate.send(topics.getValidationFailed(), partitionKey, failedEvent);
    }

}
