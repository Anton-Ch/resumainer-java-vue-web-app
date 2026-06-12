# Feature Specification: Resume Generation

**Feature Branch**: `feat/007-resume-generation`

**Created**: 2026-06-12

**Status**: Draft

**Input**: Build the full Generate Resume feature for ResumAIner: vacancy-specific resume generation using structured profile data, AI adaptation, bilingual output (EN/RU), review/editing, adaptation levels (Minimal/Balanced/Maximum/All), cover letter option, and finalization into saved HTML artifacts. PDF conversion is deferred to `feat/008-pdf-conversion`.

## User Scenarios & Testing

### User Story 1 — Enter Vacancy and Company Information (P1)

As a logged-in user, I want to enter vacancy and company details so that the system can tailor a resume to a specific job opportunity.

**Why this priority**: The vacancy information is the primary input for AI generation — without it, no meaningful adaptation is possible.

**Independent Test**: A user can enter vacancy details, navigate through the settings step, and reach the generation trigger point.

**Acceptance Scenarios**:

1. **Given** I am logged in, **When** I open the generate resume flow, **Then** I can enter vacancy title, vacancy description, company name, company description, and additional comments.
2. **Given** required vacancy fields are empty, **When** I try to proceed, **Then** I see validation errors and the generation request is not created.
3. **Given** I entered valid vacancy information, **When** I continue, **Then** the data is carried to the settings step.
4. **Given** I return to this step before generation starts, **When** I edit fields, **Then** the updated values are used in the final generation request.

---

### User Story 2 — Choose Generation Settings (P1)

As a logged-in user, I want to choose language mode, AI model, adaptation level, and whether to include a cover letter so that the generated output matches my application needs.

**Why this priority**: Settings determine the shape of AI output — language, adaptation depth, and cover letter inclusion directly affect the result.

**Independent Test**: A user can change settings and the generation request reflects those choices.

**Acceptance Scenarios**:

1. **Given** I choose English-only mode, **When** generation completes, **Then** the system produces one English-language response.
2. **Given** I choose Russian-only mode, **When** generation completes, **Then** the system produces one Russian-language response.
3. **Given** I choose Bilingual mode, **When** generation completes, **Then** the system produces both English and Russian responses from a single request.
4. **Given** I choose All Levels, **When** generation completes, **Then** each language response contains Minimal, Balanced, and Maximum adaptation variants.
5. **Given** I enable cover letter, **When** generation completes, **Then** the response includes editable cover letter text.
6. **Given** I disable cover letter, **When** generation completes, **Then** no cover letter is included.
7. **Given** I open Generate Settings, **When** I see the AI Model dropdown, **Then** I can select from available models. If I am a privileged user, hidden/internal models are also visible. API keys are never exposed in the dropdown or any API response.

---

### User Story 3 — Generate Resume Variants with AI (P1)

As a logged-in user, I want the system to generate structured resume variants based on my profile and vacancy data so that I can review the best option before saving.

**Why this priority**: AI generation is the core value proposition of the product.

**Independent Test**: After submitting generation, the system creates a generation request record and stores responses in draft status.

**Acceptance Scenarios**:

1. **Given** I have a sufficiently complete profile and valid generation settings, **When** I start generation, **Then** the system creates a generation request and invokes the AI provider.
2. **Given** the AI provider returns valid structured output, **When** response is parsed successfully, **Then** the system stores responses per language and adaptation level with their section data in draft status.
3. **Given** language mode is Bilingual and adaptation selection is All, **When** generation completes, **Then** one request produces up to six response variants (EN/RU × Minimal/Balanced/Maximum).
4. **Given** AI output is invalid or missing required sections, **When** parsing fails, **Then** the system returns a clear error message and does not create partial or corrupted saved resumes.
5. **Given** AI provider is unavailable during automated testing, **Then** tests use a mock AI provider, not the real one.
6. **Given** AI generation fails (timeout, invalid response, provider error), **When** I see the error screen, **Then** I can choose either "Try again" (retry with same settings) or "Change settings" (return to Generate Settings). Vacancy data entered earlier is preserved in both cases.
7. **Given** I choose "Change settings" after a generation failure, **When** I return to Generate Settings, **Then** my previously entered vacancy and company data is still present and editable.

---

### User Story 4 — Review and Edit Generated Content (P1)

As a logged-in user, I want to review and edit AI-generated content before finalizing so that I remain in full control of my resume.

**Why this priority**: User trust depends on the ability to correct AI output before it becomes a final resume.

**Independent Test**: A user can navigate to the review screen, see generated data, edit fields, and persist changes.

**Acceptance Scenarios**:

1. **Given** generation completed successfully, **When** I open the review screen, **Then** I see generated content grouped by section (Professional Summary, Work Experience, Skills, Projects, Courses, Personal Information, Cover Letter).
2. **Given** Bilingual mode was selected, **When** I view the review screen on a desktop-width display, **Then** English and Russian content is shown side by side.
3. **Given** all adaptation levels were generated, **When** I edit a field, **Then** I can select and edit the Minimal, Balanced, or Maximum variant independently.
4. **Given** I edit any generated field, **When** I save review changes, **Then** the edited version is persisted.
5. **Given** I navigate away with unsaved changes, **Then** the system warns me before leaving.
6. **Given** I am reviewing repeatable sections (work experience, courses, projects), **Then** entries are grouped record-first for clarity.
7. **Given** Personal Information has been generated, **Then** it appears as a separate review section and is editable.

---

### User Story 5 — Finalize Selected Adaptation Level and Save Files (P1)

As a logged-in user, I want to select one final adaptation level per language and save the result so that I obtain downloadable HTML artifacts.

**Why this priority**: Saving the finalized resume is the ultimate goal of the entire generation flow.

**Independent Test**: After finalizing one adaptation level, the system creates saved resume records with HTML file paths.

**Acceptance Scenarios**:

1. **Given** I generated all levels and selected Balanced as final, **When** I finalize, **Then** only Balanced responses are saved as final resumes.
2. **Given** Bilingual mode was used, **When** I finalize one selected level, **Then** the system creates two saved resumes: one in English and one in Russian.
3. **Given** finalization starts, **Then** the system renders and saves filled HTML to disk.
4. **Given** HTML rendering succeeds, **Then** the system stores the `html_file_path` on the saved resume record.
5. **Given** HTML rendering or file writing fails, **Then** the system reports the error and does not create a partial saved resume.
6. **Given** finalization succeeds for both languages, **Then** each saved resume stores HTML path, public code, language, and adaptation level. PDF fields are reserved for future generation (see feat/008-pdf-conversion).

---

### User Story 6 — Export and Download Results (P1)

As a logged-in user, I want to download the saved HTML, copy the public link, copy the cover letter, and see PDF actions so that I can use my resume for job applications. In feat/007, HTML download is fully functional; PDF and public-link actions are placeholders ready for `feat/008-pdf-conversion`.

**Why this priority**: Export is the final delivery step — without it, the generated resume has no practical use.

**Independent Test**: After finalization, a user can access export actions for each saved resume.

**Acceptance Scenarios**:

1. **Given** finalization completed, **When** I open the export screen, **Then** I see one export card per saved resume language with: Copy public link, Download PDF, Open PDF, Download HTML, Copy cover letter.
2. **Given** I click Download HTML, **Then** the filled HTML file is downloaded. Access is owner-scoped — another user cannot download this HTML.
3. **Given** I click Copy public link, **Then** a safe placeholder link is copied (real public PDF link deferred to feat/008). The placeholder does not expose resume data.
4. **Given** I click Download PDF, **Then** a placeholder response is shown (real PDF generation deferred to feat/008). No fake PDF file is created.
5. **Given** I click Open PDF, **Then** a placeholder response or redirect is shown (real PDF serving deferred to feat/008).
6. **Given** a cover letter exists, **Then** I can copy the cover letter text.

---

### User Story 7 — Public Recruiter PDF Access (P2) [DEFERRED with placeholder]

> **Real PDF serving deferred to `feat/008-pdf-conversion`.** In feat/007, the Export UI keeps placeholder public-link and PDF actions so the frontend contract is stable. No fake PDF files are created. `pdf_file_path` is nullable until feat/008.

As a recruiter with a public link, I want to open a PDF directly so that I can view, print, or save the candidate's resume without any login or intermediate page.

**Why this priority**: Essential for the product's value but requires PDF conversion which is tracked separately.

**Independent Test**: Not applicable in feat/007 — deferred.

**Acceptance Scenarios**:

1. **Deferred**: Real public recruiter PDF access is planned for `feat/008-pdf-conversion`.
2. **feat/007 placeholder**: Export UI keeps Copy public link, Download PDF, Open PDF buttons. These use safe placeholder URLs/responses and do not expose resume data or create fake PDF files.
3. **feat/007 security**: No public route serves HTML or resume data. The placeholder link must not leak private information.

---

### Edge Cases

- What happens when the user has insufficient profile data for meaningful generation? (e.g., missing contact details, no work experience)
- How does the system handle a timed-out or unreachable AI provider? → A temporary error screen appears with two actions: "Try again" (retry with same settings) and "Change settings" (return to Generate Settings, preserving vacancy data). Raw provider errors and technical details are never exposed to the user.
- How are partial or incomplete generation results cleaned up if a user abandons the flow?
- What happens if a bilingual generation request partially succeeds (one language fails, one language succeeds)? → The entire request fails. No partial responses are preserved. The user sees the generation error screen with "Try again" and "Change settings" options.
- How does the system handle concurrent generation requests from the same user? → Only one active generation request per user. The user must wait for the current request to complete (success or failure) before starting a new one.
- What happens when the generated file storage location is unavailable or disk space is insufficient?
- How does the public recruiter link behave under high concurrent access?

## Requirements

### Functional Requirements

- **FR-GEN-001**: The system shall allow a logged-in user to create a generation request with vacancy title, vacancy description, company name, company description, and additional comments.
- **FR-GEN-002**: The system shall store generation request settings: language mode, adaptation selection, include cover letter flag, and selected AI model reference.
- **FR-GEN-003**: The system shall support language modes: English-only, Russian-only, and Bilingual.
- **FR-GEN-004**: The system shall support adaptation selections: Minimal, Balanced, Maximum, and All Levels.
- **FR-GEN-005**: The system shall treat All Levels as a request option that generates multiple variants, not as a final saved adaptation level.
- **FR-GEN-006**: The system shall assemble AI prompts from modular, maintainable prompt components.
- **FR-GEN-007**: The system shall store rendered prompts for debugging and reproducibility.
- **FR-GEN-008**: The system shall invoke the AI provider through an abstracted client interface.
- **FR-GEN-009**: The system shall provide a mock AI client for automated testing without external dependencies.
- **FR-GEN-010**: The system shall parse structured AI output and store it in response entities and child section records.
- **FR-GEN-011**: The system shall reject invalid AI output with user-readable error messages and not create partial saved resumes.
- **FR-GEN-012**: The system shall store generated top-level resume fields (professional title, professional summary, professional aspirations, cover letter, value line) per response.
- **FR-GEN-013**: The system shall store generated work experience entries per response.
- **FR-GEN-014**: The system shall store generated course/certificate entries per response.
- **FR-GEN-015**: The system shall store generated project/volunteering entries per response.
- **FR-GEN-016**: The system shall store generated skill groups with individual skill names per response.
- **FR-GEN-017**: The system shall store generated Personal Information per response (location, spoken languages, willingness to relocate, willingness for business trips, citizenship, date of birth, work formats).
- **FR-GEN-018**: The system shall record the actual language and actual adaptation level for each response row.
- **FR-GEN-019**: The system shall use bilingual profile Education data (institution name, degree, field of study available in both English and Russian) and render the appropriate language version in the final HTML.
- **FR-GEN-020**: The system shall not show Education as an AI-generated or editable section in Resume Review — Education is profile-owned data rendered directly.
- **FR-GEN-021**: The system shall use normalized work format data from the profile for AI prompt context and final rendering.
- **FR-GEN-022**: The system shall return generated data grouped by language, section, record, field, and adaptation level for review.
- **FR-GEN-023**: The system shall allow users to edit generated values before finalization and persist those edits.
- **FR-GEN-024**: The system shall keep the cover letter editable throughout the review process.
- **FR-GEN-025**: The system shall allow the user to select exactly one final adaptation level. The same level is applied to all languages in the request. For Bilingual mode, selecting one level creates two saved resumes (EN and RU) with the same adaptation level.
- **FR-GEN-026**: The system shall render filled HTML on the backend using stored templates and response/profile data.
- **FR-GEN-027**: The system shall save filled HTML to disk during finalization.
- **FR-GEN-028**: The system shall define a dedicated backend PDF generation service boundary for future PDF conversion. Real PDF conversion is deferred to `feat/008-pdf-conversion`.
- **FR-GEN-029**: The system shall store generated files under a deterministic server-side folder structure.
- **FR-GEN-030**: The system shall create one saved resume per finalized language.
- **FR-GEN-031**: The system shall store HTML file path for each saved resume. The PDF file path may be nullable/not generated until PDF conversion is implemented in `feat/008-pdf-conversion`.
- **FR-GEN-032**: The system shall generate a unique public code for each saved resume.
- **FR-GEN-033**: The system shall support authenticated owner-scoped HTML download. The UI shall keep PDF download/open buttons as placeholders in feat/007, pointing to safe placeholder responses. Real PDF download is deferred to `feat/008-pdf-conversion`.
- **FR-GEN-034**: Public PDF access without authentication is deferred to `feat/008-pdf-conversion`. In feat/007, the Export UI may provide a placeholder public link for frontend stability; the placeholder must not expose resume data or serve real PDFs.
- **FR-GEN-035**: The system shall support copying the public link (placeholder in feat/007) and copying the cover letter text from the export screen.
- **FR-GEN-036**: The system shall allow the user to delete a saved resume, which deactivates its public link.

#### AI Model Selection

- **FR-GEN-037**: The system shall show an AI Model dropdown on the Generate Settings step. The dropdown shall display safe metadata only (display name, provider, model code) and must never expose API keys.
- **FR-GEN-038**: The system shall filter available AI models based on the current user's privileged flag. Only privileged users may see hidden/internal models.
- **FR-GEN-039**: The system shall prevent non-privileged users from selecting hidden/internal AI models even if they manipulate frontend requests.
- **FR-GEN-040**: The system shall store the selected AI model reference on the generation request.

#### Generation Error Handling

- **FR-GEN-041**: The system shall show a temporary generation error screen when AI generation fails due to any provider, API, model, timeout, or invalid response error.
- **FR-GEN-042**: The system shall allow the user to retry a failed generation with the same settings without re-entering vacancy data.
- **FR-GEN-043**: The system shall allow the user to navigate from the generation error screen back to Generate Settings to change settings, including the selected AI model, while preserving already entered vacancy data.
- **FR-GEN-044**: The system shall fail the entire request if any part of a Bilingual generation fails. No partial responses are preserved.
- **FR-GEN-045**: The system shall not expose raw provider errors, stack traces, API keys, or sensitive technical details to the user on the error screen.

> **Deferred scope note:** Real PDF conversion, PDF download/open, and public recruiter PDF access are planned for `feat/008-pdf-conversion`. In feat/007, the Export UI keeps placeholder buttons for pdfDownload, pdfOpen, and public link copy so the frontend contract is stable for feat/008. No fake PDF files are created. `pdf_file_path` is nullable until feat/008. HTML download is fully functional and owner-scoped.

### Key Entities

- **Generation Request**: Captures user input (vacancy/company details), language mode, adaptation selection, cover letter preference, AI model chosen, and prompt configuration used. Tracks processing state (pending/processing/completed/failed).
- **Generation Response (per language and adaptation level)**: Stores AI-generated content for one language and one adaptation level per request. Language and adaptation level are properties of the response, not the request. For a Bilingual + All Levels request, one request creates six responses (EN × Minimal/Balanced/Maximum + RU × Minimal/Balanced/Maximum). Contains top-level fields (professional title, summary, aspirations, cover letter, value line) and status (draft/finalized).
- **Response Work Experience**: Generated and editable work history entries for a specific response. Includes page placement and fixed display order.
- **Response Course**: Generated and editable course/certificate entries with page placement and display order.
- **Response Project**: Generated and editable project/volunteering entries with fixed display order.
- **Education (profile data)**: Bilingual profile-owned data with institution name, degree, and field of study available in both English and Russian. Not AI-generated. Final HTML rendering selects the correct language version based on the response language. Not shown in Resume Review.
- **Response Skill**: Generated skill groups and individual skill names per response.
- **Response Personal Information**: Generated and editable personal details localized per response language (location, spoken languages, relocation willingness, travel willingness, citizenship, date of birth, work formats).
- **Saved Resume**: Finalized record storing metadata (user, request, response, adaptation level, language, title), HTML file path, public code, and public URL. PDF file path is nullable/not generated until PDF conversion is implemented in `feat/008-pdf-conversion`. Supports soft-delete.
- **AI Model**: Configuration of an AI provider endpoint with model code, display name, active status, and encrypted API key.
- **AI Prompt Configuration**: Versioned bundle of modular prompt components (system prompt, language fragment, adaptation fragment, cover letter fragment). Only one active config at a time.
- **AI Usage Log**: Records each AI API call with token counts and links to one or more generation responses.
- **Resume Template**: Stored HTML template definition for future template selection. Single default template seeded for MVP.
- **Resume Budget Configuration**: DB-backed rules for content distribution across one-page or two-page layouts.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A user can generate an English-only resume from start to finish and finalize it into a saved HTML artifact.
- **SC-002**: A user can generate a Russian-only resume from start to finish and finalize it into a saved HTML artifact.
- **SC-003**: A user can generate Bilingual + All Levels and finalize one selected adaptation level into two saved HTML resumes (EN and RU). PDF conversion is deferred to `feat/008-pdf-conversion`.
- **SC-004**: Personal Information is editable in the review screen and persisted per response.
- **SC-005**: Education renders in the correct language (EN or RU) from bilingual profile fields, not from AI-generated content.
- **SC-006**: Work formats originate from the user's normalized profile data and reach the AI prompt, generation output, and final rendering.
- **SC-007**: The export step supports HTML download and cover letter copy. PDF/public-link actions are visible and use safe placeholder behavior with a clear message that PDF generation is coming in `feat/008-pdf-conversion`.
- **SC-008**: All backend tests pass without real AI provider calls.
- **SC-009**: Public recruiter PDF access is deferred to `feat/008-pdf-conversion`. Not required for feat/007.
- **SC-010**: The implementation complies with the ResumAIner Constitution principles (layered architecture, TDD, i18n, security, no ORM).
- **SC-011**: When AI generation fails, the user sees a graceful error screen with "Try again" and "Change settings" actions, without losing entered vacancy data or seeing raw technical errors.

## Clarifications

### Session 2026-06-12

- **Q: Selection of final adaptation level — one level for all languages or separate per language?** → **A:** One level for all languages. The same adaptation level (e.g., Balanced) is applied to English and Russian responses. Users who need a different level can generate a new request. This matches the frontend prototype design.
- **Q: What happens after a generation failure (timeout, invalid JSON)?** → **A:** A temporary error screen appears with two actions: "Try again" (retries with current settings) and "Change settings" (returns to Generate Settings). Vacancy data is preserved in both cases. Raw provider errors are never exposed. This replaces the earlier Option A decision with Option C (retry + change settings).
- **Q: Can a user run multiple generation requests concurrently?** → **A:** No. Only one active generation request per user. The user must wait for completion or failure before starting a new one. This limits AI costs and simplifies backend handling.
- **Q: What happens when a bilingual generation partially succeeds (one language fails)?** → **A:** The entire request fails. No partial responses are preserved. The user sees the generation error screen with "Try again" and "Change settings" options — same as any other generation failure.

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | All Java code follows layered architecture (controller/service/dao/model/config/util). No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests required. TDD for business logic. Mock AI provider used — no real API calls in tests. JaCoCo coverage tracked. |
| **III. User Experience Consistency** | i18n via messages_en.properties/messages_ru.properties. Dual validation (frontend + backend). PRG pattern for form submissions. No stack traces exposed. |
| **IV. Performance & Reliability** | PreparedStatement for all SQL queries. JDBC transaction management (commit/rollback). SQL-level pagination. UTF-8 encoding throughout. AI-generated HTML sanitized with allowlist. PDF service boundary defined for future conversion (deferred to feat/008). |
| **V. Security by Design** | Backend validation is authoritative. No secrets in logs or builds. API keys masked in UI. Owner-scoped access for generation results and HTML download. Public links are placeholders in feat/007 and must not expose resume data; real public PDF access deferred to feat/008. Soft-delete disables public access. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [ ] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [ ] PostgreSQL with Flyway migrations
- [ ] Docker Compose for deployment
- [ ] Dev + Prod Spring profiles

## Assumptions

- Users have a sufficiently complete profile (contact details, work experience, education) before attempting generation.
- The user selects an AI model from the Generate Settings dropdown. Available models depend on the user's privileged flag. Hidden models are not shown to non-privileged users.
- The AI provider may be temporarily unavailable — the system handles timeouts and errors gracefully.
- Filled HTML is the canonical generated artifact in feat/007. PDF conversion is deferred to `feat/008-pdf-conversion`.
- Files are stored on the server filesystem under a deterministic path structure; no cloud object storage integration for MVP.
- Public resume access (PDF serving, recruiter links) is deferred to `feat/008-pdf-conversion`. In feat/007, placeholder public links are used in the Export UI for frontend contract stability; they do not expose real resume content.
- Each generation response represents exactly one language and one adaptation level. A single request produces one response per language and adaptation level combination.
- For All Levels adaptation, one language produces up to three responses (Minimal, Balanced, Maximum). For Bilingual + All Levels, one request produces up to six responses.
- The single default resume template is used for all generated resumes in MVP; template selection is post-MVP.
