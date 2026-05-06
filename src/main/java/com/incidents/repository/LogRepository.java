package com.incidents.repository;

import com.incidents.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Log entities.
 * Contains custom queries for analytics and spike detection.
 *
 * Interview point: What is @Query?
 * - Allows writing JPQL (Java Persistence Query Language) directly
 * - JPQL looks like SQL but operates on Java objects, not tables
 * - Use nativeQuery=true for raw SQL when needed
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    // Used by spike detector to count recent errors per service
    long countByServiceNameAndLogLevelAndTimestampAfter(
            String serviceName,
            Log.LogLevel logLevel,
            LocalDateTime after
    );

    // Fetch recent logs by service
    List<Log> findByServiceNameOrderByTimestampDesc(String serviceName);

    // Count errors in last 24 hours
    @Query("SELECT COUNT(l) FROM Log l WHERE l.logLevel = 'ERROR' AND l.timestamp >= :since")
    long countErrorsSince(@Param("since") LocalDateTime since);

    // Get most affected service (highest error count)
    @Query("SELECT l.serviceName, COUNT(l) as cnt FROM Log l WHERE l.logLevel = 'ERROR' " +
           "GROUP BY l.serviceName ORDER BY cnt DESC LIMIT 1")
    List<Object[]> findMostAffectedService();

    // Error trend: errors per service
    @Query("SELECT l.serviceName, COUNT(l) as cnt FROM Log l WHERE l.logLevel = 'ERROR' " +
           "GROUP BY l.serviceName ORDER BY cnt DESC")
    List<Object[]> findErrorCountPerService();

    // Paginated recent logs
    List<Log> findTop50ByOrderByTimestampDesc();
}
