# API Contracts: User Profile Page

Base URL: `/api/profile`

All endpoints require authentication (`requiresAuth`). All requests/responses use `application/json`. Error responses follow existing NFR-003/004 convention: `{ "message": "user-friendly text", "errorCode": "optional-code", "timestamp": "ISO-8601" }`. All responses include `Cache-Control: no-store, private` header (SEC-005).

---

## Section Status

### GET /api/profile/status

Returns completion status for all 6 profile sections.

**Response 200:**
```json
{
  "contact": "completed",
  "experience": { "count": 3, "label": "3 records" },
  "education": { "count": 1, "label": "1 record" },
  "projects": { "count": 0, "label": "No records" },
  "courses": { "count": 12, "label": "12 records" },
  "additional": "incomplete"
}
```

**Status rules:**
- Contact: "completed" when all required fields (fullName, professionalTitle, email, phone, location) are non-empty
- Additional: "completed" when all required fields (username, defaultResumeLanguage, additionalResumeLanguage, dateOfBirth, citizenship) are non-empty
- Record-based sections: count with appropriate label ("{count} records" / "{count} record" / "No records" / "0 records")

---

## Contact Details

### GET /api/profile/contact

**Response 200:**
```json
{
  "fullName": "John Doe",
  "professionalTitle": "Software Engineer",
  "email": "john@example.com",
  "phone": "+1-555-0123",
  "location": "Kazakhstan, Astana",
  "linkedinUrl": "linkedin.com/in/johndoe",
  "portfolioUrl": "",
  "telegram": "@johndoe",
  "whatsapp": ""
}
```

### PUT /api/profile/contact

**Request body:** Same shape as GET response.

**Response 200:** Updated ContactDetails object.

**Validation:**
- `email`: valid email format
- URL fields (linkedinUrl, portfolioUrl): accept values with or without protocol prefix
- Required fields: fullName, professionalTitle, email, phone, location

---

## Work Experience

### GET /api/profile/experience

**Response 200:**
```json
[
  {
    "id": 1,
    "jobTitle": "Senior Developer",
    "companyName": "Tech Corp",
    "location": "Astana, Kazakhstan",
    "startDate": "2022-03-01",
    "endDate": null,
    "currentlyWorkHere": true,
    "description": "Led backend development team...",
    "companyUrl": "https://techcorp.com"
  }
]
```

### POST /api/profile/experience

**Request body:** WorkExperience without id, createdAt, updatedAt.

**Response 201:** Created WorkExperience with id.

### PUT /api/profile/experience/{id}

**Request body:** Full WorkExperience object.

**Response 200:** Updated WorkExperience.

### DELETE /api/profile/experience/{id}

**Response 204:** No Content (soft-delete: sets `is_deleted = TRUE`, SEC-003).

**Validation:**
- Required: jobTitle, companyName, startDate, description, location
- End date not sent when currentlyWorkHere = true
- End date must not be before start date

---

## Projects

### GET /api/profile/projects

**Response 200:** Array of Project objects.

### POST /api/profile/projects

**Response 201:** Created Project with id.

### PUT /api/profile/projects/{id}

**Response 200:** Updated Project.

### DELETE /api/profile/projects/{id}

**Response 204:** No Content (soft-delete: sets `is_deleted = TRUE`, SEC-003).

**Validation:**
- Required: projectName, description, location, startDate
- Role defaults to "Participant" if null (DEC-031)
- End date not sent when isOngoing = true
- End date must not be before start date

---

## Education

### GET /api/profile/education

**Response 200:** Array of Education objects.

### POST /api/profile/education

**Response 201:** Created Education with id.

### PUT /api/profile/education/{id}

**Response 200:** Updated Education.

### DELETE /api/profile/education/{id}

**Response 204:** No Content (soft-delete: sets `is_deleted = TRUE`, SEC-003).

**Validation:**
- Required: institutionName, degree, fieldOfStudy, startDate
- End date not sent when currentlyStudying = true
- End date must not be before start date

---

## Courses & Certificates

### GET /api/profile/courses?page=0&size=10&sort=startDate,desc&search=&dateFrom=&dateTo=

**Query parameters:**
- `page`: integer, default 0
- `size`: integer, default 10 (allowed: 10, 20, 50)
- `sort`: field,direction (allowed: courseName, provider, startDate, endDate — each with asc/desc)
- `search`: string, minimum 3 characters before filtering
- `dateFrom`: ISO date, filter start_date >= dateFrom
- `dateTo`: ISO date, filter start_date <= dateTo

**Response 200:**
```json
{
  "content": [
    {
      "id": 1,
      "courseName": "AWS Solutions Architect",
      "provider": "Coursera",
      "startDate": "2025-01-15",
      "endDate": "2025-06-20",
      "credentialUrl": "https://coursera.org/verify/abc123",
      "courseFocus": "AWS, Cloud Architecture, Security",
      "description": "Comprehensive cloud architecture course"
    }
  ],
  "totalElements": 45,
  "totalPages": 5,
  "number": 0,
  "size": 10
}
```

### POST /api/profile/courses

**Response 201:** Created Course with id.

### PUT /api/profile/courses/{id}

**Response 200:** Updated Course.

### DELETE /api/profile/courses/{id}

**Response 204:** No Content (soft-delete: sets `is_deleted = TRUE`, SEC-003).

**Validation:**
- Required: courseName, provider, startDate
- End date must not be before start date
- Date filter validation: dateTo must not be before dateFrom

---

## Additional Info

### GET /api/profile/additional

**Response 200:**
```json
{
  "username": "johndoe",
  "defaultResumeLanguage": "en",
  "additionalResumeLanguage": "ru",
  "acceptableWorkFormats": ["remote", "hybrid"],
  "willingnessToRelocate": "Negotiable",
  "willingnessForBusinessTravel": "Yes",
  "skills": "Java, Spring, PostgreSQL",
  "spokenLanguages": "English C1, Russian native",
  "professionalAspirations": "Lead software architect",
  "achievements": "Built scalable microservices...",
  "additionalContextForAI": "Prefer modern Java stack",
  "dateOfBirth": "1990-05-15",
  "citizenship": "Kazakhstan"
}
```

### PUT /api/profile/additional

**Request body:** Same shape as GET response.

**Response 200:** Updated AdditionalInfo.

**Validation:**
- `username`: English letters, digits, underscores, hyphens only. No Cyrillic, no spaces. Unique across all users.
- `defaultResumeLanguage` and `additionalResumeLanguage`: mutually exclusive (cannot be same language)
- `dateOfBirth`: valid full date, required (NOT NULL)
- `citizenship`: required (NOT NULL)
- `acceptableWorkFormats`: values must match work_format.code set
- `willingnessToRelocate` / `willingnessForBusinessTravel`: must be "Yes", "No", or "Negotiable"

---

## Common Error Codes

| HTTP Status | errorCode | Scenario |
|---|---|---|
| 400 | VALIDATION_ERROR | Missing or invalid field values |
| 404 | NOT_FOUND | Record not found for update/delete |
| 409 | USERNAME_TAKEN | Username already exists |
| 500 | INTERNAL_ERROR | Unexpected server error |
