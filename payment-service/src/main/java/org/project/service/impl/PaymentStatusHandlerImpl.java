package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.enums.PaymentType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.Payment;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentStatusHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentStatusHandlerImpl implements PaymentStatusHandler {

    PaymentRepository paymentRepository;
//    AppointmentRepository appointmentRepository;

    @Override
    public void handlePaymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

       // Appointment appointment = payment.getAppointment();

        if (payment.getPaymentType() == PaymentType.DEPOSIT) {
//            appointment.setStatus(Status.CONFIRMED);
//            appointmentRepository.save(appointment);

//            log.info("Lịch hẹn {} đã được xác nhận sau khi thanh toán đặt cọc {} thành công",
//                    appointment.getId(), paymentId);

            log.info("Lịch hẹn đã được xác nhận sau khi thanh toán đặt cọc {} thành công", paymentId);

        } else if (payment.getPaymentType() == PaymentType.FULL) {
//            appointment.setStatus(Status.CONFIRMED);
//            appointmentRepository.save(appointment);

//            log.info("Lịch hẹn {} đã được xác nhận sau khi thanh toán toàn bộ {} thành công",
//                    appointment.getId(), paymentId);
            log.info("Lịch hẹn đã được xác nhận sau khi thanh toán toàn bộ {} thành công", paymentId);

        }
    }

    @Override
    public void handlePaymentFailure(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

//        log.info("Thanh toán {} thất bại, lịch hẹn {} vẫn ở trạng thái CHỜ XỬ LÝ",
//                paymentId, payment.getAppointment().getId());

        log.info("Thanh toán {} thất bại, lịch hẹn vẫn ở trạng thái CHỜ XỬ LÝ", paymentId);
    }
}
