# Data Model: Home Page Saved Resume Details Modal Fix

**Feature**: `009-home-modal-fix` | **Date**: 2026-06-24

---

## 1. HomeSavedResumeDto

A safe canonical DTO for the Home page. Used by both:

- paginated saved-resume list (`GET /api/resumes`), and
- summary latest resume (`GET /api/user/home` → `summary.lastResume`).

The same mapper/service method should produce both shapes to prevent table/modal/latest-card drift.

### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | long | ✅ | Saved resume numeric primary key. User ownership is enforced separately through session user ID. Do not change this to UUID unless the actual current schema proves it is UUID. |
| `resumeTitle` | String | ✅ | Resume title |
| `vacancyTitle` | String | ✅ | Target vacancy title |
| `companyName` | String or null | ❌ | Target company name; null/blank → hidden in UI |
| `languageCode` | String or null | ❌ | Resume language code (EN, RU); may be null |
| `languageName` | String or null | ❌ | Human-readable language name; may be null |
| `adaptationLevel` | String or null | ❌ | Adaptation level (MINIMAL, BALANCED, MAXIMUM); may be null |
| `createdAt` | String (ISO date/time) | ✅ | Saved resume creation date, displayed to user as generation date |
| `publicUrlLink` | String or null | ❌ | Full absolute public URL; null if not applicable |
| `pdfOpenUrl` | String or null | ❌ | Canonical authenticated URL to open PDF in new tab: `/api/generate/resumes/{id}/pdf?disposition=inline` |
| `pdfDownloadUrl` | String or null | ❌ | Canonical authenticated URL to download PDF: `/api/generate/resumes/{id}/pdf` |
| `htmlDownloadUrl` | String or null | ❌ | Canonical authenticated owner-only URL to download HTML: `/api/generate/resumes/{id}/html` |
| `pdfAvailable` | boolean | ✅ | Whether PDF is ready for access |
| `pdfStatus` | String or null | ❌ | PDF generation status (PENDING, GENERATING, READY, FAILED, or current project equivalent) |
| `pdfMessage` | String or null | ❌ | User-readable message when PDF is unavailable |
| `coverLetter` | String or null | ❌ | Cover letter text; null/blank → exact approved empty-state |

### Mapping Notes

- Same DTO mapper used for both list items and `summary.lastResume` (FR-006).
- No raw filesystem paths exposed (FR-010): never expose `pdf_file_path`, `html_file_path`, storage directory, or server-local paths.
- `publicUrlLink` is computed by backend. Frontend consumes it and never constructs public URL.
- PDF/HTML URLs use canonical authenticated owner endpoints from `GenerateResumeController`.
- `ResumeDownloadController` legacy routes must not drive new DTO URLs.

---

## 2. PublicResumeLookupResult

Internal backend model/result object for public resume route lookup. This avoids collapsing deleted, not-found, missing-file, and unsafe-path states into one `null` result.

### Status Values

| Status | HTTP Response | Description |
|---|---:|---|
| `ACTIVE` | `200 OK` | Resume exists, not deleted, PDF file present |
| `DELETED` | `410 Gone` | Resume exists but is soft-deleted — known previously valid public code |
| `NOT_FOUND` | `404 Not Found` | Invalid username or public code |
| `MISSING_FILE` | `404 Not Found` | Resume is active but physical PDF file is missing |
| `UNSAFE_PATH` | `404 Not Found` | Path traversal or unsafe path attempt detected |

### Rules

- `DELETED` vs `NOT_FOUND` is distinguished internally; both produce error responses with uniform delay.
- `DELETED` renders backend Thymeleaf `410.html`.
- `410.html` must not include dynamic resume data, username, public code, deletion date, ID, file path, company/vacancy, or filename.
- `NOT_FOUND`, `MISSING_FILE`, `UNSAFE_PATH` all return generic `404` with no metadata leakage.
- Same existing uniform artificial delay applied to both `404` and `410` responses.
- If no existing delay mechanism is found, STOP and ask before inventing a new timing mechanism.

---

## 3. Public Base URL Configuration

| Parameter | Preferred Source | Fallbacks | Example |
|---|---|---|---|
| `APP_PUBLIC_BASE_URL` | Environment variable / existing app config | Forwarded headers → request origin | `http://localhost:8080` |

### Resolution Order

1. Use `APP_PUBLIC_BASE_URL` when non-blank.
2. If the project already has existing property binding for environment-backed settings, use that equivalent property path too.
3. If config is absent, use reverse-proxy headers when present: `X-Forwarded-Proto`, `X-Forwarded-Host`, optionally `X-Forwarded-Port`.
4. If forwarded headers are absent, use request scheme + host + port as local-development fallback.
5. Log a warning when fallback origin is used because `APP_PUBLIC_BASE_URL` is not configured.

### Behavior

- Do not add a new dotenv library unless the project already uses one.
- `.env.example` documents `APP_PUBLIC_BASE_URL`, but runtime must read environment/config through the Java application/container environment.
- Normalize trailing slash: `https://resumainer.com/` → `https://resumainer.com`.
- Result format: `{baseUrl}/{username}/{publicCode}` with exactly one slash before `{username}`.
- No hardcoded `localhost`, VPS IP, or production domain in source code.

---

## 4. Soft Delete Schema

Expected existing `saved_resumes` fields:

| Column | Type | Description |
|---|---|---|
| `is_deleted` | boolean | Soft-delete flag |
| `deleted_at` | timestamp | When the resume was soft-deleted |

**Action**: Verify both columns exist and are set correctly on delete. If either required column is missing or named differently, STOP and ask before creating a migration. If both exist, update/verify DAO/service delete method sets both `is_deleted = true` and `deleted_at = CURRENT_TIMESTAMP` (or project equivalent) in one consistent update.
