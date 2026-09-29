# Seatly — Event RSVP & Waitlist Manager

An event RSVP platform: organisers create events with a seat limit,
attendees RSVP and are either confirmed or placed on a waitlist, and the
next waitlisted person is promoted automatically when a seat frees up.

Full architectural and product decisions live in [CLAUDE.md](CLAUDE.md).

## Tech stack

| Layer      | Choice                                    |
| ---------- | ------------------------------------------ |
| Backend    | Java 21, Spring Boot 4.1.1 (Maven wrapper) |
| Database   | PostgreSQL 16 (Docker)                     |
| Migrations | Flyway                                     |
| Auth       | JWT (jjwt) + Spring Security               |
| API docs   | springdoc-openapi / Swagger UI             |
| Frontend   | Next.js + TypeScript                       |
| FE data    | TanStack Query, Zustand                    |
| FE styling | Tailwind                                   |

## Prerequisites

- Java 21
- Docker (with Docker Compose)
- Node.js (LTS) and npm — for the frontend

No local Maven or PostgreSQL install is needed: the backend uses the Maven
wrapper (`./mvnw`), and Postgres runs in Docker.

## Setup from a fresh clone

1. **Start the database**

   ```bash
   docker compose up -d
   ```

   This starts Postgres 16 on host port `5433` (database `seatly`, user/password
   `seatly`), matching `backend/src/main/resources/application.yml`.

2. **Create `backend/.env`**

   The backend's secrets live in `backend/.env`, which is git-ignored.
   `backend/.env.example` lists every key it needs — copy it and fill it in:

   ```bash
   cp backend/.env.example backend/.env
   openssl rand -base64 32
   ```

   Paste the generated value after `JWT_SIGNING_KEY=`. It signs access
   tokens: Base64, at least 32 bytes, and the app refuses to start without it.
   Changing it logs everyone out, since existing tokens stop verifying.

   `application.yml` imports this file (`spring.config.import`). A real
   environment variable with the same name overrides it — that's how CI and
   production supply the key, with no file on disk.

3. **Run the backend**

   ```bash
   cd backend
   ./mvnw clean spring-boot:run
   ```

   Flyway applies the schema on startup. The API is served at
   `http://localhost:8080`, with Swagger UI at
   `http://localhost:8080/swagger-ui.html`.

   Two seeded dev accounts exist (see `V2__seed_data.sql` and
   `V3__seed_second_user.sql` for the password): `organiser@seatly.dev`, who
   owns the demo events, and `attendee@seatly.dev`. In Swagger, call
   `POST /v1/auth/login`, copy `accessToken`, and paste it into **Authorize**.

   Tests don't need the key or the local database: they start their own
   Postgres with Testcontainers and use a test-only key.

   ```bash
   ./mvnw clean test
   ```

4. **Run the frontend**

   The `frontend/` directory is not yet scaffolded. Once the Next.js app
   exists there, the usual flow will be:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   This section will be filled in with the real port/URL once the frontend
   is bootstrapped.

## Repository layout

```
backend/    Spring Boot API
frontend/   Next.js app
```
