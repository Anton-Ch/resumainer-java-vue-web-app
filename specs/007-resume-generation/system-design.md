# System Design: Resume Generation

**Feature**: AI-powered resume generation with bilingual support, editable review, and HTML/PDF export
**Generated**: 2026-06-12
**Scope**: New infrastructure for feature — file storage, OpenRouter integration, PDF conversion

---

## Overview

The system extends the existing ResumAIner Docker Compose deployment (Tomcat + Nginx + PostgreSQL) with three new infrastructure concerns: server-side file storage for generated HTML and PDF artifacts, HTTP-based integration with OpenRouter API for AI generation, and an internal PDF conversion service. All user data flows through authenticated backend endpoints. Public access is limited to a single read-only PDF route.

## System Design Diagram

```mermaid
flowchart TD
    subgraph Client["Client (Browser)"]
        User["Logged-in User"]
        Recruiter["Recruiter (no auth)"]
    end

    subgraph Docker["Docker Compose (existing)"]
        subgraph Network["resumainer-network"]
            Nginx["Nginx<br/>Reverse Proxy"]
            Tomcat["Tomcat<br/>Java 21 + Spring MVC"]
            Vue["Vue SPA<br/>(served by Nginx)"]
            PG[("PostgreSQL 17")]
        end
    end

    subgraph NewInfra["New Infrastructure (this feature)"]
        FS[("File System<br/>generated_results/{username}/{public_code}/")]
        PDF["PdfGenerationService<br/>(Java, server-side)"]
    end

    ExternalAI["OpenRouter API<br/>(DeepSeek V4 Flash)"]

    %% Request flows
    User --> Nginx
    Recruiter --> Nginx

    %% Nginx routing
    Nginx -->|"/app/*"| Vue
    Nginx -->|"/api/*"| Tomcat
    Nginx -->|"/candidate/*"| Tomcat
    Nginx -->|"/"| Tomcat

    %% Backend flows
    Tomcat --> PG
    Tomcat --> FS
    Tomcat --> PDF
    Tomcat --> ExternalAI

    %% File flows
    PDF -.->|"reads filled HTML"| FS
    PDF -.->|"writes PDF"| FS

    %% Public link flow
    Recruiter -->|"GET /candidate/{publicCode}"| Tomcat
    Tomcat -->|"streams PDF"| Recruiter

    %% Legend
    subgraph Legend["Legend"]
        Existing["Existing components"]
        New["New components (this feature)"]
        External["External services"]
    end

    style NewInfra fill:#e1f5fe,stroke:#0288d1
    style PDF fill:#e1f5fe,stroke:#0288d1
    style FS fill:#e1f5fe,stroke:#0288d1
    style ExternalAI fill:#fff3e0,stroke:#f57c00
    style Docker fill:#f5f5f5,stroke:#9e9e9e
    style Client fill:#f3e5f5,stroke:#7b1fa2
```

## Infrastructure Decisions

### File System Storage for Generated Artifacts

**What**: Server-side filesystem storage at `generated_results/{username}/{public_code}/` for filled HTML and PDF files.

**Why**: The spec requires HTML to be saved before PDF conversion (DEC-073). A deterministic file path structure makes artifacts discoverable without a database scan. Filesystem storage is the simplest approach for MVP — no cloud storage, CDN, or object store needed. The path includes the username for logical organization and public_code for unique file naming.

**Alternatives considered**:

| Option | Why it wasn't chosen |
|--------|---------------------|
| Database BLOB storage | Makes backups larger, harder to stream files, and doesn't simplify export. Filesystem is more natural for file delivery |
| Cloud object storage (S3) | Adds infrastructure complexity, dependency, and cost with no MVP benefit. Can be added post-MVP without changing the business logic |

**When you'd choose differently**: If the system needs to scale horizontally across multiple Tomcat instances, shared filesystem access becomes a problem. At that point, migrate to S3-compatible object storage with the same path structure.

---

### OpenRouter API Integration

**What**: Synchronous HTTP calls from the Java backend to OpenRouter API (DeepSeek model route) for AI-powered generation.

**Why**: The OpenRouter API provides access to multiple LLMs through a single unified endpoint. The `AiClient` interface abstracts the HTTP call behind a Java interface, so the provider can be swapped (to OpenAI, Anthropic, or a self-hosted model) without changing the generation orchestration. Synchronous calls keep the MVP simple — no queue, no async job system needed. The timeout-sensitive nature of LLM calls is handled at the HTTP client level.

**Alternatives considered**:

| Option | Why it wasn't chosen |
|--------|---------------------|
| Self-hosted LLM | Requires GPU infrastructure, significant operational cost, and doesn't fit the target deployment (VPS with limited resources) |
| Async queue-based generation | Adds RabbitMQ/Redis infrastructure for MVP. LLM calls are already slow (10-30s), so the user waits anyway. Queue adds complexity without UX benefit at MVP scale |

**When you'd choose differently**: If generation requests grow to 100+ per minute, an async queue with worker pool would prevent HTTP connection exhaustion and provide better load shedding.

---

### PdfGenerationService

**What**: A separate Java service class that converts a saved HTML file to PDF using a server-side library.

**Why**: The plan decouples PDF conversion from HTML rendering so that one can succeed without the other — if PDF conversion fails, the filled HTML is already saved and can be manually converted later or re-processed. A separate service class (not a separate process for MVP) keeps the architecture simple while maintaining clear separation of concerns.

**Alternatives considered**:

| Option | Why it wasn't chosen |
|--------|---------------------|
| Client-side PDF generation in Vue | Rejected by architecture decision (DEC-034, DEC-017). The backend must control final output for consistent layout, ATS-friendly formatting, and correct Cyrillic rendering |
| wkhtmltopdf as external process | More complex deployment (requires system dependency in Docker). A pure Java library is simpler to deploy and maintain |

**When you'd choose differently**: PDF library choice will be evaluated in a future feature. If the chosen library lacks features (e.g., CSS3 support, complex layouts), consider wkhtmltopdf or a dedicated microservice.

---

## Data Flow

```mermaid
sequenceDiagram
    actor User as Logged-in User
    participant Frontend as Vue SPA
    participant Backend as Tomcat / Spring MVC
    participant DB as PostgreSQL
    participant AI as OpenRouter API
    participant FS as File System

    Note over User,FS: Generation Flow (synchronous)

    User->>Frontend: Enter vacancy + settings
    Frontend->>Backend: POST /api/generate/requests (incl. ai_model_id)
    Backend->>DB: Validate ai_model_id + insert request
    DB-->>Backend: request_id (status=pending)
    Backend-->>Frontend: { request_id }

    User->>Frontend: Click "Generate"
    Frontend->>Backend: POST /api/generate/requests/{id}/generate
    
    Backend->>DB: Load prompt config + user profile
    Backend->>Backend: Assemble prompts (Builder)
    Backend->>AI: POST OpenRouter API
    AI-->>Backend: Structured JSON response

    alt Valid JSON
        Backend->>Backend: Parse + validate JSON
        Backend->>DB: Insert response rows (transaction)
        DB-->>Backend: OK
        Backend->>DB: Update request status=completed
        Backend-->>Frontend: { status: "completed" }
        Frontend->>Frontend: Navigate to /generate/review
    else Invalid / Error
        Backend->>DB: Update request status=failed
        Backend-->>Frontend: { status: "failed", message: "..." }
        Frontend->>Frontend: Navigate to /generate/error
    end

    Note over User,FS: Review + Edit Flow

    User->>Frontend: Edit generated fields
    Frontend->>Backend: PUT /api/generate/requests/{id}/review
    Backend->>DB: Update response rows
    DB-->>Backend: OK
    Backend-->>Frontend: { success: true }

    Note over User,FS: Finalize Flow

    User->>Frontend: Select adaptation level + Finalize
    Frontend->>Backend: POST /api/generate/requests/{id}/finalize
    
    Backend->>DB: Load response data + templates
    Backend->>Backend: Render filled HTML
    Backend->>FS: Save filled HTML file
    Backend->>Backend: Convert HTML → PDF
    Backend->>FS: Save PDF file
    Backend->>DB: Insert saved_resume rows (transaction)
    DB-->>Backend: OK
    Backend-->>Frontend: { export_url, public_links }
    
    Frontend->>Frontend: Navigate to /generate/export

    Note over User,FS: Export + Public Access

    User->>Frontend: Click "Download PDF"
    Frontend->>Backend: GET /api/resumes/{id}/pdf
    Backend->>DB: Verify owner
    Backend->>FS: Stream PDF file
    FS-->>Backend: PDF bytes
    Backend-->>User: application/pdf

    Recruiter->>Backend: GET /candidate/{publicCode}
    Backend->>DB: Lookup by public_code (active only)
    alt Resume active
        Backend->>FS: Stream PDF file
        FS-->>Backend: PDF bytes
        Backend-->>Recruiter: application/pdf (200)
    else Resume deleted
        Backend-->>Recruiter: 410 Gone
    end
```

## Scaling & Reliability Notes

- **Synchronous generation**: For MVP, generation is synchronous (user waits). At higher load, this should become async with a polling endpoint.
- **File storage**: Single-server filesystem works for MVP. Horizontal scaling requires shared/NFS storage or migration to object storage.
- **OpenRouter availability**: The spec mandates a mock AI client for testing. In production, timeouts and 5xx errors from OpenRouter should show the temporary error screen with retry/change settings options.
- **Backup strategy**: Generated HTML/PDF files should be included in regular server backups. The database is the source of truth for metadata; files can be re-generated from stored response data if lost.
- **Public PDF route**: The 410 Gone response for deleted resumes prevents broken links without exposing any user data.
