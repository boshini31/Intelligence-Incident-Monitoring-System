# 🚨 Incident Intelligence Monitoring System

A production-ready backend system for real-time log ingestion, error spike detection, incident grouping, and alert generation — built with **Java 21**, **Spring Boot 3**, **Kafka**, **Redis**, and **PostgreSQL**.

---

## 📋 Table of Contents
- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [How It Works](#how-it-works)
- [Resume Points](#resume-points)
- [Interview Q&A](#interview-qa)

---

## 🏗️ Architecture Overview

```
External Services
      │
      ▼
┌─────────────┐     JWT Auth      ┌──────────────┐
│  REST API   │ ◄──────────────── │  Spring Sec  │
│ Controllers │                   └──────────────┘
└─────┬───────┘
      │
      ▼
┌─────────────┐     Saves log    ┌──────────────┐
│  Log Service│ ────────────────► │  PostgreSQL  │
└─────┬───────┘                  └──────────────┘
      │
      │ Publishes to Kafka
      ▼
┌─────────────┐
│    Kafka    │  log-ingestion-topic
└─────┬───────┘
      │
      │ Consumer listens
      ▼
┌──────────────────┐   Redis INCR   ┌──────────────┐
│ SpikeDetection   │ ◄────────────► │    Redis     │
│ Service          │                └──────────────┘
└─────┬────────────┘
      │ Threshold exceeded?
      ▼
┌──────────────┐    ┌──────────────┐
│  Incident DB │    │   Alert DB   │  + Simulated Email
└──────────────┘    └──────────────┘
      │
      ▼
┌──────────────────┐
│ Analytics APIs   │ ◄── Redis @Cacheable
│ (Dashboard feed) │
└──────────────────┘
```

---

## ✨ Features

| Module | Feature |
|--------|---------|
| **Auth** | JWT registration, login, role-based access |
| **Log Ingestion** | REST API → PostgreSQL + Kafka publish |
| **Spike Detection** | Redis counter + threshold check (10 errors / 5 min) |
| **Incident Grouping** | Same service + similar message → one incident |
| **Alerts** | Auto-generated alerts + simulated email |
| **Analytics** | Summary, per-service stats, recent alerts (Redis cached) |
| **Dashboard** | React dashboard served alongside API docs |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 21 | Language (Virtual Threads ready) |
| Spring Boot 3.2 | Application framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA | Database ORM |
| PostgreSQL | Primary data store |
| Apache Kafka | Async event streaming |
| Redis | Caching + spike counting |
| Lombok | Reduce boilerplate |
| Swagger/OpenAPI | Interactive API docs |
| Docker + Compose | Containerization |
| Maven | Build tool |

---

## 📁 Project Structure

```
smart-incident-monitoring/
├── src/main/java/com/incidents/
│   ├── SmartIncidentMonitoringApplication.java
│   ├── controller/
│   │   ├── AuthController.java          # POST /api/auth/register, /login
│   │   ├── LogController.java           # POST/GET /api/logs
│   │   ├── IncidentController.java      # GET/PATCH /api/incidents
│   │   └── AnalyticsController.java     # GET /api/analytics/*
│   ├── service/
│   │   ├── AuthService.java             # Registration + login logic
│   │   ├── LogService.java              # Save log + publish to Kafka
│   │   ├── IncidentService.java         # CRUD for incidents
│   │   ├── SpikeDetectionService.java   # Core detection algorithm
│   │   └── AnalyticsService.java        # Cached analytics queries
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── LogRepository.java           # Custom JPQL analytics queries
│   │   ├── IncidentRepository.java
│   │   └── AlertRepository.java
│   ├── entity/
│   │   ├── User.java                    # Implements UserDetails
│   │   ├── Log.java
│   │   ├── Incident.java
│   │   └── Alert.java
│   ├── dto/
│   │   ├── AuthDto.java                 # Request/Response DTOs
│   │   ├── LogDto.java
│   │   ├── IncidentDto.java
│   │   └── ApiResponse.java             # Standard response wrapper
│   ├── security/
│   │   ├── JwtUtil.java                 # Token generation + validation
│   │   └── JwtAuthenticationFilter.java # Intercept & validate every request
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security rules
│   │   ├── KafkaConfig.java             # Topic definitions
│   │   ├── RedisConfig.java             # Cache + template setup
│   │   └── SwaggerConfig.java           # OpenAPI customization
│   ├── kafka/
│   │   ├── LogProducer.java             # Publish to Kafka
│   │   └── LogConsumer.java             # Consume + trigger detection
│   └── exception/
│       ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│       ├── ResourceNotFoundException.java
│       └── BadRequestException.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+

### Option 1: Full Docker (Recommended)

```bash
# Clone the project
git clone https://github.com/yourusername/smart-incident-monitoring.git
cd smart-incident-monitoring

# Start everything (DB + Redis + Kafka + App)
docker-compose up --build

# API is live at:
http://localhost:8080

# Swagger UI:
http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development

```bash
# Start only infrastructure
docker-compose up postgres redis kafka -d

# Run Spring Boot app
mvn spring-boot:run
```

---

## 📡 API Documentation

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | Login, get JWT | ❌ |

### Log Ingestion

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/logs` | Submit a log | ✅ JWT |
| GET | `/api/logs` | Get recent 50 logs | ✅ JWT |
| GET | `/api/logs/service/{name}` | Logs by service | ✅ JWT |

**POST /api/logs request body:**
```json
{
  "serviceName": "payment-service",
  "logLevel": "ERROR",
  "message": "Payment timeout",
  "timestamp": "2026-05-06T10:30:00"
}
```

### Incidents

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/incidents` | All incidents |
| GET | `/api/incidents/open` | Open incidents only |
| GET | `/api/incidents/{id}` | Single incident |
| GET | `/api/incidents/service/{name}` | By service |
| PATCH | `/api/incidents/{id}/acknowledge` | Mark as ACK |
| PATCH | `/api/incidents/{id}/resolve` | Mark as resolved |

### Analytics

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| GET | `/api/analytics/summary` | Totals + most affected | 5 min TTL |
| GET | `/api/analytics/service-stats` | Per-service breakdown | 5 min TTL |
| GET | `/api/analytics/recent-alerts` | Last 20 alerts | Live |

---

## ⚙️ How Spike Detection Works

1. **Log arrives** at `POST /api/logs`
2. **Saved to PostgreSQL** for permanent storage
3. **Published to Kafka** (`log-ingestion-topic`) asynchronously
4. **Kafka Consumer** picks it up and calls `SpikeDetectionService`
5. **Redis INCR** increments a counter for the `groupKey` (serviceName:normalizedMessage)
6. **Redis TTL** = 5 minutes (counter auto-resets every 5 minutes)
7. **Threshold check**: if counter ≥ 10 → create Incident + Alert
8. **Simulated email** printed in application logs

### Group Key Logic
```
"payment-service" + "Payment timeout"          → payment-service:payment timeout
"payment-service" + "Payment timeout at gateway" → payment-service:payment timeout at ga
                                                  → Same incident group! ✅
```

---

## 📊 Testing the Spike Detection

```bash
# 1. Register and login
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"pass123","email":"admin@test.com"}'

# 2. Get token from response, set it:
TOKEN="your-jwt-token-here"

# 3. Send 10+ ERROR logs to trigger a spike
for i in {1..12}; do
  curl -X POST http://localhost:8080/api/logs \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"serviceName":"payment-service","logLevel":"ERROR","message":"Payment timeout"}'
done

# 4. Check incidents
curl http://localhost:8080/api/incidents \
  -H "Authorization: Bearer $TOKEN"

# 5. Check alerts
curl http://localhost:8080/api/analytics/recent-alerts \
  -H "Authorization: Bearer $TOKEN"
```

## 📝 Database Schema

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at TIMESTAMP
);

CREATE TABLE logs (
  id BIGSERIAL PRIMARY KEY,
  service_name VARCHAR(255) NOT NULL,
  log_level VARCHAR(20) NOT NULL,
  message TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  created_at TIMESTAMP,
  INDEX idx_log_service (service_name),
  INDEX idx_log_level (log_level),
  INDEX idx_log_timestamp (timestamp)
);

CREATE TABLE incidents (
  id BIGSERIAL PRIMARY KEY,
  service_name VARCHAR(255) NOT NULL,
  group_key VARCHAR(500) NOT NULL,
  message TEXT NOT NULL,
  severity VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  occurrence_count INT DEFAULT 1,
  first_seen TIMESTAMP,
  last_seen TIMESTAMP,
  created_at TIMESTAMP
);

CREATE TABLE alerts (
  id BIGSERIAL PRIMARY KEY,
  incident_id BIGINT REFERENCES incidents(id),
  service_name VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  severity VARCHAR(20) NOT NULL,
  error_count INT,
  triggered_at TIMESTAMP,
  created_at TIMESTAMP
);
```

---

## 🔧 Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `incident.detection.error-threshold` | `10` | Errors before alert fires |
| `incident.detection.window-minutes` | `5` | Time window for counting |
| `jwt.expiration` | `86400000` | Token lifetime (24h in ms) |
| `spring.kafka.consumer.group-id` | `incident-monitoring-group` | Kafka consumer group |

---

*Built as a learning project demonstrating event-driven architecture, JWT security, and observability concepts with Java Spring Boot.*
