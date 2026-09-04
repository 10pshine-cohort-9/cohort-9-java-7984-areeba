# Backend — Contact Management System

Spring Boot REST API for user authentication and contact management.

**Stack:** Spring Boot 4.1 · Java 17 · Spring Security · Spring Data JPA · PostgreSQL · JWT (JJWT 0.12.6)

---

## Table of contents

1. [Architecture](#architecture)
2. [Package structure](#package-structure)
3. [Environment variables](#environment-variables)
4. [Database schema](#database-schema)
5. [Security & JWT](#security--jwt)
6. [API reference](#api-reference)
7. [Error handling](#error-handling)
8. [CSV service](#csv-service)
9. [Tests](#tests)
10. [SonarQube](#sonarqube)
11. [Configuration reference](#configuration-reference)

---

## Architecture

```
Client (React)
    │
    ▼
ContactController / UserController / AuthController
    │
    ▼
ContactService / UserService / AuthService / ContactCsvService
    │
    ▼
ContactRepository / UserRepository / ContactEmailRepository / ContactPhoneRepository
    │
    ▼
PostgreSQL
```

**Request flow for protected endpoints:**

1. `JwtAuthenticationFilter` extracts `Authorization: Bearer <token>` header
2. `JwtService` validates signature and expiry
3. Token `tokenVersion` claim is checked against the user's current version in DB
4. `SecurityContext` is populated with the user's email
5. Controller delegates to service layer

---

## Package structure

```
com.tenpearls.contactmanagement
├── ContactManagementSystemApplication.java
├── config/
│   ├── CorsConfig.java              # CORS for frontend dev server
│   ├── JwtProperties.java           # jwt.secret, jwt.expiration-ms
│   ├── PasswordConfig.java          # BCrypt PasswordEncoder bean
│   └── SecurityConfig.java          # Security filter chain, public routes
├── controller/
│   ├── AuthController.java          # /api/auth/*
│   ├── ContactController.java       # /api/contacts/*
│   └── UserController.java          # /api/users/*
├── dto/
│   ├── auth/                        # RegisterRequest, LoginRequest, LoginResponse
│   ├── contact/                     # CreateContactRequest, ContactResponse, etc.
│   └── user/                        # UpdateProfileRequest, ChangePasswordRequest
├── entity/
│   ├── User.java
│   ├── Contact.java
│   ├── ContactEmail.java
│   ├── ContactPhone.java
│   └── enums/                       # EmailType, PhoneType
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── EmailAlreadyRegisteredException.java
│   ├── PhoneAlreadyRegisteredException.java
│   ├── ContactNotFoundException.java
│   ├── InvalidCredentialsException.java
│   └── InvalidCsvFileException.java
├── repository/                      # Spring Data JPA interfaces
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── ContactService.java
    └── ContactCsvService.java
```

---

## Environment variables

| Variable | Required | Used in | Description |
|----------|----------|---------|-------------|
| `DB_USERNAME` | Yes | `application.properties` | PostgreSQL username |
| `DB_PASSWORD` | Yes | `application.properties` | PostgreSQL password |
| `JWT_SECRET` | Yes | `application.properties` | HMAC signing key (min 32 chars) |
| `SONAR_HOST_URL` | Sonar only | Maven CLI | SonarQube server URL |
| `SONAR_TOKEN` | Sonar only | Maven CLI | SonarQube analysis token |

### application.properties (runtime)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/contact_management_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=86400000
```

### application.properties (tests)

Tests use H2 in-memory — no env vars needed:

```properties
spring.datasource.url=jdbc:h2:mem:contact_management_test;MODE=PostgreSQL
jwt.secret=test-jwt-secret-key-at-least-32-characters-long
```

---

## Database schema

Hibernate `ddl-auto=update` creates and migrates tables automatically.

### users

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, auto-increment |
| email | VARCHAR | Unique (`uk_user_email`) |
| password | VARCHAR | BCrypt hash, not null |
| phone_number | VARCHAR | Unique (`uk_user_phone_number`), nullable |
| token_version | INT | Default 0; incremented on password change / email change |
| created_at | TIMESTAMP | Set on insert |

### contacts

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, auto-increment |
| first_name | VARCHAR | Not null |
| last_name | VARCHAR | Not null |
| title | VARCHAR | Nullable |
| user_id | BIGINT | FK → users.id, not null |

### contact_emails

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK |
| email | VARCHAR | Not null, `@Email` validated |
| type | VARCHAR | `WORK`, `PERSONAL`, `OTHER` |
| contact_id | BIGINT | FK → contacts.id |

### contact_phones

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK |
| phone_number | VARCHAR | Not null |
| type | VARCHAR | `WORK`, `HOME`, `PERSONAL`, `OTHER` |
| contact_id | BIGINT | FK → contacts.id |

### Relationships

- One `User` → many `Contact` (contacts are scoped per user)
- One `Contact` → many `ContactEmail` and `ContactPhone`
- Deleting a contact cascades email/phone cleanup in the service layer

---

## Security & JWT

### Public routes (no token required)

- `POST /api/auth/register`
- `POST /api/auth/login`

### Protected routes

All other `/api/**` endpoints require a valid JWT.

### Token contents

| Claim | Value |
|-------|-------|
| Subject | User email |
| `userId` | User ID |
| `tokenVersion` | Matches `users.token_version` — invalidated on password/email change |

### Token invalidation

`tokenVersion` is incremented when:
- User changes password
- User changes email address

Old tokens are rejected even if not yet expired.

---

## API reference

Base URL: `http://localhost:8080`

### Auth

#### POST `/api/auth/register`

**Request body:**

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

**Error `409 Conflict`:**

```json
"Email is already registered"
```

#### POST `/api/auth/login`

**Request body:**

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Response `200 OK`:**

```json
{
  "id": 1,
  "email": "user@example.com",
  "token": "eyJhbG..."
}
```

**Error `401 Unauthorized`:**

```json
"Invalid email or password"
```

---

### User

All require `Authorization: Bearer <token>`.

#### GET `/api/users/me`

**Response `200 OK`:**

```json
{
  "id": 1,
  "email": "user@example.com",
  "phoneNumber": "1234567890",
  "createdAt": "2026-01-15T10:00:00"
}
```

#### PUT `/api/users/me`

**Request body:**

```json
{
  "email": "newemail@example.com",
  "phoneNumber": "9876543210"
}
```

**Response `200 OK`:** updated profile (same shape as `GET /api/users/me`).

**Error `409 Conflict`:**

```json
"Email is already registered"
```

or

```json
"Phone number is already registered"
```

#### PUT `/api/users/me/password`

**Request body:**

```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewPassword456"
}
```

**Response `204 No Content`:** empty body.

**Error `401 Unauthorized`:**

```json
"Invalid current password"
```

---

### Contacts

All require `Authorization: Bearer <token>`. Contacts are scoped to the authenticated user.

#### POST `/api/contacts`

**Request body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "title": "Engineer",
  "emails": [
    { "email": "john@work.com", "type": "WORK" },
    { "email": "john@home.com", "type": "PERSONAL" }
  ],
  "phones": [
    { "phoneNumber": "1234567890", "type": "HOME" }
  ]
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "title": "Engineer",
  "emails": [
    { "id": 1, "email": "john@work.com", "type": "WORK" }
  ],
  "phones": [
    { "id": 1, "phoneNumber": "1234567890", "type": "HOME" }
  ]
}
```

#### GET `/api/contacts?page=0&size=10&sort=lastName,asc`

**Response `200 OK`:**

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

`content` is an array of contact objects (same shape as the create response).

**Sort examples:** `lastName,asc` · `lastName,desc` · `id,desc` (newest first)

#### GET `/api/contacts/search?firstName=John&lastName=Doe&page=0&size=10`

Partial, case-insensitive match on provided name fields.

#### GET `/api/contacts/{id}` · PUT `/api/contacts/{id}` · DELETE `/api/contacts/{id}`

Standard CRUD. `DELETE` returns `204 No Content`.

#### GET `/api/contacts/export`

Returns `text/csv` file attachment: `contacts.csv`

#### POST `/api/contacts/import`

```
Content-Type: multipart/form-data
Field: file (must be .csv)
```

**Response `200 OK`:**

```json
{
  "importedCount": 8,
  "failedCount": 2,
  "errors": [
    { "rowNumber": 5, "message": "No enum constant ... MOBILE" }
  ]
}
```

---

## Error handling

`GlobalExceptionHandler` maps exceptions to HTTP responses:

| Exception | HTTP | Body |
|-----------|------|------|
| `EmailAlreadyRegisteredException` | 409 | `"Email is already registered"` |
| `PhoneAlreadyRegisteredException` | 409 | `"Phone number is already registered"` |
| `DataIntegrityViolationException` | 409 | `uk_user_email` → email message; `uk_user_phone_number` → phone message; otherwise generic integrity message |
| `InvalidCredentialsException` | 401 | Exception message |
| `UserNotFoundException` | 404 | Exception message |
| `ContactNotFoundException` | 404 | Exception message |
| `InvalidCsvFileException` | 400 | Exception message |
| `MethodArgumentNotValidException` | 400 | `{ "fieldName": "error message" }` |

---

## CSV service

`ContactCsvService` handles export and import.

### Export behavior

- Fetches all contacts for the authenticated user (sorted by last name, first name)
- Emails and phones sorted by record `id` for stable order
- Multiple values joined with `|` in their respective columns
- Fields containing commas, quotes, or newlines are RFC-quoted

### Import behavior

- Validates exact header: `firstName,lastName,title,email,emailType,phone,phoneType`
- Parses complete CSV records (supports quoted multiline fields)
- Splits pipe-separated email/phone values
- Each row creates a new contact via `ContactService.createContact()`
- Per-row failures are collected; successful rows are still imported

### Valid enum values

| Column | Values |
|--------|--------|
| `emailType` | `WORK`, `PERSONAL`, `OTHER` |
| `phoneType` | `WORK`, `HOME`, `PERSONAL`, `OTHER` |

---

## Tests

### Run

```powershell
.\mvnw.cmd clean test
```

### Test classes

| Class | Coverage |
|-------|----------|
| `AuthServiceTest` | Register, login, duplicate email |
| `UserServiceTest` | Profile update, password change, duplicate phone |
| `ContactServiceTest` | CRUD, search, pagination |
| `ContactCsvServiceTest` | Export, import, round-trip, special characters |
| `GlobalExceptionHandlerTest` | Error response messages |
| `JwtServiceTest` | Token generation and validation |
| `AuthControllerTest` | Controller delegation |
| `ContactControllerIntegrationTest` | Full HTTP flow with mocked repos |
| `UserControllerIntegrationTest` | User endpoints with JWT |
| `ContactManagementSystemApplicationTests` | Spring context loads |

### Coverage report (JaCoCo)

```powershell
.\mvnw.cmd clean verify
```

Report: `target/site/jacoco/index.html`

---

## SonarQube

### Plugins (pom.xml)

| Plugin | Version | Purpose |
|--------|---------|---------|
| `jacoco-maven-plugin` | 0.8.12 | Code coverage |
| `sonar-maven-plugin` | 4.0.0.4121 | SonarQube scanner |

### Properties (pom.xml)

```xml
<sonar.projectKey>contact-management-system</sonar.projectKey>
<sonar.projectName>Contact Management System</sonar.projectName>
<sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
<sonar.exclusions>**/webapp/my-app/node_modules/**,**/webapp/my-app/dist/**</sonar.exclusions>
```

### Run analysis

```powershell
$env:SONAR_HOST_URL = "https://sonarcloud.io"
$env:SONAR_TOKEN = "your-sonar-token"

.\mvnw.cmd clean verify sonar:sonar `
  -Dsonar.host.url=$env:SONAR_HOST_URL `
  -Dsonar.token=$env:SONAR_TOKEN
```

**Steps performed:**
1. `clean` — removes previous build artifacts
2. `verify` — runs tests + generates JaCoCo XML report
3. `sonar:sonar` — uploads analysis to SonarQube/SonarCloud

---

## Configuration reference

| File | Purpose |
|------|---------|
| `src/main/resources/application.properties` | Runtime DB + JWT config |
| `src/test/resources/application.properties` | H2 test DB config |
| `pom.xml` | Dependencies, JaCoCo, SonarQube |
| `config/SecurityConfig.java` | URL access rules, filter chain |
| `config/JwtProperties.java` | Binds `jwt.secret` and `jwt.expiration-ms` |

### Key dependencies

| Dependency | Version |
|------------|---------|
| Spring Boot | 4.1.0 |
| JJWT | 0.12.6 |
| PostgreSQL driver | Managed by Spring Boot |
| H2 (test) | Managed by Spring Boot |
| Lombok | Managed by Spring Boot |
