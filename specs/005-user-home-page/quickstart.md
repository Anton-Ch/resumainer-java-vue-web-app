# Quickstart: User Home Page & Resume Workspace

**Date**: 2026-06-06 | **Feature**: 005-user-home-page

## Branch

```bash
git checkout feat/005-user-home-page
```

## Implementation Order

This feature has both backend and frontend work. The recommended implementation order:

### Phase A: Backend Foundation

1. **Routing config** — Update `WebConfig.java` for `/app/*` SPA path handling + CORS for new endpoints
2. **Security config** — Update auth filter/interceptor to recognize `/app/*` patterns
3. **Flyway migration** — Create/verify `saved_resumes` table if not existing
4. **ResumeDao** — CRUD with paginated query, soft-delete, search/filter/sort
5. **UserHomeService** — Profile readiness calculation (TDD first)
6. **ResumeService** — Paginated listing with search/filter/sort, soft-delete
7. **UserHomeController** — `GET /api/user/home`
8. **ResumeController** — `GET /api/resumes`, `DELETE /api/resumes/{id}`

### Phase B: Frontend Infrastructure

9. **Router restructure** — Move all routes under `/app/*`, add new profile/generate/admin routes, update guards
10. **i18n** — Add full home namespace to `en.json` and `ru.json`
11. **AppHeader.vue** — Create shared navigation header with role-based Admin visibility

### Phase C: User Home Page

12. **useUserHome composable** — Data fetching, state management, error handling
13. **userHomeService + resumeService** — API client functions
14. **GuidedNextStep.vue** — Profile readiness guidance block (complete + ready states)
15. **ProfileChecklist.vue** — Checklist with status indicators
16. **SummaryCards.vue** — Three summary cards
17. **SavedResumesTable.vue** — DataTable with all filters, sort, pagination
18. **ResumeDetailsDialog.vue** — Modal with actions + cover letter
19. **UserHomePage.vue** — Compose all components, wire states

### Phase D: Placeholders & Polish

20. **PlaceholderPage.vue** — Reusable placeholder for profile/generate/admin pages
21. **App.vue** — Add global Toast + ConfirmDialog
22. **Existing UserHomePage → rewritten** — Remove old token dashboard code

## Verification

### Backend

```bash
cd backend
mvn clean test                          # Unit tests
mvn clean package                       # Build
```

### Frontend

```bash
cd frontend
npm run build                           # TypeScript + Vite build
```

### Integration (Docker)

```bash
docker compose up --build               # Full stack
# Visit http://localhost/ → landing page
# Visit http://localhost/app/home → User Home
```

## Key Files

| File | Purpose |
|------|---------|
| `specs/005-user-home-page/spec.md` | Feature specification (46 FR, 11 SC) |
| `specs/005-user-home-page/plan.md` | Implementation plan with structure |
| `specs/005-user-home-page/data-model.md` | API entities and contracts |
| `specs/005-user-home-page/contracts/api-contracts.md` | API request/response contracts |
| `specs/005-user-home-page/research.md` | Technology decisions and patterns |
| `specs/005-user-home-page/spec_input_files/` | Design prototype and briefs |

## Design Reference

Visual prototype available at: `specs/005-user-home-page/spec_input_files/dist/index.html` (static) and `specs/005-user-home-page/spec_input_files/vite-prototype/` (Vite source).

Key prototype states:
- `/demo/home-ready` — Ready profile with resumes
- `/demo/home-incomplete` — Incomplete profile
- `/demo/home-empty` — Ready profile, no resumes
- `/demo/home-loading` — Loading state

> **Note**: Prototype uses demo routes (`/demo/...`). Production routes are under `/app/...`.
