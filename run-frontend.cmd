@echo off
REM Starts the Vite dev server on http://localhost:5173 (proxies /api to the backend on :8080).
cd /d "%~dp0frontend"
if not exist node_modules call npm install
call npm run dev
