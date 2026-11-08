#!/usr/bin/env bash
# Starts the Spring Boot backend on http://localhost:8080 using the in-memory H2 "dev" profile.
# Requires JDK 21. No PostgreSQL needed.
set -euo pipefail
cd "$(dirname "$0")/backend"
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
