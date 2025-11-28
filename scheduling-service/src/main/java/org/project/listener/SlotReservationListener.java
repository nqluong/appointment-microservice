package org.project.listener;

import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCancelledEvent;
import org.project.repository.DoctorAvailableSlotRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotReservationListener {
    DoctorAvailableSlotRepository doctorAvailableSlotRepository;

    // release slot khi appointment bị cancel
    @KafkaListener(
            topics = "#{@schedulingKafkaTopics.appointmentCancelled}",
            groupId = "scheduling-service",
            concurrency = "3"
    )
    @Transactional
    public void handleAppointmentCancelled(AppointmentCancellationInitiatedEvent event) {
        log.info("Nhận AppointmentCancelledEvent: slotId={}",
                 event.getSlotId());

        doctorAvailableSlotRepository.findById(event.getSlotId()).ifPresentOrElse(
                slot -> {
                    slot.setAvailable(true);
                    doctorAvailableSlotRepository.save(slot);
                    log.info("Đã release slot: id={}", slot.getId());
                },
                () -> log.warn("Slot {} not found for release", event.getSlotId())
        );
    }

}
