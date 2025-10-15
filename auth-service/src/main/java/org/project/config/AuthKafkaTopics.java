package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class AuthKafkaTopics {
    //publish
    private String patientValidated;
    private String validationFailed;

    //consume
    private String appointmentCreated;
}
