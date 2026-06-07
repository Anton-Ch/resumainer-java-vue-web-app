# System Design: User Profile Page

**Feature**: 6-section User Profile management with backend persistence and frontend UI
**Generated**: 2026-06-07
**Scope**: New persistence infrastructure for profile data

---

## Overview

The Profile feature adds 5 new database tables and 6 new API endpoint groups to the existing ResumAIner stack. The frontend communicates with the backend exclusively via REST JSON over HTTP. The existing Docker Compose topology (backend container + frontend container + PostgreSQL container) remains unchanged — no new infrastructure services are introduced.

## System Design Diagram

```mermaid
flowchart LR
    subgraph Browser["Browser"]
        VUE["Vue 3 SPA<br/>ProfilePage.vue"]
    end

    subgraph Docker["Docker Compose"]
        subgraph Frontend_Container["Frontend (Nginx)"]
            STATIC["Static Vue Build<br/>/app/* routes"]
        end

        subgraph Backend_Container["Backend (Tomcat 10.1)"]
            API["ProfileController<br/>REST /api/profile/*"]
            SVC["ProfileService<br/>+ Transaction Mgmt"]
            DAOS["6 Profile DAOs<br/>+ ContactDetailDao"]
            CACHE_CTRL["Cache-Control<br/>no-store, private"]
        end

        subgraph DB_Container["Database (PostgreSQL 17)"]
            EXISTING["Existing Tables<br/>users, contact_detail,<br/>saved_resumes, ..."]
            NEW["New Tables (V9-V15)<br/>work_experience, education,<br/>project, course_certificate,<br/>additional_profile_info,<br/>work_format, user_work_format"]
            FLYWAY["Flyway<br/>Auto-migration on startup"]
        end
    end

    VUE -->|HTTP GET/POST/PUT/DELETE| API
    API --> SVC
    SVC --> DAOS
    DAOS --> EXISTING
    DAOS --> NEW
    FLYWAY --> NEW
    FLYWAY --> EXISTING
    API --> CACHE_CTRL

    style NEW fill:#c8e6c9
    style EXISTING fill:#bbdefb
    style FLYWAY fill:#fff9c4
```

## Infrastructure Decisions

### PostgreSQL for Profile Data Storage

**What**: All profile data persisted in PostgreSQL 17, extending the existing database with 7 new tables (5 entity tables + 2 lookup/junction tables for work formats).

**Why**: The project already uses PostgreSQL for all persistence (Feature 003 established the schema). Adding profile tables to the same database keeps transaction management simple — the ProfileService can coordinate saves across `additional_profile_info` and `user_work_format` in a single JDBC transaction. A separate database would add cross-database transaction complexity with no benefit since all data belongs to the same application.

**Alternatives considered**:

| Option | Why it wasn't chosen |
|---|---|
| Separate profile database | Adds cross-database transaction complexity. Profile data is tightly coupled to `users` table via foreign keys. |
| MongoDB for profile flexibility | The schema is well-defined (BA data dictionary has complete field specs). No benefit from schema-less storage. Relational queries (e.g., "find users with specific work format") are simpler in PostgreSQL. |

**When you'd choose differently**: If the profile feature evolved into a separate microservice with its own team and release cycle, a separate database would make sense for deployment independence.

### Flyway for Schema Migrations

**What**: Versioned SQL migrations V9-V15 applied automatically on application startup.

**Why**: Consistent with the existing migration pattern (V1-V8 from Features 003 and 005). Flyway ensures all environments (dev, test, prod) have identical schemas and provides rollback documentation through versioned scripts.

**Alternatives considered**: Manual SQL scripts — rejected because they create drift between environments and require manual tracking.

## Data Flow

### Primary Request Path (Save a Profile Section)

```mermaid
sequenceDiagram
    actor User
    participant Vue as Vue SPA
    participant API as ProfileController
    participant Service as ProfileService
    participant DAO as ProfileDAO
    participant DB as PostgreSQL

    User->>Vue: Fill form & click Save
    Vue->>Vue: Zod validation (frontend)
    alt Validation fails
        Vue-->>User: Show inline errors
    else Validation passes
        Vue->>API: PUT /api/profile/{section}<br/>(with session cookie + CSRF token)
        API->>API: Extract userId from session
        API->>Service: save{Section}(userId, data)
        Service->>Service: Validate business rules
        Service->>DAO: save(userId, data)
        DAO->>DAO: Build PreparedStatement<br/>with user_id = ? (SEC-001)
        DAO->>DB: INSERT or UPDATE
        DB-->>DAO: Success
        DAO-->>Service: Saved record
        Service-->>API: Saved DTO
        API-->>Vue: 200 OK + saved data<br/>+ Cache-Control: no-store, private
        Vue-->>User: Success toast, reset dirty state
    end
```

### Courses Paginated Read Path

```mermaid
sequenceDiagram
    actor User
    participant Vue as CoursesSection.vue
    participant API as ProfileController
    participant DAO as CourseCertificateDao
    participant DB as PostgreSQL

    User->>Vue: Navigate to /profile/courses
    Vue->>Vue: DataTable lazy mode: onLoad(event)
    Vue->>API: GET /api/profile/courses<br/>?page=0&size=10&sort=startDate,desc
    API->>DAO: findByUserId(userId, page, size, sort)
    DAO->>DB: SELECT ... WHERE user_id = ?<br/>AND is_deleted = FALSE<br/>ORDER BY start_date DESC<br/>LIMIT ? OFFSET ?
    DB-->>DAO: {rows, totalCount}
    DAO-->>API: CoursePage(content, totalElements, totalPages)
    API-->>Vue: 200 OK + paginated response
    Vue->>User: Render DataTable with rows + paginator
```

## Scaling & Reliability Notes

- **Database scaling**: The profile tables are scoped to a single user — no cross-user queries. Indexing on `user_id` ensures efficient lookups regardless of total user count. Current volume estimates (< 300 courses per user, < 20 others) do not require read replicas or sharding.
- **Connection pool**: The existing custom `SimpleConnectionPool` (Feature 004) handles all database connections. Profile DAOs use connection-accepting overloads for service-level transaction management.
- **Failure mode**: If the database is unreachable, the global exception handler returns a user-friendly error (NFR-004). The frontend shows a descriptive toast. Profile data in localStorage is not used as fallback — the user must retry when the database is available.
- **Cache strategy**: `Cache-Control: no-store, private` prevents caching of personal data. No server-side caching layer (Redis) is needed for MVP — profile data is per-user and accessed infrequently compared to resume generation.
