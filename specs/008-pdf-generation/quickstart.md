# Quickstart — Feature 008

## Prerequisites

- Feature 007 codebase fully working
- PostgreSQL running (Docker or local)
- Maven + Node.js installed

## Quick Verification (after implementation)

### 1. Start the stack

```bash
docker-compose up -d
```

### 2. Run Flyway migrations

Migrations run automatically on backend startup. Verify new tables:

```sql
SELECT * FROM generation_response_experience_bullet LIMIT 1;
SELECT * FROM generation_response_project_bullet LIMIT 1;
SELECT * FROM resume_pdf_fit_limits WHERE active = true;
SELECT * FROM resume_pdf_fill_targets;
```

### 3. Run backend tests

```bash
cd backend
mvn test
```

### 4. Run frontend build

```bash
cd frontend
npm run build
```

### 5. Manual smoke test

1. Register and login.
2. Fill My Profile with EN+RU data.
3. Generate Resume → select adaptation level → wait for AI response.
4. Open Review → edit bullet points → save.
5. Click Finalize → wait for loading screen → verify success.
6. Download PDF → verify 1 or 2 pages, selectable text, page notes.
7. Download HTML → verify parity with PDF layout.
8. Copy public link → open in incognito → verify PDF loads inline.
9. Retry with RU-only, Bilingual, different levels.

### 6. Fitting failure test

1. Fill profile with extremely long text in work experience descriptions.
2. Generate + finalize → verify "Try again" message appears.
3. Click "Try again" → verify return to Review with edits preserved.

### 7. Inspect generated artifacts

PDF and HTML files stored in configured directory (check application properties):
```
{storage.dir}/{savedResumeId}/resume_{lang}.pdf
{storage.dir}/{savedResumeId}/resume_{lang}.html
```

### 8. Debug fitting attempts

Enable DEBUG level for `com.resumainer.service.pdf`:
```properties
logging.level.com.resumainer.service.pdf=DEBUG
```

View attempt logs:
```
Attempt 1: font=9.0, lh=1.3, gaps=16/8/4/2, pages=3, fill=0.92 — REJECTED (page count)
Attempt 2: font=8.0, lh=1.2, gaps=14/6/3/1, pages=2, fill=0.87 — ACCEPTED
```

## Troubleshooting

| Symptom | Check |
|---|---|
| PDF generation fails with font error | Verify Inter-*.ttf and Manrope-*.ttf in backend resources/fonts/ |
| Blank pages in PDF | Check `PdfBlankPageCleaner` logs; verify A4 page size in CSS |
| Cyrillic text missing | Verify font supports Cyrillic (Inter does); check UTF-8 encoding |
| Fitting never converges | Increase `max_attempts` in `resume_pdf_fit_limits` |
| Public link returns 404 | Verify `saved_resume` is active + `public_code` is not null |
