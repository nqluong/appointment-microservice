package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class AppointmentKafkaTopics {
    //publish
    private String appointmentCreated;
    private String appointmentConfirmed;
    private String appointmentCancelled;
    private String appointmentCancellationInitiated;

    //consume
    private String slotReserved;
    private String patientValidated;
    private String doctorValidated;
    private String validationFailed;
    private String paymentCompleted;
    private String paymentFailed;
}
