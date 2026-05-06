package com.incidents.dto;

import com.incidents.entity.Log;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs for Log ingestion and response.
 */
public class LogDto {

    /**
     * What external services send when reporting a log.
     * Example JSON:
     * {
     *   "serviceName": "payment-service",
     *   "logLevel": "ERROR",
     *   "message": "Payment timeout",
     *   "timestamp": "2026-05-06T10:30:00"
     * }
     */
    @Data
    public static class LogRequest {
        @NotBlank(message = "Service name is required")
        private String serviceName;

        @NotNull(message = "Log level is required")
        private Log.LogLevel logLevel;

        @NotBlank(message = "Message is required")
        private String message;

        private LocalDateTime timestamp;
    }

    /**
     * What we return in API responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogResponse {
        private Long id;
        private String serviceName;
        private Log.LogLevel logLevel;
        private String message;
        private LocalDateTime timestamp;
        private LocalDateTime createdAt;
    }
}
