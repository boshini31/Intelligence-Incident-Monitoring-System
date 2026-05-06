package com.incidents.kafka;

import com.incidents.dto.LogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer: publishes log events to the Kafka topic.
 *
 * Interview point: Why publish to Kafka instead of directly calling spike detection?
 * - Decoupling: the API doesn't wait for spike detection to finish
 * - If Kafka consumer is slow, logs still get saved (API stays fast)
 * - Kafka retains messages — if consumer crashes, it can replay from where it left off
 *
 * Interview point: What is KafkaTemplate?
 * - Spring's abstraction over Kafka producer API
 * - send() is async and returns a CompletableFuture
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, LogDto.LogResponse> kafkaTemplate;

    @Value("${kafka.topics.log-ingestion}")
    private String logIngestionTopic;

    /**
     * Publish a log entry to Kafka.
     * @param logResponse - the log that was saved to DB
     */
    public void publishLog(LogDto.LogResponse logResponse) {
        // Use serviceName as the Kafka message key
        // This ensures all logs from the same service go to the same partition (ordering guarantee)
        CompletableFuture<SendResult<String, LogDto.LogResponse>> future =
                kafkaTemplate.send(logIngestionTopic, logResponse.getServiceName(), logResponse);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Log published to Kafka | service={} | partition={} | offset={}",
                        logResponse.getServiceName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish log to Kafka | service={} | error={}",
                        logResponse.getServiceName(), ex.getMessage());
            }
        });
    }
}
