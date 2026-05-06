package com.incidents.controller;

import com.incidents.dto.ApiResponse;
import com.incidents.dto.LogDto;
import com.incidents.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for log ingestion and retrieval.
 *
 * Interview point: What happens when POST /api/logs is called?
 * 1. Request body is validated (@Valid)
 * 2. LogService.ingestLog() saves to DB and publishes to Kafka
 * 3. Kafka consumer (async) runs SpikeDetectionService
 * 4. Response is returned immediately (non-blocking)
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Log Ingestion", description = "Submit and retrieve logs from services")
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final LogService logService;

    @PostMapping
    @Operation(summary = "Ingest a log", description = "External services use this to submit logs. ERROR logs trigger spike detection via Kafka.")
    public ResponseEntity<ApiResponse<LogDto.LogResponse>> ingestLog(
            @Valid @RequestBody LogDto.LogRequest request) {
        LogDto.LogResponse response = logService.ingestLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Log ingested successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get recent logs", description = "Returns the 50 most recent logs across all services")
    public ResponseEntity<ApiResponse<List<LogDto.LogResponse>>> getRecentLogs() {
        return ResponseEntity.ok(ApiResponse.success(logService.getRecentLogs()));
    }

    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get logs by service", description = "Filter logs for a specific service name")
    public ResponseEntity<ApiResponse<List<LogDto.LogResponse>>> getLogsByService(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(ApiResponse.success(logService.getLogsByService(serviceName)));
    }
}
