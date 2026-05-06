package com.incidents.service;

import com.incidents.dto.IncidentDto;
import com.incidents.entity.Alert;
import com.incidents.entity.Incident;
import com.incidents.repository.AlertRepository;
import com.incidents.repository.IncidentRepository;
import com.incidents.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics service with Redis caching.
 *
 * Interview point: What is @Cacheable?
 * - On first call, runs the method and stores result in Redis
 * - On subsequent calls (within TTL), returns cached result without hitting DB
 * - Cache key = method name + parameters
 * - TTL configured in RedisConfig (5 minutes)
 *
 * Interview point: When do you invalidate the cache?
 * - When a new alert is created in SpikeDetectionService, we delete "analytics:summary"
 * - Could also use @CacheEvict annotation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LogRepository logRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;

    @Cacheable(value = "analytics", key = "'summary'")
    public IncidentDto.AnalyticsSummary getSummary() {
        log.debug("Fetching analytics summary from DB (cache miss)");

        long totalLogs = logRepository.count();
        long totalIncidents = incidentRepository.count();
        long totalAlerts = alertRepository.count();
        long openIncidents = incidentRepository.countByStatus(Incident.Status.OPEN);
        long errorsLast24h = logRepository.countErrorsSince(LocalDateTime.now().minusHours(24));

        // Find most affected service
        List<Object[]> topService = logRepository.findMostAffectedService();
        String mostAffectedService = topService.isEmpty()
                ? "N/A"
                : (String) topService.get(0)[0];

        return IncidentDto.AnalyticsSummary.builder()
                .totalLogs(totalLogs)
                .totalIncidents(totalIncidents)
                .totalAlerts(totalAlerts)
                .openIncidents(openIncidents)
                .mostAffectedService(mostAffectedService)
                .errorLogsLast24Hours(errorsLast24h)
                .build();
    }

    @Cacheable(value = "analytics", key = "'service-stats'")
    public List<IncidentDto.ServiceStats> getServiceStats() {
        log.debug("Fetching service stats from DB (cache miss)");

        List<Object[]> errorCounts = logRepository.findErrorCountPerService();
        List<Object[]> incidentCounts = incidentRepository.findIncidentCountPerService();

        return errorCounts.stream().map(row -> {
            String serviceName = (String) row[0];
            long errorCount = (Long) row[1];

            // Find incident count for this service
            long incidentCount = incidentCounts.stream()
                    .filter(iRow -> serviceName.equals(iRow[0]))
                    .mapToLong(iRow -> (Long) iRow[1])
                    .findFirst()
                    .orElse(0L);

            return IncidentDto.ServiceStats.builder()
                    .serviceName(serviceName)
                    .errorCount(errorCount)
                    .incidentCount(incidentCount)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<IncidentDto.AlertResponse> getRecentAlerts() {
        return alertRepository.findTop20ByOrderByTriggeredAtDesc()
                .stream()
                .map(this::mapAlertToResponse)
                .collect(Collectors.toList());
    }

    private IncidentDto.AlertResponse mapAlertToResponse(Alert alert) {
        return IncidentDto.AlertResponse.builder()
                .id(alert.getId())
                .incidentId(alert.getIncident().getId())
                .serviceName(alert.getServiceName())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .errorCount(alert.getErrorCount())
                .triggeredAt(alert.getTriggeredAt())
                .build();
    }
}
