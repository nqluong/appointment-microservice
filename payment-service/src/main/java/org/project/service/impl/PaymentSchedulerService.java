package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.service.PaymentQueryService;
import org.project.service.PaymentTimeoutService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentSchedulerService {

    static final long PAYMENT_CHECK_INTERVAL = 300000; // 5 phút

    PaymentQueryService paymentQueryService;
    PaymentTimeoutService paymentTimeoutService;

    /**
     * Xử lý các payment PENDING quá hạn (> 15 phút)
     */
    @Scheduled(fixedRate = PAYMENT_CHECK_INTERVAL)
    @Async
    public void processExpiredPendingPayments() {
        String taskName = "processExpiredPendingPayments";
        log.info("Bắt đầu scheduled task: {}", taskName);

        try {
            paymentTimeoutService.processExpiredPendingPayments();
            log.info("Hoàn thành scheduled task: {}", taskName);
        } catch (Exception e) {
            log.error("Lỗi trong scheduled task {}: {}", taskName, e.getMessage(), e);
        }
    }

    /**
     * Xử lý các payment PROCESSING - query VNPay để lấy kết quả
     */
    @Scheduled(fixedRate = PAYMENT_CHECK_INTERVAL)
    @Async
    public void processProcessingPayments() {
        String taskName = "processProcessingPayments";
        log.info("Bắt đầu scheduled task: {}", taskName);

        try {
            paymentQueryService.processProcessingPayments();
            log.info("Hoàn thành scheduled task: {}", taskName);
        } catch (Exception e) {
            log.error("Lỗi trong scheduled task {}: {}", taskName, e.getMessage(), e);
        }
    }
}
