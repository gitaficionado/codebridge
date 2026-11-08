@echo off
REM Starts the Spring Boot backend on http://localhost:8080 using the in-memory H2 "dev" profile.
REM Requires JDK 21 (JAVA_HOME must point to it). No PostgreSQL needed.
cd /d "%~dp0backend"
set SPRING_PROFILES_ACTIVE=dev
call mvnw.cmd spring-boot:run
