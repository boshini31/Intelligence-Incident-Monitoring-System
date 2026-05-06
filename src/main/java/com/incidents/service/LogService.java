package com.incidents.service;

import com.incidents.dto.LogDto;
import com.incidents.entity.Log;
import com.incidents.kafka.LogProducer;
import com.incidents.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for log ingestion.
 *
 * Flow:
 * 1. Receive LogRequest from controller
 * 2. Save to PostgreSQL (permanent storage)
 * 3. Publish to Kafka (for async spike detection)
 * 4. Return saved log
 *
 * Interview point: What is @Transactional?
 * - Wraps the method in a database transaction
 * - If DB save succeeds but Kafka publish fails, the DB record is NOT rolled back
 *   (Kafka is not part of the DB transaction - this is acceptable here)
 * - For full transactional guarantees with Kafka, you'd use Kafka Transactions (advanced topic)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final LogProducer logProducer;

    @Transactional
    public LogDto.LogResponse ingestLog(LogDto.LogRequest request) {
        // Convert DTO to entity
        Log logEntry = Log.builder()
                .serviceName(request.getServiceName())
                .logLevel(request.getLogLevel())
                .message(request.getMessage())
                .timestamp(request.getTimestamp())
                .build();

        // Save to DB
        Log savedLog = logRepository.save(logEntry);
        log.debug("Log saved | id={} | service={} | level={}",
                savedLog.getId(), savedLog.getServiceName(), savedLog.getLogLevel());

        // Convert to response DTO
        LogDto.LogResponse response = mapToResponse(savedLog);

        // Publish to Kafka async (non-blocking)
        logProducer.publishLog(response);

        return response;
    }

    public List<LogDto.LogResponse> getRecentLogs() {
        return logRepository.findTop50ByOrderByTimestampDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LogDto.LogResponse> getLogsByService(String serviceName) {
        return logRepository.findByServiceNameOrderByTimestampDesc(serviceName)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LogDto.LogResponse mapToResponse(Log log) {
        return LogDto.LogResponse.builder()
                .id(log.getId())
                .serviceName(log.getServiceName())
                .logLevel(log.getLogLevel())
                .message(log.getMessage())
                .timestamp(log.getTimestamp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
