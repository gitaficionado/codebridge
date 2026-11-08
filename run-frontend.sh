#!/usr/bin/env bash
# Starts the Vite dev server on http://localhost:5173 (proxies /api to the backend on :8080).
set -euo pipefail
cd "$(dirname "$0")/frontend"
[ -d node_modules ] || npm install
npm run dev
