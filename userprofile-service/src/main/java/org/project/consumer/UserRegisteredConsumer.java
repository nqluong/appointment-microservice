package org.project.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.events.UserRegisteredEvent;
import org.project.service.UserProfileService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(
            topics = "${kafka.topics.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserRegisteredEvent(
            UserRegisteredEvent event,
            Acknowledgment acknowledgment) {

        try {

            userProfileService.createEmptyProfile(event);

            acknowledgment.acknowledge();
            log.info("Successfully created profile for userId: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Error processing user registered event for userId: {}", event.getUserId(), e);
            throw e;
        }
    }
}
