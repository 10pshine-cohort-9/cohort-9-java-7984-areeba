# Cohort 9 — Contact Management System

**Author:** Areeba  
**Cohort:** 9 — Java Fullstack (Java + React)  
**Repository:** `cohort-9-java-7984-areeba`

A full-stack contact directory where users register, sign in with JWT, and manage personal contacts with multiple emails and phone numbers. Includes search, pagination, CSV import/export, profile settings, and SonarQube quality analysis.

---

## Repository layout

```
cohort-9-java-7984-areeba/
├── README.md                          ← You are here (repository overview)
└── contact-management-system/         ← Main application
    ├── README.md                      ← Start here: setup & quick start
    ├── BACKEND.md                     ← Spring Boot API, database, tests, SonarQube
    ├── pom.xml
    ├── src/main/java/                 ← Backend source
    ├── src/test/java/                 ← Backend tests
    └── webapp/my-app/
        └── README.md                  ← React frontend guide
```

---

## Quick start

All commands below assume you are in `contact-management-system/`.

### 1. Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| PostgreSQL | 14+ (local instance) |
| Node.js | 20.19+ or 22.12+ |
| npm | 10+ |
| Maven | Included via `mvnw` wrapper |

### 2. Database

```sql
CREATE DATABASE contact_management_db;
```

### 3. Environment variables (backend — required)

**PowerShell:**

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your-strong-random-secret-at-least-32-chars"
```

### 4. Run backend

```powershell
.\mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`

### 5. Run frontend

```powershell
cd webapp/my-app
npm install
npm run dev
```

App: `http://localhost:5173`

---

## Documentation map

| Document | What it covers |
|----------|----------------|
| [contact-management-system/README.md](contact-management-system/README.md) | Full project guide, features, env vars, run instructions |
| [contact-management-system/BACKEND.md](contact-management-system/BACKEND.md) | API reference, database schema, security, tests, SonarQube |
| [contact-management-system/webapp/my-app/README.md](contact-management-system/webapp/my-app/README.md) | React app structure, routes, auth flow, build |

---

## Features at a glance

- User registration and JWT login
- Protected routes and session persistence across page refresh
- Contact CRUD with multiple emails and phones per contact
- Search, pagination, and sorting on the contacts list
- CSV import and export (pipe-separated multi-value fields)
- User profile and password change
- Dashboard with KPI cards and recent contacts
- JUnit 5 tests with JaCoCo coverage and SonarQube integration

---

## Branching workflow

Feature branches follow `BR-<number>-<feature-name>`:

| Branch | Feature |
|--------|---------|
| BR-1 – BR-10 | Backend foundation, auth, user APIs |
| BR-11 | Contact management API |
| BR-12 | React frontend |
| BR-13 | SonarQube + initial README |
| BR-14 | CSV import/export |
| BR-15 | Final documentation and polish |

Each branch is merged to `main` via pull request with CodeRabbit review.

---

## Quality & testing

```powershell
# Run all backend tests (uses in-memory H2)
.\mvnw.cmd clean test

# Run SonarQube analysis (requires SONAR_HOST_URL and SONAR_TOKEN)
$env:SONAR_HOST_URL = "https://sonarcloud.io"
$env:SONAR_TOKEN = "your-sonar-token"
.\mvnw.cmd clean verify sonar:sonar `
  -Dsonar.host.url=$env:SONAR_HOST_URL `
  -Dsonar.token=$env:SONAR_TOKEN
```

See [BACKEND.md](contact-management-system/BACKEND.md) for full SonarQube and test details.

---

## Optional / out of scope

The following SRS items were deferred:

- Contact address fields
- Audit trail (created/updated timestamps on contacts)
- Forgot-password flow

---

## Need help?

1. Read [contact-management-system/README.md](contact-management-system/README.md) — troubleshooting section
2. Confirm PostgreSQL is running and env vars are set in the **same terminal** that starts the backend
3. Confirm frontend `.env` exists (copy from `.env.example`)
