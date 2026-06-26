# What I Learned: Admin Console Users and Resumes

**Feature**: Admin Console for ResumAIner — dashboard, resume moderation, user management, access control, and soft-delete
**Generated**: 2026-06-26
**Scope**: Full feature (Phases 0-13 plus QA hardening and security polish)
**Implementation status**: ~230/232 tasks completed across 15 commits

---

## Key Decisions

### 1. Layered Monolith Instead of Microservices

**What we did**: Added admin capabilities inside the existing Spring MVC monolith — new controller, service, and DAO in `backend/src/main/java/com/resumainer/`.

**Why**: The admin feature is a cross-cutting concern that reads and mutates the same database as the user-facing app. Splitting admin into a separate service would add network latency, duplicate authentication, require a second deployment, and introduce data consistency problems — all for a feature used by only 1-2 people.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Separate admin microservice | Adds deployment complexity, network calls for every page load, and requires sharing auth sessions across services |
| Admin-only database read replica | The admin feature needs to write data (access updates, soft-delete), so writes would still hit the primary |

**When you'd choose differently**: If the admin feature had drastically different scalability requirements (e.g., real-time analytics on millions of users), or if the team had dedicated admin-service ownership, a separate service would make sense.

---

### 2. Plain JDBC With Whitelist Sort Instead of ORM or Dynamic Query Builders

**What we did**: All admin SQL queries use `PreparedStatement` with manual column mapping. Sort fields are validated against a `Set<String>` whitelist and mapped to fixed SQL column expressions.

**Why**: The project constitution forbids ORM/JPA. Dynamic sort is the most common SQL injection vector in admin panels. A whitelist map is explicit, testable, and prevents injection without requiring a complex query builder. Each sort field maps to a known-good SQL expression — there's no path for user input to reach `ORDER BY` unsanitized.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| String concatenation of sort fields | Direct SQL injection risk; blocks like `createdAt DESC; DROP TABLE users` would work |
| Generic query builder / criteria API | Overengineered for 8-10 queries; adds a learning curve and hides what SQL actually runs |
| JPA Criteria API | Explicitly banned by project constitution |

**When you'd choose differently**: If the admin feature had 50+ queries with complex dynamic filtering, a query builder or specification pattern would reduce boilerplate. For 8-10 admin queries, explicit SQL is clearer.

---

### 3. Soft-Delete With Dual Flags Instead of Hard Delete

**What we did**: Every destructive action marks records with `is_deleted = TRUE` and `deleted_at = NOW()` via `UPDATE`, never `DELETE FROM`. User soft-delete cascades to 6 related tables in a single JDBC transaction.

**Why**: Hard delete loses data forever — debugging, audit trails, and accidental recovery become impossible. Soft-delete with both a boolean flag and a timestamp lets list queries filter by `is_deleted = FALSE` and public route queries check the same flag for `410 Gone`. The transaction ensures the cascade is atomic: if any step fails, nothing is deleted.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Hard delete | Irreversible; no audit trail |
| Single `deleted_at` timestamp | Requires `IS NULL` checks everywhere; dual flags (`is_deleted` + `deleted_at`) are more explicit and match existing project convention |
| Separate archive table | Duplicates schema; complex querying when admin needs to "undelete" |

**When you'd choose differently**: If GDPR right-to-erasure requirements apply, you'd need real deletion — but you'd implement it as a separate batch job, not inline in a DELETE endpoint.

---

### 4. Backend Authorization in Interceptor, Not in Each Controller Method

**What we did**: A single check in `AuthInterceptor.preHandle()` blocks all non-admin requests to `/api/admin/**` paths before any controller method executes.

**Why**: Centralizing authorization prevents the "forgot to check" bug. Every developer adding a new admin endpoint automatically gets protection — there's nothing to remember. Using `request.getServletPath()` (not `getRequestURI()`) makes it context-path-safe.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| `@Secured` or `@PreAuthorize` annotations | Not available in plain Spring MVC (no Spring Boot); would require AOP configuration |
| Role check in each controller method | Easy to forget on new endpoints; violates DRY |
| Separate admin-only subdomain | Infrastructure overhead for a single interceptor check |

**When you'd choose differently**: With Spring Boot + Spring Security, you'd use a security filter chain or expression-based access control. The interceptor approach is the simplest drop-in solution for a non-Boot Spring MVC app.

---

### 5. Jackson `@JsonProperty` for Boolean Property Naming

**What we did**: Added `@JsonProperty("isCurrentAdmin")` and `@JsonProperty("isPrivileged")` on boolean getters to force JSON serialization to include the `is` prefix.

**Why**: Jackson's default JavaBean introspection strips the `is` prefix from boolean getters — so `isCurrentAdmin()` produces `currentAdmin` in JSON. The frontend contract expected `isCurrentAdmin` and `isPrivileged`. The annotation is a one-character-per-field explicit override that solves this without renaming Java methods or fields.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Rename Java getters to `getCurrentAdmin()` | Would break existing internal usage and look unnatural in Java |
| Accept `currentAdmin` in frontend | Would break consistency with other boolean fields and existing API contracts |
| Jackson `ObjectMapper` config to not strip `is` | Global change that could break other parts of the app |

**When you'd choose differently**: If you're starting a new project, decide upfront whether your JSON API uses `isX` or `x` for booleans, and configure Jackson once. For existing projects, `@JsonProperty` on individual problematic getters is safer.

---

### 6. Session-Based Admin Identity Instead of Token-Based

**What we did**: Admin identity is obtained from `@SessionAttribute(value = "user") UserSession currentAdmin`. The session is created by the existing auth login flow.

**Why**: The app already uses server-side sessions for all authenticated users. Adding JWT or API tokens for admin would introduce a second authentication mechanism without benefit — admin users are already web users logged in through the same interface.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| JWT in Authorization header | Requires separate token management, refresh logic, and frontend storage — duplicates existing session infrastructure |
| API key passed as query param | Insecure (logged in referrers, server logs) |
| `userId` sent in request body | Trivially forged — admin identity must come from session, not client |

**When you'd choose differently**: If the admin API needed to be consumed by non-browser clients (automation scripts, CI/CD), token-based auth would be necessary. For browser-only admin, sessions are simpler and secure.

---

### 7. Separate Admin DTOs Instead of Reusing User-Facing DTOs

**What we did**: Created 8 new DTOs in `dto/admin/` package instead of reusing the existing user-facing `User`, `ContactDetail`, or `SavedResume` entities.

**Why**: Admin views need different fields than user views. Admin resume list needs `ownerEmail`, `ownerUserId`, `pdfAvailable` — fields that the user-facing DTO doesn't have. Admin user details exposes `isPrivileged` and lookup codes that user profiles don't. Mixing both concerns into one DTO would either expose admin-only fields to regular users or force the user-facing API to exclude fields that the admin needs.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Add admin fields to existing DTOs, null them for non-admin | Hard to audit; easy to accidentally expose sensitive fields |
| Return raw entities as JSON | Would expose password_hash, role IDs, and other internals |
| Single shared DTO with inheritance | Overengineered for this scale; separate DTOs are explicit and testable |

**When you'd choose differently**: If the admin and user views were 90%+ identical, a shared DTO with a few extra nullable fields would be simpler. Here they share less than 50% of fields.

---

### 8. Transactional Cascade in DAO, Not in Service Layer

**What we did**: `AdminDao.adminSoftDeleteUser()` opens a connection, sets `autoCommit(false)`, performs 6 UPDATE statements, and calls `commit()`. The service layer calls this as a single opaque method.

**Why**: The cascade is tightly coupled to the database (specific table names, column names, WHERE clauses). Placing transaction management in the service layer would require the service to manage connections and pass them between multiple DAO calls, which would either leak connection handling into business logic or require complex connection-passing overloads.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Service-level transaction via `TransactionTemplate` | Not available in plain Spring MVC without Spring Boot auto-configuration |
| Connection passed from service to DAO | Would require adding connection-accepting overloads to every DAO method — lots of boilerplate for a single operation |
| Distributed transaction | Massive overkill for a single-PostgreSQL operation |

**When you'd choose differently**: If the app used Spring Boot with `@Transactional`, the service-level approach would be cleaner. If multiple DAOs needed transactional composition, a connection-passing pattern with explicit overloads would be justified.

---

### 9. Whitelist-Based Date Validation Before SQL Execution

**What we did**: Date parameters (`createdFrom`, `createdTo`) are validated with `LocalDate.parse()` in the service layer before reaching the DAO. Invalid dates produce a `ServiceException` caught by `GlobalExceptionHandler` and returned as 400.

**Why**: Passing unvalidated date strings to PostgreSQL with `?::date` cast means the database handles the error — but the database error message and stack trace are not user-friendly, and they vary by database driver version. Pre-validating in Java gives consistent, localized error messages.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Let PostgreSQL parse and return error | DB error messages expose internal detail; inconsistent across locales |
| Frontend-only validation | Backend must validate independently for security — frontend is just UX |

**When you'd choose differently**: If you trusted the client completely (never), you could skip backend validation. If validation rules were complex (business day rules, holiday calendars), a dedicated validation service would be appropriate.

---

## Concepts to Know

### PagedResponse / Lazy Loading Pattern

**What it is**: A pattern where the server returns one page of data at a time, sending pagination metadata (`page`, `size`, `totalElements`, `totalPages`) alongside the current page's `items`. The frontend uses this metadata to render pagination controls and requests new pages as the user navigates.

**Where we used it**: `PagedResponse<T>` in `backend/src/main/java/com/resumainer/model/PagedResponse.java` and matching TypeScript interface in `frontend/src/types/admin.ts`. Applied to all admin list endpoints (resumes, users, user-scoped resumes).

**Why it matters**: Without pagination, a single large response could crash the browser (memory), waste bandwidth (users only see first 20 rows), and slow the database (fetching 10,000 rows to show 10). Using `items` (not `content`) as the field name maintains consistency with the existing codebase — a seemingly minor detail that prevents frontend-backend contract mismatches.

---

### Sort Whitelist Pattern

**What it is**: Instead of accepting arbitrary sort field strings from the client, define a fixed set of allowed sort keys in the backend. Each key maps to a safe SQL column expression. Invalid keys are rejected or fall back to a default.

**Where we used it**: `AdminDao.java` — two whitelist maps (`ALLOWED_RESUME_SORT_FIELDS` with 9 keys, `ALLOWED_USER_SORT_FIELDS` with 9 keys). Each key maps to a table-qualified column name.

**Why it matters**: Sort fields are the most common SQL injection vector in admin panels because they appear in `ORDER BY` clauses that can't use parameterized placeholders. A whitelist eliminates this entire class of vulnerability with a simple `Set.contains()` check — no regex, no escaping, no false positives.

---

### Self-Protection Pattern

**What it is**: A set of checks that prevent an admin from performing destructive actions on their own account. The backend is the authoritative source of truth; the frontend only prevents obvious mistakes.

**Where we used it**: `AdminService.updateUserAccess()` checks `targetUserId.equals(currentAdminId)` and rejects self-demotion (`ADMIN → USER`) and self-block (`ACTIVE → BLOCKED`). `AdminService.deleteUser()` rejects self-delete. The frontend `AdminUserAccountTab.vue` disables the role/status selects and hides the delete button when `isCurrentAdmin === true`.

**Why it matters**: An admin who accidentally demotes themselves to USER or blocks their own account can't undo it — they've lost admin access and there's no "forgot admin password" workflow. Self-protection is the engineering equivalent of a physical safety guard on a power tool.

---

### Dual-Flag Soft-Delete

**What we did**: Every deletable record has both `is_deleted` (boolean) and `deleted_at` (timestamp). Cascade deletes update 6 tables in a single transaction.

**Where we used it**: `AdminDao.adminSoftDeleteUser()` in a JDBC transaction updates `users`, `saved_resumes`, `work_experience`, `education`, `project`, `course_certificate`. Each has `is_deleted = TRUE` and `deleted_at = NOW()`. Tables without these columns (`contact_detail`, `additional_profile_info`) are intentionally skipped.

**Why it matters**: The dual flag exists because different queries check different columns. List queries often filter on `is_deleted = FALSE` (indexed boolean is fast). Public route lookups also check `is_deleted` for `410 Gone` detection. The timestamp enables "when was this deleted?" auditing without a separate audit log table.

---

### Prefixed i18n With Computed Paginator Templates

**What we did**: PrimeVue DataTable's `currentPageReportTemplate` uses raw strings like `Showing {first} to {last} of {totalRecords}` computed from locale, NOT passed through `$t()`.

**Where we used it**: `AdminResumesTable.vue`, `AdminUsersTable.vue`, `SavedResumesTable.vue` — a `pageReportTemplate` computed that returns locale-specific raw strings with PrimeVue's `{}` placeholders intact.

**Why it matters**: Vue i18n's `$t()` function treats `{first}` as an interpolation placeholder, consuming it before PrimeVue can use it. The result is broken pagination text like `— из `. The fix is a hard-learned lesson: framework-specific placeholders must not be passed through another framework's interpolation layer. This exact bug appeared in three table components independently, suggesting it's a common gotcha worth internalizing.

---

## Architecture Overview

The admin feature follows the existing project's layered monolith structure. The backend adds a new vertical slice across all layers:

```
AdminController → AdminService → AdminDao → PostgreSQL
                                      ↘ UserDao, RoleDao, etc. (read-only lookup)
```

The frontend adds a new admin module with its own views, components, services, types, and i18n:

```
views/Admin*.vue → components/admin/*.vue → services/adminService.ts → /api/admin/*
                                      ↘ types/admin.ts (interfaces matching backend DTOs)
```

Key architectural invariants:
- **No shared state** between admin and user-home components. Admin tables have their own loading/pagination/filter state.
- **No global store**. Each admin page manages its own reactive state with `ref`/`reactive`.
- **No new backend endpoint for modal data**. Resume details use the already-loaded table DTO data.
- **Auth isolation**. Backend interceptor protects all `/api/admin/**` paths. Frontend route guard duplicates checks for UX, but backend is authoritative.

---

## Glossary

| Term | Meaning |
|------|---------|
| Soft-delete | Marking a record as deleted (`is_deleted = TRUE`) instead of removing it physically. Enables recovery and audit. |
| Self-protection | Backend checks that prevent an admin from accidentally demoting, blocking, or deleting their own account. |
| Cascade delete | When deleting a user, also deleting their related records (resumes, work experience, etc.) in one transaction. |
| Sort whitelist | A predefined set of allowed sort fields that the backend accepts. Prevents SQL injection through `ORDER BY` clauses. |
| WIP badge | A visual indicator (e.g., "WIP" or "В разработке") on intentionally unfinished features to manage user expectations. |
| `PagedResponse<T>` | A generic wrapper containing a page of items plus pagination metadata (`page`, `size`, `totalElements`, `totalPages`). The field name `items` (not `content`) must match between backend and frontend. |
| `@SessionAttribute` | Spring MVC annotation that injects a pre-existing session attribute into a controller method parameter. Used here to obtain the current admin's identity. |
