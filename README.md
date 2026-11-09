# codebridge
GitHub repository compatibility and integration risk analysis platform.

The project has two separate apps:

| Folder      | What                                                  | Port |
|-------------|-------------------------------------------------------|------|
| `backend/`  | Spring Boot 4 REST API (Java 21)                      | 8080 |
| `frontend/` | React + TypeScript SPA (Vite, MUI / Material Design) | 5173 |

In development the frontend calls relative `/api/...` URLs. The Vite dev server
proxies them to the backend, so there is no CORS setup to worry about.

---

## Prerequisites

- **JDK 21**, with `JAVA_HOME` pointing at it. Check with `java -version`.
- **Node.js 20.19+ or 22.12+**. Check with `node -v`.
- Maven is **not** needed; the project ships the Maven wrapper (`mvnw`).
- PostgreSQL is **not** needed for local runs (see the `dev` profile below).

## Quick start (two terminals)

**Terminal 1 – backend** (Windows: double-click or run `run-backend.cmd`)

```bash
./run-backend.sh            # macOS / Linux / Git Bash
```

This starts Spring Boot with the `dev` profile: an in-memory H2 database, with the schema
created by Flyway. Wait until you see `Started BackendApplication`.
Check it: <http://localhost:8080/api/health> should print "CodeBridge Backend is Running!".

**Terminal 2 – frontend** (Windows: `run-frontend.cmd`)

```bash
./run-frontend.sh           # runs npm install the first time, then npm run dev
```

Open <http://localhost:5173>. The status pill in the header turns green when the backend is reachable.

### Running the frontend without the backend

```bash
cd frontend
npm install
npm run dev:mock
```

The UI then runs on built-in sample data, which is handy for working on the UI or for a
demo when Java isn't available.

## Backend profiles

| Profile          | Database               | How to run                                                           |
|------------------|------------------------|-----------------------------------------------------------------------|
| *(default)*      | PostgreSQL `codebridge_db` | `DB_PASSWORD=... ./mvnw spring-boot:run` (used by CI)            |
| `dev`            | H2 in-memory           | `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` or `run-backend.*` |

IntelliJ: *Run → Edit Configurations → BackendApplication → Active profiles:* `dev`.

The `dev` profile also sets a fixed login (`demo` / `demo123`) instead of Spring's random
generated password. You can override it with the `CODEBRIDGE_USER` / `CODEBRIDGE_PASSWORD`
environment variables. If you change it, copy `frontend/.env.example` to `frontend/.env` and
set the same values there.

## How the frontend talks to the backend

- **Auth:** every endpoint except `/api/health` needs HTTP Basic. The Vite proxy adds the
  `Authorization` header (see `frontend/vite.config.ts`), so credentials never ship to the browser.
- **CSRF:** Spring uses a cookie-based CSRF token. Before the first POST, the Axios client
  (`frontend/src/api/client.ts`) calls `GET /api/csrf`, then sends the `XSRF-TOKEN` cookie
  back as the `X-XSRF-TOKEN` header automatically.
- **Endpoints used:** `POST /api/maven/compare`, `GET /api/github/repositories/{owner}/{repo}`,
  `GET /api/health`.
- TypeScript versions of the Java DTOs live in `frontend/src/types/api.ts`.

## Frontend structure

```
frontend/src
├── api/           Axios instance (+ CSRF), typed API service, mock API
├── components/
│   ├── compare/   CompareForm, RepoCard, SummaryStats, ConflictTable, LoadingState, EmptyState
│   └── layout/    AppHeader (backend status, light/dark toggle)
├── hooks/         useRepoComparison, useBackendHealth
├── lib/           owner/repo parsing
├── theme.ts       Material Design theme (palette, Roboto, shapes, component defaults)
└── types/api.ts   interfaces matching the backend DTOs
```

The UI is built with [MUI](https://mui.com/material-ui/) (Material UI), the React
implementation of Google's Material Design. Colours, typography and component defaults
live in `src/theme.ts`. Repository A uses the `primary` colour and repository B uses
`secondary`.

**Windows note:** MUI is plain JavaScript, so Windows Smart App Control doesn't block it.
For the same reason, `package.json` overrides Rollup with its WebAssembly build
(`@rollup/wasm-node`). Smart App Control blocks Rollup's native Windows binary.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `release version 21 not supported` / `invalid target release` | `JAVA_HOME` points at an older JDK. Install JDK 21 and point `JAVA_HOME` at it. |
| Header says **Backend offline** | The backend isn't running on :8080, or another app is using the port. |
| "The backend rejected the login" | The credentials in `frontend/.env` don't match the backend's `dev` profile. |
| "GitHub API rate limit reached" | Unauthenticated GitHub allows 60 requests/hour per IP, and each comparison uses about 4. Wait, or switch networks. |
| "Root pom.xml not found" | The repository has no `pom.xml` at its root (e.g. a Gradle project, or the pom is in a subfolder). |
| "An Application Control policy has blocked this file" | Windows Smart App Control blocked a native npm binary. Delete `frontend/node_modules` and `frontend/package-lock.json`, then run `npm install` again so the `rollup` override takes effect. |
| `Port 5173 is in use` | Stop the other Vite server, or change `server.port` in `vite.config.ts`. |
