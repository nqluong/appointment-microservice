package org.project.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.project.client.SchedulingServiceClient;
import org.project.enums.Status;
import org.project.model.Appointment;
import org.project.repository.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job để tự động hủy appointments PENDING quá 15 phút
 * và release slot tương ứng
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentExpirationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final SchedulingServiceClient schedulingServiceClient;

    private static final int PENDING_TIMEOUT_MINUTES = 15;


    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cancelExpiredPendingAppointments() {
        log.debug("Chạy cleanup expired pending appointments...");

        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);

        // Tìm tất cả appointments PENDING quá 15 phút
        List<Appointment> expiredAppointments = appointmentRepository
                .findExpiredPendingAppointments(expirationTime);

        if (expiredAppointments.isEmpty()) {
            log.debug("Không tìm thấy appointments PENDING quá 15 phút");
            return;
        }

        log.info("Tìm thấy {} appointments PENDING quá 15 phút để hủy", expiredAppointments.size());

        for (Appointment appointment : expiredAppointments) {
            try {
                // Cancel appointment
                appointment.setStatus(Status.CANCELLED);
                appointmentRepository.save(appointment);

                // Release slot
                schedulingServiceClient.releaseSlot(appointment.getSlotId());

                log.info("Đã hủy appointment {} và giải phóng slot {}",
                        appointment.getId(), appointment.getSlotId());

            } catch (Exception e) {
                log.error("Lỗi khi hủy appointment {}: {}",
                        appointment.getId(), e.getMessage());
            }
        }

        log.info("Đã hủy {} appointments PENDING quá 15 phút", expiredAppointments.size());
    }
}

