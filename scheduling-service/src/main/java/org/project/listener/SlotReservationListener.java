package org.project.listener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.config.SchedulingKafkaTopics;
import org.project.dto.events.*;
import org.project.exception.CustomException;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotReservationListener {
    DoctorAvailableSlotRepository doctorAvailableSlotRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    SchedulingKafkaTopics topics;

    @KafkaListener(
            topics = "#{@schedulingKafkaTopics.appointmentCreated}",
            groupId = "scheduling-service"
    )
    @Transactional
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Nhận AppointmentCreatedEvent: sagaId={}, slotId={}",
                event.getSagaId(), event.getSlotId());

        try {
            // Lấy và lock slot
            DoctorAvailableSlot slot = doctorAvailableSlotRepository.findByIdWithLock(event.getSlotId())
                    .orElseThrow(() -> new CustomException("Slot không tồn tại"));

            // Validate slot
            if (!slot.isAvailable()) {
                publishValidationFailed(event, "Slot đã được đặt");
                return;
            }

            if (!slot.getDoctorId().equals(event.getDoctorUserId())) {
                publishValidationFailed(event, "Slot không thuộc bác sĩ này");
                return;
            }

            LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
            if (slotDateTime.isBefore(LocalDateTime.now())) {
                publishValidationFailed(event, "Slot đã qua thời gian");
                return;
            }

            // Reserve slot
            slot.setAvailable(false);
            slot.setReservedBy(event.getPatientUserId());
            slot.setReservedAt(LocalDateTime.now());
            doctorAvailableSlotRepository.save(slot);

            // Publish success event
            SlotReservedEvent reservedEvent = SlotReservedEvent.builder()
                    .sagaId(String.valueOf(event.getSagaId()))
                    .slotId(slot.getId())
                    .appointmentId(event.getAppointmentId())
                    .appointmentDate(slot.getSlotDate())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .doctorUserId(slot.getDoctorId())
                    .reservedBy(event.getPatientUserId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(topics.getSlotReserved(), String.valueOf(event.getSagaId()), reservedEvent);

            log.info("Đã reserve slot: id={}, sagaId={}", slot.getId(), event.getSagaId());

        } catch (Exception e) {
            log.error("Lỗi khi reserve slot");
            publishValidationFailed(event, "Lỗi hệ thống khi reserve slot");
        }
    }

    @KafkaListener(
            topics = "#{@schedulingKafkaTopics.appointmentCancelled}",
            groupId = "scheduling-service"
    )
    @Transactional
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("Nhận AppointmentCancelledEvent: sagaId={}, slotId={}",
                event.getSagaId(), event.getSlotId());

        DoctorAvailableSlot slot = doctorAvailableSlotRepository.findById(event.getSlotId())
                .orElse(null);

        if (slot != null) {
            slot.setAvailable(true);
            slot.setReservedBy(null);
            slot.setReservedAt(null);
            doctorAvailableSlotRepository.save(slot);

            log.info("Đã release slot: id={}", slot.getId());
        }
    }


    @KafkaListener(
            topics = "#{@schedulingKafkaTopics.appointmentConfirmed}",
            groupId = "scheduling-service"
    )
    @Transactional
    public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
        log.info("Nhận AppointmentConfirmedEvent: slotId={}", event.getSlotId());

        DoctorAvailableSlot slot = doctorAvailableSlotRepository.findById(event.getSlotId())
                .orElseThrow();

        // Slot đã reserved rồi, chỉ cần đảm bảo isAvailable=false
        slot.setAvailable(false);
        doctorAvailableSlotRepository.save(slot);

        log.info("Slot đã được confirm unavailable: id={}", slot.getId());
    }

    private void publishValidationFailed(AppointmentCreatedEvent originalEvent, String reason) {
        ValidationFailedEvent failedEvent = ValidationFailedEvent.builder()
                .sagaId(String.valueOf(originalEvent.getSagaId()))
                .appointmentId(originalEvent.getAppointmentId())
                .reason(reason)
                .failedService("scheduling-service")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("validation-failed-topic", String.valueOf(originalEvent.getSagaId()), failedEvent);

        log.warn("Đã publish ValidationFailedEvent: reason={}", reason);
    }
}
