package com.incidents.service;

import com.incidents.dto.LogDto;
import com.incidents.entity.Alert;
import com.incidents.entity.Incident;
import com.incidents.repository.AlertRepository;
import com.incidents.repository.IncidentRepository;
import com.incidents.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core intelligence module: detects error spikes and creates incidents/alerts.
 *
 * Algorithm:
 * 1. For each ERROR log consumed from Kafka:
 *    - Build a groupKey (serviceName + normalizedMessage)
 *    - Increment a Redis counter for that key (with 5-minute TTL)
 *    - If counter exceeds threshold → create/update incident → generate alert
 *
 * Interview point: Why Redis for counting instead of querying the DB?
 * - Redis INCR is atomic and O(1) - counting in DB would require a COUNT query
 * - Redis auto-expires the counter after 5 minutes (the detection window)
 * - Much faster: Redis can handle 100K+ ops/sec vs DB's ~10K
 *
 * Interview point: What is the groupKey?
 * - serviceName + first 30 chars of normalized message
 * - Groups "Payment timeout" and "Payment timeout at gateway" under one incident
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpikeDetectionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final LogRepository logRepository;

    @Value("${incident.detection.error-threshold}")
    private int errorThreshold;

    @Value("${incident.detection.window-minutes}")
    private int windowMinutes;

    private static final String REDIS_KEY_PREFIX = "spike:";

    /**
     * Main entry point called by the Kafka consumer.
     * Processes an ERROR log and decides if an alert should be created.
     */
    @Transactional
    public void processErrorLog(LogDto.LogResponse logResponse) {
        String groupKey = buildGroupKey(logResponse.getServiceName(), logResponse.getMessage());
        String redisKey = REDIS_KEY_PREFIX + groupKey;

        // Atomically increment the error counter for this group key
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        // Set expiry on first increment (so counter resets after the time window)
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofMinutes(windowMinutes));
        }

        log.debug("Error spike counter | key={} | count={} | threshold={}",
                groupKey, currentCount, errorThreshold);

        // Check if we've hit the threshold
        if (currentCount != null && currentCount >= errorThreshold) {
            // Only create alert on exact threshold (not on every subsequent log)
            if (currentCount == errorThreshold) {
                log.warn("ERROR SPIKE DETECTED | service={} | count={} | key={}",
                        logResponse.getServiceName(), currentCount, groupKey);
                createOrUpdateIncidentAndAlert(logResponse, groupKey, currentCount.intValue());
            } else {
                // Update existing incident's lastSeen and count
                updateIncidentCount(groupKey, currentCount.intValue(), logResponse);
            }
        }
    }

    /**
     * Create a new incident (or find existing one by groupKey) and generate an alert.
     */
    private void createOrUpdateIncidentAndAlert(
            LogDto.LogResponse logResponse,
            String groupKey,
            int errorCount
    ) {
        // Try to find an existing OPEN incident with this groupKey
        Optional<Incident> existingIncident = incidentRepository
                .findByGroupKeyAndStatus(groupKey, Incident.Status.OPEN);

        Incident incident;
        if (existingIncident.isPresent()) {
            // Update existing incident
            incident = existingIncident.get();
            incident.setOccurrenceCount(incident.getOccurrenceCount() + errorCount);
            incident.setLastSeen(LocalDateTime.now());
            incident = incidentRepository.save(incident);
            log.info("Updated existing incident | id={} | service={}", incident.getId(), incident.getServiceName());
        } else {
            // Create new incident
            Incident.Severity severity = determineSeverity(errorCount);
            incident = Incident.builder()
                    .serviceName(logResponse.getServiceName())
                    .groupKey(groupKey)
                    .message(logResponse.getMessage())
                    .severity(severity)
                    .status(Incident.Status.OPEN)
                    .occurrenceCount(errorCount)
                    .firstSeen(LocalDateTime.now())
                    .lastSeen(LocalDateTime.now())
                    .build();
            incident = incidentRepository.save(incident);
            log.info("New incident created | id={} | service={} | severity={}",
                    incident.getId(), incident.getServiceName(), incident.getSeverity());
        }

        // Create an alert linked to this incident
        Alert alert = Alert.builder()
                .incident(incident)
                .serviceName(logResponse.getServiceName())
                .message(String.format(
                    "ALERT: Service '%s' has exceeded error threshold! %d errors in %d minutes. Message: %s",
                    logResponse.getServiceName(), errorCount, windowMinutes, logResponse.getMessage()
                ))
                .severity(incident.getSeverity())
                .errorCount(errorCount)
                .triggeredAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);

        // Simulate email notification
        sendSimulatedEmailAlert(alert);

        // Clear Redis cache for analytics (stale data after new alert)
        redisTemplate.delete("analytics:summary");
    }

    /**
     * Update incident count for ongoing spikes.
     */
    private void updateIncidentCount(String groupKey, int count, LogDto.LogResponse logResponse) {
        incidentRepository.findByGroupKeyAndStatus(groupKey, Incident.Status.OPEN)
                .ifPresent(incident -> {
                    incident.setOccurrenceCount(count);
                    incident.setLastSeen(LocalDateTime.now());
                    incidentRepository.save(incident);
                });
    }

    /**
     * Build a normalized groupKey from service name and message.
     * "Payment timeout" and "Payment timeout at gateway" → same group
     */
    public String buildGroupKey(String serviceName, String message) {
        // Normalize: lowercase, remove special chars, take first 40 chars
        String normalizedMessage = message
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .substring(0, Math.min(40, message.length()));

        return serviceName.toLowerCase() + ":" + normalizedMessage;
    }

    /**
     * Determine alert severity based on error count.
     */
    private Incident.Severity determineSeverity(int errorCount) {
        if (errorCount >= 50) return Incident.Severity.CRITICAL;
        if (errorCount >= 25) return Incident.Severity.HIGH;
        if (errorCount >= 10) return Incident.Severity.MEDIUM;
        return Incident.Severity.LOW;
    }

    /**
     * Simulate sending an email alert (printed to logs).
     * In production, this would integrate with SendGrid, SES, or similar.
     */
    private void sendSimulatedEmailAlert(Alert alert) {
        log.warn("""
            ╔══════════════════════════════════════════════════════╗
            ║            📧 SIMULATED EMAIL ALERT                  ║
            ╠══════════════════════════════════════════════════════╣
            ║ To:       ops-team@company.com                       ║
            ║ Subject:  [{}] Incident Alert - {}
            ║ Body:     {}
            ║ Errors:   {} in {} minutes                           ║
            ╚══════════════════════════════════════════════════════╝""",
                alert.getSeverity(),
                alert.getServiceName(),
                alert.getMessage(),
                alert.getErrorCount(),
                windowMinutes);
    }
}
