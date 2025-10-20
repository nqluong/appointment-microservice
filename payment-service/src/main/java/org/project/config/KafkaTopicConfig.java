package org.project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final PaymentKafkaTopics topics;

    // Số partition và replication factor
    private static final int NUM_PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 2;

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(topics.getPaymentCompleted())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(topics.getPaymentFailed())
                .partitions(NUM_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("min.insync.replicas", "1")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }
}

