package org.project.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.project.config.PaymentKafkaTopics;
import org.project.dto.events.PaymentCompletedEvent;
import org.project.dto.events.PaymentFailedEvent;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Payment;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentStatusHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentStatusHandlerImpl implements PaymentStatusHandler {

    PaymentRepository paymentRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    PaymentKafkaTopics topics;

    @Override
    public void handlePaymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        log.info("Thanh toán thành công xác nhận từ query: paymentId={}, appointmentId={}", 
                paymentId, payment.getAppointmentId());
        publishPaymentCompletedEvent(payment);
    }

    @Override
    public void handlePaymentFailure(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        log.info("Thanh toán thất bại xác nhận từ query: paymentId={}, appointmentId={}", 
                paymentId, payment.getAppointmentId());
        
        publishPaymentFailedEvent(payment, "Thanh toán thất bại (xác nhận từ query)", true);
    }
    
    private void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .paymentDate(payment.getPaymentDate())
                .timestamp(LocalDateTime.now())
                .build();
                
        kafkaTemplate.send(topics.getPaymentCompleted(), payment.getAppointmentId().toString(), event);
        log.info("Đã gửi PaymentCompletedEvent (từ query) cho appointment: {}", payment.getAppointmentId());
    }
    
    private void publishPaymentFailedEvent(Payment payment, String reason, boolean confirmedFailure) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .transactionId(payment.getTransactionId())
                .reason(reason)
                .failedService("payment-service")
                .timestamp(LocalDateTime.now())
                .confirmedFailure(confirmedFailure)
                .build();
                
        kafkaTemplate.send(topics.getPaymentFailed(), payment.getAppointmentId().toString(), event);
        log.info("Đã gửi PaymentFailedEvent (từ query) cho appointment: {}, confirmedFailure: {}", 
                payment.getAppointmentId(), confirmedFailure);
    }
}
