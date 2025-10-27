package org.project.producer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.project.config.AppointmentKafkaTopics;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCancelledEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppointmentKafkaTopics topics;

    public void publishAppointmentCancellationInitiated(UUID appointmentId, UUID userId, UUID doctorId,
                                                        String reason, String cancelledBy,
                                                        LocalDate appointmentDate, String appointmentTime) {
        try {
            AppointmentCancellationInitiatedEvent event = AppointmentCancellationInitiatedEvent.builder()
                    .appointmentId(appointmentId)
                    .userId(userId)
                    .doctorId(doctorId)
                    .reason(reason)
                    .cancelledBy(cancelledBy)
                    .appointmentDate(appointmentDate)
                    .appointmentTime(appointmentTime)
                    .initiatedAt(LocalDateTime.now())
                    .message("Appointment cancellation is being processed. Refund will be processed shortly.")
                    .build();

            kafkaTemplate.send(topics.getAppointmentCancellationInitiated(), event);
            log.info("Published appointment cancellation initiated event for appointment: {}", appointmentId);
        } catch (Exception e) {
            log.error("Failed to publish appointment cancellation initiated event for appointment: {}", appointmentId, e);
            throw new RuntimeException("Failed to publish appointment cancellation initiated event", e);
        }
    }

    public void publishAppointmentCancelled(UUID appointmentId, UUID userId, UUID doctorId, UUID slotId,
                                            String reason, String cancelledBy,
                                            LocalDate appointmentDate, LocalTime appointmentTime) {
        try {
            AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                    .appointmentId(appointmentId)
                    .patientUserId(userId)
                    .doctorUserId(doctorId)
                    .slotId(slotId)
                    .reason(reason)
                    .cancelledAt(LocalDateTime.now())
                    .cancelledBy(cancelledBy)
                    .appointmentDate(appointmentDate)
                    .startTime(appointmentTime)
                    .build();

            kafkaTemplate.send(topics.getAppointmentCancelled(), event);
            log.info("Published appointment cancellation event for appointment: {}", appointmentId);
        } catch (Exception e) {
            log.error("Failed to publish appointment cancellation event for appointment: {}", appointmentId, e);
            throw new RuntimeException("Failed to publish appointment cancellation event", e);
        }
    }
}
