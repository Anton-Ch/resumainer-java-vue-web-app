# What I Learned: Home Page Saved Resume Details Modal Fix

**Feature**: Fix the Home page saved resume details modal end-to-end — repair modal triggers, adopt canonical DTO fields, implement delete flow, add 410 Gone for soft-deleted public links
**Generated**: 2026-06-25
**Scope**: Full feature
**Implementation status**: 79/79 frontend tests, 944/944 backend tests, Playwright E2E verified

---

## Key Decisions

### 1. Vue `computed` Get/Set Bridge Instead of `ref(props.visible)`

**What we did**: Replaced `ref(props.visible)` with a `computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })` bridge for the modal's v-model binding.

**Why**: The old code copied the prop value once on component mount and never synchronized again. When the parent set `modalVisible = true`, the dialog's local ref stayed `false`. PrimeVue Dialog needs a writable reactive value for `v-model:visible`. A computed get/set bridge turns a read-only prop into a two-way binding by proxying reads to the prop and writes to the emit.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| `watch` prop → update local ref | More verbose (need watcher + ref), easier to desynchronize on rapid toggles |
| `defineModel` (Vue 3.4+) | Cleaner API but not used elsewhere in this codebase; would introduce inconsistency |

**When you'd choose differently**: For a new project or a component that owns its own state, `defineModel` is cleaner. The computed bridge is the right fit when you need backward compatibility with Vue 3.3 and consistency with existing patterns.

---

### 2. Row Click Instead of Details Column/Button

**What we did**: The saved-resumes table opens the modal on row click via PrimeVue DataTable's `@row-click` event. No separate "Details" column or button was added.

**Why**: The spec explicitly required this (FR-002, FR-003). Adding a details column would widen an already long table (6 columns) and create unnecessary visual noise. Row click is a standard table interaction pattern that users expect — clicking a row to see details is intuitive.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Details button per row | Adds column width, duplicates row-click behavior, violates spec |
| Double-click to open | Non-standard, users won't discover it |

**When you'd choose differently**: If the table had fewer columns (2-3) or if each row had multiple actions (edit, delete, share), a dedicated actions column would be appropriate. Row click works best when each row represents one entity and the primary action is "view details."

---

### 3. 410 Gone Instead of 404 for Soft-Deleted Resumes

**What we did**: The public route returns `410 Gone` for soft-deleted resumes, with a uniform artificial delay matching the `404` response time.

**Why**: This was a deliberate product decision (User Story 6). A recruiter who opens a previously valid shared link should see a clear "resume removed" message (Thymeleaf page with explanation) rather than a generic "not found" page. The 410 status code semantically means "this resource existed but is gone" — which is exactly what happened.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Return 404 for everything | Hides the deletion from recruiters (bad UX). Also leaks less info but at the cost of confusing users |
| Return 200 with a JSON body | Would require frontend rendering for a non-Vue page; 410 is the correct HTTP semantic |
| 410 with no delay | Would let attackers distinguish valid-but-deleted from never-existed by response time (CWE-208) |

**When you'd choose differently**: If the threat model prioritizes hiding all existence information over recruiter UX (e.g., a document-sharing platform where links are highly sensitive), return 404 for everything. The uniform delay is mandatory either way.

---

### 4. Backend-Owned Canonical URLs Instead of Frontend-Constructed URLs

**What we did**: The backend `HomeSavedResumeDto` carries full absolute `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`. The frontend consumes these URLs directly and never constructs them.

**Why**: Feature 008 (PDF generation) established this pattern with `SavedResumeExportDto`. If we let the frontend construct URLs by concatenating strings, a backend route change would break the frontend without any type error. By having the backend provide the complete URLs, we create a single source of truth for URL construction. The backend also has access to request context (`APP_PUBLIC_BASE_URL`, forwarded headers) that the frontend doesn't.

**When you'd choose differently**: For a simple app where the frontend and backend are always deployed together and routes rarely change, frontend URL construction saves a few bytes per response. But for any app with a reverse proxy, multiple environments, or separate deployment cycles, backend-owned URLs are more maintainable.

---

### 5. CSRF Cookie-to-Header Pattern (and Why Raw `fetch()` Broke)

**What we did**: Changed `resumeService.ts` from raw `fetch()` to `apiRequest()` from `httpClient.ts`. The httpClient reads the `XSRF-TOKEN` cookie (set by the backend `CsrfFilter`) and sends it as the `X-CSRF-Token` header on unsafe methods (POST, PUT, DELETE).

**Why**: The Playwright E2E test revealed that DELETE requests returned 403 Forbidden. The CSRF filter implements the OWASP cookie-to-header pattern: the server sets a non-HTTP-only cookie with the CSRF token, and JavaScript must read it and send it back as a request header. The old `resumeService.ts` used raw `fetch()` with `credentials: 'include'` (which sends the session cookie) but never sent the CSRF header.

**When you'd choose differently**: If your API is stateless (JWT-based auth, no session cookies), you don't need CSRF protection at all — CSRF is a cookie-specific concern. If you use a framework like Spring Boot, CSRF protection is built in. But for plain Spring MVC with custom filters (like this project), you own the implementation and must ensure every unsafe request includes the token.

---

### 6. Loading State Owned by Parent Composable, Not Dialog Child

**What we did**: The `deleteLoading` ref lives in `useUserHome.ts` composable (parent), is passed as a prop to `ResumeDetailsDialog`, and the parent resets it in a `finally` block.

**Why**: The dialog emits a `delete` event and doesn't know whether the parent's API call succeeded or failed. If the dialog owned `deleteLoading`, it would stay `true` forever on API failure, preventing retry. By moving ownership to the composable, the parent sets `deleteLoading = true` before the API call and always resets it in `finally` — on both success and failure. The dialog just renders the prop.

**When you'd choose differently**: If the dialog component had its own self-contained API call (no parent involvement), local loading state would be simpler. But whenever a child component delegates an action to a parent via emits, the parent should own any loading/disabled state that needs to survive past the event emission.

---

## Concepts to Know

### Writable Computed (Vue 3 Composition API)

**What it is**: A `computed` that has both a `get` and a `set` function. Normally computed values are read-only — they derive from other reactive state. A writable computed lets you intercept both reads and writes, making it act like a reactive proxy.

**Where we used it**: `ResumeDetailsDialog.vue` — the `visible` computed bridges the prop and emit for `v-model:visible`.

**Why it matters**: Custom components that need `v-model` support must provide both a prop binding and an emit. A writable computed is the cleanest way to do this in the Composition API. Without it, you'd need a `watch` + manual emit, which is more code and easier to break.

---

### OWASP Cookie-to-Header CSRF Pattern

**What it is**: A CSRF defense where the server sets a non-HTTP-only cookie with a random token. JavaScript reads the cookie and sends the token value as a custom request header (typically `X-CSRF-Token`). The server validates that the header matches the cookie. An attacker's site cannot read the cookie (same-origin policy) and cannot forge the header, so the request is rejected.

**Where we used it**: `CsrfFilter.java` (backend) sets `XSRF-TOKEN` cookie. `httpClient.ts` (frontend) reads it and sends `X-CSRF-Token` header.

**Why it matters**: Without CSRF protection, an attacker could trick a logged-in user into performing state-changing actions (like deleting resumes) by embedding a form or script on a different site. The cookie-to-header pattern is the standard defense for SPA + backend API architectures.

---

### Canonical DTO Pattern

**What it is**: A Data Transfer Object that carries fully-resolved URLs and computed fields rather than raw IDs or paths that the consumer would need to resolve. The backend is the single source of truth for URL construction.

**Where we used it**: `HomeSavedResumeDto` carries `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl` — all pre-computed absolute URLs.

**Why it matters**: When multiple consumers (web frontend, mobile app, API clients) need to construct the same URLs, having each consumer do it independently creates duplication and the risk of drift. If the route changes, every consumer breaks. With canonical DTOs, only the backend mapper needs to change. This pattern also centralizes environment-aware logic (like `APP_PUBLIC_BASE_URL` resolution) in one place.

---

### Soft Delete with Dual Flags

**What it is**: When deleting a record, instead of removing it from the database, set `is_deleted = TRUE` AND `deleted_at = NOW()`. Two flags protect against inconsistency: `deleted_at IS NULL` queries catch the deletion, and `is_deleted = FALSE` queries catch it too.

**Where we used it**: `ResumeDao.softDelete()` sets both flags. List queries filter by `deleted_at IS NULL`, public route checks `is_deleted`.

**Why it matters**: Different parts of the system may use different filter patterns. If list queries check `deleted_at` but the public route checks `is_deleted`, deleting via a query that only sets one flag leaves the other flag inconsistent — the record is "partially deleted." Setting both flags guarantees that any deletion filter catches the record.

---

## Architecture Overview

Feature 009 is a layered repair across backend and frontend with clear separation:

```
Backend (Java/Spring MVC)
  ├── PublicResumeController   → public route (410/404), rate limit, uniform delay
  ├── ResumeController         → paginated list + soft-delete with canonical DTO
  ├── UserHomeController       → summary.lastResume with canonical DTO
  ├── HomeSavedResumeMapper    → transforms entity to DTO, builds canonical URLs
  ├── PublicUrlService         → URL resolution (env → forwarded headers → fallback)
  └── ResumeDao               → dual-flag soft delete, JOIN with users for DTO fields

Frontend (Vue 3/PrimeVue)
  ├── ResumeDetailsDialog      → canonical fields, v-model bridge, cover letter preview
  ├── SavedResumesTable        → row click, pointer cursor, canonical field display
  ├── SummaryCards             → conditional clickable latest-resume card
  ├── UserHomePage             → wires composable to components, passes deleteLoading
  ├── useUserHome (composable) → owns modal state, deleteLoading, API calls
  ├── resumeService            → CSRF-safe API calls via httpClient
  └── userHomeService          → type definitions matching backend DTO
```

The key principle: **backend prepares data, frontend renders it**. No URL construction in the frontend. No i18n strings in the backend. Loading state follows the API call, not the UI component.

---

## Glossary

| Term | Meaning |
|------|---------|
| CSRF (Cross-Site Request Forgery) | An attack where a malicious site tricks a user's browser into making an unauthorized request to another site where the user is authenticated. The cookie-to-header pattern prevents this. |
| Canonical DTO | A data transfer object that carries fully-resolved values (absolute URLs, computed fields) rather than raw data that the consumer would need to process. |
| Uniform Delay | An artificial delay applied to all error responses (404, 410) so that an attacker cannot distinguish valid vs invalid inputs by measuring response time. |
| Soft Delete | Marking a record as deleted without physically removing it from the database. Allows recovery and maintains referential integrity. |
| Thymeleaf | A server-side Java template engine. Used here for backend-rendered error pages (404, 410) that are served outside the Vue SPA. |
