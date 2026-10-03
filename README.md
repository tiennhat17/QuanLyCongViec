# Task Manager API

Spring Boot REST API scaffold for the personal task management project.

## Requirements

- JDK 21
- Docker Desktop with Docker Compose, for the MySQL-backed local stack
- Git

Maven is provided by the checked-in Maven Wrapper.

## Run tests

In PowerShell, from this directory:

```powershell
.\mvnw.cmd -B verify
```

Tests use an in-memory H2 database; they do not require Docker or MySQL.

## Run the application with MySQL

Copy `.env.example` to `.env`, replace both sample passwords with local values, then start the stack:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

The API listens on `http://localhost:8080`. Check `GET /api/hello` and `GET /actuator/health`. Stop the stack with `Ctrl+C`; run `docker compose down` to stop containers. Use `docker compose down -v` only when you intend to delete the local database volume.

Spring Boot does not load `.env` automatically. Compose reads it for container configuration; when launching from the IDE, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the run configuration or the shell environment.

## Current scope

This scaffold provides a smoke-test endpoint and local infrastructure only. The task, category, account, and authorization features remain to be implemented. The current HTTP Basic setup is temporary; replace it with the authentication design selected for the project before adding protected application APIs. `ddl-auto=update` is for local development and must not be used as a production schema-management strategy.