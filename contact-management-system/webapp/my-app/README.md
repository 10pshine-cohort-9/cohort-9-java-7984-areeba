# Frontend — Contact Management System

React single-page application for the Contact Management System.

**Stack:** React 19 · Vite 8 · React Router 7 · Plain CSS (no UI framework)

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Environment variables](#environment-variables)
3. [Run locally](#run-locally)
4. [Build for production](#build-for-production)
5. [Application routes](#application-routes)
6. [Project structure](#project-structure)
7. [Authentication flow](#authentication-flow)
8. [API client](#api-client)
9. [Pages & features](#pages--features)
10. [Components](#components)
11. [Styling](#styling)
12. [Troubleshooting](#troubleshooting)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Node.js | **20.19+** or **22.12+** (required by Vite 8) |
| npm | 10+ (bundled with Node.js) |
| Backend API | Running on `http://localhost:8080` |

Check your version:

```powershell
node -v
npm -v
```

---

## Environment variables

Copy `.env.example` to `.env`:

```powershell
copy .env.example .env
```

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | *(empty)* | API base URL |

### Local development (recommended)

Leave `VITE_API_URL` empty. Vite's dev server proxies `/api` to `http://localhost:8080`:

```js
// vite.config.js
proxy: { "/api": { target: "http://localhost:8080" } }
```

### Direct API calls

```env
VITE_API_URL=http://localhost:8080
```

### Production

```env
VITE_API_URL=https://your-api-domain.com
```

> `VITE_API_URL` must be `https://` in production. `http://localhost` is allowed during local development only (enforced in `client.js`).

Serve the frontend over **HTTPS**. When `VITE_API_URL` is empty, authenticated requests use relative URLs and the client rejects bearer tokens unless the page is HTTPS (localhost HTTP is allowed during `npm run dev` only). Configure HTTPS redirects and HSTS on your reverse proxy or hosting platform.

---

## Run locally

From `webapp/my-app/`:

```powershell
npm install
npm run dev
```

App URL: **http://localhost:5173**

### Other scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start dev server with HMR |
| `npm run build` | Production build → `dist/` |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint |

---

## Build for production

```powershell
npm run build
```

Output directory: `dist/`

Serve `dist/` with any static file server (Nginx, Netlify, Vercel, etc.) and set `VITE_API_URL` to your deployed backend URL **before** building.

---

## Application routes

| Path | Access | Page | Description |
|------|--------|------|-------------|
| `/login` | Public | AuthEntryPage | Sign in |
| `/register` | Public | AuthEntryPage | Create account |
| `/dashboard` | Protected | DashboardPage | KPIs + recent contacts |
| `/contacts` | Protected | ContactsPage | Contact list, search, CSV import/export |
| `/contacts/new` | Protected | NewContactPage | Create contact form |
| `/contacts/:id/edit` | Protected | EditContactPage | Edit contact form |
| `/profile` | Protected | ProfilePage | View profile (read-only) |
| `/settings` | Protected | SettingsPage | Edit email/phone, change password |
| `/` | Redirect | → `/dashboard` | |
| `*` | Redirect | → `/dashboard` | |

Protected routes are wrapped by `ProtectedRoute` — unauthenticated users are redirected to `/login`.

---

## Project structure

```
src/
├── main.jsx                    # React entry point
├── App.jsx                     # Router + AuthProvider
├── index.css                   # Global styles
├── api/
│   └── client.js               # Fetch wrapper, auth token, all API calls
├── context/
│   └── AuthContext.jsx         # Login, register, logout, session state
├── pages/
│   ├── AuthEntryPage.jsx       # Login / register form
│   ├── DashboardPage.jsx       # Dashboard with KPIs
│   ├── ContactsPage.jsx        # Contact table, search, pagination, CSV
│   ├── NewContactPage.jsx      # Create contact
│   ├── EditContactPage.jsx     # Edit contact
│   ├── ProfilePage.jsx         # View profile
│   └── SettingsPage.jsx        # Update profile / password
├── components/
│   ├── ProtectedRoute.jsx      # Auth guard
│   ├── ErrorBoundary.jsx       # Catches render errors
│   ├── layout/
│   │   ├── AppLayout.jsx       # Sidebar + header + footer shell
│   │   ├── AuthLayout.jsx      # Centered auth page layout
│   │   ├── Header.jsx
│   │   ├── Sidebar.jsx
│   │   └── Footer.jsx
│   ├── contacts/
│   │   ├── ContactForm.jsx     # Shared create/edit form
│   │   ├── ContactTable.jsx    # Paginated contact table
│   │   └── ContactTypeBadge.jsx
│   ├── dashboard/
│   │   ├── KpiCard.jsx
│   │   └── WelcomeSection.jsx
│   └── common/
│       ├── Button.jsx
│       ├── Card.jsx
│       ├── Icons.jsx
│       ├── IconButton.jsx
│       ├── Modal.jsx
│       └── Tooltip.jsx
└── utils/
    └── contactUtils.js         # Contact type derivation, KPI helpers
```

---

## Authentication flow

```
Login/Register
    │
    ▼
api.login() → receives JWT
    │
    ▼
setToken(token) → saves to memory + localStorage["token"]
    │
    ▼
AuthContext.token state updated → isAuthenticated = true
    │
    ▼
ProtectedRoute allows access
```

### Session persistence

On page load, `AuthProvider` reads the token from `localStorage` via `getToken()` and restores the session without requiring re-login.

### Session expiry

- API returns `401` → `clearAuth()` removes token from memory and `localStorage`
- `AuthContext` state set to `null` → user redirected to `/login`
- Password change also invalidates the token server-side (`tokenVersion`)

---

## API client

All HTTP calls go through `src/api/client.js`.

### Core functions

| Export | Purpose |
|--------|---------|
| `setToken(token)` | Store JWT in memory + localStorage |
| `getToken()` | Read current JWT |
| `clearAuth()` | Remove token, call auth clear handler |
| `setAuthClearHandler(fn)` | Register callback for 401/logout |

### API methods (`api` object)

| Method | Endpoint |
|--------|----------|
| `api.register(body)` | POST `/api/auth/register` |
| `api.login(body)` | POST `/api/auth/login` |
| `api.getCurrentUser()` | GET `/api/users/me` |
| `api.updateProfile(body)` | PUT `/api/users/me` |
| `api.changePassword(body)` | PUT `/api/users/me/password` |
| `api.listContacts(page, size, sort)` | GET `/api/contacts` |
| `api.listAllContacts(sort, pageSize)` | Fetches all pages |
| `api.searchContacts(firstName, lastName, ...)` | GET `/api/contacts/search` |
| `api.getContact(id)` | GET `/api/contacts/{id}` |
| `api.createContact(body)` | POST `/api/contacts` |
| `api.updateContact(id, body)` | PUT `/api/contacts/{id}` |
| `api.deleteContact(id)` | DELETE `/api/contacts/{id}` |
| `api.exportContacts()` | GET `/api/contacts/export` → blob download |
| `api.importContacts(file)` | POST `/api/contacts/import` → FormData |

### Error handling

`parseResponseBody()` safely handles both JSON and plain-text error responses (Spring Boot returns plain strings for many errors). Non-JSON bodies no longer cause `SyntaxError`.

---

## Pages & features

### Dashboard (`/dashboard`)

- Welcome message with user's email
- KPI cards: total contacts, with email, with phone, rich profiles
- Recent contacts list (5 newest, sorted by `id` descending)

### Contacts (`/contacts`)

- Paginated table with sort dropdown (Name A–Z / Z–A)
- Search by first name and/or last name
- Delete with confirmation modal
- **Import CSV** — file picker, shows imported/failed counts
- **Export CSV** — downloads `contacts.csv`
- Link to create new contact

### New / Edit contact

- Shared `ContactForm` component
- Multiple email and phone fields (add/remove rows)
- Values trimmed before submit
- Email types: `WORK`, `PERSONAL`, `OTHER`
- Phone types: `WORK`, `HOME`, `PERSONAL`, `OTHER`

### Profile (`/profile`)

- Read-only view of email, phone, member since date

### Settings (`/settings`)

- Update email and phone number
- Change password (current + new)
- Shows API error messages (e.g. duplicate email/phone)

---

## Components

### ContactTypeBadge

Derives a display type from the contact's primary email/phone type:

| Badge | Condition |
|-------|-----------|
| Business | `WORK` email or phone |
| Personal | `PERSONAL` or `HOME` phone, or `PERSONAL` email |
| Other | Everything else |

Logic in `utils/contactUtils.js`.

### ProtectedRoute

Checks `isAuthenticated` from `AuthContext`. Redirects to `/login` if false.

### ErrorBoundary

Wraps the app to catch unexpected React render errors and show a fallback UI.

---

## Styling

- **No CSS framework** — custom CSS in `src/index.css`
- CSS variables for colors, spacing, and typography
- Responsive layout with sidebar navigation
- `.visually-hidden` utility for screen-reader-only labels (file inputs, icon buttons)

---

## Troubleshooting

### `VITE_API_URL must be a valid absolute URL...`

- Leave `.env` value empty for local dev, or use `http://localhost:8080`
- Do not use `http://` with a non-localhost domain

### API calls fail with CORS errors

- Use the Vite proxy (empty `VITE_API_URL`) during development
- Backend `CorsConfig` allows `http://localhost:5173`

### Logged out after page refresh

- Confirm `localStorage["token"]` exists after login (DevTools → Application)
- Ensure you are on the latest `AuthContext.jsx` (restores token on mount)

### CSV import shows 0 imported

- Check `phoneType` values: use `HOME` or `WORK`, not `Mobile`
- Emails must be valid (`user@domain.com`)
- Save as `.csv`, not `.xlsx`

### `npm run dev` fails — Node version

Vite 8 requires Node.js **20.19+** or **22.12+**. Upgrade Node.js if needed.

---

## Related documentation

- [Project README](../../README.md) — Full setup guide
- [Backend guide](../../BACKEND.md) — API reference, database, SonarQube
- [Repository README](../../../README.md) — Repository overview
