package org.project.listener;

import java.time.LocalDateTime;

import org.project.config.AppointmentKafkaTopics;
import org.project.dto.events.AppointmentCancelledEvent;
import org.project.dto.events.PaymentCompletedEvent;
import org.project.dto.events.PaymentFailedEvent;
import org.project.enums.SagaStatus;
import org.project.enums.Status;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Appointment;
import org.project.repository.AppointmentRepository;
import org.project.repository.SagaStateRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
public class AppointmentPaymentSagaListener {
    
    AppointmentRepository appointmentRepository;
    SagaStateRepository sagaStateRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    AppointmentKafkaTopics topics;

    /**
     * Xử lý khi payment hoàn thành thành công
     */
    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentCompleted}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Nhận PaymentCompletedEvent: appointmentId={}, paymentId={}", 
                event.getAppointmentId(), event.getPaymentId());

        try {
            Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

            if (Status.PENDING.equals(appointment.getStatus())) {
                appointment.setStatus(Status.CONFIRMED);
                appointmentRepository.save(appointment);
                
                updateSagaStateByAppointmentId(
                    appointment.getId(), 
                    SagaStatus.PAYMENT_COMPLETED, 
                    "PAYMENT_COMPLETED"
                );
                
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


    @KafkaListener(
            topics = "#{@appointmentKafkaTopics.paymentFailed}",
            groupId = "appointment-service",
            concurrency = "3"
    )
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Nhận PaymentFailedEvent: appointmentId={}, reason={}, confirmedFailure={}", 
                event.getAppointmentId(), event.getReason(), event.isConfirmedFailure());

        try {
            Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));

            if (event.isConfirmedFailure()) {

                if (Status.PENDING.equals(appointment.getStatus())) {
                    updateSagaStateByAppointmentId(
                        appointment.getId(),
                        SagaStatus.COMPENSATING,
                        "PAYMENT_FAILED_COMPENSATING"
                    );
                    
                    // Cancel appointment
                    appointment.setStatus(Status.CANCELLED);
                    appointmentRepository.save(appointment);

                    publishAppointmentCancelledEvent(appointment, event.getReason());

                    updateSagaStateByAppointmentId(
                        appointment.getId(),
                        SagaStatus.COMPENSATED,
                        "PAYMENT_FAILED_COMPENSATED"
                    );
                    
                    log.warn("Appointment {} đã bị HỦY do thanh toán thất bại (confirmed) và đã release slot", 
                            appointment.getId());
                } else {
                    log.warn("Appointment {} ở trạng thái {}, không thể hủy", 
                            appointment.getId(), appointment.getStatus());
                }
            } else {
                log.warn("Payment chưa có kết quả cho appointment {}, giữ trạng thái PENDING, " +
                        "system sẽ query để kiểm tra. Status hiện tại: {}", 
                        appointment.getId(), appointment.getStatus());
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý PaymentFailedEvent: appointmentId={}", 
                    event.getAppointmentId(), e);
        }
    }


    private void updateSagaStateByAppointmentId(
            java.util.UUID appointmentId, 
            SagaStatus status, 
            String step
    ) {
        sagaStateRepository.findByAppointmentId(appointmentId)
                .ifPresent(sagaState -> {
                    sagaState.setStatus(status);
                    sagaState.setCurrentStep(step);
                    sagaState.setUpdatedAt(LocalDateTime.now());
                    if (SagaStatus.COMPENSATING.equals(status) || SagaStatus.FAILED.equals(status)) {
                        sagaState.setFailureReason("Payment failed");
                    }
                    sagaStateRepository.save(sagaState);
                    log.debug("Cập nhật saga state: appointmentId={}, status={}, step={}", 
                            appointmentId, status, step);
                
                });
    }

    private void publishAppointmentCancelledEvent(Appointment appointment, String reason) {
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlotId())
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        String partitionKey = appointment.getId().toString();
        kafkaTemplate.send(topics.getAppointmentCancelled(), partitionKey, cancelEvent);
        
        log.info("Đã gửi AppointmentCancelledEvent để release slot: appointmentId={}, slotId={}, partitionKey={}", 
                appointment.getId(), appointment.getSlotId(), partitionKey);
    }
}

