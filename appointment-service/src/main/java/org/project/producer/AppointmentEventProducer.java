package org.project.producer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.project.config.AppointmentKafkaTopics;
import org.project.events.*;
import org.project.model.Appointment;
import org.project.service.OutboxService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentEventProducer {

    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentKafkaTopics topics;
    OutboxService outboxService;


    public void publishAppointmentCreated(AppointmentCreatedEvent event) {
        sendEvent(
                topics.getAppointmentCreated(),
                event.getAppointmentId().toString(),
                event,
                "AppointmentCreated",
                event.getEventId().toString()
        );
    }

    public void publishAppointmentCancellationInitiated(AppointmentCancellationInitiatedEvent event) {
        sendEvent(
                topics.getAppointmentCancellationInitiated(),
                event.getAppointmentId().toString(),
                event,
                "AppointmentCancellationInitiated",
                event.getEventId().toString()
        );
    }

    public void publishAppointmentCancelled(AppointmentCancelledEvent event) {
        sendEvent(
                topics.getAppointmentCancelled(),
                event.getAppointmentId().toString(),
                event,
                "AppointmentCancelled",
                event.getEventId().toString()
        );
    }

    public void publishAppointmentConfirmed(Appointment appointment, PaymentCompletedEvent paymentEvent) {
        AppointmentConfirmedEvent event = AppointmentConfirmedEvent.builder()
                .appointmentId(appointment.getId())
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
                .consultationFee(appointment.getConsultationFee())
                .notes(appointment.getNotes())
                .paymentId(paymentEvent.getPaymentId())
                .paymentAmount(paymentEvent.getAmount())
                .paymentType(paymentEvent.getPaymentType())
                .transactionId(paymentEvent.getTransactionId())
                .paymentDate(paymentEvent.getPaymentDate())
                .timestamp(LocalDateTime.now())
                .build();

        sendEvent(topics.getAppointmentConfirmed(),
                appointment.getId().toString(),
                event,
                "AppointmentConfirmed",
                null);
    }

    private void sendEvent(String topic, String key, Object event, String eventType, String eventId) {
        try {
            kafkaTemplate.send(topic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            handlePublishFailure(eventType, key, eventId, ex);
                        } else {
                            handlePublishSuccess(eventType, key, eventId, result);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending {}: key={}", eventType, key, e);
            if (eventId != null) {
                outboxService.markAsFailed(eventId, e.getMessage());
            }
            throw new RuntimeException("Failed to send " + eventType, e);
        }
    }

    private void handlePublishSuccess(String eventType, String key, String eventId, Object result) {

        if (eventId != null) {
            outboxService.markAsProcessed(eventId);
        }
    }


    private void handlePublishFailure(String eventType, String key, String eventId, Throwable ex) {

        if (eventId != null) {
            outboxService.markAsFailed(eventId, ex.getMessage());
        }

        throw new RuntimeException("Failed to publish " + eventType, ex);
    }
}
