# Data Model: User Home Page & Resume Workspace

**Date**: 2026-06-06 | **Feature**: 005-user-home-page

## API Entities

### UserHomeSummary

Response from `GET /api/user/home`.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `profileReady` | boolean | yes | Computed: contactComplete && hasWorkExperience && hasEducation |
| `profileChecklist` | object | yes | Breakdown of readiness |
| `profileChecklist.contactDetails` | boolean | yes | Full name + email + phone + location filled |
| `profileChecklist.workExperience` | boolean | yes | At least 1 complete non-deleted work record |
| `profileChecklist.education` | boolean | yes | At least 1 complete non-deleted education record |
| `summary` | object | yes | Aggregate stats |
| `summary.savedResumesCount` | integer | yes | Total non-deleted saved resumes for this user |
| `summary.profileStatus` | string | yes | `"READY"` or `"INCOMPLETE"` |
| `summary.lastResumeId` | integer | no | ID of most recent resume (null if none) |
| `lastResume` | SavedResume | no | Most recent resume preview (null if no resumes) |

### SavedResume

Item in paginated list response from `GET /api/resumes`, and `lastResume` in UserHomeSummary.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | integer | yes | Resume ID |
| `resumeTitle` | string | yes | User-defined title |
| `vacancy` | string | yes | Target vacancy name |
| `company` | string | yes | Target company name |
| `language` | string | yes | `"EN"` or `"RU"` |
| `adaptationLevel` | string | yes | `"MINIMAL"`, `"BALANCED"`, or `"MAXIMUM"` |
| `createdAt` | string | yes | ISO date `YYYY-MM-DD` |
| `publicUrl` | string | yes | Relative public URL path (e.g., `/john.doe/sfeg-2025`) |
| `pdfUrl` | string | yes | Relative PDF download URL (e.g., `/api/resumes/101/pdf`) |
| `coverLetter` | string | no | Cover letter text (null/empty if none) |

### PagedResponse<T>

Generic paginated wrapper.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `items` | T[] | yes | Page items |
| `page` | integer | yes | Current page number (0-indexed) |
| `size` | integer | yes | Page size requested |
| `totalElements` | integer | yes | Total items across all pages |
| `totalPages` | integer | yes | Total pages count |

### Query Parameters: `GET /api/resumes`

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `search` | string | empty | Search in resume title, vacancy, company |
| `language` | string | all | Comma-separated: `EN,RU` |
| `adaptationLevel` | string | all | Comma-separated: `MINIMAL,BALANCED,MAXIMUM` |
| `createdDate` | string | none | Exact date filter: `YYYY-MM-DD` |
| `sort` | string | `createdAt,desc` | Sort field and direction |
| `page` | integer | `0` | Page number (0-indexed) |
| `size` | integer | `10` | Page size (10, 20, or 50) |

## Validation Rules

| Field | Rule | Source |
|-------|------|--------|
| `search` | Minimum 3 characters to trigger search | Clarification (brainstorming) |
| `size` | Must be 10, 20, or 50 | FR-027 |
| `sort` | Whitelist: `resumeTitle`, `vacancy`, `company`, `language`, `adaptationLevel`, `createdAt` | FR-021 |
| `page` | Must be >= 0 | Implicit |
| `createdDate` | Must be valid `YYYY-MM-DD` format | FR-026 |
| `language` | Values: `EN`, `RU` | FR-024 |
| `adaptationLevel` | Values: `MINIMAL`, `BALANCED`, `MAXIMUM` | FR-025 |

## Database Query Rules

- **Soft-delete filter**: All resume list queries MUST include `WHERE deleted_at IS NULL` (SEC-003).
- **Owner filter**: All resume queries MUST filter by the authenticated user's user_id (SEC-002).
- **Sort safety**: Sort field MUST be validated against a whitelist. Direction MUST be restricted to `asc` or `desc` only. Never interpolate raw user input into ORDER BY (SEC-001).

## State Transitions

### Resume Lifecycle (for this feature)

```
[CREATED] → [SAVED] → [SOFT-DELETED] (via DELETE endpoint, backend handles)
```

Frontend only observes: created resumes appear in list, deleted resumes disappear with toast + refresh.

### User Home Page States

```
[LOADING] → [DATA] → [ERROR (inline per block)]
         → [EMPTY (profile incomplete, no resumes)]
         → [EMPTY (profile ready, no resumes)]
         → [DATA (partial failure — one block error, one block data)]
```

Each state transition is independent per block (Guided+Summary vs Table).
