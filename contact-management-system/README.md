# Contact Management System

Full-stack contact directory built for **Cohort 9 — Java + React**. Users can register, sign in with JWT, manage contacts (CRUD, search, pagination), and update their profile.

## Tech stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 4.1, Java 17, Spring Security, JPA |
| Database | PostgreSQL |
| Auth | JWT (JJWT 0.12.6) |
| Frontend | React 19, Vite 8, React Router |
| Quality | JUnit 5, JaCoCo, SonarQube |

## Prerequisites

- **Java 17+**
- **Maven** (or use included `./mvnw`)
- **PostgreSQL** running locally
- **Node.js 18+** and **npm** (for frontend)

## Environment variables

### Backend (required)

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Signing key, **minimum 32 characters** |

**PowerShell example:**

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your-strong-random-secret-at-least-32-chars"
```

Create the database once:

```sql
CREATE DATABASE contact_management_db;
```

### Frontend (optional)

Copy `webapp/my-app/.env.example` to `webapp/my-app/.env`.

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Leave empty to use Vite dev proxy (`/api` → `localhost:8080`) |

## Run the backend

From `contact-management-system/`:

```powershell
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`

## Run the frontend

```powershell
cd webapp/my-app
npm install
npm run dev
```

App URL: `http://localhost:5173`

## Run tests

```powershell
./mvnw clean test
```

Tests use an in-memory H2 database (see `src/test/resources/application.properties`).

## SonarQube analysis

1. Set SonarQube server URL and token (from your mentor / SonarCloud project):

```powershell
$env:SONAR_HOST_URL = "https://sonarcloud.io"
$env:SONAR_TOKEN = "your-sonar-token"
```

2. Run analysis with coverage:

```powershell
./mvnw clean verify sonar:sonar `
  -Dsonar.host.url=$env:SONAR_HOST_URL `
  -Dsonar.token=$env:SONAR_TOKEN
```

Configuration: `sonar-project.properties` and JaCoCo in `pom.xml`.

## API overview

### Auth (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register `{ "email", "password" }` |
| POST | `/api/auth/login` | Login → returns `{ id, email, token }` |

### User (Bearer JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Current user profile |
| PUT | `/api/users/me` | Update email / phone |
| PUT | `/api/users/me/password` | Change password |

### Contacts (Bearer JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/contacts` | Create contact |
| GET | `/api/contacts/{id}` | Get contact by id |
| PUT | `/api/contacts/{id}` | Update contact |
| DELETE | `/api/contacts/{id}` | Delete contact |
| GET | `/api/contacts` | List (page, size, sort) |
| GET | `/api/contacts/search` | Search by firstName / lastName |

Send the JWT in the header:

```
Authorization: Bearer <token>
```

## Project structure

```
contact-management-system/
├── src/main/java/...     # Spring Boot backend
├── src/test/java/...     # Unit & integration tests
├── webapp/my-app/        # React frontend (Vite)
├── pom.xml
├── sonar-project.properties
└── README.md
```

## Branching

Feature branches follow `BR-<n>-<feature-name>` (e.g. `BR-12-frontend-app`, `BR-13-sonarqube-readme`), merged to `main` via pull request.
