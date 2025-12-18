package org.project.listener;

import org.project.events.AppointmentCancelledEvent;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
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
    DoctorSlotRedisCache doctorSlotRedisCache;

    // release slot khi appointment bị cancel
    @KafkaListener(
            topics = "#{@schedulingKafkaTopics.appointmentCancelled}",
            groupId = "scheduling-service",
            concurrency = "3"
    )
    @Transactional
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("Nhận AppointmentCancelledEvent: slotId={}",
                 event.getSlotId());

        doctorAvailableSlotRepository.findById(event.getSlotId()).ifPresentOrElse(
                slot -> {
                    slot.setAvailable(true);
                    doctorAvailableSlotRepository.save(slot);
                    log.info("Đã mở khóa slot: id={}", slot.getId());
                    try {
                        doctorSlotRedisCache.updateSlotAvailability(
                                slot.getDoctorId(),
                                slot.getSlotDate(),
                                slot.getId(),
                                true  // available = true
                        );
                        log.info("Đã cập nhật cache Redis cho slot: id={}", slot.getId());
                    } catch (Exception e) {
                        log.error("Lỗi khi cập nhật Redis cache cho slot {}: {}",
                                slot.getId(), e.getMessage());
                        // Cache sẽ tự động bị evict trong updateSlotAvailability nếu có lỗi
                    }
                },
                () -> log.warn("Slot {} not found for release", event.getSlotId())
        );
    }

}
