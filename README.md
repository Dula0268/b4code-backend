# B4Code Backend

This repository contains the backend infrastructure for the **B4Code** hospitality platform. Built on modern Java standards, the backend utilizes a scalable Domain-Driven architecture to empower multi-team student development while avoiding merge conflicts.

## 🚀 Tech Stack

- **Runtime Engine:** Java 21 LTS
- **Framework:** Spring Boot 3.2.4
- **Database:** PostgreSQL 16
- **Cache / Sessions:** Redis 7
- **DevOps / Orchestration:** Docker & Kubernetes (Helm)
- **CI/CD:** GitHub Actions (SAST, DAST)

---

## 🏗️ Architecture & Structure

The repository enforces strict boundaries by splitting the application logic from the underlying infrastructure.

```text
b4code-backend/
├── docker/
│   └── docker-compose.yml          # Local PostgreSQL + Redis sandbox
├── kubernetes/                     # Kubernetes definitions & Helm Charts
├── src/main/java/com/b4code/backend/
│   ├── modules/                    # 🎯 ROLE-BASED DOMAIN LOGIC
│   │   ├── auth/       ── Login, Registration, JWT, User entity
│   │   ├── guest/      ── Guest Booking, Chatting, Bookings & Messages
│   │   ├── owner/      ── Host Management, Property Listings, Payouts
│   │   ├── staff/      ── Hotel F&B, Menu Items, Order processing
│   │   └── admin/      ── Platform moderation, Global Analytics
│   ├── common/                     # 🛡️ SYSTEM UTILITIES (Shared by all teams)
│   │   ├── config/                 # Security rules, CORS policies
│   │   ├── exception/              # Global error handlers
│   │   └── security/               # Encryption, JWT Parsing logic
│   └── infrastructure/             # ⚙️ EXTERNAL ADAPTERS
│       ├── messaging/              
│       ├── redis/                  
│       ├── storage/                
│       └── websocket/              
└── src/main/resources/             # Configuration YML and Flyway Migrations
```

---

## 🛠️ Quick Start Guide

### 1. Launch the Database
You must have Docker and Docker Compose installed. We utilize a localized volume sandbox to simulate the PostgreSQL instance and the Redis caching layer.

```bash
cd docker
docker-compose down -v
docker-compose up -d
```
> Note: The database explicitly runs on port **5433** to prevent colliding with any native Postgres installations you may have on port `5432`.

### 2. Run the Application
Start the Spring Boot instance locally using the Maven wrapper. Ensure you are executing this command from the *project root directory*.

```bash
mvn clean compile spring-boot:run
```

The server will automatically start on `http://localhost:8080`.

### 3. Verify Frontend Connection
The development environment has been pre-configured with a **CORS whitelist** allowing standard frontend instances connected via `localhost:3000` or `localhost:5173` to smoothly request data without origin violations.

Visit the following API to test connectivity:
`GET http://localhost:8080/api/test/ping`

---

## 🔒 Security Posture

- **Enforced Least-Privilege:** By default, all `/api/**` endpoints are fully locked down behind OAuth2 Resource Server constraints. Routes must explicitly whitelist public traffic.
- **Encryption at Rest:** Sensitive PII columns across entities are safeguarded using AES-256 via the `AttributeEncryptor`.

*(Documentation drafted during Phase 1 - Architecture Setup)*
