package com.incidents.kafka;

import com.incidents.dto.LogDto;
import com.incidents.service.SpikeDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer: listens for log events and triggers spike detection.
 *
 * Interview point: What is @KafkaListener?
 * - Marks this method as a Kafka message consumer
 * - groupId ensures multiple instances of this app share the work (each message processed once)
 *
 * Interview point: What happens if this consumer crashes?
 * - Kafka tracks the "offset" (which messages were consumed)
 * - On restart, the consumer picks up from where it left off (auto-offset-reset: earliest)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final SpikeDetectionService spikeDetectionService;

    @KafkaListener(
            topics = "${kafka.topics.log-ingestion}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLog(
            @Payload LogDto.LogResponse logResponse,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.debug("Consumed log from Kafka | service={} | level={} | partition={} | offset={}",
                logResponse.getServiceName(),
                logResponse.getLogLevel(),
                partition,
                offset);

        // Only process ERROR and FATAL logs for spike detection
        if (logResponse.getLogLevel() != null) {
            String levelName = logResponse.getLogLevel().name();
            if ("ERROR".equals(levelName) || "FATAL".equals(levelName)) {
                spikeDetectionService.processErrorLog(logResponse);
            }
        }
    }
}
