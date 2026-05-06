package com.incidents.controller;

import com.incidents.dto.ApiResponse;
import com.incidents.dto.IncidentDto;
import com.incidents.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Analytics APIs — data feeds for the dashboard.
 *
 * Interview point: How does caching work here?
 * - GET /summary → AnalyticsService.getSummary() → @Cacheable checks Redis first
 * - If cache hit → returns in microseconds
 * - If cache miss → queries DB, stores result in Redis with 5-min TTL
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "System-wide metrics and trends (Redis cached)")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary",
               description = "Returns total logs, incidents, alerts, most affected service. Cached in Redis for 5 minutes.")
    public ResponseEntity<ApiResponse<IncidentDto.AnalyticsSummary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSummary()));
    }

    @GetMapping("/service-stats")
    @Operation(summary = "Get per-service error and incident counts",
               description = "Returns error trends grouped by service. Use this for bar/pie charts.")
    public ResponseEntity<ApiResponse<List<IncidentDto.ServiceStats>>> getServiceStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getServiceStats()));
    }

    @GetMapping("/recent-alerts")
    @Operation(summary = "Get 20 most recent alerts",
               description = "Used by the dashboard to show the alert feed.")
    public ResponseEntity<ApiResponse<List<IncidentDto.AlertResponse>>> getRecentAlerts() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRecentAlerts()));
    }
}
