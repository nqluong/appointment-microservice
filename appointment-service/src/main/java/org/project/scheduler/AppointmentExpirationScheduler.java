package org.project.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.project.client.PaymentServiceClient;
import org.project.client.SchedulingServiceClient;
import org.project.enums.Status;
import org.project.model.Appointment;
import org.project.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentExpirationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final SchedulingServiceClient schedulingServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Value("${appointment.pending-timeout}")
    private int PENDING_TIMEOUT_MINUTES;

    @Value("${appointment.grace_period}")
    private int GRACE_PERIOD_MINUTES;


    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void cancelExpiredPendingAppointments() {
        log.debug("Chạy cleanup expired pending appointments...");

        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);
        LocalDateTime gracePeriodExpiration = LocalDateTime.now().minusMinutes(GRACE_PERIOD_MINUTES);

        List<Appointment> expiredAppointments = appointmentRepository
                .findExpiredPendingAppointments(expirationTime);

        if (expiredAppointments.isEmpty()) {
            log.debug("Không tìm thấy appointments PENDING quá {} phút", PENDING_TIMEOUT_MINUTES);
            return;
        }

        log.info("Tìm thấy {} appointments PENDING quá {} phút để kiểm tra", 
                expiredAppointments.size(), PENDING_TIMEOUT_MINUTES);

        int cancelledCount = 0;
        int deferredCount = 0;

        for (Appointment appointment : expiredAppointments) {
            try {
                boolean hasProcessingPayment = paymentServiceClient.hasProcessingPayment(appointment.getId());
                
                if (hasProcessingPayment) {

                    if (appointment.getCreatedAt().isBefore(gracePeriodExpiration)) {
                        log.warn("Appointment {} đã quá grace period ({} phút) dù có payment PROCESSING. Buộc hủy.",
                                appointment.getId(), GRACE_PERIOD_MINUTES);
                        cancelAppointment(appointment);
                        cancelledCount++;
                    } else {
                        log.info("Appointment {} có payment PROCESSING, tạm hoãn cancel trong grace period",
                                appointment.getId());
                        deferredCount++;
                    }
                } else {
                    log.info("Appointment {} không có payment PROCESSING, hủy ngay", appointment.getId());
                    cancelAppointment(appointment);
                    cancelledCount++;
                }

            } catch (Exception e) {
                log.error("Lỗi khi xử lý appointment {}: {}", appointment.getId(), e.getMessage(), e);
            }
        }

        log.info("Kết quả cleanup: {} appointments đã hủy, {} appointments tạm hoãn (có payment PROCESSING)",
                cancelledCount, deferredCount);
    }
    
    private void cancelAppointment(Appointment appointment) {
        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);

        try {
            schedulingServiceClient.releaseSlot(appointment.getSlotId());
            log.info("Đã hủy appointment {} và giải phóng slot {}", 
                    appointment.getId(), appointment.getSlotId());
        } catch (Exception e) {
            log.error("Đã hủy appointment {} nhưng lỗi khi giải phóng slot {}: {}", 
                    appointment.getId(), appointment.getSlotId(), e.getMessage());
        }
    }
}

