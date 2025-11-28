package org.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.enums.Status;
import org.project.events.*;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.enums.SagaStatus;
import org.project.model.Appointment;
import org.project.model.AppointmentSagaState;
import org.project.producer.AppointmentEventProducer;
import org.project.repository.AppointmentRepository;
import org.project.repository.SagaStateRepository;
import org.project.service.AppointmentSagaEventHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSagaEventHandlerImpl implements AppointmentSagaEventHandler {
    AppointmentRepository appointmentRepository;
    AppointmentEventProducer eventProducer;
    SagaStateRepository sagaStateRepository;

    @Transactional
    @Override
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Xử lý thanh toán hoàn tất: appointmentId={}, paymentId={}",
                event.getAppointmentId(), event.getPaymentId());

        Appointment appointment = getAppointment(event.getAppointmentId());

        if (!Status.PENDING.equals(appointment.getStatus())) {
            log.info("Lịch hẹn {} đã ở trạng thái {}, bỏ qua",
                    appointment.getId(), appointment.getStatus());
            return;
        }

        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(Status.CONFIRMED);
        appointmentRepository.save(appointment);

        // Cập nhật trạng thái saga
        updateSagaState(appointment.getId(), SagaStatus.PAYMENT_COMPLETED, "PAYMENT_COMPLETED", null);

        // Phát sự kiện xác nhận
        eventProducer.publishAppointmentConfirmed(appointment, event);

        log.info("Lịch hẹn {} đã được xác nhận thành công", appointment.getId());
    }

    @Transactional
    @Override
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Xử lý thanh toán thất bại: appointmentId={}, lý do={}, xác nhận={}",
                event.getAppointmentId(), event.getReason(), event.isConfirmedFailure());

        if (!event.isConfirmedFailure()) {
            log.warn("Kết quả thanh toán không chắc chắn cho lịch hẹn {}, giữ trạng thái PENDING",
                    event.getAppointmentId());
            return;
        }

        Appointment appointment = getAppointment(event.getAppointmentId());

        if (!Status.PENDING.equals(appointment.getStatus())) {
            log.warn("Lịch hẹn {} đang ở trạng thái {}, không thể hủy",
                    appointment.getId(), appointment.getStatus());
            return;
        }

        // Bắt đầu bù trừ
        updateSagaState(appointment.getId(), SagaStatus.COMPENSATING,
                "PAYMENT_FAILED_COMPENSATING", event.getReason());

        // Hủy lịch hẹn
        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);

        // Tạo và phát sự kiện hủy lịch
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(getSagaId(appointment.getId()))
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlotId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .reason(event.getReason())
                .cancelledBy("SYSTEM")
                .cancelledAt(LocalDateTime.now())
                .build();
        
        eventProducer.publishAppointmentCancelled(cancelEvent);

        // Hoàn tất bù trừ
        updateSagaState(appointment.getId(), SagaStatus.COMPENSATED,
                "PAYMENT_FAILED_COMPENSATED", null);

        log.warn("Lịch hẹn {} đã bị hủy do thanh toán thất bại", appointment.getId());
    }

    @Transactional
    @Override
    public void handleValidationFailed(ValidationFailedEvent event) {
        log.error("Xử lý xác thực thất bại: sagaId={}, appointmentId={}, lý do={}",
                event.getSagaId(), event.getAppointmentId(), event.getReason());

        // Cập nhật trạng thái saga sang đang bù trừ
        AppointmentSagaState sagaState = sagaStateRepository.findById(event.getSagaId())
                .orElseThrow(() -> new CustomException(ErrorCode.SAGA_NOT_FOUND));

        sagaState.setStatus(SagaStatus.COMPENSATING);
        sagaState.setFailureReason(event.getReason());
        sagaState.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(sagaState);

        // Hủy lịch hẹn
        Appointment appointment = getAppointment(event.getAppointmentId());
        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);

        // Tạo và phát sự kiện hủy lịch
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(UUID.fromString(event.getSagaId()))
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlotId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .reason(event.getReason())
                .cancelledBy("SYSTEM")
                .cancelledAt(LocalDateTime.now())
                .build();
        
        eventProducer.publishAppointmentCancelled(cancelEvent);

        // Hoàn tất bù trừ
        sagaState.setStatus(SagaStatus.COMPENSATED);
        sagaState.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(sagaState);

        log.info("Lịch hẹn {} đã được bù trừ sau khi xác thực thất bại", appointment.getId());
    }

    @Transactional
    @Override
    public void handlePatientValidated(PatientValidatedEvent event) {
        log.info("Xử lý xác thực bệnh nhân: sagaId={}, appointmentId={}",
                event.getSagaId(), event.getAppointmentId());

        // Cập nhật trạng thái saga
        AppointmentSagaState sagaState = sagaStateRepository.findById(event.getSagaId())
                .orElseThrow(() -> new CustomException(ErrorCode.SAGA_NOT_FOUND));

        sagaState.setStatus(SagaStatus.PATIENT_VALIDATED);
        sagaState.setCurrentStep("PATIENT_VALIDATED");
        sagaState.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(sagaState);

        // Bổ sung thông tin bệnh nhân vào lịch hẹn
        Appointment appointment = getAppointment(event.getAppointmentId());
        appointment.setPatientName(event.getPatientName());
        appointment.setPatientEmail(event.getPatientEmail());
        appointment.setPatientPhone(event.getPatientPhone());
        appointmentRepository.save(appointment);

        log.info("Lịch hẹn {} đã được bổ sung thông tin bệnh nhân", appointment.getId());
    }


    @Transactional
    @Override
    public void handleRefundProcessed(PaymentRefundProcessedEvent event) {
        log.info("Xử lý kết quả hoàn tiền: appointmentId={}, thành công={}",
                event.getAppointmentId(), event.isSuccess());

        Appointment appointment = getAppointment(event.getAppointmentId());

        if (appointment.getStatus() != Status.CANCELLING) {
            log.warn("Lịch hẹn {} không ở trạng thái CANCELLING, bỏ qua xử lý hoàn tiền",
                    appointment.getId());
            return;
        }

        if (event.isSuccess()) {
            handleRefundSuccess(appointment);
        } else {
            handleRefundFailure(appointment, event.getErrorMessage());
        }
    }

    private void handleRefundSuccess(Appointment appointment) {
        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);

        // Tạo và phát sự kiện hủy lịch để giải phóng khung giờ
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(getSagaId(appointment.getId()))
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlotId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .reason(appointment.getNotes())
                .cancelledBy("USER")
                .cancelledAt(LocalDateTime.now())
                .build();
        
        eventProducer.publishAppointmentCancelled(cancelEvent);

        // Cập nhật trạng thái saga
        updateSagaState(appointment.getId(), SagaStatus.COMPENSATED,
                "REFUND_COMPLETED", null);

        log.info("Lịch hẹn {} đã được hủy thành công với hoàn tiền", appointment.getId());
    }

    private void handleRefundFailure(Appointment appointment, String errorMessage) {
        // Vẫn hủy lịch hẹn nhưng đánh dấu hoàn tiền thất bại
        appointment.setStatus(Status.CANCELLED);
        String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
        appointment.setNotes(currentNotes + " | HOÀN_TIỀN_THẤT_BẠI: " + errorMessage);
        appointmentRepository.save(appointment);

        // Vẫn giải phóng khung giờ
        AppointmentCancelledEvent cancelEvent = AppointmentCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(getSagaId(appointment.getId()))
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlotId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .reason(appointment.getNotes())
                .cancelledBy("USER")
                .cancelledAt(LocalDateTime.now())
                .build();
        
        eventProducer.publishAppointmentCancelled(cancelEvent);

        // Cập nhật trạng thái saga với lỗi
        updateSagaState(appointment.getId(), SagaStatus.FAILED,
                "REFUND_FAILED", errorMessage);

        log.warn("Lịch hẹn {} đã bị hủy nhưng hoàn tiền thất bại: {}",
                appointment.getId(), errorMessage);
    }


    private Appointment getAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }

    private void updateSagaState(UUID appointmentId, SagaStatus status,
                                 String step, String failureReason) {
        sagaStateRepository.findByAppointmentId(appointmentId)
                .ifPresent(saga -> {
                    saga.setStatus(status);
                    saga.setCurrentStep(step);
                    saga.setUpdatedAt(LocalDateTime.now());
                    if (failureReason != null) {
                        saga.setFailureReason(failureReason);
                    }
                    sagaStateRepository.save(saga);
                    log.debug("Đã cập nhật trạng thái saga: appointmentId={}, trạng thái={}, bước={}",
                            appointmentId, status, step);
                });
    }

    private UUID getSagaId(UUID appointmentId) {
        return sagaStateRepository.findByAppointmentId(appointmentId)
                .map(saga -> UUID.fromString(saga.getSagaId()))
                .orElse(null);
    }

}
