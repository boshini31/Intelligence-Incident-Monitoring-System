package com.incidents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a log entry received from external services.
 * This is the core ingestion model.
 *
 * Interview point: Why not use a NoSQL DB for logs?
 * - PostgreSQL is sufficient for mid-scale
 * - We get ACID, JPA, and easy querying
 * - For high scale (millions/day), you'd switch to Elasticsearch/Cassandra
 */
@Entity
@Table(name = "logs", indexes = {
        @Index(name = "idx_log_service", columnList = "service_name"),
        @Index(name = "idx_log_level", columnList = "log_level"),
        @Index(name = "idx_log_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false)
    private LogLevel logLevel;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL
    }
}
