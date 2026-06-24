# API Contracts: Home Page Saved Resume Details Modal Fix

**Feature**: `009-home-modal-fix` | **Date**: 2026-06-24

> **Instruction for DeepSeek / OpenCode**: This corrected API contract supersedes the previously generated contract. If actual controller response shapes differ, STOP and report the exact current shape before changing frontend assumptions.

---

## Canonical URL Rule

Home modal actions MUST use canonical authenticated export endpoints, not deprecated legacy download routes:

```text
GET /api/generate/resumes/{id}/pdf?disposition=inline   # Open PDF in new browser tab
GET /api/generate/resumes/{id}/pdf                       # Download PDF
GET /api/generate/resumes/{id}/html                      # Download HTML, owner-only
```

`ResumeDownloadController` is deprecated legacy fallback and MUST NOT drive new `HomeSavedResumeDto` URL generation or frontend modal actions.

---

## 1. GET /api/resumes (updated)

Paginated saved resume list — response items use `HomeSavedResumeDto`.

### Response (200 OK)

```json
{
  "items": [
    {
      "id": 42,
      "resumeTitle": "Senior Software Engineer - Acme Corp",
      "vacancyTitle": "Senior Software Engineer",
      "companyName": "Acme Corp",
      "languageCode": "EN",
      "languageName": "English",
      "adaptationLevel": "BALANCED",
      "createdAt": "2026-06-20T14:30:00",
      "publicUrlLink": "http://localhost:8080/johndoe/GTFQ",
      "pdfOpenUrl": "http://localhost:8080/api/generate/resumes/42/pdf?disposition=inline",
      "pdfDownloadUrl": "http://localhost:8080/api/generate/resumes/42/pdf",
      "htmlDownloadUrl": "http://localhost:8080/api/generate/resumes/42/html",
      "pdfAvailable": true,
      "pdfStatus": "READY",
      "pdfMessage": null,
      "coverLetter": "Dear Hiring Manager..."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### Field changes from current frontend usage

- **REMOVED**: standalone `publicUrl` — replaced by `publicUrlLink`
- **REMOVED**: standalone `pdfUrl` — replaced by `pdfOpenUrl` and `pdfDownloadUrl`
- **ADDED**: `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfStatus`, `pdfMessage`
- **RENAMED/normalised**: old vacancy/company display fields should become `vacancyTitle` / `companyName` in the Home DTO
- **FORBIDDEN**: raw `pdf_file_path`, `html_file_path`, storage directory, or filesystem paths in any API response

---

## 2. GET /api/user/home (updated)

Home summary — `summary.lastResume` uses the same `HomeSavedResumeDto` mapper as `/api/resumes` list items.

### Canonical response shape

`lastResume` MUST live under `summary.lastResume` because the Home page summary card consumes it as `summary.lastResume`.

### Response (200 OK)

```json
{
  "profileReady": true,
  "profileChecklist": { },
  "summary": {
    "savedResumesCount": 5,
    "profileStatus": "READY",
    "lastResumeId": 42,
    "lastResume": {
      "id": 42,
      "resumeTitle": "Senior Software Engineer - Acme Corp",
      "vacancyTitle": "Senior Software Engineer",
      "companyName": "Acme Corp",
      "languageCode": "EN",
      "languageName": "English",
      "adaptationLevel": "BALANCED",
      "createdAt": "2026-06-20T14:30:00",
      "publicUrlLink": "http://localhost:8080/johndoe/GTFQ",
      "pdfOpenUrl": "http://localhost:8080/api/generate/resumes/42/pdf?disposition=inline",
      "pdfDownloadUrl": "http://localhost:8080/api/generate/resumes/42/pdf",
      "htmlDownloadUrl": "http://localhost:8080/api/generate/resumes/42/html",
      "pdfAvailable": true,
      "pdfStatus": "READY",
      "pdfMessage": null,
      "coverLetter": "Dear Hiring Manager..."
    }
  }
}
```

### Guardrail

Do not introduce a second root-level `lastResume` field unless the current backend already has that shape and user approves the compatibility mapping. If current code returns root-level `lastResume`, STOP and report before changing contract or frontend state.

---

## 3. DELETE /api/resumes/{id}

Soft-delete a saved resume owned by the authenticated user.

### Success response

```json
{
  "message": "Resume deleted"
}
```

### Error behavior

- Return the same public response status/body shape for non-owned and non-existent IDs.
- Frontend must show one generic i18n toast for all delete failures, for example:
  - EN: `Failed to delete resume.`
  - RU: `Не удалось удалить резюме.`
- Backend may log detailed server-side reasons, but must not expose ownership/existence details to the client.

---

## 4. GET /{username}/{publicCode} (updated)

Public resume route — behavior changes for deleted state.

### Response scenarios

| Condition | HTTP Status | Body |
|---|---:|---|
| Active resume, PDF exists | `200 OK` | PDF inline |
| Soft-deleted known public code | `410 Gone` | Backend Thymeleaf `410.html` page |
| Invalid username/code | `404 Not Found` | Generic 404 |
| Missing physical PDF file | `404 Not Found` | Generic 404 |
| Path traversal attempt | `404 Not Found` | Generic 404 |

### Timing and metadata

- All error responses (`404` and `410`) use the same existing uniform artificial delay.
- If no existing delay mechanism exists, STOP and ask before inventing one.
- `410.html` must not include dynamic resume data, username, public code, deletion date, saved resume ID, file path, company/vacancy, or filename.
- Public resume route MUST NOT intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page routes, or Vue SPA assets.
