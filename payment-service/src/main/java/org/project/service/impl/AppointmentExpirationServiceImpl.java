package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.enums.PaymentStatus;
import org.project.repository.PaymentRepository;
import org.project.service.AppointmentExpirationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentExpirationServiceImpl implements AppointmentExpirationService {

    private static final int EXPIRATION_MINUTES = 15;
    private static final int PAYMENT_GRACE_PERIOD_MINUTES = 30;
    private static final List<PaymentStatus> CANCELLABLE_PAYMENT_STATUSES =
            List.of(PaymentStatus.PENDING);
    private static final List<PaymentStatus> PROCESSING_PAYMENT_STATUSES =
            List.of(PaymentStatus.PROCESSING);
    private static final String CANCELLATION_NOTE = "Hủy do appointment hết hạn";

//    AppointmentRepository appointmentRepository;
    PaymentRepository paymentRepository;
//    SlotStatusService slotStatusService;

    //Tìm và xử lý các appointment đã ở trạng thái chờ quá 15 phút
//    @Override
//    public void processExpiredAppointments() {
//        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
//        List<Appointment> expiredAppointments = findExpiredAppointments(expiredTime);
//
//        log.info("Found {} expired appointments to process", expiredAppointments.size());
//
//        for (Appointment appointment : expiredAppointments) {
//            processExpiredAppointment(appointment.getId());
//        }
//    }

    // Xử lý appointment đã hết hạn
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void processExpiredAppointment(UUID appointmentId) {
//        try {
//            Appointment appointment = findAppointmentById(appointmentId);
//
//            if (!isEligibleForExpiration(appointment)) {
//                log.debug("Appointment {} không đủ điều kiện để xử lý hết hạn", appointmentId);
//                return;
//            }
//
//            ExpirationDecision decision = determineExpirationAction(appointment);
//
//            switch (decision.getAction()) {
//                case CANCEL_IMMEDIATELY:
//                    executeExpirationProcess(appointment);
//                    log.info("Đã xử lý thành công appointment hết hạn: {}", appointmentId);
//                    break;
//
//                case DEFER:
//                    log.info("Appointment {} có payment PROCESSING, tạm hoãn hủy trong grace period (còn {} phút)",
//                            appointmentId, decision.getRemainingGraceMinutes());
//                    break;
//
//                case FORCE_CANCEL:
//                    log.warn("Appointment {} có payment PROCESSING nhưng đã quá grace period. Buộc phải hủy",
//                            appointmentId);
//                    executeExpirationProcess(appointment);
//                    break;
//            }
//
//        } catch (Exception e) {
//            log.error("Lỗi khi xử lý appointment hết hạn {}: {}", appointmentId, e.getMessage(), e);
//            throw new RuntimeException("Không thể xử lý appointment hết hạn: " + appointmentId, e);
//        }
//    }
//
//    private List<Appointment> findExpiredAppointments(LocalDateTime expirationThreshold) {
//        List<Appointment> expiredAppointments = appointmentRepository
//                .findExpiredPendingAppointments(expirationThreshold);
//
//        return expiredAppointments.stream()
//                .filter(this::shouldProcessImmediately)
//                .toList();
//    }

//    private ExpirationDecision determineExpirationAction(Appointment appointment) {
//        List<Payment> processingPayments = findProcessingPayments(appointment.getId());
//
//        if (processingPayments.isEmpty()) {
//            return ExpirationDecision.cancelImmediately();
//        }
//
//        LocalDateTime gracePeriodExpiry = calculateGracePeriodExpiry(appointment);
//        LocalDateTime now = LocalDateTime.now();
//
//        if (now.isAfter(gracePeriodExpiry)) {
//            return ExpirationDecision.forceCancel();
//        } else {
//            long remainingMinutes = Duration.between(now, gracePeriodExpiry).toMinutes();
//            return ExpirationDecision.defer(remainingMinutes);
//        }
//    }

//    private boolean shouldProcessImmediately(Appointment appointment) {
//        return determineExpirationAction(appointment).getAction() != ExpirationAction.DEFER;
//    }
//
//    private List<Payment> findProcessingPayments(UUID appointmentId) {
//        return paymentRepository.findValidPaymentsByAppointmentIdAndStatus(
//                appointmentId, PROCESSING_PAYMENT_STATUSES);
//    }
//
//    private LocalDateTime calculateGracePeriodExpiry(Appointment appointment) {
//        return appointment.getCreatedAt()
//                .plusMinutes(EXPIRATION_MINUTES + PAYMENT_GRACE_PERIOD_MINUTES);
//    }
//
//    private Appointment findAppointmentById(UUID appointmentId) {
//        return appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
//    }
//
//    private boolean isEligibleForExpiration(Appointment appointment) {
//        if (appointment.getStatus() != Status.PENDING) {
//            return false;
//        }
//
//        return appointment.getCreatedAt()
//                .isBefore(LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES));
//    }
//
//    private void executeExpirationProcess(Appointment appointment) {
//        ExpirationResult result = new ExpirationResult();
//
//        try {
//            cancelAppointment(appointment);
//            result.setAppointmentCancelled(true);
//
//            int cancelledPayments = cancelAssociatedPayments(appointment.getId());
//            result.setPaymentsCancelled(cancelledPayments);
//
//            releaseAssociatedSlot(appointment);
//            result.setSlotReleased(true);
//
//            log.info("Expiration process completed for appointment {}: {}",
//                    appointment.getId(), result);
//
//        } catch (Exception e) {
//            log.error("Partial failure in expiration process for appointment {}: {}",
//                    appointment.getId(), result, e);
//            throw e;
//        }
//    }
//
//    private void cancelAppointment(Appointment appointment) {
//        appointment.setStatus(Status.CANCELLED);
//        appointmentRepository.save(appointment);
//        log.debug("Đã hủy appointment: {}", appointment.getId());
//    }
//
//    private int cancelAssociatedPayments(UUID appointmentId) {
//        List<Payment> paymentsToCancel = paymentRepository
//                .findValidPaymentsByAppointmentIdAndStatus(appointmentId, CANCELLABLE_PAYMENT_STATUSES);
//
//        paymentsToCancel.forEach(this::cancelPayment);
//
//        log.debug("Đã hủy {} payment cho appointment: {}", paymentsToCancel.size(), appointmentId);
//        return paymentsToCancel.size();
//    }
//
//    private void cancelPayment(Payment payment) {
//        payment.setPaymentStatus(PaymentStatus.CANCELLED);
//        payment.setNotes(CANCELLATION_NOTE);
//        paymentRepository.save(payment);
//    }
//
//    private void releaseAssociatedSlot(Appointment appointment) {
//        if (appointment.getSlot() != null) {
//            try {
//                slotStatusService.releaseSlot(appointment.getSlot().getId());
//                log.debug("Đã giải phóng slot: {}", appointment.getSlot().getId());
//            } catch (Exception e) {
//                log.warn("Không thể giải phóng slot {}: {}",
//                        appointment.getSlot().getId(), e.getMessage());
//                // Không throw exception để không làm fail toàn bộ process
//            }
//        }
//    }
//
//    private boolean isAppointmentExpired(Appointment appointment) {
//        if (appointment == null || appointment.getStatus() != Status.PENDING) {
//            return false;
//        }
//
//        return appointment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15));
//    }
//
//    private void cancelExpiredAppointment(Appointment appointment) {
//        appointment.setStatus(Status.CANCELLED);
//        appointmentRepository.save(appointment);
//    }

}
