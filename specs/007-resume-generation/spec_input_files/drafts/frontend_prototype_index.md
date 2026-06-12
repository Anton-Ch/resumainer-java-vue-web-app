# Frontend Prototype Index — Generate Resume Feature

**Project:** ResumAIner  
**Prototype dump:** `frontend_prototype_dump.md`  
**Purpose:** help OpenCode / DeepSeek V4 Flash understand the frontend prototype without rereading the whole dump every time.  
**Use this index first** when implementing or debugging the Java/Vue production feature.

---

## 1. Reading Rules for OpenCode

1. Start with this file before opening `frontend_prototype_dump.md`.
2. Open only the prototype files relevant to the current task.
3. Do not copy built `dist/` files into source implementation. Use `dist/` only as evidence that the prototype was built successfully.
4. Use the prototype as **behavioral and visual reference**, not as final architecture.
5. Production code must use real backend APIs instead of `generateMockService.ts`.
6. Preserve the UX decisions from the prototype unless a newer BA artifact explicitly supersedes them.
7. All visible text must go through Vue i18n EN/RU.

---

## 2. Feature Scope Represented by the Prototype

The frontend prototype covers the authenticated Generate Resume wizard:

1. `/generate/vacancy` — vacancy/company input.
2. `/generate/settings` — language mode, adaptation selection, cover letter option.
3. `/generate/review` — editable generated variants grouped by section, language, and adaptation level.
4. `/generate/export` — public links, PDF actions, HTML download action, cover letter copy.

The prototype is not responsible for final PDF/HTML rendering. It only presents and edits structured response data. Final template rendering is backend-owned.

---

## 3. Highest-Value Files to Read First

| Priority | File | Open When | Why It Matters |
|---|---|---|---|
| 1 | `src/composables/useGenerateResumeFlow.ts` | Implementing wizard state, navigation, selected level, bilingual state, public links | Central state machine for frontend prototype. Shows how wizard pages share data. |
| 2 | `src/types/generate.ts` | Designing frontend DTOs and API contracts | Defines frontend shape of generated variants, language/adaptation values, personal info, review sections. |
| 3 | `src/services/generateMockService.ts` | Replacing mock calls with backend API calls | Shows expected frontend behavior for generate, save/finalize, public links, loaders, and mock response shape. |
| 4 | `src/views/generate/GenerateVacancyPage.vue` | Implementing page 1 | Route-level page around vacancy form and stepper. |
| 5 | `src/components/generate/VacancyStepForm.vue` | Implementing vacancy form fields and validation | Source of field layout and labels for vacancy/company inputs. |
| 6 | `src/views/generate/GenerateSettingsPage.vue` | Implementing page 2 | Route-level page around settings form. |
| 7 | `src/components/generate/SettingsStepForm.vue` | Implementing language/adaptation/cover letter settings | Source of UI behavior for language mode, adaptation selection, cover letter checkbox. |
| 8 | `src/views/generate/GenerateReviewPage.vue` | Implementing page 3 route logic | Coordinates generation result loading, tabbed review page, selected level, save/finalize action. |
| 9 | `src/components/generate/ReviewStepForm.vue` | Implementing review UI | Core review layout: tabs, bilingual two-column layout, level variants, Personal Information tab. |
| 10 | `src/components/generate/GeneratedRecordGroup.vue` | Implementing repeatable generated sections | Shows record-first layout for work experience, courses, projects. |
| 11 | `src/components/generate/GeneratedVariantTextarea.vue` | Implementing field-level editable variants | Shows how Minimal/Balanced/Maximum variants are edited per field. |
| 12 | `src/components/generate/AdaptationLevelRadioGroup.vue` | Implementing selected adaptation level | Use for selected level. Do not duplicate warnings if parent already shows them. |
| 13 | `src/views/generate/GenerateExportPage.vue` | Implementing export route | Coordinates export data and public link display. |
| 14 | `src/components/generate/ExportResult.vue` | Implementing export cards/buttons | Source for public link, copy, PDF buttons, HTML download button. |
| 15 | `src/i18n/en.json` and `src/i18n/ru.json` | Adding or checking strings | All user-facing strings must be mirrored EN/RU. |
| 16 | `src/router/index.ts` | Wiring routes | Defines `/generate/*` route structure. |
| 17 | `src/assets/styles/vue_general.css` | Preserving visual DNA | Shared style tokens, cards, labels, form styling. |
| 18 | `src/components/generate/WhimsicalLoader.vue` | Implementing generation loader | Loading behavior while backend generation is running. |
| 19 | `src/components/generate/GenerateStepper.vue` | Stepper behavior | Wizard progress indicator. |
| 20 | `src/components/generate/BilingualDivider.vue` | Bilingual layout | Visual divider used in bilingual review/export layouts. |

---

## 4. Files Usually Not Needed

| File/Folder | Why Usually Skip |
|---|---|
| `dist/` | Built output only. Do not edit. Use only if source file is missing or to verify build output. |
| `package-lock.json` | Only inspect if dependency installation fails. |
| `vite.log`, `vite.err` | Only inspect when build behavior differs from prototype. |
| `primeicons-*` assets | Static dependency output. |

---

## 5. Frontend Behavior to Preserve

### 5.1 Wizard Routes

Production frontend should keep the same user journey:

```text
/generate/vacancy
→ /generate/settings
→ /generate/review
→ /generate/export
```

The user should not jump forward if required data from previous steps is missing.

### 5.2 Vacancy Step

The vacancy step collects:

- vacancy title;
- vacancy description;
- company name;
- company description;
- additional comments / user generation instructions.

Production backend should persist these fields in `resume_generation_request`.

### 5.3 Settings Step

The settings step collects:

- language mode: English only, Russian only, Bilingual;
- adaptation selection: Minimal, Balanced, Maximum, All levels;
- include cover letter: true/false.

Important: `ALL` is a request selection only. Generated response rows must store actual adaptation levels: `MINIMAL`, `BALANCED`, `MAXIMUM`.

### 5.4 Review Step

Current review tabs:

1. Professional Positioning
2. Work Experience
3. Courses and Certifications
4. Projects and Volunteering
5. Skills
6. Personal Information

Education is **not** edited on Review. Education is profile-owned bilingual factual data and must be collected in My Profile.

### 5.5 Review Layout Rules

- Desktop bilingual layout: EN left, RU right.
- Mobile: stack sections vertically.
- For repeatable sections, use record-first grouping:
  - Work item 1 → EN/RU columns → all adaptation levels.
  - Work item 2 → EN/RU columns → all adaptation levels.
- For single-value sections, use field-first grouping:
  - Professional title → Minimal/Balanced/Maximum.
  - Value line → Minimal/Balanced/Maximum.
  - Summary → Minimal/Balanced/Maximum.
- Level badges must use one word:
  - EN: Minimal / Balanced / Maximum
  - RU: Минимальная / Сбалансированная / Максимальная

### 5.6 Personal Information Review

Personal Information must be an editable Review tab and must include:

- location;
- spoken languages;
- willingness to relocate;
- willingness for business trips;
- work formats;
- citizenship;
- date of birth;
- GPA / grade when available.

The frontend should edit `generation_response_personal`, not raw profile data.

### 5.7 Export Page

Export page receives from backend:

- `public_url_link` — public PDF/recruiter link;
- `pdf_file_path` or backend download endpoint reference;
- `html_file_path` or backend download endpoint reference;
- cover letter text if generated.

Export actions:

- Copy public link.
- Download PDF.
- Open PDF in new tab.
- Download HTML.
- Copy cover letter.

The `Download HTML` button should have muted helper text similar to:

- EN: `You can edit the saved HTML manually if needed after downloading.`
- RU: `При необходимости ты сможешь вручную подредактировать HTML после скачивания.`

---

## 6. Frontend API Integration Targets

Replace mock service calls with real backend API calls.

Recommended production endpoints:

| Frontend Action | Method + Endpoint | Backend Responsibility |
|---|---|---|
| Create generation request | `POST /api/generate/requests` | Save vacancy/settings and return request ID. |
| Start generation | `POST /api/generate/requests/{id}/generate` | Call OpenRouter/mock client, persist response rows. |
| Get review data | `GET /api/generate/requests/{id}/review` | Return grouped language/adaptation variants. |
| Save review edits | `PUT /api/generate/requests/{id}/review` | Persist edited response fields and child tables. |
| Finalize selected level | `POST /api/generate/requests/{id}/finalize` | Render HTML, convert PDF, save `saved_resume`. |
| Get export results | `GET /api/generate/requests/{id}/export` | Return public links and file download info. |
| Download PDF | `GET /api/resumes/{savedResumeId}/pdf` | Stream PDF with authorization. |
| Open public PDF | `GET /candidate/{publicCode}` or `/candidate/{username}/{publicCode}` | Public direct PDF route. |
| Download HTML | `GET /api/resumes/{savedResumeId}/html` | Stream saved HTML with authorization. |

Endpoint names may be adjusted to match existing project conventions, but the behavior must stay equivalent.

---

## 7. My Profile Frontend Dependencies

Generate Resume depends on existing My Profile data. When touching profile UI, preserve the earlier profile feature patterns:

- card list + Add/Edit form for repeatable sections;
- Courses table with pagination;
- work formats as lookup multi-select backed by `work_format` + `user_work_format`;
- Additional resume language fields hidden from frontend for now.

Education must be updated to bilingual required fields:

- institution name RU / EN;
- degree RU / EN;
- field of study RU / EN.

Do not ask AI to translate Education in Review. Education is factual profile data.

---

## 8. Common Traps for DeepSeek V4 Flash

1. Do not render final resume HTML inside Vue.
2. Do not use the prototype `dist/` files as source.
3. Do not keep `generateMockService.ts` as production logic.
4. Do not use `/resume-en` or `/resume-ru` suffixes in public links.
5. Do not create one public URL for both EN and RU files. Each saved resume gets its own public code/link.
6. Do not store work formats as comma-separated text in profile tables. Use `work_format` + `user_work_format`.
7. Do not put Education back into Review. Make profile Education bilingual instead.
8. Do not hardcode UI strings. Use i18n.
9. Do not make PDF buttons purely frontend placeholders in production. They must call backend endpoints.
10. Do not expose local server file paths directly to users. Use backend download endpoints.

---

## 9. Quick Implementation Checklist

Before finishing frontend implementation, verify:

- [ ] Vacancy page sends expected request DTO.
- [ ] Settings page sends `languageMode`, `adaptationSelection`, `includeCoverLetter`.
- [ ] Review page can display EN-only, RU-only, and bilingual responses.
- [ ] Review page can display one selected level or all three levels.
- [ ] Review page includes Personal Information tab.
- [ ] Export page shows PDF and HTML actions.
- [ ] All new labels exist in EN/RU i18n files.
- [ ] `npm run build` succeeds.
- [ ] No production code imports mock generation data.
