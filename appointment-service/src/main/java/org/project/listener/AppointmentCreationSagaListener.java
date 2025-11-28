//package org.project.listener;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//
//import org.project.config.AppointmentKafkaTopics;
//import org.project.enums.SagaStatus;
//import org.project.enums.Status;
//import org.project.events.AppointmentCancelledEvent;
//import org.project.events.PatientValidatedEvent;
//import org.project.events.PaymentRefundProcessedEvent;
//import org.project.events.ValidationFailedEvent;
//import org.project.exception.CustomException;
//import org.project.exception.ErrorCode;
//import org.project.model.Appointment;
//import org.project.model.AppointmentSagaState;
//import org.project.producer.AppointmentEventProducer;
//import org.project.repository.AppointmentRepository;
//import org.project.repository.SagaStateRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class AppointmentCreationSagaListener {
//    AppointmentRepository appointmentRepository;
//    SagaStateRepository sagaStateRepository;
//    KafkaTemplate<String, Object> kafkaTemplate;
//    AppointmentKafkaTopics topics;
//    AppointmentEventProducer appointmentEventProducer;
//
//
//    @KafkaListener(
//            topics = "#{@appointmentKafkaTopics.validationFailed}",
//            groupId = "appointment-service",
//            concurrency = "3"
//    )
//    @Transactional
//    public void handleValidationFailed(ValidationFailedEvent event, Acknowledgment ack) {
//        log.error("Validation failed: sagaId={}, reason={}",
//                event.getSagaId(), event.getReason());
//        // Cập nhật saga state
//        AppointmentSagaState sagaState = sagaStateRepository.findById(event.getSagaId())
//                .orElseThrow();
//        sagaState.setStatus(SagaStatus.COMPENSATING);
//        sagaState.setFailureReason(event.getReason());
//        sagaStateRepository.save(sagaState);
//
//        // Cancel appointment
//        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
//                .orElseThrow();
//        appointment.setStatus(Status.CANCELLED);
//        appointmentRepository.save(appointment);
//
//        appointmentEventProducer.publishAppointmentCancelled(
//                appointment.getId(),
//                appointment.getPatientUserId(),
//                appointment.getDoctorUserId(),
//                appointment.getSlotId(),
//                event.getReason(),
//                "SYSTEM",
//                appointment.getAppointmentDate(),
//                appointment.getStartTime()
//        );
//
//        sagaState.setStatus(SagaStatus.COMPENSATED);
//        sagaStateRepository.save(sagaState);
//        String partitionKey = event.getAppointmentId().toString();
//
//        log.info("Đã compensate appointment: id={}, partitionKey={}", appointment.getId(), partitionKey);
//
//        ack.acknowledge();
//    }
//
//    @KafkaListener(
//            topics = "#{@appointmentKafkaTopics.patientValidated}",
//            groupId = "appointment-service",
//            concurrency = "3"
//    )
//    @Transactional
//    public void handlePatientValidated(PatientValidatedEvent event, Acknowledgment ack) {
//        log.info("Nhận PatientValidatedEvent: sagaId={}", event.getSagaId());
//
//        updateSagaState(event.getSagaId(), SagaStatus.COMPLETED, "COMPLETED");
//
//        Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
//                .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
//
//        appointment.setPatientName(event.getPatientName());
//        appointment.setPatientEmail(event.getPatientEmail());
//        appointment.setPatientPhone(event.getPatientPhone());
//
//        appointmentRepository.save(appointment);
//
//        log.info("Appointment đã được enrich đầy đủ thông tin patient: id={}", appointment.getId());
//
//        ack.acknowledge();
//    }
//
//    @KafkaListener(
//            topics = "payment.refund.processed",
//            groupId = "appointment-service",
//            concurrency = "3"
//    )
//    @Transactional
//    public void handlePaymentRefundProcessed(PaymentRefundProcessedEvent event, Acknowledgment ack) {
//        log.info("Nhận PaymentRefundProcessedEvent: appointmentId={}, success={}",
//                event.getAppointmentId(), event.isSuccess());
//
//        try {
//            Appointment appointment = appointmentRepository.findById(event.getAppointmentId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.APPOINTMENT_NOT_FOUND));
//
//            // Chỉ xử lý nếu appointment đang ở trạng thái CANCELLING
//            if (appointment.getStatus() == Status.CANCELLING) {
//                if (event.isSuccess()) {
//                    // Refund thành công, chuyển sang CANCELLED và publish event cancelled
//                    appointment.setStatus(Status.CANCELLED);
//                    appointmentRepository.save(appointment);
//
//                    // Publish event cancelled để scheduling service có thể release slot
//                    appointmentEventProducer.publishAppointmentCancelled(
//                            appointment.getId(),
//                            appointment.getPatientUserId(),
//                            appointment.getDoctorUserId(),
//                            appointment.getSlotId(),
//                            appointment.getNotes(),
//                            "SYSTEM",
//                            appointment.getAppointmentDate(),
//                            appointment.getStartTime()
//                    );
//
//                    log.info("Appointment {} đã được cancelled sau khi refund thành công", appointment.getId());
//                } else {
//                    // Refund thất bại, vẫn chuyển sang CANCELLED nhưng ghi log lỗi
//                    appointment.setStatus(Status.CANCELLED);
//                    appointment.setNotes(appointment.getNotes() + " | REFUND_FAILED: " + event.getErrorMessage());
//                    appointmentRepository.save(appointment);
//
//                    // Vẫn publish event cancelled để release slot
//                    appointmentEventProducer.publishAppointmentCancelled(
//                            appointment.getId(),
//                            appointment.getPatientUserId(),
//                            appointment.getDoctorUserId(),
//                            appointment.getSlotId(),
//                            appointment.getNotes(),
//                            "SYSTEM",
//                            appointment.getAppointmentDate(),
//                            appointment.getStartTime()
//                    );
//
//                    log.warn("Appointment {} đã được cancelled nhưng refund thất bại: {}",
//                            appointment.getId(), event.getErrorMessage());
//                }
//            } else {
//                log.warn("Appointment {} không ở trạng thái CANCELLING, bỏ qua event refund",
//                        appointment.getId());
//            }
//        } catch (Exception e) {
//            log.error("Lỗi khi xử lý PaymentRefundProcessedEvent cho appointment: {}",
//                    event.getAppointmentId(), e);
//        } finally {
//            ack.acknowledge();
//        }
//    }
//
//    private void updateSagaState(String sagaId, SagaStatus status, String step) {
//        AppointmentSagaState sagaState = sagaStateRepository.findById(sagaId)
//                .orElseThrow();
//        sagaState.setStatus(status);
//        sagaState.setCurrentStep(step);
//        sagaState.setUpdatedAt(LocalDateTime.now());
//        sagaStateRepository.save(sagaState);
//    }
//
//}
