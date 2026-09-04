# Contact Management System

Full-stack contact directory for **Cohort 9 — Java + React**. Users register, authenticate with JWT, and manage a private contact book with multiple emails and phone numbers per contact.

---

## Table of contents

1. [Tech stack](#tech-stack)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Environment variables](#environment-variables)
5. [Database setup](#database-setup)
6. [Run the application](#run-the-application)
7. [Run tests](#run-tests)
8. [SonarQube analysis](#sonarqube-analysis)
9. [API overview](#api-overview)
10. [CSV import/export](#csv-importexport)
11. [Project structure](#project-structure)
12. [Documentation](#documentation)
13. [Branching](#branching)
14. [Troubleshooting](#troubleshooting)

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 4.1, Java 17, Spring Security, Spring Data JPA |
| Database | PostgreSQL (runtime), H2 (tests) |
| Auth | JWT via JJWT 0.12.6 |
| Frontend | React 19, Vite 8, React Router 7 |
| Quality | JUnit 5, Mockito, JaCoCo, SonarQube Maven plugin |

---

## Features

### Authentication
- Register with email and password
- Login returns a JWT (24-hour expiry)
- Token stored in `localStorage` and restored on page refresh
- Automatic logout on 401 (expired or invalidated token)

### Contacts
- Create, read, update, delete contacts
- Multiple emails and phones per contact with type labels
- Paginated list with sort options (name A–Z / Z–A)
- Search by first name and/or last name
- CSV export and import

### User profile
- View profile (email, phone, member since)
- Update email and phone number
- Change password (invalidates existing JWT via token version)

### Dashboard
- KPI cards: total contacts, with email, with phone, rich profiles
- Recent contacts (newest first, sorted by `id` descending)

---

## Prerequisites

| Requirement | Notes |
|-------------|-------|
| **Java 17+** | `java -version` |
| **PostgreSQL** | Running locally on port 5432 |
| **Node.js 20.19+ or 22.12+** | Required by Vite 8 |
| **npm** | Bundled with Node.js |
| **Maven wrapper** | Use `.\mvnw.cmd` (Windows) or `./mvnw` (macOS/Linux) — no global Maven install needed |

---

## Environment variables

### Backend (required before starting the API)

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_USERNAME` | Yes | PostgreSQL username |
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `JWT_SECRET` | Yes | Signing key — **minimum 32 characters** |

**PowerShell (set in the same terminal session that runs the backend):**

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your-strong-random-secret-at-least-32-chars"
```

**macOS / Linux (bash):**

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-strong-random-secret-at-least-32-chars
```

> **Important:** If any of these are missing, Spring Boot will fail at startup with a placeholder resolution error.

### Backend (optional — SonarQube only)

| Variable | Description |
|----------|-------------|
| `SONAR_HOST_URL` | SonarQube server URL (e.g. `https://sonarcloud.io`) |
| `SONAR_TOKEN` | Analysis token from SonarCloud / SonarQube |

### Frontend (optional)

Copy `webapp/my-app/.env.example` to `webapp/my-app/.env`:

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | *(empty)* | Leave empty to use Vite dev proxy (`/api` → `localhost:8080`). Set to `http://localhost:8080` for direct calls. Must be `https://` in production, or `http://localhost` during local dev only. |

---

## Database setup

### 1. Create the database

Connect to PostgreSQL and run:

```sql
CREATE DATABASE contact_management_db;
```

### 2. Connection settings

Configured in `src/main/resources/application.properties`:

| Property | Value |
|----------|-------|
| URL | `jdbc:postgresql://localhost:5432/contact_management_db` |
| Username | `${DB_USERNAME}` |
| Password | `${DB_PASSWORD}` |
| DDL mode | `update` (Hibernate creates/updates tables automatically) |

### 3. Tables (created automatically)

| Table | Purpose |
|-------|---------|
| `users` | Accounts (email, password hash, phone, token version) |
| `contacts` | Contact records linked to a user |
| `contact_emails` | Email addresses per contact |
| `contact_phones` | Phone numbers per contact |

Full schema details: [BACKEND.md](BACKEND.md#database-schema)

---

## Run the application

### Backend

From `contact-management-system/`:

```powershell
.\mvnw.cmd spring-boot:run
```

- API base URL: **http://localhost:8080**
- Hibernate SQL logging is enabled in dev (`show-sql=true`)

### Frontend

```powershell
cd webapp/my-app
npm install
npm run dev
```

- App URL: **http://localhost:5173**
- Vite proxies `/api` requests to the backend (see `vite.config.js`)

### Production build (frontend)

```powershell
cd webapp/my-app
npm run build
```

Output: `webapp/my-app/dist/` — serve statically and point `VITE_API_URL` to your deployed API.

---

## Run tests

Backend tests use an **in-memory H2 database** (no PostgreSQL needed for tests).

```powershell
.\mvnw.cmd clean test
```

Test configuration: `src/test/resources/application.properties`

| Test type | Location |
|-----------|----------|
| Unit tests | `src/test/java/.../service/` |
| Controller tests | `src/test/java/.../controller/` |
| Integration tests | `ContactControllerIntegrationTest`, `UserControllerIntegrationTest` |
| Application context | `ContactManagementSystemApplicationTests` |

---

## SonarQube analysis

### Configuration

Sonar properties and JaCoCo are defined in `pom.xml`:

| Property | Value |
|----------|-------|
| Project key | `contact-management-system` |
| Coverage report | `target/site/jacoco/jacoco.xml` |
| Exclusions | `webapp/my-app/node_modules/**`, `webapp/my-app/dist/**` |

### Run analysis

```powershell
$env:SONAR_HOST_URL = "https://sonarcloud.io"
$env:SONAR_TOKEN = "your-sonar-token"

.\mvnw.cmd clean verify sonar:sonar `
  -Dsonar.host.url=$env:SONAR_HOST_URL `
  -Dsonar.token=$env:SONAR_TOKEN
```

`verify` runs tests and generates the JaCoCo coverage report before Sonar picks it up.

Full details: [BACKEND.md — SonarQube](BACKEND.md#sonarqube)

---

## API overview

All protected endpoints require:

```
Authorization: Bearer <jwt-token>
```

### Auth (public)

| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/auth/register` | `{ "email", "password" }` | `{ id, email }` |
| POST | `/api/auth/login` | `{ "email", "password" }` | `{ id, email, token }` |

### User (protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Current user profile |
| PUT | `/api/users/me` | Update email / phone |
| PUT | `/api/users/me/password` | Change password |

### Contacts (protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/contacts` | Create contact |
| GET | `/api/contacts/{id}` | Get contact by ID |
| PUT | `/api/contacts/{id}` | Update contact |
| DELETE | `/api/contacts/{id}` | Delete contact |
| GET | `/api/contacts` | List contacts (`?page=0&size=10&sort=lastName,asc`) |
| GET | `/api/contacts/search` | Search (`?firstName=&lastName=`) |
| GET | `/api/contacts/export` | Download contacts as CSV |
| POST | `/api/contacts/import` | Upload CSV (`multipart/form-data`, field: `file`) |

Full request/response examples: [BACKEND.md — API reference](BACKEND.md#api-reference)

---

## CSV import/export

### Export

`GET /api/contacts/export` returns `contacts.csv` with header:

```
firstName,lastName,title,email,emailType,phone,phoneType
```

### Import

`POST /api/contacts/import` accepts a `.csv` file. Response:

```json
{
  "importedCount": 5,
  "failedCount": 2,
  "errors": [
    { "rowNumber": 3, "message": "..." }
  ]
}
```

### Rules

| Field | Valid values |
|-------|-------------|
| `emailType` | `WORK`, `PERSONAL`, `OTHER` |
| `phoneType` | `WORK`, `HOME`, `PERSONAL`, `OTHER` (not `MOBILE`) |
| Multiple emails/phones | Pipe-separated: `work@x.com\|home@x.com` with `WORK\|PERSONAL` |
| Email format | Must include `@` and domain (e.g. `user@example.com`) |

### Example row

```csv
John,Doe,Engineer,john@work.com|john@home.com,WORK|PERSONAL,1111111111|2222222222,WORK|HOME
```

---

## Project structure

```
contact-management-system/
├── pom.xml                          # Maven build, JaCoCo, SonarQube
├── README.md                        # This file
├── BACKEND.md                       # Backend deep dive
├── src/
│   ├── main/
│   │   ├── java/com/tenpearls/contactmanagement/
│   │   │   ├── config/              # Security, CORS, JWT properties
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Request/response objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions + global handler
│   │   │   ├── repository/          # Spring Data JPA repos
│   │   │   ├── security/            # JWT filter, entry point
│   │   │   └── service/             # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/                    # Unit & integration tests
│       └── resources/
│           └── application.properties  # H2 test config
└── webapp/my-app/                   # React frontend
    ├── README.md                    # Frontend guide
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/client.js            # API client
        ├── context/AuthContext.jsx  # Auth state
        ├── pages/                   # Route pages
        └── components/              # UI components
```

---

## Documentation

| Document | Audience |
|----------|----------|
| [BACKEND.md](BACKEND.md) | Backend developers — API, security, DB, tests, SonarQube |
| [webapp/my-app/README.md](webapp/my-app/README.md) | Frontend developers — routes, components, auth, build |
| [../README.md](../README.md) | Repository-level overview |

---

## Branching

Feature branches: `BR-<number>-<feature-name>`

```
main
 └── BR-1-initial-springio-commit
 └── BR-6-user-registration-api
 └── ...
 └── BR-14-csv-import-export
 └── BR-15-final-readme        ← documentation & polish
```

Merge via pull request with CodeRabbit review.

---

## Troubleshooting

### Backend won't start — `Could not resolve placeholder 'DB_USERNAME'`

Set all three required env vars in the **same terminal** before running `spring-boot:run`.

### `Connection refused` to PostgreSQL

- Confirm PostgreSQL service is running
- Confirm database `contact_management_db` exists
- Check username/password match your `DB_USERNAME` / `DB_PASSWORD`

### Frontend shows network errors

- Confirm backend is running on port 8080
- For local dev, leave `VITE_API_URL` empty (uses Vite proxy)
- Restart `npm run dev` after changing `.env`

### CSV import — all rows fail

- Check `phoneType` is `WORK`, `HOME`, `PERSONAL`, or `OTHER` (not `Mobile`)
- Check emails are valid (`name@domain.com`)
- Save file as `.csv` (comma-delimited), not `.xlsx`

### Session lost on refresh

- Ensure you are on the latest code — `AuthProvider` restores token from `localStorage`
- Check browser dev tools → Application → Local Storage → `token` key exists after login

### SonarQube analysis fails

- Confirm `SONAR_TOKEN` is valid and not expired
- Run `.\mvnw.cmd clean verify` first to generate JaCoCo report
- Pass `-Dsonar.host.url` and `-Dsonar.token` explicitly if env vars are not picked up

### JWT errors after password change

Expected behavior — changing password increments `tokenVersion`, invalidating old tokens. Log in again.
