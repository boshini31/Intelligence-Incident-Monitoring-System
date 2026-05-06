package com.incidents.controller;

import com.incidents.dto.ApiResponse;
import com.incidents.dto.IncidentDto;
import com.incidents.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "View and manage grouped incidents")
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Get all incidents")
    public ResponseEntity<ApiResponse<List<IncidentDto.IncidentResponse>>> getAllIncidents() {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getAllIncidents()));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open incidents", description = "Returns only incidents with OPEN status")
    public ResponseEntity<ApiResponse<List<IncidentDto.IncidentResponse>>> getOpenIncidents() {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getOpenIncidents()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<ApiResponse<IncidentDto.IncidentResponse>> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidentById(id)));
    }

    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get incidents by service name")
    public ResponseEntity<ApiResponse<List<IncidentDto.IncidentResponse>>> getByService(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidentsByService(serviceName)));
    }

    @PatchMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge an incident", description = "Mark incident as acknowledged (being investigated)")
    public ResponseEntity<ApiResponse<IncidentDto.IncidentResponse>> acknowledgeIncident(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Incident acknowledged", incidentService.acknowledgeIncident(id)));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve an incident", description = "Mark incident as resolved")
    public ResponseEntity<ApiResponse<IncidentDto.IncidentResponse>> resolveIncident(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Incident resolved", incidentService.resolveIncident(id)));
    }
}
