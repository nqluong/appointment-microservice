package org.project.listener;

import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorProfileUpdateListener {

//    DoctorProfileCacheService doctorProfileCacheService;
//
//    /**
//     * Listen to doctor profile update events and invalidate cache
//     */
//    @KafkaListener(
//            topics = "doctor-profile-updated-topic",
//            groupId = "userprofile-service-cache",
//            concurrency = "2"
//    )
//    public void handleDoctorProfileUpdated(Map<String, Object> event, Acknowledgment acknowledgment) {
//        try {
//            UUID doctorId = UUID.fromString(event.get("doctorId").toString());
//
//            log.info("Received DoctorProfileUpdatedEvent for doctorId: {}", doctorId);
//
//            // Invalidate cache
//            doctorProfileCacheService.invalidateDoctorProfile(doctorId);
//
//            acknowledgment.acknowledge();
//
//            log.info("Successfully invalidated doctor profile cache for doctorId: {}", doctorId);
//
//        } catch (Exception e) {
//            log.error("Error processing DoctorProfileUpdatedEvent", e);
//            acknowledgment.acknowledge(); // Still acknowledge to avoid reprocessing
//        }
//    }
}

