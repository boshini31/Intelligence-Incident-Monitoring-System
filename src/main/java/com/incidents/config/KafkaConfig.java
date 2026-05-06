package com.incidents.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration.
 *
 * Interview point: What is Kafka and why use it here?
 * - Kafka is a distributed message broker
 * - Instead of calling spike detection synchronously (blocking the API),
 *   we publish to Kafka and a separate consumer handles processing asynchronously
 * - Benefits: faster API response, decoupled processing, resilience
 *
 * Interview point: What are partitions and replication factor?
 * - Partitions: how many parallel consumers can read simultaneously
 * - Replication factor: how many copies of data across brokers (for fault tolerance)
 * - For local dev, we use 1 partition + 1 replica
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.log-ingestion}")
    private String logIngestionTopic;

    @Value("${kafka.topics.alert-notification}")
    private String alertNotificationTopic;

    @Bean
    public NewTopic logIngestionTopic() {
        return TopicBuilder.name(logIngestionTopic)
                .partitions(3)       // 3 partitions for parallel processing
                .replicas(1)         // 1 replica (fine for local dev)
                .build();
    }

    @Bean
    public NewTopic alertNotificationTopic() {
        return TopicBuilder.name(alertNotificationTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
