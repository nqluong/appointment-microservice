package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.service.AppointmentExpirationService;
import org.project.service.PaymentQueryService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentSchedulerService {

    private static final long EXPIRED_PAYMENT_INTERVAL = 300_000; // 5 phút
    private static final long PENDING_PAYMENT_INTERVAL = 600_000;

    AppointmentExpirationService appointmentExpirationService;
    PaymentQueryService paymentQueryService;

//    @Scheduled(fixedRate = EXPIRED_PAYMENT_INTERVAL) //5 phut
//    @Async
//    public void processExpiredPayments(){
//        String taskName = "processExpiredPayments";
//        try {
//            appointmentExpirationService.processExpiredAppointments();
//            log.debug("Hoàn thành scheduled task: {}", taskName);
//        } catch (Exception e) {
//            log.error("Lỗi trong scheduled task {}: {}", taskName, e.getMessage());
//        }
//    }

//    @Scheduled(fixedRate = PENDING_PAYMENT_INTERVAL) // 10 minutes
//    @Async
//    public void processPendingPayments() {
//        String taskName = "processPendingPayments";
//
//        try {
//            paymentQueryService.processProcessingPayments();
//            log.info("Hoàn thành scheduled task: {}", taskName);
//        } catch (Exception e) {
//            log.error("Lỗi trong scheduled task {}: {}", taskName, e.getMessage());
//        }
//    }
}
