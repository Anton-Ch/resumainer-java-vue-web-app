# Spec Input Draft — Generate Resume Feature

**Recommended Spec Kit command:** `/speckit.specify`  
**Feature branch suggestion:** `feat/007-generate-resume`  
**Feature name:** Generate Resume  
**Audience:** OpenCode with DeepSeek V4 Flash  
**Reference first:** `frontend_prototype_index.md`, `backend_prototype_index.md`  

---

## 0. Instruction to OpenCode Before Running Spec Kit

Before creating the Spec Kit artifacts for this feature:

1. Execute the repository memory preparation workflow required by `workflow.md`:
   - run `/speckit.memory-md.prepare-context` if available;
   - read `docs/memory/INDEX.md`;
   - read the generated/current `memory-synthesis.md`;
   - check `.specify/memory/constitution.md`, `architecture_constitution.md`, `DECISIONS.md`, and `BUGS.md` when relevant.
2. Read `constitution.md` and treat it as mandatory project governance.
3. Read `frontend_prototype_index.md` and `backend_prototype_index.md` before opening the large prototype dumps.
4. Use the prototype dumps only as reference. Do not copy prototype shortcuts into production.
5. If you find a hard conflict between this draft, project memory, constitution, and BA artifacts, stop and ask the user.

---

## 1. Feature Description for `/speckit.specify`

Build the full Generate Resume feature for ResumAIner.

The feature lets a logged-in user create vacancy-specific resumes using structured profile data and AI generation. The user enters vacancy/company information, chooses generation settings, waits for AI generation, reviews/edit generated content, selects the final adaptation level, and finalizes the result into saved resume artifacts. The backend must save filled HTML to disk before PDF generation, generate or prepare a PDF through a separate backend service, store file paths/links, and provide export actions for PDF and HTML.

The feature must support English-only, Russian-only, and bilingual generation. For bilingual generation, one generation request can produce both English and Russian response variants. It must also support adaptation levels: Minimal, Balanced, Maximum, and All levels. If the user chooses All levels, the system generates Minimal, Balanced, and Maximum variants for review, but final save uses one selected level.

The feature must use the latest frontend and backend prototypes as behavioral references:

- `frontend_prototype_dump.md` — Vue Generate Resume wizard and Review/Export UX.
- `backend_prototype_dump.md` — executable Python reference pipeline for prompt config, OpenRouter generation, response persistence, HTML rendering, and final artifact storage.
- `complete_business_analysis.md` — authoritative BA model, requirements, decisions, templates, and data dictionary.

---

## 2. Business Need

Job seekers spend too much time manually adapting resumes for each vacancy. ResumAIner should reduce manual resume adaptation from hours to minutes by combining structured profile data, AI-assisted adaptation, editable review, bilingual output, and server-side PDF/HTML generation.

The feature contributes directly to the product goal: produce professionally adapted resumes quickly while keeping user control through review and editing before final save.

---

## 3. Users and Actors

### Primary Actor

**Logged-in user** — creates and finalizes vacancy-specific resumes from their profile.

### Supporting Actor

**AI provider / OpenRouter** — returns structured resume JSON based on prompt and profile/vacancy data.

### Secondary Actor

**Recruiter / external viewer** — opens the public resume link and sees the PDF directly.

---

## 4. User Stories

### User Story 1 — Enter Vacancy and Company Information (P1)

As a logged-in user, I want to paste vacancy and company details so that the system can generate a resume adapted to a specific opportunity.

**Acceptance Scenarios**

1. Given I am logged in, when I open `/generate/vacancy`, then I can enter vacancy title, vacancy description, company name, company description, and additional comments.
2. Given required vacancy fields are empty, when I try to continue, then I see validation errors and the request is not created.
3. Given I entered valid vacancy information, when I continue, then the system stores or carries this data to the settings step.
4. Given I return to this step before generation, when I edit fields, then updated values are used in the generation request.

---

### User Story 2 — Choose Generation Settings (P1)

As a logged-in user, I want to choose language mode, adaptation level, and cover letter option so that the generated output matches my application needs.

**Acceptance Scenarios**

1. Given I open `/generate/settings`, when I choose English only, then the system prepares only English response rows.
2. Given I choose Russian only, then the system prepares only Russian response rows.
3. Given I choose Bilingual, then the system prepares English and Russian response rows from one generation request.
4. Given I choose All levels, then the system prepares Minimal, Balanced, and Maximum variants for each selected language.
5. Given I enable cover letter, then the generated response includes editable cover letter text.
6. Given I disable cover letter, then no cover letter is generated or displayed.

---

### User Story 3 — Generate Resume Variants (P1)

As a logged-in user, I want the system to generate structured resume variants from my profile and vacancy data so that I can review them before saving.

**Acceptance Scenarios**

1. Given I have a completed enough profile and generation settings, when I start generation, then the backend creates `resume_generation_request` and calls the configured AI client.
2. Given OpenRouter returns valid structured JSON, when parsing succeeds, then the backend stores generated responses and child section rows in draft status.
3. Given language mode is Bilingual and adaptation selection is All, then one request creates six generated response rows: EN/RU × Minimal/Balanced/Maximum.
4. Given AI output is invalid JSON or missing required sections, then the system returns a clear user-readable error and does not create partial finalized resumes.
5. Given real OpenRouter is unavailable, then automated tests use Mock AI provider, not the real provider.

---

### User Story 4 — Review and Edit Generated Content (P1)

As a logged-in user, I want to review and edit generated resume content before finalizing so that I remain in control of the final resume.

**Acceptance Scenarios**

1. Given generation completed, when I open `/generate/review`, then I see generated content grouped by section.
2. Given bilingual generation was selected, then desktop review displays English and Russian content side-by-side.
3. Given all adaptation levels were generated, then each editable field shows Minimal, Balanced, and Maximum variants.
4. Given I edit any generated field, when I save review edits, then the backend persists the edited version.
5. Given I navigate away with unsaved review changes, then the frontend warns me.
6. Given I review repeatable sections, then work experience, courses, and projects use record-first grouping.
7. Given I review Personal Information, then it appears as a separate tab and is editable.

---

### User Story 5 — Use Bilingual Education from Profile (P1)

As a logged-in user, I want my education data to be stored in both Russian and English in my profile so that generated English and Russian resumes use correct factual education names without AI guessing.

**Acceptance Scenarios**

1. Given I add or edit Education in My Profile, then the form requires institution name RU/EN, degree RU/EN, and field of study RU/EN.
2. Given I generate an English resume, then the final HTML uses English education fields.
3. Given I generate a Russian resume, then the final HTML uses Russian education fields.
4. Given Education is factual profile data, then it is not shown as an AI-generated editable section in Generate Review.
5. Given old single-language education fields exist in older code, then migrations and DTOs must update to the bilingual model without silently losing required fields.

---

### User Story 6 — Review Personal Information from AI Response (P1)

As a logged-in user, I want Personal Information to be generated and editable per response so that localized resume-ready personal details can be adjusted before final save.

**Acceptance Scenarios**

1. Given generation completed, then each `resume_generation_response` has one `generation_response_personal` row.
2. Given the response is English, then Personal Information uses English display text.
3. Given the response is Russian, then Personal Information uses Russian display text.
4. Given my profile has selected work formats, then generated Personal Information includes only those selected formats.
5. Given I edit personal information in Review, then changes are saved to `generation_response_personal`.
6. Given final HTML is rendered, then Personal Information is filled from `generation_response_personal`, with profile fallback only where allowed.

---

### User Story 7 — Finalize Selected Level and Save Resume Files (P1)

As a logged-in user, I want to choose the final adaptation level and save the result so that I get downloadable PDF and HTML artifacts.

**Acceptance Scenarios**

1. Given I generated all levels, when I select Balanced and finalize, then only Balanced responses are saved as final resumes.
2. Given I generated bilingual output, when I finalize one selected level, then the backend creates two saved resumes: one EN and one RU.
3. Given finalization starts, then the backend renders and saves filled HTML to disk before PDF conversion.
4. Given HTML rendering succeeds, then a separate PDF generation service converts or prepares the PDF.
5. Given PDF conversion fails, then the system reports the failure without losing the saved HTML artifact.
6. Given finalization succeeds, then `saved_resume` stores HTML path, PDF path, public code, public URL, language, and template metadata.

---

### User Story 8 — Export and Download Results (P1)

As a logged-in user, I want to download or open generated files and copy public links so that I can use the resume for job applications.

**Acceptance Scenarios**

1. Given finalization completed, when I open `/generate/export`, then I see one export card per saved resume language.
2. Given I click Copy Link, then the public PDF link is copied.
3. Given I click Download PDF, then the backend streams the saved PDF file.
4. Given I click Open PDF, then the public PDF route opens in a new tab.
5. Given I click Download HTML, then the backend streams the saved filled HTML file.
6. Given cover letter exists, then I can copy the generated cover letter text.

---

### User Story 9 — Public Recruiter PDF Link (P2)

As a recruiter, I want a public link to open the PDF directly so that I can quickly view, print, or download the resume.

**Acceptance Scenarios**

1. Given a saved resume is active, when a recruiter opens its public URL, then the PDF is served directly.
2. Given a saved resume is deleted or deactivated, when a recruiter opens its public URL, then the system returns a safe Gone/Not Found page.
3. Given a public link is opened, then private profile data not included in the PDF is not exposed.

---

## 5. Functional Requirements

### Generation Request

- **FR-GEN-001**: The system shall allow a logged-in user to create a generation request with vacancy title, vacancy description, company name, company description, and additional comments.
- **FR-GEN-002**: The system shall store generation request settings: language mode, adaptation selection, include cover letter, AI model, prompt config, and budget config used.
- **FR-GEN-003**: The system shall support language modes: English only, Russian only, Bilingual.
- **FR-GEN-004**: The system shall support adaptation selections: Minimal, Balanced, Maximum, All.
- **FR-GEN-005**: The system shall treat All as a request option only, not as a saved response adaptation level.

### AI Prompt and Generation

- **FR-GEN-006**: The system shall assemble prompts from DB-backed modular prompt config.
- **FR-GEN-007**: The system shall store rendered prompt logs for debugging/reproducibility.
- **FR-GEN-008**: The system shall call OpenRouter through an AI client abstraction.
- **FR-GEN-009**: The system shall provide a mock AI client for automated tests and local development.
- **FR-GEN-010**: The system shall parse structured JSON AI output into response entities and child tables.
- **FR-GEN-011**: The system shall reject invalid AI output with user-readable errors.

### Generated Response Storage

- **FR-GEN-012**: The system shall store generated top-level resume fields in `resume_generation_response`.
- **FR-GEN-013**: The system shall store generated work experience rows in `generation_response_experience`.
- **FR-GEN-014**: The system shall store generated courses/certificates rows in `generation_response_course`.
- **FR-GEN-015**: The system shall store generated projects/volunteering rows in `generation_response_project`.
- **FR-GEN-016**: The system shall store generated skill groups in `generation_response_skill_group` and related skills.
- **FR-GEN-017**: The system shall store generated/editable Personal Information in `generation_response_personal`.
- **FR-GEN-018**: The system shall store actual language and actual adaptation level per response row.

### Profile Dependencies

- **FR-GEN-019**: The system shall require bilingual Education fields in profile: RU/EN institution, RU/EN degree, RU/EN field of study.
- **FR-GEN-020**: The system shall use profile bilingual Education directly in final HTML rendering.
- **FR-GEN-021**: The system shall not show Education as an AI-generated Review section.
- **FR-GEN-022**: The system shall use normalized `work_format` + `user_work_format` data to provide work formats to the prompt payload.
- **FR-GEN-023**: The system shall not store selected work formats as comma-separated profile text.

### Review

- **FR-GEN-024**: The system shall return generated response data grouped for Review by language, section, record, field, and adaptation level.
- **FR-GEN-025**: The system shall allow users to edit generated values before finalization.
- **FR-GEN-026**: The system shall persist review edits.
- **FR-GEN-027**: The system shall keep cover letter editable when it is generated.

### Finalization and Files

- **FR-GEN-028**: The system shall allow user to select one final adaptation level before saving.
- **FR-GEN-029**: The system shall render filled HTML on the backend using stored templates and response/profile data.
- **FR-GEN-030**: The system shall save filled HTML to disk before PDF conversion.
- **FR-GEN-031**: The system shall convert filled HTML to PDF through a separate backend service.
- **FR-GEN-032**: The system shall store generated files under a deterministic server-side structure such as `generated_results/{username}/{public_code}/`.
- **FR-GEN-033**: The system shall create one saved resume per finalized language.
- **FR-GEN-034**: The system shall store `html_file_path`, `pdf_file_path`, and `public_url_link` for export.
- **FR-GEN-035**: The system shall generate unique public codes per saved resume.

### Export

- **FR-GEN-036**: The system shall show export cards for finalized saved resumes.
- **FR-GEN-037**: The system shall support PDF download through authenticated endpoint.
- **FR-GEN-038**: The system shall support HTML download through authenticated endpoint.
- **FR-GEN-039**: The system shall support public PDF opening through public URL.
- **FR-GEN-040**: The system shall support copy public link and copy cover letter actions.

---

## 6. Non-Functional Requirements

- **NFR-GEN-001**: All backend DAO queries must use `PreparedStatement`.
- **NFR-GEN-002**: Generation persistence and finalization must use explicit JDBC transactions.
- **NFR-GEN-003**: Automated tests must not call real OpenRouter.
- **NFR-GEN-004**: API keys must be masked in UI and must not appear in logs.
- **NFR-GEN-005**: PII must not be logged in normal application logs.
- **NFR-GEN-006**: AI-generated limited HTML must be sanitized with an allowlist before storage/rendering.
- **NFR-GEN-007**: User-facing frontend strings must be externalized in EN/RU i18n files.
- **NFR-GEN-008**: Backend validation is authoritative; frontend validation is immediate UX feedback.
- **NFR-GEN-009**: Filled HTML/PDF files must use UTF-8 and preserve Cyrillic text.
- **NFR-GEN-010**: PDF must be generated server-side from HTML templates.
- **NFR-GEN-011**: PDF output must have selectable text and correct one/two-page A4 layout.
- **NFR-GEN-012**: Owner-scoped access must protect private generation results and file downloads.

---

## 7. Key Entities

- `resume_generation_request`
- `resume_generation_response`
- `generation_response_experience`
- `generation_response_course`
- `generation_response_project`
- `generation_response_skill_group`
- `generation_response_skill`
- `generation_response_personal`
- `saved_resume`
- `ai_model`
- `ai_prompt_config`
- `ai_system_prompt`
- `ai_request_prompt_language`
- `ai_request_prompt_adaptation`
- `ai_request_prompt_cover_letter`
- `ai_prompt_render_log`
- `resume_template`
- `education` with bilingual fields
- `work_format`
- `user_work_format`

---

## 8. Out of Scope

- Admin UI for editing prompt config.
- Admin UI for editing budget config beyond existing baseline.
- Multiple resume templates chosen by user.
- Public HTML links for recruiters.
- Permanent cloud object storage integration.
- Payment/paid AI model billing.
- Resume analytics after recruiter opens link.

---

## 9. Success Criteria

1. User can generate English-only resume from UI and finalize it into saved HTML/PDF.
2. User can generate Russian-only resume from UI and finalize it into saved HTML/PDF.
3. User can generate Bilingual + All levels and finalize one selected level into two saved resumes.
4. Personal Information is editable in Review and saved per response.
5. Education renders correctly in EN/RU from bilingual profile fields.
6. Work formats come from normalized profile tables and reach prompt/output/rendering.
7. Export page supports public PDF link, PDF download/open, HTML download, and cover letter copy.
8. Backend tests pass without real OpenRouter calls.
9. `mvn clean package` and frontend build pass.
10. The implementation obeys the ResumAIner Constitution.
