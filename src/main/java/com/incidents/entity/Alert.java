package com.incidents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an alert triggered when an incident exceeds the error threshold.
 *
 * Flow: Log → Kafka → Consumer → SpikeDetection → if threshold exceeded → Alert
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_incident", columnList = "incident_id"),
        @Index(name = "idx_alert_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Incident.Severity severity;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.triggeredAt == null) this.triggeredAt = LocalDateTime.now();
    }
}
