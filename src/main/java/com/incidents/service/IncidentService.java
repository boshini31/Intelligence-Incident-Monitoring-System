package com.incidents.service;

import com.incidents.dto.IncidentDto;
import com.incidents.entity.Incident;
import com.incidents.exception.ResourceNotFoundException;
import com.incidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public List<IncidentDto.IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IncidentDto.IncidentResponse> getOpenIncidents() {
        return incidentRepository.findByStatusOrderByLastSeenDesc(Incident.Status.OPEN)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IncidentDto.IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
        return mapToResponse(incident);
    }

    @Transactional
    public IncidentDto.IncidentResponse acknowledgeIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        incident.setStatus(Incident.Status.ACKNOWLEDGED);
        incidentRepository.save(incident);
        log.info("Incident acknowledged | id={}", id);
        return mapToResponse(incident);
    }

    @Transactional
    public IncidentDto.IncidentResponse resolveIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        incident.setStatus(Incident.Status.RESOLVED);
        incidentRepository.save(incident);
        log.info("Incident resolved | id={}", id);
        return mapToResponse(incident);
    }

    public List<IncidentDto.IncidentResponse> getIncidentsByService(String serviceName) {
        return incidentRepository.findByServiceNameOrderByLastSeenDesc(serviceName)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private IncidentDto.IncidentResponse mapToResponse(Incident incident) {
        return IncidentDto.IncidentResponse.builder()
                .id(incident.getId())
                .serviceName(incident.getServiceName())
                .groupKey(incident.getGroupKey())
                .message(incident.getMessage())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .occurrenceCount(incident.getOccurrenceCount())
                .firstSeen(incident.getFirstSeen())
                .lastSeen(incident.getLastSeen())
                .build();
    }
}
