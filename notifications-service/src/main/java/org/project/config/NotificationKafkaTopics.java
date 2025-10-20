package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class NotificationKafkaTopics {
    private String appointmentConfirmed;
    private String appointmentCancelled;
}

