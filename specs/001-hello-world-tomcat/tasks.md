# Tasks: Hello World Tomcat Setup

**Input**: Design documents from `specs/001-hello-world-tomcat/`

**Prerequisites**: [plan.md](plan.md) (required), [spec.md](spec.md) (required for user stories)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format

- `[P]` — Can run in parallel (different files, no dependencies)
- `[US1]` through `[US4]` — Which user story this task belongs to
- File paths are relative to repository root

---

## Phase 1: Setup (Project Scaffold)

**Purpose**: Initialize project structure, build tools, and git hygiene

- [ ] T001 Create backend directory structure per plan.md (`backend/src/main/java/com/resumainer/`, `backend/src/main/resources/`, `backend/src/main/webapp/WEB-INF/views/`, `backend/src/test/java/com/resumainer/`, `docker/`, `docker/scripts/`)
- [ ] T002 [P] Generate Maven Wrapper in `backend/` (creates `backend/mvnw`, `backend/mvnw.cmd`, `backend/.mvn/wrapper/maven-wrapper.properties` — at same level as `backend/pom.xml` per Maven docs)
- [ ] T003 Create `backend/pom.xml` with dependencies: Spring MVC 6.x, Tomcat 10.1 (provided), JSP (provided), SLF4J + Logback, JUnit 5 + Mockito, PostgreSQL JDBC driver, Servlet API (provided)
- [ ] T004 Update `.gitignore` with Java, Maven, IDE, OS, secrets, and Docker patterns

**Checkpoint**: `./mvnw clean package` compiles (may fail on missing sources — expected at this stage)

---

## Phase 2: Foundational (Spring MVC Application Skeleton)

**Purpose**: Core Spring MVC app that MUST be complete before any user story can be verified

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 [P] Create `AppInitializer.java` in `backend/src/main/java/com/resumainer/initializer/` extending `AbstractAnnotationConfigDispatcherServletInitializer` — registers `DispatcherServlet` with `"/"` mapping, loads `WebConfig`
- [ ] T006 [P] Create `WebConfig.java` in `backend/src/main/java/com/resumainer/config/` with `@Configuration`, `@EnableWebMvc`, `ViewResolver` for JSP (`/WEB-INF/views/`, `.jsp`)
- [ ] T007 [P] Create `HelloWorldController.java` in `backend/src/main/java/com/resumainer/controller/` — maps `GET /`, adds "appName", "serverTime", "activeProfile" to model
- [ ] T008 [P] Create `hello.jsp` in `backend/src/main/webapp/WEB-INF/views/` — displays app name, current server time, active Spring profile
- [ ] T009 Create `application.properties`, `application-dev.properties`, `application-prod.properties` in `backend/src/main/resources/` — logging levels per profile (dev=DEBUG, prod=INFO), server port config, UTF-8 encoding, error handling (`server.error.include-stacktrace=never` to satisfy FR-009)

**Checkpoint**: `./mvnw clean package` compiles successfully — `backend/target/*.war` exists

---

## Phase 3: User Story 1 — Docker Compose Hello World (Priority: P1) 🎯 MVP

**Goal**: Start the application with `docker compose up` and see Hello World page in a browser

**Independent Test**: Run `docker compose up` from `docker/`, open `http://localhost:8080`, see Hello World page with app name and time

- [ ] T010 [P] [US1] Create multi-stage `Dockerfile` in `docker/Dockerfile` — stage 1: Maven build (mvnw clean package), stage 2: Tomcat 10.1 with WAR copied as ROOT.war, non-root user
- [ ] T011 [P] [US1] Create `docker-compose.yml` in `docker/` — Tomcat service (port 8080, env vars from `.env`), PostgreSQL service (port 5432, named volume `pgdata`), shared network; use `${DB_PASSWORD:?error}` for required vars and `${DB_USER:-resumainer}` for optional ones
- [ ] T011b [P] [US1] Create `docker/.env.example` with placeholder values (no real secrets) — document all required and optional env vars for developers; this file IS committed to the repository (safe: contains only keys with example values, no secrets)
- [ ] T012 [US1] Add `wait-for-it.sh` script to `docker/scripts/` — pins to upstream commit, source comment; Tomcat entrypoint waits for PostgreSQL:5432 before starting (depends on T010)
- [ ] T013 [US1] Verify `docker compose up` → Hello World page in browser with app name and server time (depends on T010, T011, T012, T005-T009)

**Checkpoint**: At this point, User Story 1 should be fully functional — MVP delivers validated Docker setup

---

## Phase 4: User Story 2 — Maven Build Verification (Priority: P2)

**Goal**: Verify the build is reproducible and CI-ready with `mvn clean package`

**Independent Test**: Run `./mvnw clean package` on a clean checkout, verify WAR exists in `backend/target/`

- [ ] T014 [US2] Run `./mvnw clean package` and verify `backend/target/*.war` exists with correct file size (depends on T001-T009)
- [ ] T015 [US2] Deploy WAR to local Tomcat and verify Hello World page responds at root context (depends on T014)

**Checkpoint**: User Story 2 complete — build pipeline validated

---

## Phase 5: User Story 3 — Multi-Stage Docker Build (Priority: P3)

**Goal**: Verify Docker multi-stage build produces a minimal image under 300 MB

**Independent Test**: Run `docker build -t resumainer .` from `docker/`, verify image size and container starts correctly

- [ ] T016 [US3] Run `docker build -t resumainer:latest -f docker/Dockerfile .` and verify image size is under 300 MB (depends on T010)
- [ ] T017 [US3] Run container from built image, verify application responds on configured port, stop container gracefully within 10 seconds (depends on T016)

**Checkpoint**: User Story 3 complete — reproducible image pipeline validated

---

## Phase 6: User Story 4 — Configuration Externalization (Priority: P3)

**Goal**: Verify profiles and environment variables control application behavior without code changes

**Independent Test**: Run app with `SPRING_PROFILES_ACTIVE=prod` and `SERVER_PORT=9090`, verify logging level and port change

- [ ] T018 [P] [US4] Start Docker Compose with `SPRING_PROFILES_ACTIVE=prod` and verify only INFO-level logging in console (depends on T009, T010-T012)
- [ ] T019 [P] [US4] Start Docker Compose with `SERVER_PORT=9090` and verify application responds on port 9090 (depends on T009, T010-T012)

**Checkpoint**: All user stories complete

---

## Phase 7: Testing & Polish

**Purpose**: Unit testing and final validation

- [ ] T020 Create `HelloWorldControllerTest.java` in `backend/src/test/java/com/resumainer/controller/` — JUnit 5 + Mockito, test status 200 and model attributes for `GET /` (depends on T005-T008)
- [ ] T021 Run full verification suite: `./mvnw clean package` + `docker compose up` + browser check (depends on T013, T014, T016, T017, T018, T019, T020)

**Checkpoint**: Feature complete — all acceptance criteria satisfied

---

## Execution Wave DAG

```
Wave 0 (parallel):     T001  T002  T004
Wave 1 (parallel):     T003                    (depends on T001, T002)
Wave 2 (parallel):     T005  T006              (depends on T001-T003)
Wave 3 (parallel):     T007  T008  T009        (depends on T005, T006)
Wave 4 (build gate):   [mvnw clean package]    (depends on T007, T008, T009)
Wave 5 (parallel):     T010  T011  T011b        (depends on T009)
Wave 6:                T012                    (depends on T010)
Wave 7 (MVP gate):     T013                    (depends on T010-T012, T005-T009)
Wave 8 (parallel):     T014  T016              (depends on T001-T009, T010)
Wave 9:                T015                    (depends on T014)
Wave 10:               T017                    (depends on T016)
Wave 11 (parallel):    T018  T019              (depends on T009, T010-T012)
Wave 12:               T020                    (depends on T005-T008)
Wave 13 (final gate):  T021                    (depends on all)
```

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational
  - US1 (P1, MVP) can be delivered independently after Foundational
  - US2 (P2) depends on Foundational (app must exist to build)
  - US3 (P3) depends on Dockerfile (T010)
  - US4 (P3) depends on Foundational + Docker Compose
- **Testing (Phase 7)**: Depends on all user stories

### Parallel Opportunities

- T001 + T002 + T004 — Setup tasks can run in parallel
- T005 + T006 — AppInitializer + WebConfig are independent
- T007 + T008 + T009 — Controller + View + Config are independent
- T010 + T011 + T011b — Dockerfile + Compose + .env.example are independent
- T014 + T016 — Maven build + Docker build are independent
- T018 + T019 — Profile + Port verification are independent

### Implementation Strategy (MVP First)

1. **Waves 0-4**: Setup + Foundational → App compiles
2. **Waves 5-7**: US1 (Docker) → **MVP**: `docker compose up` → Hello World in browser
3. **STOP and VALIDATE**: Test US1 independently
4. **Waves 8-10**: US2 + US3 (Maven build + Multi-stage Docker)
5. **Wave 11**: US4 (Configuration externalization)
6. **Waves 12-13**: Unit test + final verification
