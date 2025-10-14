package org.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class UserProfileKafkaTopics {
    // publish
    private String doctorValidated;
    private String validationFailed;

    // consume
    private String patientValidated;
}
