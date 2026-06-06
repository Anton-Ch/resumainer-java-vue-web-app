# Research: User Home Page & Resume Workspace

**Date**: 2026-06-06 | **Feature**: 005-user-home-page

## Technology Decisions

### Frontend: Vue 3 + Vite + PrimeVue 4 DataTable

**Decision**: Use PrimeVue DataTable in **lazy mode** (server-driven pagination) to match the backend paginated API. The `:lazy` prop triggers `@page`, `@sort`, and `@filter` callbacks that send parameterized requests to `GET /api/resumes`.

**Rationale**: The backend API is designed for server-side pagination with LIMIT/OFFSET, search, multi-select filters, and sort. Using lazy mode means the DataTable only loads the current page's data from the server and relies on `:totalRecords` for the paginator UI. This avoids loading all resumes on every page change and eliminates client/server filter duplication.

**Pattern**:
- `DataTable` with `lazy`, `:totalRecords="totalRecords"`, `:loading="loading"`, `:first="first"`
- `@page="onPage($event)"` — sends `page`/`size` to backend
- `@sort="onSort($event)"` — sends `sortField`/`sortOrder` to backend (whitelist validated)
- `@filter="onFilter($event)"` — sends language/adaptation/date filters to backend
- Global search via InputText + 300ms debounce, min 3 chars — triggers backend search param
- `paginator` with `rowsPerPageOptions="[10,20,50]"` and `currentPageReportTemplate`
- `@row-click` for opening Resume Details modal
- Skeleton for initial load, DataTable `:loading` overlay for subsequent requests

**Sources**: PrimeVue 4 DataTable documentation (Context7 verified).

---

### Frontend: PrimeVue Dialog + ConfirmDialog + Toast

**Decision**: Use PrimeVue Dialog for Resume Details modal, ConfirmDialog with `useConfirm` composable for delete confirmation, Toast with `useToast` composable for notifications.

**Rationale**: All three components are PrimeVue-native and already available in the project. ConfirmDialog's programmatic API (`confirm.require()`) matches the delete flow: accept → call delete API, reject → close only. Toast provides severity-based notifications (success for copy/delete, error for failures).

**Pattern**:
- `Dialog` with `:dismissableMask="true"`, `:closable="true"`, `@update:visible` for hide handling
- `ConfirmDialog` headless or templated with custom button labels (Cancel/Delete)
- `Toast` positioned globally in App.vue, used from composables

**Sources**: PrimeVue 4 Dialog, ConfirmDialog, Toast documentation (Context7 verified).

---

### Frontend: Vue Router `/app/...` restructure

**Decision**: Move all SPA routes under `/app/...` prefix. Use nested route layout with `createWebHistory`. Update navigation guards for `/app/*` patterns.

**Rationale**: The spec requires root `/` to serve the existing Thymeleaf landing page, and all SPA routes under `/app/...`. Vue Router's `createWebHistory` supports this with Nginx configuration to route `/app/*` requests to the SPA index.html.

**Current routes to migrate**: `/login` → `/app/auth`, `/register` → `/app/auth`, `/home` → `/app/home`, `/admin` → `/app/admin`.
**New routes**: All profile and generate routes under `/app/profile/*` and `/app/generate/*`.

---

### Backend: Spring MVC Paginated Endpoint

**Decision**: Use `@RequestParam` for query parameters (search, language, adaptationLevel, createdDate, sort, page, size). Return JSON with items array and pagination metadata.

**Pattern**:
```java
@GetMapping("/api/resumes")
@ResponseBody
public PagedResponse<SavedResume> listResumes(
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String language,
    @RequestParam(required = false) String adaptationLevel,
    @RequestParam(required = false) String createdDate,
    @RequestParam(defaultValue = "createdAt,desc") String sort,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    // delegate to service layer
}
```

SQL pagination via `LIMIT ? OFFSET ?` with PreparedStatement. Sort handled by whitelist-based column mapping (prevent SQL injection).

**Sources**: Spring Framework 6 @RequestParam documentation (Context7 verified).

---

### Backend: Home Summary Endpoint

**Decision**: Single `GET /api/user/home` endpoint returning profile readiness, checklist status, summary stats, and last resume preview. Service layer computes readiness from DAO calls.

**Pattern**:
- `UserHomeController` → `UserHomeService` → `UserDao` + `ResumeDao`
- Service composes response from multiple DAO queries
- Profile readiness: `UserService` checks contact fields + at least one work experience + at least one education

---

### Routing: Landing Page + SPA Coexistence

**Decision**: Nginx serves `/` → Thymeleap landing page (existing), `/app/*` → Vue SPA index.html with fallback. Backend Tomcat handles `/api/*` for REST and `/` for Thymeleaf.

**Pattern** (Nginx):
```nginx
location / {
    proxy_pass http://backend:8080/;   # Thymeleaf landing page
}
location /app/ {
    autoindex off;
    try_files $uri /app/index.html;    # Vue SPA — no $uri/ directory fallback (SEC-004)
}
location /api/ {
    proxy_pass http://backend:8080/api/;  # REST API
}
```

---

## Edge Cases & State Handling Summary

| State | Approach |
|-------|----------|
| Initial load (both endpoints pending) | Header rendered immediately. Guided+Summary skeleton. Table skeleton. (FR-028) |
| Home summary succeeds, resumes fails | Guided+Summary shows data, Table shows inline error with Retry. (FR-046) |
| Home summary fails, resumes succeeds | Table shows data. Guided+Summary shows inline error with Retry. (FR-046) |
| Both fail | Both blocks show inline error with Retry. (FR-045 + FR-046) |
| No resumes, profile incomplete | Empty state: "No resumes yet" with Complete Profile CTA. (FR-029) |
| No resumes, profile ready | Empty state: "No resumes yet" with Generate Resume CTA. (FR-029) |
| Search returns 0 results | Table shows "No resumes found" with filter change suggestion. (FR-029) |
| Delete confirmed | Modal closes, table refreshes, summary updates, toast: "Resume deleted." (FR-040) |
| Copy link/cover letter | Clipboard API with fallback. Success/error toast. (FR-042) |
| Mobile | Cards stack, DataTable horizontal scroll. (FR-010 via Edge Cases) |
