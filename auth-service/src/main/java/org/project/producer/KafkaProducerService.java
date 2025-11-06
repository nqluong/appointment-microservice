package org.project.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.config.AuthKafkaTopics;
import org.project.events.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuthKafkaTopics authKafkaTopics;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send(authKafkaTopics.getUserRegistered(), event.getUserId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("User registered event sent successfully for userId: {}", event.getUserId());
                        } else {
                            log.error("Failed to send user registered event for userId: {}", event.getUserId(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending user registered event for userId: {}", event.getUserId(), e);
        }
    }
}
