package org.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class AuthKafkaTopics {
    //publish
    private String patientValidated;
    private String validationFailed;

    //consume
    private String slotReserved;
}
