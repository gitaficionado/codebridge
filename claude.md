# Git Compare Project Guidelines

## Tech Stack
- Backend: Spring Boot (Java) running on port 8080
- Frontend: React (Vite) + TypeScript + MUI (Material UI / Material Design) running on port 5173

## API Routing
- All frontend API calls must use relative paths starting with `/api` (handled by Vite proxy).
- Compare endpoint: `POST /api/maven/compare` with `{ existingRepositoryUrl, incomingRepositoryUrl }` (full https://github.com/owner/repo URLs)
- Repo metadata: `GET /api/github/repositories/{owner}/{repo}`; health: `GET /api/health` (public)
- Backend requires HTTP Basic (added by the Vite proxy) and a CSRF token (`XSRF-TOKEN` cookie -> `X-XSRF-TOKEN` header; prime it with `GET /api/csrf`)
- Run the backend locally with the `dev` profile (H2 in-memory): `run-backend.cmd` / `run-backend.sh`

## Code Style
- Frontend: Strict TypeScript, functional components, hooks for data fetching.
- Do not modify files in the backend directory unless explicitly asked to adjust a controller mapping.
