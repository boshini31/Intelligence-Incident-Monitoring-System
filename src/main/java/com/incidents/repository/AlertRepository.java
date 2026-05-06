package com.incidents.repository;

import com.incidents.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Recent alerts (used by analytics and dashboard)
    List<Alert> findTop20ByOrderByTriggeredAtDesc();

    // Alerts for a specific service
    List<Alert> findByServiceNameOrderByTriggeredAtDesc(String serviceName);

    // Count alerts by severity
    long countBySeverity(com.incidents.entity.Incident.Severity severity);
}
