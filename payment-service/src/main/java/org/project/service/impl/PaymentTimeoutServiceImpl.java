package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.enums.PaymentStatus;
import org.project.model.Payment;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentStatusHandler;
import org.project.service.PaymentTimeoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTimeoutServiceImpl implements PaymentTimeoutService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHandler paymentStatusHandler;

    @Value("${payment.pending-timeout-minutes:15}")
    private static int pendingTimeoutMinutes;

    @Override
    @Transactional
    public void processExpiredPendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = now.minusMinutes(pendingTimeoutMinutes);

        log.info("Đang tìm các payment PENDING quá hạn (trước {})", timeoutThreshold);

        // Tìm các payment PENDING đã quá 15 phút
        List<Payment> expiredPayments = paymentRepository
                .findByPaymentStatusAndCreatedAtBefore(PaymentStatus.PENDING, timeoutThreshold);

        if (expiredPayments.isEmpty()) {
            log.info("Không có payment PENDING nào quá hạn");
            return;
        }

        log.info("Tìm thấy {} payment PENDING quá hạn, bắt đầu xử lý", expiredPayments.size());

        int processedCount = 0;
        int errorCount = 0;

        for (Payment payment : expiredPayments) {
            try {
                processExpiredPayment(payment);
                processedCount++;
            } catch (Exception e) {
                log.error("Lỗi khi xử lý payment quá hạn: paymentId={}, transactionId={}",
                        payment.getId(), payment.getTransactionId(), e);
                errorCount++;
            }
        }

        log.info("Hoàn thành xử lý payment PENDING quá hạn. Đã xử lý: {}, Lỗi: {}, Tổng: {}",
                processedCount, errorCount, expiredPayments.size());
    }

    private void processExpiredPayment(Payment payment) {
        log.warn("Payment {} đã quá hạn PENDING (tạo lúc: {}), đánh dấu thất bại",
                payment.getId(), payment.getCreatedAt());

        // Cập nhật trạng thái payment
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Publish PaymentFailedEvent để hủy appointment
        paymentStatusHandler.handlePaymentFailure(payment.getId());

        log.info("Đã đánh dấu payment {} thất bại và publish event", payment.getId());
    }
}
