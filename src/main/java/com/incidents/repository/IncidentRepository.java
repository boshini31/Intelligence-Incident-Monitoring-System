package com.incidents.repository;

import com.incidents.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Used for grouping: find existing open incident with same groupKey
    Optional<Incident> findByGroupKeyAndStatus(String groupKey, Incident.Status status);

    // All open incidents
    List<Incident> findByStatusOrderByLastSeenDesc(Incident.Status status);

    // Incidents for a specific service
    List<Incident> findByServiceNameOrderByLastSeenDesc(String serviceName);

    // Count open incidents
    long countByStatus(Incident.Status status);

    // Service-level stats
    @Query("SELECT i.serviceName, COUNT(i) FROM Incident i GROUP BY i.serviceName ORDER BY COUNT(i) DESC")
    List<Object[]> findIncidentCountPerService();
}
