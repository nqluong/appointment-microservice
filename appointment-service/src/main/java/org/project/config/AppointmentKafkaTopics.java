package org.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class AppointmentKafkaTopics {
    //publish
    private String appointmentCreated;
    private String appointmentConfirmed;
    private String appointmentCancelled;

    //consume
    private String slotReserved;
    private String patientValidated;
    private String doctorValidated;
    private String validationFailed;
}
