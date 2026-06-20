# API Contracts — Feature 008

## POST /api/generate/requests/{requestId}/finalize

**Purpose**: Finalize a generated resume — triggers PDF + HTML generation.

**Auth**: Owner-scoped (session-based, role USER)

**Behavior**:
1. Validates requestId belongs to authenticated user.
2. Sets request status to FINALIZING (blocks concurrent finalization).
3. Loads finalized response + profile + budget data.
4. Generates HTML + PDF via ported spike renderer.
5. Validates PDF (page count, content, text extraction, fill, blank pages).
6. On success: promotes files to storage, commits saved_resume with metadata.
7. On failure: deletes staged files, resets status, returns error.

**Response 200** (success):
```json
{
  "success": true,
  "message": "Resume finalized successfully",
  "exportResult": { ... ExportResultDto ... }
}
```

**Response 409** (concurrent):
```json
{
  "success": false,
  "message": "Finalization is already in progress. Please wait."
}
```

**Response 422** (fitting failure):
```json
{
  "success": false,
  "message": "Resume could not be generated. Please try again and reduce the longest texts in your resume fields.",
  "retryAction": "review"
}
```

**Response 401/403**: Standard auth errors.

---

## GET /api/generate/requests/{requestId}/export

**Purpose**: Get export result with PDF/HTML availability.

**Auth**: Owner-scoped

**Response 200**:
```json
{
  "resumes": [
    {
      "savedResumeId": 123,
      "languageCode": "en",
      "adaptationLevel": "BALANCED",
      "htmlDownloadUrl": "/api/generate/resumes/123/html",
      "pdfDownloadUrl": "/api/generate/resumes/123/pdf",
      "pdfOpenUrl": "/api/generate/resumes/123/pdf?disposition=inline",
      "publicUrlLink": "/johndoe/abc123def",
      "pdfAvailable": true,
      "pdfMessage": null,
      "coverLetter": "Dear Hiring Manager..."
    }
  ]
}
```

---

## GET /api/generate/resumes/{savedResumeId}/html

**Purpose**: Download PDF-parity HTML artifact.

**Auth**: Owner-scoped
**Content-Type**: `text/html; charset=UTF-8`
**Disposition**: `attachment; filename="resume_{id}_{lang}.html"`

---

## GET /api/generate/resumes/{savedResumeId}/pdf

**Purpose**: Download PDF artifact.

**Auth**: Owner-scoped
**Content-Type**: `application/pdf`
**Disposition**: `attachment` (default) or `inline` (if `?disposition=inline`)

---

## GET /{username}/{publicCode}

**Purpose**: Public PDF access for recruiters (no authentication).

**Content-Type**: `application/pdf`
**Disposition**: `inline`

**Status codes**:
- 200: PDF served inline
- 404: Invalid username, invalid publicCode, deleted/disabled resume, or non-existent

**Security**:
- No cover letter
- No private HTML
- No metadata leakage
- Rate-limited against brute-forcing (via existing infrastructure)
