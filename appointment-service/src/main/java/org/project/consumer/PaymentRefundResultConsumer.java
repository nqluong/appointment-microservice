package org.project.consumer;

import org.project.events.PaymentRefundProcessedEvent;
import org.project.service.AppointmentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundResultConsumer {

    private final AppointmentService appointmentService;

    @KafkaListener(topics = "payment.refund.processed", groupId = "appointment-service", concurrency = "3")
    public void handlePaymentRefundResult(PaymentRefundProcessedEvent event) {
        log.info("Received payment refund result event: {}", event);

        try {
            // Cập nhật trạng thái appointment dựa trên kết quả refund
            appointmentService.updateAppointmentRefundStatus(
                event.getAppointmentId(), 
                event.isSuccess(), 
                event.getRefundAmount(),
                event.getRefundType()
            );
            
            if (event.isSuccess()) {
                log.info("Successfully updated appointment {} with refund success", event.getAppointmentId());
            } else {
                log.warn("Updated appointment {} with refund failure: {}", 
                    event.getAppointmentId(), event.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Failed to update appointment refund status for appointment: {}", 
                event.getAppointmentId(), e);
        }
    }
}
