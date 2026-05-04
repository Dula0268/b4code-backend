# B4Code Backend

This repository contains the backend infrastructure for the **B4Code** hospitality platform. Built on modern Java standards, the backend utilizes a scalable Domain-Driven architecture to empower multi-team student development while avoiding merge conflicts.

## 🚀 Tech Stack

- **Runtime Engine:** Java 21 LTS
- **Framework:** Spring Boot 3.2.4
- **Database:** PostgreSQL 15
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

Start the Spring Boot instance locally using the Maven wrapper. Ensure you are executing this command from the _project root directory_.

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

_(Documentation drafted during Phase 1 - Architecture Setup)_

---

## 🔄 CI/CD Pipeline

Complete GitHub Actions workflow for Continuous Integration and Continuous Deployment.

### Pipeline Overview

| Job                       | Purpose                                  | Trigger              | Duration  |
| ------------------------- | ---------------------------------------- | -------------------- | --------- |
| **build-and-test**        | Compile & run unit tests                 | Every PR/push        | ~3-5 min  |
| **code-quality**          | CodeQL + OWASP dependency check          | Every PR/push        | ~5-10 min |
| **docker-build-and-push** | Build & push Docker image                | Push to main/develop | ~3-5 min  |
| **deploy-to-staging**     | Auto-deploy to staging                   | Push to develop      | ~2-3 min  |
| **deploy-to-production**  | Deploy to production (requires approval) | Push to main         | ~2-3 min  |

### Setup Instructions

#### 1. Configure Docker Registry

Add these to GitHub repository **Settings → Secrets and variables → Actions**:

```
DOCKER_USERNAME     # Your Docker registry username
DOCKER_PASSWORD     # Your Docker registry password or token
DOCKER_REGISTRY     # Your Docker registry (e.g., docker.io, ghcr.io, myacr.azurecr.io)
```

**Example for DockerHub:**

```
DOCKER_USERNAME = your-dockerhub-username
DOCKER_PASSWORD = your-dockerhub-token
DOCKER_REGISTRY = docker.io
```

**Example for Azure Container Registry:**

```
DOCKER_USERNAME = your-acr-name
DOCKER_PASSWORD = your-acr-password
DOCKER_REGISTRY = your-acr-name.azurecr.io
```

#### 2. Setup Environment Approvals (Production)

For production deployment to require manual approval:

1. Go to **Settings → Environments**
2. Create environment named `production`
3. Enable **Required reviewers** (add team members)
4. Click **Save protection rules**

Now anyone deploying to production needs approval from your reviewers.

#### 3. Configure Deployment Scripts

Edit `.github/workflows/ci.yml` and add your deployment commands:

**For Kubernetes (Helm):**

```bash
- name: Deploy to Production
  run: |
    helm repo add b4code https://your-helm-repo.com
    helm upgrade backend b4code/backend \
      --set image.tag=${{ github.sha }} \
      --namespace production
```

**For Docker Swarm:**

```bash
- name: Deploy to Production
  run: |
    docker service update backend_service \
      --image ${{ vars.DOCKER_REGISTRY }}/b4code/backend:${{ github.sha }}
```

**For Custom Scripts:**

```bash
- name: Deploy to Production
  run: bash ./deploy.sh ${{ github.sha }}
```

### Workflow Flow

```
GitHub Event (PR/Push)
│
├─→ build-and-test (all branches)
│   └─ mvn clean package & mvn test
│
├─→ code-quality (parallel, all branches)
│   └─ CodeQL + OWASP analysis
│
└─→ [IF PUSH TO main/develop]
    │
    ├─→ docker-build-and-push
    │   └─ Build and push Docker image
    │
    └─→ [IF develop] deploy-to-staging
    │   └─ Auto-deploy to staging
    │
    └─→ [IF main] deploy-to-production
        └─ Requires manual approval before deploying
```

### Quick Testing

Before creating a PR:

```bash
# Test build
mvn clean package

# Run tests
mvn test

# Check Java version (must be 21)
java -version
```

### Pre-Merge Checklist

- [ ] All GitHub Actions checks pass (build-and-test ✅, code-quality ✅)
- [ ] No critical CodeQL or dependency vulnerabilities
- [ ] Tests pass locally: `mvn test`
- [ ] Build passes locally: `mvn clean package`
- [ ] Code reviewed and approved

### View Results

1. Go to **Actions** tab in GitHub
2. Click workflow run to see job details
3. Download security reports from **Artifacts** section
4. For production deployments, approve in **Environments** tab

### Troubleshooting

```bash
# Build fails
mvn clean package -X  # Verbose output

# Test fails
mvn test -Dtest=TestClassName  # Run specific test

# Check dependencies
mvn dependency:tree

# Manual Docker build & push
docker build -t $REGISTRY/b4code/backend:latest .
docker push $REGISTRY/b4code/backend:latest
```

**Workflow file**: `.github/workflows/ci.yml`
