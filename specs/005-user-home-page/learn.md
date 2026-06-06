# What I Learned: User Home Page & Resume Workspace

**Feature**: Production-ready User Home page with guided block, summary cards, saved resumes DataTable, Resume Details modal, and delete flow
**Generated**: 2026-06-06
**Scope**: Full feature — 41 tasks, 10 phases
**Implementation status**: 41/41 tasks completed

---

## Key Decisions

### 1. PrimeVue DataTable Lazy Mode Instead of Client-Side

**What we did**: Used PrimeVue DataTable in `lazy` mode — every page change, sort, or filter sends a request to the backend API (`GET /api/resumes`). The DataTable only holds the current page's data.

**Why**: The backend API was designed for server-side pagination (LIMIT/OFFSET with search/filter). If we used client-side mode, the DataTable would load ALL resumes into browser memory on every page load, sort/filter client-side, and defeat the purpose of the paginated backend. Lazy mode keeps the frontend lightweight regardless of how many resumes the user has.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Client-side mode (default) | Would load all records into memory — defeats backend pagination, slow with 100+ resumes |
| Virtual scrolling | Over-engineered for MVP — DataTable with 10/20/50 rows per page is standard for this data volume |

**When you'd choose differently**: For tiny datasets (<50 records, no backend pagination), client-side mode is simpler and avoids the extra API calls. For real-time datasets (1000+ records changing frequently), virtual scrolling would be better.

---

### 2. Independent Block Loading Instead of Single API

**What we did**: Split the User Home data into two independent API endpoints: `GET /api/user/home` (profile summary) and `GET /api/resumes` (resume list). Each loads independently with its own loading/error state.

**Why**: If the resumes API fails, the user can still see their profile status and guidance. If the profile API fails, the saved resumes table still works. This is the opposite of a single monolithic endpoint where one failure breaks everything.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Single `/api/user/home` returning everything | One failure blocks the whole page. Violates FR-046 |
| GraphQL | Over-engineered for MVP — two endpoints is simple and clear |

**When you'd choose differently**: If the blocks genuinely share the same data source, a single endpoint is simpler. The tradeoff is simplicity vs resilience.

---

### 3. SPA Under `/app/...` With Landing Page at `/`

**What we did**: Moved all Vue SPA routes under `/app/...` prefix. Root `/` continues to serve the Thymeleaf landing page. Nginx routes `/app/*` to the SPA and proxies `/api/*` to the backend.

**Why**: The landing page (Thymeleaf) needs to be accessible at `/`. The SPA uses Vue Router's `createWebHistory('/app/')` so the base path is baked into the router. Nginx has three location blocks: `/` → backend, `/app/` → SPA, `/api/` → backend.

**Key detail**: The Vite `base: '/app/'` config means built assets reference `/app/assets/...`. The SPA's `index.html` is served from `/usr/share/nginx/html/app/`. The Dockerfile copies the build to this subdirectory.

**When you'd choose differently**: If you didn't need a separate landing page, you could serve the SPA at root (`/`). If you used a different reverse proxy (Caddy, Traefik), the config would differ but the principle is the same.

---

### 4. Custom Thread-Safe Connection Pool (Not HikariCP)

**What we did**: Used the custom `SimpleConnectionPool` from Feature 004. All DAOs inject `DataSource` (which is the pool), not pool-specific classes.

**Why**: Every DAO calls `dataSource.getConnection()` which returns a pooled connection from `SimpleConnectionPool`. This makes the pool a drop-in replacement — if we switch to HikariCP later, only `DataSourceConfig.java` changes.

**Key insight**: The pool `implements javax.sql.DataSource`, so Spring injects it everywhere a `DataSource` is needed. No DAO code knows it's using a custom pool.

**When you'd choose differently**: For production (after Capstone), replace with HikariCP. The architecture makes this a one-file change.

---

### 5. PrimeVue 4 Plugins Need Explicit Registration

**What we did**: Added `app.use(ToastService)`, `app.use(ConfirmationService)`, `app.directive('tooltip', Tooltip)`, and `import 'primeicons/primeicons.css'` in `main.ts`.

**Why**: PrimeVue 4 changed from PrimeVue 3 — services and directives are NOT auto-registered. Without explicit registration, `useToast()` throws "No PrimeVue Toast provided!", `v-tooltip` silently does nothing, and `pi-*` icons render as invisible characters.

**Lesson**: Always check PrimeVue 4 migration docs when starting a new project. The setup checklist is: ToastService, ConfirmationService, Tooltip directive, PrimeIcons CSS.

---

## Concepts to Know

### Smart vs Presentational Components (UserHomePage vs home/* components)

**What it is**: "Smart" components manage state and data flow. "Presentational" components receive props and emit events — they don't know where their data comes from.

**Where we used it**: `UserHomePage.vue` is the smart component — it calls `useUserHome()` and passes data down to `GuidedNextStep`, `SummaryCards`, `SavedResumesTable`, and `ResumeDetailsDialog`. These child components only use `props` and `$emit` — they never fetch data themselves.

**Why it matters**: Separating data logic from presentation makes components testable, reusable, and easier to understand. If the DataTable needs to be used elsewhere, it just needs the same props.

### Composable Pattern for Data Logic

**What it is**: A Vue 3 composable is a function that uses Vue's reactivity system outside a component. It encapsulates state and behavior that multiple components can share.

**Where we used it**: `useUserHome.ts` manages all User Home state (summary, resumes, loading, errors, filters) and exposes methods like `fetchAll()`, `refresh()`, `handleDelete()`. The `UserHomePage.vue` just calls `const { summary, ... } = useUserHome()` and wires it to the template.

**Why it matters**: Without the composable, the state management would be scattered across the component. The composable makes the data layer testable independently and reusable if another page needs similar functionality.

### Lazy Mode: Server-Side DataTable Pattern

**What it is**: "Lazy" means the DataTable fires events when the user pages, sorts, or filters — and expects the PARENT to handle the API call and update the data. The DataTable itself doesn't paginate or filter — it just displays what it receives.

**Where we used it**: `SavedResumesTable.vue` has `:lazy="true"`, `:totalRecords`, `@page`, `@sort` callbacks. The `useUserHome.ts` composable listens to these events, updates query params, and calls the backend API. The DataTable only sees the current page's data.

**Why it matters**: Client-side mode (default) would load ALL data into the browser. With server-side data (PostgreSQL with LIMIT/OFFSET), lazy mode is the only correct choice.

### CSS Grid for Exact 3-Column Layout

**What it is**: CSS Grid with `grid-template-columns: repeat(3, 1fr)` creates exactly three equal columns, regardless of content width. Each child gets exactly 1/3 of the container.

**Where we used it**: The profile subnav (`ProfilePlaceholderPage.vue`) on mobile uses CSS Grid to guarantee 3 items per row — no more, no less. This prevents the "4 items on row 1, 2 on row 2" bug that flexbox allowed.

**Why it matters**: Flexbox with `flex-wrap: wrap` lets items shrink — short words (like "Курсы") would squeeze and allow 4 items per row. CSS Grid forces the exact layout you specify, which is crucial when the visual design demands strict row counts.

---

## Architecture Overview

```
Browser
  │
  ├── / → Nginx → Backend Tomcat → Thymeleaf landing page
  │
  ├── /app/* → Nginx → /usr/share/nginx/html/app/index.html → Vue SPA
  │     │
  │     └── Vue Router (base: '/app/')
  │           ├── /auth → AuthPage (login/register)
  │           ├── /home → UserHomePage (smart component)
  │           │     ├── useUserHome (composable, data logic)
  │           │     ├── GuidedNextStep (presentational)
  │           │     ├── SummaryCards (presentational)
  │           │     ├── SavedResumesTable (presentational, lazy)
  │           │     └── ResumeDetailsDialog (presentational)
  │           ├── /profile/* → ProfilePlaceholderPage (with subnav)
  │           └── /generate/* → GeneratePlaceholderPage (with stepper)
  │
  └── /api/* → Nginx → Backend Tomcat (AuthInterceptor → Controller → Service → Dao → PostgreSQL)
        ├── GET /api/user/home → UserHomeController → UserHomeService
        └── GET/DELETE /api/resumes → ResumeController → ResumeService → ResumeDao
```

Backend uses strict layered architecture: Controller (HTTP) → Service (business logic + validation) → DAO (SQL via PreparedStatement) → PostgreSQL. All resume queries filter by `user_id` (owner isolation) and `deleted_at IS NULL` (soft-delete exclusion). Sort fields validated against a whitelist (SQL injection prevention).

Frontend uses composable pattern for data logic. Components are split between "smart" (UserHomePage, orchestrates everything) and "presentational" (child components, receive props, emit events). i18n is reactive — filter options recompute when the locale changes.

## Glossary

| Term | Meaning |
|------|---------|
| **Lazy mode** | DataTable fires events for page/sort/filter; parent handles API calls and updates data |
| **Composable** | Vue 3 function that uses reactive state outside a component — reusable state logic |
| **Smart/Presentational** | Smart = manages state; Presentational = receives props, emits events |
| **Soft-delete** | Setting `deleted_at` timestamp instead of physically removing a row |
| **Owner filter** | Every SQL query includes `WHERE user_id = ?` — users can only see their own data |
| **Sort whitelist** | Sort field validated against a fixed `Set<String>` — prevents SQL injection in ORDER BY |
