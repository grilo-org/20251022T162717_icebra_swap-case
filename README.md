# Swap Case - Processadora (Java 21, Spring Boot 3)

## Overview
This project implements the technical test: schedule a job that fetches GitHub issues and contributors and posts a JSON payload to a configured webhook exactly 24 hours after the request.

## Important design decisions (based on your clarifications)
a) The webhook is called exactly 24 hours after the request (the job's `executeAt = requestedAt + 24 hours`).
b) The JSON contains at least the requested fields (`user`, `repository`, `issues`, `contributors`) and includes an extra `generatedAt` timestamp.
c) The GitHub client uses pagination (`per_page=100` + page iteration).
d) Persisting scheduled jobs: jobs are stored in PostgreSQL so a service restart won't lose pending jobs. This avoids relying only on memory.
e) Tests: basic unit tests are included; you should add more integration tests in a complete solution.

## How to run (with Docker)
1. Build the jar:
   ```
   mvn -DskipTests package
   ```
2. Start Postgres and the app (see docker-compose.yml):
   ```
   docker compose up --build
   ```

## API
- `POST /api/schedule` -> schedule a job (body: { githubUser, repository, webhookUrl })
- `GET /api/jobs/{id}` -> get job status

## Notes
- The scheduled processor runs every minute and will execute jobs whose `executeAt` <= now.
- The GitHub client uses unauthenticated requests (rate limits apply). For production, use authentication (token) and better error handling / retries.
