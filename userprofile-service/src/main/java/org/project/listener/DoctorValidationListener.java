package org.project.listener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.AuthServiceClient;
import org.project.config.UserProfileKafkaTopics;
import org.project.dto.events.DoctorValidatedEvent;
import org.project.dto.events.PatientValidatedEvent;
import org.project.dto.events.ValidationFailedEvent;
import org.project.dto.response.MedicalProfileResponse;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.exception.CustomException;
import org.project.model.Specialty;
import org.project.model.UserProfile;
import org.project.repository.MedicalProfileRepository;
import org.project.repository.SpecialtyRepository;
import org.project.repository.UserProfileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
            topics = "#{@userProfileKafkaTopics.patientValidated}",
            groupId = "userprofile-service"
    )
    @Transactional
    public void handlePatientValidated(PatientValidatedEvent event) {
        log.info("Nhận PatientValidatedEvent để validate doctor: sagaId={}", event.getSagaId());

        try {
            UUID doctorUserId = event.getDoctorUserId();
            log.info("Doctor User ID : {}", doctorUserId);
            UserBasicInfoResponse doctorUser = authServiceClient.getUserBasicInfo(doctorUserId);

            if (!doctorUser.isActive()) {
                publishValidationFailed(event, "Bác sĩ không hoạt động");
                return;
            }

            // Kiểm tra doctor role
            boolean hasDoctorRole = authServiceClient.hasRole(doctorUserId, "DOCTOR");
            if (!hasDoctorRole) {
                publishValidationFailed(event, "User không có role DOCTOR");
                return;
            }

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

            if (!medicalProfile.isDoctorApproved()) {
                publishValidationFailed(event, "Bác sĩ chưa được phê duyệt");
                return;
            }

            String specialtyName = specialtyRepository.findById(medicalProfile.getSpecialtyId())
                    .map(Specialty::getName)
                    .orElse("Không xác định");

            // Publish success với consultation fee
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
                    .consultationFee(medicalProfile.getConsultationFee())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(topics.getDoctorValidated(), event.getSagaId(), validatedEvent);

            log.info("Doctor đã được validate: userId={}", doctorUserId);

        } catch (Exception e) {
            log.error("Lỗi khi validate doctor");
            publishValidationFailed(event, "Lỗi hệ thống khi validate doctor");
        }
    }

    private void publishValidationFailed(PatientValidatedEvent event, String reason) {
        ValidationFailedEvent failedEvent = ValidationFailedEvent.builder()
                .sagaId(event.getSagaId())
                .appointmentId(event.getAppointmentId())
                .reason(reason)
                .failedService("userprofile-service")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("validation-failed-topic", event.getSagaId(), failedEvent);
    }

}
