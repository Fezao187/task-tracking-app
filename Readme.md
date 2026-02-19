# Task Tracking API (Java Developer Assessment)

## Table of Contents

- [Overview](#overview)
- [Setup & Requirements](#setup--requirements)
- [Core Design Decisions & Trade-offs](#core-design-decisions--trade-offs)
- [What I’d Improve With More Time](#what-id-improve-with-more-time)
- [How to Test / Observe the Scheduler](#how-to-test--observe-the-scheduler)
- [API Endpoints Overview](#api-endpoints-overview)

---

## Overview

This project is a lightweight Task-tracking REST API built as a Java Developer Assessment.
It demonstrates:

- Java 21 and Spring Boot 4.x
- Spring Data JPA with PostgreSQL and Flyway migrations
- Spring Security with JWT (access + refresh tokens)
- Bean Validation (Jakarta Validation)
- Background processing via Spring Scheduling
- API documentation with SpringDoc OpenAPI 3 (Swagger UI)

The API exposes endpoints for:

- User authentication (signup, login, refresh token)
- CRUD-style operations on tasks
- Admin-only user creation
- Filtering tasks by status, due date, and assignee

---

## Setup & Requirements

### Java & Tools

- Docker 24+
- Docker Compose v2+
- Java 21 (only required if running locally without Docker)

### Environment Variables

The application reads configuration from environment variables. Typical values:

- `POSTGRES_USER` – database user
- `POSTGRES_PASSWORD` – database password
- `POSTGRES_DB` - database name
- `SPRING_DATASOURCE_URL` – JDBC URL, for example:  
  `jdbc:postgresql://localhost:5432/taskdb`
- `JWT_SECRET` – secret key for signing JWT access and refresh tokens

### Running Locally (without Docker)

1. Start PostgreSQL and create a database (e.g. `taskdb`).
2. Export the required environment variables:

   ```bash
   export POSTGRES_USER=taskuser
   export POSTGRES_PASSWORD=taskpass
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskdb
   export JWT_SECRET=change_me
   ```

3. Run the application:

   ```bash
   ./gradlew bootRun
   ```

4. The API will be available at `http://localhost:8080`.

### Running with Docker

The repo includes a `docker-compose-demo` file for running the application: 

#### 1. Before running the Application
Add `.env` file with the following:
```bash
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/{POSTGRES_DB}
JWT_SECRET=
```

#### 2. Start the Application
  ```bash
  docker compose -f docker-compose-demo.yml up --build
  ```
This will:
- Build the Spring Boot application using the Gradle wrapper
- Start a PostgreSQL 17 container
- Start the API container
- Automatically apply database migrations (Liquibase/Flyway)
- Seed initial data

#### 3. Access Points
- **API**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Swagger-docs**: http://localhost:8080/swagger-ui.html

---
## Core Design Decisions & Trade-offs

### Technology Stack

- **Spring Boot 4.x + Java 21**
  - Modern, production-ready stack with strong ecosystem support.
- **Spring Data JPA + PostgreSQL**
  - JPA simplifies CRUD and query logic.
  - PostgreSQL is a reliable relational database with good support in Spring and Flyway.
- **Flyway for migrations**
  - Simple versioned SQL scripts applied on startup.
- **Spring Security + JWT**
  - Stateless authentication suitable for REST APIs.
  - Access + refresh tokens implemented via a dedicated JWT service and refresh token store.

**Trade-off:**  
Using JPA and annotations couples persistence to the domain model. For a larger system, a more explicit domain layer (with richer aggregates) might be preferable, but JPA keeps this assessment lightweight and productive.

### Task Model & Filtering

- `TaskStatus` enum with `NEW`, `IN_PROGRESS`, `COMPLETED`, `DELAYED`, `OVERDUE`.
- Dynamic filtering implemented via `Specification<Task>` + `JpaSpecificationExecutor`:
  - Filter by status, due date range, and assignee.

**Trade-off:**  
Specifications are slightly more complex than simple repository query methods, but they scale better as more filters are added.

### Security & Roles

- Roles: `ADMIN` and `USER`.
- Method-level security via `@PreAuthorize("hasRole('ADMIN')")` for admin-only operations.
- JWT tokens used for both access and refresh, with a service managing refresh token validity and rotation.

**Trade-off:**  
The two-role model is intentionally simple. In a real system, more granular permissions might be implemented (e.g. per-project roles or permissions).

---

## What I’d Improve With More Time

Given more time, the following improvements would be prioritized:

- **UI**  
  - Build a minimal Angular front-end to interact with the API (login, task list, filters, admin screens).
  - Add unit and end-to-end tests.

- **Testing**  
  - Add integration and Acceptance tests using Testcontainers with PostgreSQL to validate migrations, JPA mappings, and security end-to-end.

- **CI/CD**  
  - Add a GitHub Actions pipeline for deploying and testing (intergration, end-2-end, acceptance) PR.
  
- **Deployment**
  - Deploy the UI and API on AWS
  
- **Observability**  
  - Add structured logging, request correlation IDs, and metrics (e.g. Micrometer + Prometheus) for scheduler execution and API usage.

- **Features**
  - Email notifications for overdue tasks and email notification when user is created using Spring Mail.
  - Which admin created and assigned the task.
  
These changes would move the project closer to full production readiness while keeping the core design intact.

---

## How to Test / Observe the Scheduler

The scheduler is responsible for marking tasks as `OVERDUE` once their `dueDate` has passed.

### Configuration

The schedule is configured via a cron expression in `application.yml`, for example:

```yaml
schedule:
  task-overdue-schedule-time: "0 0 * * * *" # every hour
```

The scheduler:

- Finds tasks whose `dueDate` is before “now” and whose status is in a configured set (e.g. `NEW`, `IN_PROGRESS`, `DELAYED`).
- Uses a bulk update to set their status to `OVERDUE`.
- Logs before and after execution, including how many tasks were updated.

### How to Observe It

1. Start the application (locally or via Docker).
2. Create several tasks via the API:
   - Some with `dueDate` in the past.
   - Some with `dueDate` in the near future.
3. Wait for the scheduler to run (based on the cron expression).
4. Check the logs:
   - Look for log entries mentioning the overdue task scheduler starting and finishing, and the number of tasks updated.
5. Call the task endpoints:
   - Verify that tasks with past `dueDate` have their `status` changed to `OVERDUE`.

---

## API Endpoints Overview

### Authentication (`/api/auth`)

- `POST /api/auth/signup`  
  Registers a new user with `USER` role and returns access + refresh tokens.

- `POST /api/auth/login`  
  Authenticates a user and returns access + refresh tokens.

- `POST /api/auth/refresh`  
  Accepts a refresh token and returns a new access token and rotated refresh token.

### Tasks (`/api/task`)

- `POST /api/task/create` (ADMIN)  
  Creates a new task.

- `PUT /api/task/update/{id}` (ADMIN)  
  Updates an existing task.

- `GET /api/task/{id}`  
  Returns a single task by id.

- `GET /api/task/my-tasks`  
  Returns tasks assigned to the authenticated user.

- `GET /api/task/all` (ADMIN)  
  Returns all tasks.

- `DELETE /api/task/{id}` (ADMIN)  
  Deletes a task; returns a confirmation response.

- `POST /api/task/{id}/assign` (ADMIN)  
  Assigns a task to a user.

- `GET /api/task/search` (ADMIN)  
  Returns a paginated list of tasks filtered by:
  - `status`
  - `dueDateFrom` / `dueDateTo`
  - `assignedUserId`

### Users (`/api/user`)

- `POST /api/user/create` (ADMIN)  
  Creates a new user account (typically non-admin) from an admin context.