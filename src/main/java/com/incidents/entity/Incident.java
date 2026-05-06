package com.incidents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a grouped incident - a cluster of similar error logs.
 *
 * Interview point: How do you group similar incidents?
 * - Same service name + similar message substring = same incident group
 * - We use a "groupKey" which is serviceName + normalized message prefix
 */
@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incident_service", columnList = "service_name"),
        @Index(name = "idx_incident_group_key", columnList = "group_key"),
        @Index(name = "idx_incident_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "group_key", nullable = false)
    private String groupKey;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount;

    @Column(name = "first_seen")
    private LocalDateTime firstSeen;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.occurrenceCount == null) this.occurrenceCount = 1;
        if (this.status == null) this.status = Status.OPEN;
        if (this.firstSeen == null) this.firstSeen = LocalDateTime.now();
        if (this.lastSeen == null) this.lastSeen = LocalDateTime.now();
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN, ACKNOWLEDGED, RESOLVED
    }
}
