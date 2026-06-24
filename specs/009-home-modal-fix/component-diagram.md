# Component Diagram: Home Page Saved Resume Details Modal Fix

**Feature**: Repair the Home page saved resume details modal — row-click trigger, canonical DTO fields, delete flow, public route 410, and Thymeleaf error page.
**Generated**: 2026-06-24
**Scope**: Full feature

---

## Overview

This diagram shows all components modified by Feature 009, their data flows, and the boundaries between frontend (Vue SPA) and backend (Spring MVC). The feature touches two backend controllers, creates one new DTO and one new service, updates three Vue components, and modifies the public route behavior.

## Component Diagram

```mermaid
flowchart TD
    subgraph Frontend["Frontend — Vue 3 SPA"]
        HomePage["UserHomePage.vue<br/>Parent component<br/>loads summary + table"]
        SummaryCards["SummaryCards.vue<br/>Latest-resume card<br/>conditional clickable"]
        Table["SavedResumesTable.vue<br/>Row click trigger<br/>cursor/hover styling"]
        Modal["ResumeDetailsDialog.vue<br/>v-model bridge<br/>canonical fields<br/>actions + delete"]
        HomeService["userHomeService.ts<br/>DTO types + fetch"]
        ResumeService["resumeService.ts<br/>delete API + error handling"]
        I18n["en.json / ru.json<br/>All new UI strings"]
    end

    subgraph Backend["Backend — Spring MVC"]
        direction TB
        subgraph Controllers["Controllers"]
            UserHomeCtrl["UserHomeController<br/>GET /api/user/home<br/>summary.lastResume→HomeDto"]
            ResumeCtrl["ResumeController<br/>GET /api/resumes<br/>list→HomeDto<br/>DELETE /api/resumes/{id}"]
            PublicCtrl["PublicResumeController<br/>GET /{username}/{publicCode}<br/>200/410/404 + delay"]
            GenResumeCtrl["GenerateResumeController<br/>Canonical export endpoints<br/>pdf/html download"]
        end
        subgraph Services["Services"]
            UserHomeSvc["UserHomeService<br/>Compose summary<br/>+ lastResume mapping"]
            ResumeSvc["ResumeService<br/>Paginated list + delete<br/>+ HomeDto mapping"]
            PublicUrlSvc["PublicUrlService NEW<br/>Build publicUrlLink<br/>APP_PUBLIC_BASE_URL<br/>forwarded headers fallback"]
        end
        subgraph Models["Models / DTOs"]
            HomeDto["HomeSavedResumeDto NEW<br/>Canonical fields:<br/>publicUrlLink, pdfOpenUrl<br/>pdfDownloadUrl, htmlDownloadUrl<br/>pdfAvailable, coverLetter"]
            LookupResult["PublicResumeLookupResult NEW<br/>ACTIVE / DELETED / NOT_FOUND<br/>MISSING_FILE / UNSAFE_PATH"]
        end
        subgraph DAOs["DAOs"]
            ResumeDao["ResumeDao<br/>Select + soft-delete<br/>is_deleted + deleted_at"]
        end
        subgraph Templates["Thymeleaf"]
            Err410["410.html NEW<br/>Deleted resume page<br/>static/i18n content only"]
            Err404["404.html<br/>Generic not-found page"]
        end
    end

    subgraph Config["Configuration"]
        EnvExample[".env.example<br/>APP_PUBLIC_BASE_URL"]
    end

    subgraph External["External"]
        DB[("PostgreSQL<br/>saved_resumes")]
        Browser["Browser<br/>PDF inline / new tab"]
    end

    %% Frontend internal flows
    HomePage --> SummaryCards
    HomePage --> Table
    HomePage --> Modal
    HomePage -->|fetchSummary| HomeService
    HomePage -->|loadResumes| ResumeService
    SummaryCards -->|openLastResume| Modal
    Table -->|row-click resume| Modal
    Modal -->|DELETE confirm| ResumeService

    %% Frontend → Backend
    HomeService -->|GET /api/user/home| UserHomeCtrl
    ResumeService -->|GET /api/resumes| ResumeCtrl
    ResumeService -->|DELETE /api/resumes/{id}| ResumeCtrl
    Modal -->|pdfOpenUrl| GenResumeCtrl
    Modal -->|pdfDownloadUrl| GenResumeCtrl
    Modal -->|htmlDownloadUrl| GenResumeCtrl

    %% Backend internal
    UserHomeCtrl --> UserHomeSvc
    ResumeCtrl --> ResumeSvc
    ResumeSvc --> ResumeDao
    UserHomeSvc --> ResumeDao
    PublicCtrl --> PublicUrlSvc
    PublicCtrl --> LookupResult
    PublicCtrl --> Err410
    PublicCtrl --> Err404
    ResumeSvc --> HomeDto
    UserHomeSvc --> HomeDto
    PublicUrlSvc -->|reads| EnvExample

    %% Backend → DB
    ResumeDao --> DB

    %% External
    PublicCtrl -->|200 OK| Browser
    GenResumeCtrl -->|PDF inline/download| Browser
    Browser -->|public URL| PublicCtrl

    %% Styles
    classDef new fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    classDef modified fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef external fill:#f3e5f5,stroke:#7b1fa2,stroke-width:1px

    class PublicUrlSvc,HomeDto,LookupResult,Err410 new
    class Modal,Table,SummaryCards,HomeService,ResumeService,UserHomeCtrl,ResumeCtrl,PublicCtrl,UserHomeSvc,ResumeSvc modified
    class DB,Browser external
```

---

## Component Breakdown

### Frontend Components

#### UserHomePage.vue

**Role**: Parent orchestration component that loads summary and saved-resumes table, then passes data to child components.

**Why this exists as a separate component**: Already exists. No structural changes needed — only data wiring verification.

**Key interactions**:
- → `userHomeService.ts`: fetches home summary (`GET /api/user/home`)
- → `resumeService.ts`: fetches paginated list (`GET /api/resumes`)
- → `ResumeDetailsDialog`: passes `selectedResume` + `modalVisible`

---

#### ResumeDetailsDialog.vue

**Role**: Modal dialog displaying saved resume details, actions, and delete flow.

**Why this exists as a separate component**: Already exists — this is the primary component being fixed. The visibility binding bug (`ref(props.visible)` instead of computed bridge) is the original reason for this feature.

**Key interactions**:
- ← `UserHomePage`: receives `visible` and `resume` props
- → `emit('update:visible')`: closes modal
- → `emit('delete', id)`: triggers delete flow
- → `GenerateResumeController`: opens PDF/HTML via canonical URLs from DTO

---

#### SavedResumesTable.vue

**Role**: Paginated table of saved resumes with row-click trigger for modal.

**Why this exists as a separate component**: Already exists. The change adds row-click handler and cursor/hover styling. No "Details" column or button is added per FR-002.

**Key interactions**:
- → `emit('openResume', resume)`: triggers modal open for clicked row

---

#### SummaryCards.vue

**Role**: Summary card showing profile readiness and latest resume card.

**Why this exists as a separate component**: Already exists. The change makes the latest-resume card clickable only when `summary.lastResume` exists.

**Key interactions**:
- → `emit('openLastResume', lastResume)`: triggers modal open for latest resume

---

#### userHomeService.ts

**Role**: TypeScript types and API fetch for home summary.

**Why this exists as a separate component**: Already exists. The DTO fields change from old `publicUrl`/`pdfUrl` to canonical `publicUrlLink`/`pdfOpenUrl`/`pdfDownloadUrl`/etc.

**Key interactions**:
- → `UserHomeController.GET /api/user/home`

---

#### resumeService.ts

**Role**: Resume CRUD — paginated list and soft-delete.

**Why this exists as a separate component**: Already exists. The delete error handling changes: on failure, frontend shows a generic i18n toast and keeps modal open (per SEC-003).

**Key interactions**:
- → `ResumeController.GET /api/resumes`
- → `ResumeController.DELETE /api/resumes/{id}`

---

### Backend Components

#### UserHomeController

**Role**: Serves home summary including profile readiness and last resume preview.

**Why modified**: `summary.lastResume` must now use the same `HomeSavedResumeDto` mapper as the paginated list (FR-006). The backend structure of `/api/user/home` response is updated to include `summary.lastResume` as a `HomeSavedResumeDto`.

**Key interactions**:
- → `UserHomeService`: delegates summary composition + DTO mapping

---

#### ResumeController

**Role**: Paginated saved-resume list and soft-delete.

**Why modified**: The paginated list response items switch from raw `SavedResume` entity mapping to `HomeSavedResumeDto`. Delete must set both `is_deleted` and `deleted_at`.

**Key interactions**:
- → `ResumeService`: delegates list query + DTO mapping + delete operation

---

#### PublicResumeController

**Role**: Public unauthenticated route serving PDF inline for active resumes.

**Why modified**: Adds `410 Gone` for soft-deleted known public codes. Implements uniform artificial delay for both 404 and 410. Uses `PublicResumeLookupResult` to distinguish DELETED, NOT_FOUND, MISSING_FILE, UNSAFE_PATH states.

**Key interactions**:
- → `PublicResumeLookupResult`: status model for response routing
- → `410.html`: Thymeleaf page for deleted resumes
- → `404.html`: existing generic not-found page

---

#### GenerateResumeController

**Role**: Canonical authenticated export endpoints for PDF and HTML.

**Why referenced**: This is the source of truth for PDF/HTML download URLs in the new `HomeSavedResumeDto` (per Canonical Endpoint Decision). `ResumeDownloadController` legacy routes must not be used.

**Key interactions**:
- ← Frontend modal: consumes `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl` from DTO

---

#### UserHomeService

**Role**: Composes home summary from multiple DAOs.

**Why modified**: `getHomeSummary()` must map `lastResume` through the same `HomeSavedResumeDto` mapper used by the paginated list.

---

#### ResumeService

**Role**: Business logic for resume listing and soft-delete.

**Why modified**: List method maps to `HomeSavedResumeDto`. Delete method ensures both `is_deleted` and `deleted_at` are set consistently.

---

#### PublicUrlService (NEW)

**Role**: Builds full absolute `publicUrlLink` for Home DTO.

**Why this exists as a separate component**: URL construction has enough subtlety (config resolution order: env var → forwarded headers → request origin, trailing slash normalization, warning logging) to justify a focused service with dedicated tests, following the single-responsibility principle. Embedding this in a controller would make the URL logic untestable.

**Key interactions**:
- → Reads `APP_PUBLIC_BASE_URL` from environment/config
- → Falls back to forwarded headers or request origin
- ← Consumed by `ResumeService` and `UserHomeService` when building `HomeSavedResumeDto`

**Key decisions** (from plan.md):
1. Use `APP_PUBLIC_BASE_URL` when non-blank
2. Support existing property binding if project already has one
3. Fallback: `X-Forwarded-Proto` + `X-Forwarded-Host` headers
4. Last resort: request scheme + host + port for local dev
5. Log warning when fallback origin is used

---

#### HomeSavedResumeDto (NEW)

**Role**: Canonical DTO for saved resume data on Home page, shared between list and summary.

**Why this exists as a separate component**: A dedicated DTO prevents coupling between the internal `SavedResume` entity (which may contain raw fields like `pdf_file_path`) and the public-facing API. It also consolidates the canonical URL fields that the frontend modal consumes.

**Key fields**: `id`, `resumeTitle`, `vacancyTitle`, `companyName`, `languageCode`, `languageName`, `adaptationLevel`, `createdAt`, `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfStatus`, `pdfMessage`, `coverLetter`

---

#### PublicResumeLookupResult (NEW)

**Role**: Internal result model that replaces a `null`-only return value for public route lookups with a status-aware enum.

**Why this exists as a separate component**: Previously, the public route's DAO/service returned `null` or a PDF path, making it impossible to distinguish "not found" from "deleted" from "missing file". A status-aware result (ACTIVE, DELETED, NOT_FOUND, MISSING_FILE, UNSAFE_PATH) enables the controller to return the correct HTTP status for each case without leaking internal details.

---

#### 410.html (NEW if missing)

**Role**: Thymeleaf error page for soft-deleted public resume links.

**Why this exists as a separate page**: The spec requires `410 Gone` with a clear user-facing message. Following the existing `404.html` / `500.html` error page pattern keeps the public route's error handling consistent with the rest of the application. This is a pure Thymeleaf template — no Vue SPA involvement.

**Security constraint**: Must contain only static branded text and i18n strings. No dynamic resume data, username, public code, deletion date, IDs, file paths, or company/vacancy data.

---

## Design Reasoning

### Why this structure?

The component structure follows the project's existing layered architecture (controller → service → DAO → DB) with a thin Vue SPA on top. This feature is a repair, so the structure intentionally mirrors what already exists.

Three new components are introduced:
1. **`HomeSavedResumeDto`** — isolates the frontend-facing resume shape from the internal entity, preventing raw filesystem paths from leaking into API responses (Constitution V, SEC-002).
2. **`PublicUrlService`** — concentrates URL resolution logic (env var → forwarded headers → request origin) in one testable place rather than scattering it across controllers (Constitution I).
3. **`PublicResumeLookupResult`** — enables the public controller to return the correct HTTP status (200, 410, 404) without collapsing all error states into a single null return (FR-019, SEC-001).

### Alternatives considered

| Structure | Why it wasn't chosen |
|-----------|---------------------|
| Add canonical fields directly to `SavedResume` entity | Would expose internal entity fields to the API; violates separation of concerns and could accidentally leak `pdf_file_path` or storage paths |
| Frontend constructs URLs from IDs (D31 anti-pattern) | Would bypass the canonical export endpoint contract and create fragile coupling; Feature 008 explicitly replaced this pattern |
| Merge `ResumeDownloadController` as canonical source | Plan explicitly designates it as deprecated legacy; new URLs must use `GenerateResumeController` |
| Single service for both list and summary mapping | Would couple two different query patterns; `UserHomeService` and `ResumeService` have different DB query needs (pagination vs single latest) |
| Embed URL logic in `ResumeService` | Would make URL resolution untestable independently and violate SRP when the feature adds forwarded-header fallback and config resolution |

### When you'd restructure

If the feature grows to support multiple public URL domains (e.g., per-tenant custom domains), `PublicUrlService` would need to become tenant-aware and potentially store domain mapping in the database. If the application adds a message queue or event bus, the delete flow would shift from synchronous soft-delete to an event-driven delete handler. For the current scope, the synchronous structure is sufficient.

---

## Summary

| Element | Type | Status |
|---------|------|--------|
| UserHomePage.vue | Vue component | Verify (minor/no change) |
| SummaryCards.vue | Vue component | Modified (conditional clickable) |
| SavedResumesTable.vue | Vue component | Modified (row click + styling) |
| ResumeDetailsDialog.vue | Vue component | **Fixed** (v-model bridge + actions) |
| userHomeService.ts | Frontend service | Modified (canonical fields) |
| resumeService.ts | Frontend service | Modified (delete error handling) |
| UserHomeController | Backend controller | Modified (DTO mapping) |
| ResumeController | Backend controller | Modified (DTO mapping + delete) |
| PublicResumeController | Backend controller | Modified (410 + delay) |
| GenerateResumeController | Backend controller | Referenced (canonical URLs) |
| UserHomeService | Backend service | Modified (DTO mapping) |
| ResumeService | Backend service | Modified (DTO mapping + delete consistency) |
| **PublicUrlService** | Backend service | **NEW** |
| **HomeSavedResumeDto** | Backend DTO | **NEW** |
| **PublicResumeLookupResult** | Backend model | **NEW** |
| ResumeDao | Backend DAO | Modified (verify delete_at) |
| **410.html** | Thymeleaf template | **NEW if missing** |
| .env.example | Config | Modified (APP_PUBLIC_BASE_URL) |
