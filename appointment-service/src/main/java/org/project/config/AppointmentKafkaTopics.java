package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class AppointmentKafkaTopics {


    //Booking
    private String appointmentCreated;
    private String paymentCompleted;
    private String paymentFailed;

    //Validate
    private String validationFailed;
    private String patientValidated;

    //Cancellation
    private String appointmentCancellationInitiated;
    private String appointmentCancelled;

    //Refund
    private String refundProcessed;

    //Notifications
    private String appointmentConfirmed;
}
