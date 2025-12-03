package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class PaymentKafkaTopics {
    private String paymentCompleted;
    private String paymentFailed;
    private String paymentRefundProcessed;

    public String getPaymentCompleted() {
        return paymentCompleted != null ? paymentCompleted : "payment.completed";
    }

    public String getPaymentFailed() {
        return paymentFailed != null ? paymentFailed : "payment.failed";
    }

    public String getPaymentRefundProcessed() {
        return paymentRefundProcessed != null ? paymentRefundProcessed : "payment.refund.processed";
    }
}

