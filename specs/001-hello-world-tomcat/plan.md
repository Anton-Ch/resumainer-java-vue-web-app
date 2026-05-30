# Implementation Plan: Hello World Tomcat Setup

**Branch**: `feat/001-hello-world-tomcat` | **Date**: 2026-05-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-hello-world-tomcat/spec.md`

---

## Summary

Set up a minimal Java Spring MVC application that deploys as a WAR to Tomcat, serves a Hello World page, and runs via Docker Compose with PostgreSQL. The build uses Maven with Maven Wrapper, a multi-stage Dockerfile, and externalized configuration via Spring profiles and environment variables.

**Primary goal**: Validate the full development workflow from `git clone` to `docker compose up` to "Hello World" in the browser.

---

## Technical Context

| Attribute | Value |
|---|---|
| **Language/Version** | Java 21 LTS |
| **Web Framework** | Spring MVC 6.x (Jakarta Servlet 6.0) |
| **Servlet Container** | Apache Tomcat 10.1.x |
| **Build Tool** | Maven 3.9.x (via Maven Wrapper) |
| **Storage** | PostgreSQL 16 (container only, not used by Hello World) |
| **Testing** | JUnit 5 + Mockito (basic smoke test for controller) |
| **Target Platform** | Docker (local dev) → Linux VPS (future) |
| **Project Type** | Web application (backend only) |
| **Performance Goals** | Cold start < 2s, Docker image < 300MB |
| **Constraints** | No Spring Boot, no JPA/Hibernate, plain JDBC (future), UTF-8 encoding |
| **Scale/Scope** | Single developer local setup, 1 WAR, 2 Docker containers |

---

## Constitution Check

*GATE: Must pass before proceeding to task breakdown.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | Minimal code (1 controller, 1 view). Package structure: `controller/`, `config/`. Maven CLI build via `mvnw`. |
| **II. Testing Excellence** | ✅ Pass | Basic smoke test for controller. Coverage target 50% not applicable yet — no business logic in this feature. |
| **III. User Experience Consistency** | ✅ Pass | i18n not applicable — Hello World page is English. PRG not applicable — no forms. |
| **IV. Performance & Reliability** | ✅ Pass | `PreparedStatement` not applicable yet — no database queries. Docker healthcheck not used (startup script instead). UTF-8 configured. |
| **V. Security by Design** | ✅ Pass | No stack traces exposed (FR-009). No secrets in build or images. Logback configured. |

**No violations.** Complexity tracking not required.

---

## Architecture & Design

### Overview

```
┌──────────────────────────────────────────────────┐
│                  Docker Compose                  │
│                                                  │
│  ┌──────────────────┐      ┌──────────────────┐  │
│  │   tomcat-server  │      │  postgres-db     │  │
│  │  (Tomcat 10.1)   │      │ (PostgreSQL 16)  │  │
│  │                  │      │                  │  │
│  │  WAR: resumainer │      │  port: 5432      │  │
│  │  port: 8080      │      │  volume: pgdata  │  │
│  │                  │      │                  │  │
│  │  Wait for DB ┄┄┄┄┼──────┤  Ready check     │  │
│  └──────────────────┘      └──────────────────┘  │
└──────────────────────────────────────────────────┘
         │
         │ HTTP GET /
         ▼
   ┌──────────────┐
   │   Browser    │
   │ Hello World  │
   └──────────────┘
```

### Layers

1. **Controller** — `controller/HelloWorldController.java`
   - Maps `GET /` to Hello World view
   - Injects current server time into model
   
2. **Config** — `config/WebConfig.java`
   - Spring MVC annotation-driven configuration
   - View resolver setup
   - No component scan (explicit bean registration)

3. **View** — `WEB-INF/views/hello.jsp` (or Thymeleaf template)
   - Displays application name, server time, profile name

4. **AppInitializer** — `AppInitializer.java`
   - Extends `AbstractAnnotationConfigDispatcherServletInitializer`
   - Registers DispatcherServlet, loads `WebConfig`
   - Auto-detected by Tomcat 10+ via ServletContainerInitializer mechanism (no web.xml needed)

### Startup Sequence (Docker)

```
1. docker compose up
2. PostgreSQL container starts, creates volume
3. Tomcat container starts
4. wait-for-it.sh blocks until PostgreSQL:5432 is reachable
5. Tomcat catalina.sh starts
6. WAR auto-deploys (ROOT.war)
7. Application responds on http://localhost:8080
```

---

## Project Structure

```
backend/
├── pom.xml                          # Maven project descriptor
├── mvnw                             # Maven Wrapper script (at pom.xml level per Maven docs)
├── mvnw.cmd                         # Maven Wrapper script for Windows
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties # Maven version pinning (3.9.x)
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── resumainer/
│       │           ├── config/
│       │           │   └── WebConfig.java          # @EnableWebMvc + ViewResolver
│       │           ├── controller/
│       │           │   └── HelloWorldController.java
│       │           └── initializer/
│       │               └── AppInitializer.java     # extends AbstractAnnotationConfigDispatcherServletInitializer
│       ├── resources/
│       │   ├── application.properties
│       │   ├── application-dev.properties
│       │   └── application-prod.properties
│       └── webapp/
│           └── WEB-INF/
│               └── views/
│                   └── hello.jsp
│   └── test/
│       └── java/
│           └── com/
│               └── resumainer/
│                   └── controller/
│                       └── HelloWorldControllerTest.java

docker/
├── Dockerfile                        # Multi-stage build
├── docker-compose.yml                # Tomcat + PostgreSQL
└── scripts/
    └── wait-for-it.sh                # Startup readiness script

dev-docs/
└── learnings.md                      # Learning document (already created)

.gitignore                            # Project root (to be created)
```

---

## Implementation Phases

### Phase 0: Project Scaffold
- Create `backend/pom.xml` with Maven Wrapper
- Create `.gitignore`
- Verify `./mvnw clean package` succeeds

### Phase 1: Spring MVC Hello World
- Create `WebConfig.java` (annotation-driven Spring MVC)
- Create `HelloWorldController.java` (GET / → model + view)
- Create `hello.jsp` (display app name, time, active profile)
- Create `AppInitializer.java` (extends `AbstractAnnotationConfigDispatcherServletInitializer`, loads `WebConfig`, maps `/`)
- Create `application-{dev,prod}.properties` (profiles, log levels, port)

### Phase 2: Docker Setup
- Create `docker/Dockerfile` (multi-stage: Maven build → Tomcat run)
- Create `docker/docker-compose.yml` (tomcat + postgres services)
- Add `wait-for-it.sh` to Docker image
- Verify `docker compose up` → Hello World in browser

### Phase 3: Testing
- Unit test for `HelloWorldController` (status 200, model attributes)
- Manual Docker Compose smoke test

---

## Key Technical Decisions

| Decision | Choice | Rationale |
|---|---|---|
| View technology | JSP | Simplest for Hello World. Thymeleaf is for Landing Page (future). |
| Servlet initialization | `AppInitializer` extends `AbstractAnnotationConfigDispatcherServletInitializer` | Standard Spring MVC 6 approach for Jakarta EE 10. Auto-detected by Tomcat 10+. No web.xml needed. |
| Spring config | Java config (`@Configuration` + `@EnableWebMvc`) | No XML, type-safe, compile-time checked. Modern Spring MVC practice. |
| WAR packaging | WAR (not JAR) | Required for external Tomcat. No embedded server. |
| Logging | SLF4J + Logback via Maven | Matches constitution requirement. dev=DEBUG, prod=INFO. |
| DB driver | PostgreSQL JDBC driver in `pom.xml` | Not used yet, but required for future features. |

---

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Port 8080/5432 already in use | Medium | Medium | Document port override via env vars. |
| Maven Wrapper download failure | Low | Medium | Wrapper JAR committed to repo, properties pin version. |
| Docker not installed | Low | High | Document prerequisites in README. |
| Windows path issues in Docker | Medium | Low | Use forward slashes, `.dockerignore` for Windows files. |

---

## Out of Scope (this feature)

- Frontend (Vue 3) — will be added in a separate feature
- Database schema or migrations — PostgreSQL container only, no schema
- Business logic, user auth, resume generation
- CI/CD pipeline — manual Docker Compose only
- Swagger/OpenAPI — will be added with admin features later
