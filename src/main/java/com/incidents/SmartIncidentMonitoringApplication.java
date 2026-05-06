package com.incidents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Smart Incident Monitoring System.
 *
 * @EnableScheduling - enables cron/scheduled jobs (used for spike detection cleanup)
 */
@SpringBootApplication
@EnableScheduling
public class SmartIncidentMonitoringApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartIncidentMonitoringApplication.class, args);
    }
}
