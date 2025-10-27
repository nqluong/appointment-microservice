package org.project.listener;

import java.time.LocalDateTime;

import org.project.dto.request.AppointmentFailureNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;
import org.project.events.AppointmentCancelledEvent;
import org.project.events.AppointmentConfirmedEvent;
import org.project.service.AsyncEmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentNotificationListener {

    AsyncEmailService asyncEmailService;

    @KafkaListener(
            topics = "#{@notificationKafkaTopics.appointmentConfirmed}",
            groupId = "notifications-service",
            concurrency = "3"
    )
    public void handleAppointmentConfirmed(AppointmentConfirmedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Nhận AppointmentConfirmedEvent: appointmentId={}, patientEmail={}", 
                    event.getAppointmentId(), event.getPatientEmail());

            PaymentNotificationRequest notificationRequest = PaymentNotificationRequest.builder()
                    .patientEmail(event.getPatientEmail())
                    .patientName(event.getPatientName() != null ? event.getPatientName() : "Bệnh nhân")
                    .doctorName(event.getDoctorName() != null ? event.getDoctorName() : "Bác sĩ")
                    .appointmentId(event.getAppointmentId().toString())
                    .appointmentDate(LocalDateTime.of(event.getAppointmentDate(), event.getStartTime()))
                    .paymentAmount(event.getPaymentAmount())
                    .transactionId(event.getTransactionId())
                    .paymentDate(event.getPaymentDate())
                    .paymentType(event.getPaymentType())
                    .doctorSpecialty(event.getSpecialtyName())
                    .build();

            asyncEmailService.sendAppointmentConfirmationEmailAsync(notificationRequest);
            
            acknowledgment.acknowledge();
            
            log.info("Đã xử lý thành công AppointmentConfirmedEvent cho appointment: {}", 
                    event.getAppointmentId());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý AppointmentConfirmedEvent: appointmentId={}", 
                    event.getAppointmentId(), e);
        }
    }

    @KafkaListener(
            topics = "#{@notificationKafkaTopics.appointmentCancelled}",
            groupId = "notifications-service",
            concurrency = "3"
    )
    public void handleAppointmentCancelled(AppointmentCancelledEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Nhận AppointmentCancelledEvent: appointmentId={}, patientEmail={}, reason={}", 
                    event.getAppointmentId(), event.getPatientEmail(), event.getReason());

            AppointmentFailureNotificationRequest notificationRequest = AppointmentFailureNotificationRequest.builder()
                    .patientEmail(event.getPatientEmail())
                    .patientName(event.getPatientName() != null ? event.getPatientName() : "Bệnh nhân")
                    .doctorName(event.getDoctorName() != null ? event.getDoctorName() : "Bác sĩ")
                    .appointmentId(event.getAppointmentId().toString())
                    .appointmentDate(event.getAppointmentDate() != null && event.getStartTime() != null 
                            ? LocalDateTime.of(event.getAppointmentDate(), event.getStartTime()) 
                            : null)
                    .reason(event.getReason())
                    .transactionId(event.getTransactionId())
                    .failureTime(event.getTimestamp())
                    .build();

            asyncEmailService.sendAppointmentFailureEmailAsync(notificationRequest);
            
            acknowledgment.acknowledge();
            
            log.info("Đã xử lý thành công AppointmentCancelledEvent cho appointment: {}", 
                    event.getAppointmentId());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý AppointmentCancelledEvent: appointmentId={}", 
                    event.getAppointmentId(), e);
        }
    }
}

