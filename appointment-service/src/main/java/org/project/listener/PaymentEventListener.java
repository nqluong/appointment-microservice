package org.project.listener;

import org.project.config.AppointmentKafkaTopics;
import org.project.dto.events.PaymentCompletedEvent;
import org.project.dto.events.PaymentFailedEvent;
import org.project.enums.Status;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Appointment;
import org.project.repository.AppointmentRepository;
import org.springframework.kafka.annotation.KafkaListener;
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
public class PaymentEventListener {
    
    AppointmentRepository appointmentRepository;
    AppointmentKafkaTopics topics;

    /**
     * Xử lý khi payment hoàn thành thành công
     * Update appointment status → CONFIRMED (nếu đang PENDING)
     */
    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentCompleted}",
            groupId = "appointment-service"
    )
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Nhận PaymentCompletedEvent: appointmentId={}, paymentId={}", 
                event.getAppointmentId(), event.getPaymentId());

        try {
            Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

            // Nếu appointment đang PENDING, chuyển sang CONFIRMED
            if (Status.PENDING.equals(appointment.getStatus())) {
                appointment.setStatus(Status.CONFIRMED);
                appointmentRepository.save(appointment);
                
                log.info("Appointment {} đã được CONFIRMED sau khi thanh toán thành công", 
                        appointment.getId());
            } else {
                log.info("Appointment {} đã ở trạng thái {}, không cần update", 
                        appointment.getId(), appointment.getStatus());
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý PaymentCompletedEvent: appointmentId={}", 
                    event.getAppointmentId(), e);
        }
    }

    /**
     * Xử lý khi payment thất bại
     * Có thể giữ nguyên PENDING hoặc log để admin xử lý
     */
    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentFailed}",
            groupId = "appointment-service"
    )
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Nhận PaymentFailedEvent: appointmentId={}, reason={}", 
                event.getAppointmentId(), event.getReason());

        try {
            Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

            // Log để tracking, có thể implement logic hủy appointment sau X lần fail
            log.warn("Payment failed cho appointment {}, status hiện tại: {}", 
                    appointment.getId(), appointment.getStatus());
            
            // TODO: Có thể implement auto-cancel sau 24h nếu không thanh toán
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý PaymentFailedEvent: appointmentId={}", 
                    event.getAppointmentId(), e);
        }
    }
}

