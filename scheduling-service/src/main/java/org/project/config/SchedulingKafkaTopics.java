package org.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class SchedulingKafkaTopics {
    //publish
    private String slotReserved;
    private String validationFailed;

    //consume
    private String appointmentCreated;
    private String appointmentConfirmed;
    private String appointmentCancelled;

}
