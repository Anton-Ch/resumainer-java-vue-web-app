# API Contracts: User Home Page & Resume Workspace

**Date**: 2026-06-06 | **Feature**: 005-user-home-page

## Endpoint: GET /api/user/home

Returns the user's home summary: profile readiness, checklist, stats, and last resume preview.

### Request

```
GET /api/user/home
Cookie: JSESSIONID=abc123
```

### Response 200 OK

```json
{
  "profileReady": true,
  "profileChecklist": {
    "contactDetails": true,
    "workExperience": true,
    "education": true
  },
  "summary": {
    "savedResumesCount": 12,
    "profileStatus": "READY",
    "lastResumeId": 101
  },
  "lastResume": {
    "id": 101,
    "resumeTitle": "Business Analyst Resume",
    "vacancy": "Middle Business Analyst",
    "company": "Example Company",
    "language": "EN",
    "adaptationLevel": "BALANCED",
    "createdAt": "2025-01-09",
    "publicUrl": "/john.doe/ba-example-2025",
    "pdfUrl": "/api/resumes/101/pdf",
    "coverLetter": "Dear Hiring Manager..."
  }
}
```

### Response 401 Unauthorized

```json
{
  "error": "Authentication required"
}
```

### Errors

| Status | Condition |
|--------|-----------|
| 401 | Not authenticated |
| 500 | Server error (logged, no stack trace exposed) |

---

## Endpoint: GET /api/resumes

Paginated list of saved resumes with search/filter/sort.

### Request

```
GET /api/resumes?search=analyst&language=EN,RU&adaptationLevel=MINIMAL,BALANCED&sort=createdAt,desc&page=0&size=10
Cookie: JSESSIONID=abc123
```

### Query Parameters

| Parameter | Type | Default | Example |
|-----------|------|---------|---------|
| `search` | string | empty | `analyst` |
| `language` | string | `EN,RU` | `EN` |
| `adaptationLevel` | string | `MINIMAL,BALANCED,MAXIMUM` | `BALANCED` |
| `createdDate` | string | none | `2025-01-09` |
| `sort` | string | `createdAt,desc` | `resumeTitle,asc` |
| `page` | integer | `0` | `1` |
| `size` | integer | `10` | `20` |

### Sort Whitelist

Allowed sort fields: `resumeTitle`, `vacancy`, `company`, `language`, `adaptationLevel`, `createdAt`.

### Response 200 OK

```json
{
  "items": [
    {
      "id": 101,
      "resumeTitle": "Business Analyst Resume",
      "vacancy": "Middle Business Analyst",
      "company": "Example Company",
      "language": "EN",
      "adaptationLevel": "BALANCED",
      "createdAt": "2025-01-09",
      "publicUrl": "/john.doe/ba-example-2025",
      "pdfUrl": "/api/resumes/101/pdf",
      "coverLetter": "Dear Hiring Manager..."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 12,
  "totalPages": 2
}
```

### Response 401 Unauthorized

```json
{
  "error": "Authentication required"
}
```

### Errors

| Status | Condition |
|--------|-----------|
| 401 | Not authenticated |
| 400 | Invalid parameter (e.g., page < 0, size not 10/20/50) |
| 500 | Server error (logged, no stack trace exposed) |

---

## Endpoint: DELETE /api/resumes/{id}

Soft-delete a saved resume.

### Request

```
DELETE /api/resumes/101
Cookie: JSESSIONID=abc123
```

### Response 200 OK

```json
{
  "message": "Resume deleted"
}
```

### Errors

| Status | Condition |
|--------|-----------|
| 401 | Not authenticated |
| 403 | Not the owner of this resume |
| 404 | Resume not found or already deleted |
| 500 | Server error (logged, no stack trace exposed) |
