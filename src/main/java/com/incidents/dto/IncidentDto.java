package com.incidents.dto;

import com.incidents.entity.Incident;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs for Incident and Alert responses.
 */
public class IncidentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentResponse {
        private Long id;
        private String serviceName;
        private String groupKey;
        private String message;
        private Incident.Severity severity;
        private Incident.Status status;
        private Integer occurrenceCount;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertResponse {
        private Long id;
        private Long incidentId;
        private String serviceName;
        private String message;
        private Incident.Severity severity;
        private Integer errorCount;
        private LocalDateTime triggeredAt;
    }

    /**
     * Analytics summary returned by GET /api/analytics/summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsSummary {
        private long totalLogs;
        private long totalIncidents;
        private long totalAlerts;
        private long openIncidents;
        private String mostAffectedService;
        private long errorLogsLast24Hours;
    }

    /**
     * Service-level breakdown for trend charts.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStats {
        private String serviceName;
        private long errorCount;
        private long incidentCount;
    }
}
