package org.project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final AppointmentKafkaTopics topics;

    private static final int NUM_PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 2;

    @Bean
    public NewTopic appointmentCreatedTopic() {
        return TopicBuilder.name(topics.getAppointmentCreated())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    @Bean
    public NewTopic appointmentConfirmedTopic() {
        return TopicBuilder.name(topics.getAppointmentConfirmed())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic appointmentCancelledTopic() {
        return TopicBuilder.name(topics.getAppointmentCancelled())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic appointmentCancellationInitiatedTopic() {
        return TopicBuilder.name(topics.getAppointmentCancellationInitiated())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

//    @Bean
//    public NewTopic slotReservedTopic() {
//        return TopicBuilder.name(topics.getSlotReserved())
//                .partitions(NUM_PARTITIONS)
//                .replicas(REPLICATION_FACTOR)
//                .config("min.insync.replicas", "1")
//                .config("retention.ms", "604800000")
//                .build();
//    }

    @Bean
    public NewTopic patientValidatedTopic() {
        return TopicBuilder.name(topics.getPatientValidated())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

//    @Bean
//    public NewTopic doctorValidatedTopic() {
//        return TopicBuilder.name(topics.getDoctorValidated())
//                .partitions(NUM_PARTITIONS)
//                .replicas(REPLICATION_FACTOR)
//                .config("min.insync.replicas", "1")
//                .config("retention.ms", "604800000")
//                .build();
//    }

    @Bean
    public NewTopic validationFailedTopic() {
        return TopicBuilder.name(topics.getValidationFailed())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(topics.getPaymentCompleted())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(topics.getPaymentFailed())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic paymentRefundProcessedTopic() {
        return TopicBuilder.name("payment.refund.processed")
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000")
                .build();
    }
}

