# Decision Log

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-10
**Last Updated:** 2026-06-11
**Author:** Anton
**Version:** 18.0
**Status:** Active
**Related BABOK Area:** 3.3 Plan Business Analysis Governance

---

## 1. Description

This document records major business analysis, system analysis, architecture, scope, security, and implementation decisions made during the project.

Each decision includes context, selected option, rejected alternatives, rationale, impact, and follow-up actions.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Record only meaningful decisions that affect scope, requirements, architecture, data model, UI/UX, deployment, security, or project process.
- Do not record minor wording or formatting changes here.
- Do not delete decisions after they are made.
- If a decision changes, mark the old decision as `Superseded` and create a new decision.
- Use `Change Request Log` if a decision causes a non-trivial change to approved artifacts.
- Use consistent decision IDs: `DEC-001`, `DEC-002`, `DEC-003`.

### 2.2 Decision Type Values

| Value | Meaning |
|---|---|
| Architecture | System structure, layers, frameworks, backend/frontend approach |
| Scope | MVP, stretch goals, post-MVP, or future scope |
| Requirement | FR/NFR, acceptance criteria, or business rule decision |
| Data Model | Entities, tables, fields, relationships, or storage format |
| UI/UX | Screens, flows, layout, interaction, or page behavior |
| Deployment | Hosting, Docker, server, domain, or environment |
| Security | Authentication, authorization, secrets, public access, API keys |
| Process | BA workflow, governance, documentation, or implementation sequence |

### 2.3 Decision Status Values

| Value | Meaning |
|---|---|
| Proposed | Decision is suggested but not approved |
| Approved | Decision is accepted and active |
| Superseded | Decision was replaced by a newer decision |
| Rejected | Decision option was considered but not selected |

## 3. Summary Table

| ID | Date | Type | Title | Rationale | Impact | Status |
|---|---|---|---|---|---|---|
| DEC-001 | 2026-05-10 | Architecture | Use plain JDBC instead of ORM | Mandatory capstone constraint; ORM is not allowed | Affects DAO layer, transaction handling, and object mapping | Approved |
| DEC-002 | 2026-05-11 | Scope | Landing Page is mandatory for MVP | Needed for visitor understanding and login/register entry | Adds Landing Page to MVP scope | Approved |
| DEC-003 | 2026-05-11 | UI/UX | Replace Dashboard terminology with Home page | Home is clearer for main post-login pages | Affects UI labels, sitemap, and documentation | Approved |
| DEC-004 | 2026-05-11 | Scope | Move User Settings into My Profile | Reduces page count and navigation complexity | Eliminates separate Settings page | Approved |
| DEC-005 | 2026-05-12 | Scope | Integrate resume listing into User Home | Faster access to saved resumes | Eliminates separate Resume History page | Approved |
| DEC-006 | 2026-05-11 | UI/UX | Public recruiter link opens PDF directly | Recruiters need fast PDF viewing, printing, and saving | Changes public access behavior | Approved |
| DEC-007 | 2026-05-11 | Architecture | Use hybrid frontend approach | Thymeleaf is enough for Landing Page; Vue fits main app dynamics | Affects frontend architecture and deployment | Approved |
| DEC-008 | 2026-05-13 | Security | Mask saved API keys in AI Model Details | Prevents accidental secret exposure | Affects admin UI, logging, and model settings | Approved |
| DEC-009 | 2026-05-11 | Process | Use mock AI generation before real OpenRouter integration | Reduces external dependency risk | Affects implementation sequence and testing | Approved |
| DEC-010 | 2026-05-13 | UI/UX | Use wireframe field findings as approved input | Field-level details were confirmed during wireframe preparation | Affects My Profile and Generate Resume requirements | Approved |
| DEC-011 | 2026-05-13 | UI/UX | Use card list + Add/Edit form for repeatable profile sections | Clear and reusable pattern for profile records | Affects Work Experience, Projects, Education, Courses | Approved |
| DEC-012 | 2026-05-13 | UI/UX | Use automatic sorting for repeatable profile sections | Reduces manual ordering effort and keeps resumes logical | Affects profile list display and query ordering | Approved |
| DEC-013 | 2026-05-13 | Data Model | Use simplified Additional Info table for MVP | Keeps profile data manageable within MVP timeline | Affects profile data model and My Profile scope | Approved |
| DEC-014 | 2026-05-15 | Scope | Remove Resume Details pages (user and admin) | Pages add no value; PDF actions belong in list views and post-save flow | Eliminates user and admin Resume Details pages; PDF actions from User Home and Resumes table | Approved |
| DEC-015 | 2026-05-16 | UI/UX | Resume Details as modal popup on User Home | Provides PDF link copy, download, and cover letter in one place without a separate page | Replaces PDF action column with Details column + modal popup | Approved |
| DEC-016 | 2026-05-16 | Scope | Cover letter generation is MVP | Less implementation effort than post-MVP phase | Cover letter is generated by LLM, editable in Resume Review, viewable in Details modal | Approved |
| DEC-017 | 2026-05-16 | Architecture | HTML-to-PDF generation on Java server side | Easier to maintain structured layout with HTML templates than direct PDF generation | Resume generated as HTML first, then converted to PDF on Java backend | Approved |
| DEC-018 | 2026-05-16 | Scope | Courses and Certificates section is mandatory for MVP | System targets users with practical experience and completed courses | Courses section stays in MVP scope, FR-006 priority unchanged (Medium) | Approved |
| DEC-019 | 2026-05-16 | Data Model | Two-page default HTML resume template for all resumes | Ensures consistent visual quality and even content spreading | Standard two-page layout; separate technical approach doc needed for content distribution rules | Approved |
| DEC-032 | 2026-05-18 | UI/UX | Resume delete from User Home with confirmation | Users can delete saved resumes; soft-delete deactivates public link with HTTP 410 | New FR-013; `public_url_link` field in `saved_resume`; HTTP 410 Gone page | Approved |
| DEC-033 | 2026-05-18 | Data Model | Add professional_title to resume_generation_response | AI generates vacancy-specific professional title distinct from user's contact_detail title | New field `professional_title varchar(250)` in `resume_generation_response` | Approved |
| DEC-034 | 2026-05-20 | Architecture | Backend controls template rendering | Java backend generates final HTML/PDF from structured data and templates | Vue does not render final PDF templates | Approved |
| DEC-035 | 2026-05-20 | Architecture | Resume Review: Vue shows editable review form only | During review, Vue shows structured editable fields, not PDF preview | Final PDF preview available only after save/finalize | Approved |
| DEC-036 | 2026-05-20 | Architecture | AI returns structured JSON (MVP) | AI returns structured JSON matching generation response contract; backend parses and stores in DB | Enables direct entity mapping from AI output; simplifies prompt-to-DB pipeline | Approved |
| DEC-037 | 2026-05-20 | Data Model | Text fields may contain limited HTML in generated content | AI may use approved HTML tags inside text fields for formatting | Backend must sanitize AI-provided HTML using allowlist | Approved |
| DEC-038 | 2026-05-20 | Security | Backend sanitizes AI-generated HTML | Backend sanitizes AI-provided HTML using allowlist before storing/rendering | Prevents XSS and template breakage | Approved |
| DEC-039 | 2026-05-20 | Requirement | Fixed resume section order | Resume sections follow fixed order; AI adjusts content not order | Section order defined in Resume Template Details document | Approved |
| DEC-040 | 2026-05-20 | Scope | One-page and two-page templates both in MVP | Both template modes included in MVP scope | Backend decides mode programmatically via Page Profile scoring | Approved |
| DEC-041 | 2026-05-20 | Scope | Page Profile scoring is part of MVP | Density scoring and content budget selection are MVP features | Backend calculates Page Profile before AI generation call | Approved |
| DEC-042 | 2026-05-20 | Architecture | PDF generated server-side | Final PDF generation on Java backend for stability and consistency | HTML-to-PDF conversion on server | Approved |
| DEC-043 | 2026-05-20 | Requirement | PDF page count is technically validated | One-page template → exactly 1 PDF page; two-page → exactly 2 pages | Post-generation validation via PDF library | Approved |
| DEC-044 | 2026-05-20 | Requirement | Date of Birth included in Personal Information | If user provides DOB, it is included in resume Personal Information | Code-determined field display | Approved |
| DEC-045 | 2026-05-20 | Requirement | Professional Aspirations always expected in AI output | Even without user input, AI must generate Aspirations | Rendered as standard resume section | Approved |
| DEC-046 | 2026-05-20 | Design | AI may expand Aspirations as space filler | AI expands Aspirations to fill available space on page 2 | User reviews and edits before final save | Approved |
| DEC-047 | 2026-05-20 | UI/UX | Page navigation notes kept on two-page template | "See the next page" / "See the previous page" notes maintained | Standard UX pattern for multi-page resumes | Approved |
| DEC-048 | 2026-05-20 | Requirement | Page 2 Additional WE compact at high density | Additional WE uses one short sentence per job at max density | No bullet points when 7+ additional jobs | Approved |
| DEC-049 | 2026-05-20 | Architecture | Content budgets in external YAML configuration | Sentence counts, bullet limits stored in external config file | Easier tuning without code changes | Superseded |
| DEC-050 | 2026-05-20 | Scope | Profile picture moved from MVP to POST-MVP | Photo field not supported by current templates; removes unnecessary MVP complexity | FR-007 updated; photo_file_path deferred | Approved |
| DEC-051 | 2026-05-21 | Process | Add error handling, code quality, and build NFRs | Mandatory per Capstone specification; ensures professional codebase quality | New NFRs added to requirements log; affects implementation process | Approved |
| DEC-052 | 2026-05-21 | Architecture | Use Vue 3 + Vite + PrimeVue for frontend SPA | PrimeVue provides Vue-native responsive components with ready themes and good documentation; best fit for a developer new to Vue SPA | Replaces generic "Vue.js" placeholder; hybrid approach kept for Landing Page | Approved |
| DEC-053 | 2026-05-21 | Data Model | Add DB layer NFRs: transactions, SQL scripts, PreparedStatement, UTF-8, custom Connection Pool | Mandatory per Capstone specification covering transaction management, SQL injection prevention, Cyrillic support, and manual Connection Pool | NFR-012–NFR-016 added to Requirements Log; Connection Pool documentation required | Approved |
| DEC-054 | 2026-05-21 | Security | Add UI security and dual validation NFRs | Mandatory per Capstone specification covering form resubmission, XSS sanitization, and dual frontend/backend validation with Spring annotations | NFR-017–NFR-019 added to Requirements Log; validation layer expanded | Approved |
| DEC-055 | 2026-05-21 | UI/UX | Use Vuelidate + native Vue 3 validation for frontend | Vuelidate pairs naturally with Vue 3 Composition API, avoids jQuery dependency, and is lighter than PrimeVue's built-in validation for complex rules | Frontend validation tool defined; mentioned in NFR-019 and Confirmed Elicitation Results | Approved |
| DEC-056 | 2026-05-21 | Architecture | Document 4 design patterns with rationale | Mandatory per Capstone: minimum 4 patterns with justification, Interceptors, AOP, SOLID, DRY | NFR-021–NFR-023 added; ResumePromptBuilder formalized as Builder; patterns listed in Strategic Context | Approved |
| DEC-057 | 2026-05-21 | Process | Establish testing standards: JUnit 5, Mockito, JaCoCo, TDD | Mandatory per Capstone: JUnit 5, 50%+ coverage, Mockito, JaCoCo reports, test structure, TDD | NFR-024–NFR-027 added; JaCoCo in pom.xml; coverage reports generated during build | Approved |
| DEC-058 | 2026-05-21 | Architecture | Add dev/prod profiles, Swagger, Docker Compose | Swagger for API docs with ADMIN-only prod access; Docker Compose with backend, frontend, PostgreSQL; dev/prod Spring profiles | NFR-031, NFR-032 added; NFR-028 updated; Docker tech stack extended | Approved |
| DEC-059 | 2026-05-21 | Process | Approve NFR baseline for development handoff | All NFRs reviewed and approved; acceptance criteria confirmed; open questions closed | 32 NFRs set to Approved/Ready; OQ-007 and OQ-009 closed; FR-001/007/011-013 acceptance criteria finalized | Approved |
| DEC-060 | 2026-05-23 | Architecture | Replace YAML budget config with DB-backed config | YAML is developer-oriented, not admin/data-oriented; DB-backed config is easier to inspect, test, and demonstrate | YAML config section replaced with DB-backed approach; new DB tables added; Decision Log, Requirements Log, Change Request Log, ERDs updated | Approved |
| DEC-061 | 2026-05-23 | Requirement | Fixed resume section order stays in backend code | Section order is fixed and not configurable through DB admin panel | Section order belongs to template rendering logic, not runtime budget configuration | Approved |
| DEC-062 | 2026-05-23 | Data Model | PostgreSQL partial unique index for one active budget config | Protects data integrity even though backend has fallback logic | Partial unique index in Flyway migration; DBML notes added | Approved |
| DEC-063 | 2026-06-10 | Data Model | Support bilingual generation with language-specific responses | Independent EN/RU AI calls can produce inconsistent bilingual resumes; one request must support single-language and bilingual modes | `language_id` moved from request to response; request-response becomes 1:N; `value_line` and `ai_usage_log_response` added | Approved |
| DEC-064 | 2026-06-10 | Architecture | Use versioned modular AI prompt configuration | Avoids 16 duplicated full request prompts and supports maintainable prompt assembly | Adds AI prompt config bundle tables, prompt fragments, and rendered prompt traceability | Approved |
```markdown
| DEC-065 | 2026-06-10 | Architecture | Store system prompt separately | Keeps stable model-level rules independent from request-specific generation settings | Adds `ai_system_prompt` linked to `ai_prompt_config` | Approved |
| DEC-066 | 2026-06-10 | Architecture | Store language-specific prompt fragments | Avoids duplication of single-language and bilingual response rules across full prompts | Adds `ai_request_prompt_language` for ENGLISH_ONLY, RUSSIAN_ONLY, and BILINGUAL modes | Approved |
| DEC-067 | 2026-06-10 | Architecture | Store adaptation-specific prompt fragments | Keeps minimal, balanced, maximum, and all-level generation rules independently maintainable | Adds `ai_request_prompt_adaptation` for MINIMAL, BALANCED, MAXIMUM, and ALL selections | Approved |
| DEC-068 | 2026-06-10 | Architecture | Store cover-letter prompt fragments for enabled/disabled mode | Makes cover-letter behavior explicit and prevents unwanted cover-letter generation | Adds `ai_request_prompt_cover_letter` for true/false cover-letter modes | Approved |
| DEC-069 | 2026-06-10 | Architecture | Store rendered prompt log for debugging and reproducibility | Allows exact prompt inspection, QA, and comparison of AI output across prompt versions | Adds `ai_prompt_render_log` linked to generation request and prompt config | Approved |
| DEC-070 | 2026-06-12 | Data Model | Store bilingual education profile fields | Education is profile-owned and not AI-generated, but resumes can be rendered in EN/RU | Replaces single-language education fields with mandatory RU/EN education fields | Approved |
| DEC-071 | 2026-06-12 | Data Model | Add generated Personal Information response table | Personal information needs localized editable output per generated language/adaptation | Adds `generation_response_personal`; Resume Review can edit Personal Information before final save | Approved |
| DEC-072 | 2026-06-12 | UI/UX | Add Personal Information tab to Resume Review | Users need to review and edit localized personal information before PDF generation | Adds last Review tab after Skills; hides profile resume-language preference fields from current UI | Approved |
| DEC-073 | 2026-06-12 | Architecture | Store generated HTML and PDF artifacts under user/code folders | HTML is canonical intermediate artifact before server-side PDF conversion | Saves files under `generated_results/{username}/{public_code}/`; Export adds HTML download action | Approved |


## 4. Details

### DEC-001 Use Plain JDBC Instead of ORM

**Date:** 2026-05-10  
**Type:** Architecture  
**Status:** Approved  
**Context:** The Capstone requires database access through plain JDBC and demonstration of DAO pattern, SQL handling, transactions, and custom Connection Pool.  
**Selected Option:** Plain JDBC with DAO layer and manual Connection Pool.  
**Rejected Alternatives:** Hibernate, JPA, Spring Data JPA, MyBatis.  
**Rationale:** Plain JDBC is required and demonstrates direct database access skills.  
**Impact:** Requires DAO classes, SQL scripts, ResultSet mapping, explicit transaction handling, and a custom documented Connection Pool.  
**Follow-up Actions:** Keep architecture and requirements documents free of ORM-based assumptions. The custom Connection Pool must include thorough internal documentation covering thread-safety mechanism, connection lifecycle, timeout handling, and edge cases (DEC-053).

### DEC-002 Landing Page Is Mandatory for MVP

**Date:** 2026-05-11  
**Type:** Scope  
**Status:** Approved  
**Context:** UI/UX elicitation confirmed that the Landing Page is essential for explaining product value and converting visitors to users.
**Selected Option:** Landing Page is included in MVP.  
**Rejected Alternatives:** Optional or post-MVP Landing Page.  
**Rationale:** Visitors need a clear entry point and short product explanation before login/register.  
**Impact:** Adds Landing Page to MVP and frontend scope.  
**Follow-up Actions:** Implement Landing Page with product value, short “How it works”, and login/register links.

### DEC-003 Replace Dashboard Terminology with Home Page

**Date:** 2026-05-11  
**Type:** UI/UX  
**Status:** Approved  
**Context:** UI/UX elicitation found "Dashboard" terminology confusing; "Home page" is clearer for main post-login pages.
**Selected Option:** Use `User Home` and `Admin Home`.  
**Rejected Alternatives:** Generic `Dashboard` wording.  
**Rationale:** Home page wording is clearer for main post-login pages.  
**Impact:** Affects sitemap, UI labels, wireframes, and documentation.  
**Follow-up Actions:** Use Home terminology consistently.

### DEC-004 Move User Settings into My Profile

**Date:** 2026-05-11  
**Type:** Scope  
**Status:** Approved 
**Context:** UI/UX elicitation confirmed that separate User Settings page increases navigation complexity without sufficient benefit.
**Selected Option:** User settings are sections inside My Profile.  
**Rejected Alternatives:** Separate User Settings page.  
**Rationale:** Reduces navigation complexity and keeps user-owned data in one place.  
**Impact:** Eliminates separate Settings page and expands My Profile structure.  
**Follow-up Actions:** Add language, account, and resume preference settings to My Profile.

### DEC-005 Integrate Resume Listing into User Home

**Date:** 2026-05-12  
**Type:** Scope  
**Status:** Approved
**Context:** Updated elicitation results showed it is more logical to display user's resumes directly in User Home for quick access rather than separate page.
**Selected Option:** User Home includes searchable/sortable resume listing table.  
**Rejected Alternatives:** Separate Resume History page.  
**Rationale:** Users need fast access to saved resumes after login.  
**Impact:** Eliminates separate Resume History page and expands User Home.  
**Follow-up Actions:** User Home must support resume search, sorting, details access, PDF download, and link copying.

### DEC-006 Public Recruiter Link Opens PDF Directly

**Date:** 2026-05-11  
**Type:** UI/UX  
**Status:** Approved  
**Context:** UI/UX elicitation confirmed that recruiters prefer direct PDF access without intermediate web page.
**Selected Option:** Public link serves the saved PDF directly.  
**Rejected Alternatives:** Public web wrapper page before PDF.  
**Rationale:** Recruiters need direct viewing, printing, copying text, and saving PDF.  
**Impact:** Public access flow serves PDF, not HTML.  
**Follow-up Actions:** Implement public URL routing with safe access checks.

### DEC-007 Use Hybrid Frontend Approach

**Date:** 2026-05-11  
**Type:** Architecture  
**Status:** Approved  
**Context:** UI/UX elicitation confirmed need for simple landing page but dynamic UI for authenticated sections.
**Selected Option:** Thymeleaf only for Landing Page; Vue for main application if feasible.  
**Rejected Alternatives:** Pure Thymeleaf/JSP or pure Vue for everything.  
**Rationale:** Landing Page is simple; authenticated UI needs dynamic forms, tables, and review flows.  
**Impact:** Affects frontend structure, Docker Compose, and backend endpoint design.  
**Follow-up Actions:** Validate Vue feasibility before implementation baseline.

### DEC-008 Mask Saved API Keys in AI Model Details

**Date:** 2026-05-13  
**Type:** Security  
**Status:** Approved  
**Context:** UI/UX elicitation confirmed that administrators need to see full API keys for oversight and management.
**Selected Option:** Saved API keys are always masked; admin can replace or delete the key, but cannot view it in full after saving.  
**Rejected Alternatives:** Showing full API keys in admin UI; logging API keys.  
**Rationale:** API keys are secrets. Displaying or logging them creates unnecessary security risk.  
**Impact:** Affects AI Model Details UI, logging rules, and admin model settings.  
**Follow-up Actions:** Define API key storage, masking, replacement, deletion, and logging rules.

### DEC-009 Use Mock AI Generation Before Real OpenRouter Integration

**Date:** 2026-05-11  
**Type:** Process  
**Status:** Approved  
**Context:** UI/UX elicitation confirmed that mock AI generation should be used initially to reduce dependency risk.
**Selected Option:** Implement mock AI generation first.  
**Rejected Alternatives:** Starting with real OpenRouter integration immediately.  
**Rationale:** Protects MVP development from external API dependency and allows earlier vertical slice testing.  
**Impact:** Affects implementation sequence and testing.  
**Follow-up Actions:** Build AI integration behind an interface with mock and OpenRouter implementations.

### DEC-010 Use Wireframe Field Findings as Approved Input

**Date:** 2026-05-13  
**Type:** UI/UX  
**Status:** Approved  
**Context:** During wireframes preparation updated information arised on fields to be used.
**Selected Option:** Use field-level wireframe findings as approved input for UI/UX requirements and data model drafts.  
**Rejected Alternatives:** Keeping only high-level page descriptions.  
**Rationale:** Wireframe preparation clarified actual fields, validation rules, error messages, and data model needs.  
**Impact:** Affects My Profile, Generate Resume, validation requirements, and traceability.  
**Follow-up Actions:** Create `wireframe_field_requirements.md` and update related logs/matrices.

### DEC-011 Use Card List + Add/Edit Form Pattern for Repeatable Profile Sections

**Date:** 2026-05-13  
**Type:** UI/UX  
**Status:** Approved  
**Context:** During wireframes preparations decision was made to use card list
**Selected Option:** Repeatable sections use a card list with Add/Edit form.  
**Rejected Alternatives:** One large form for all records; table-only editing.  
**Rationale:** Card lists are clearer for profile records and easier to review on wireframes.  
**Impact:** Applies to Work Experience, Projects & Volunteering, Education, Courses & Certificates.  
**Follow-up Actions:** Reflect this pattern in wireframes and UI requirements.

### DEC-012 Use Automatic Sorting for Repeatable Profile Sections

**Date:** 2026-05-13  
**Type:** UI/UX  
**Status:** Approved  
**Context:** During wireframes preparations decision was made on how to sort cards.
**Selected Option:** Sort repeatable profile records automatically.  
**Rejected Alternatives:** Manual ordering in MVP.  
**Rationale:** Automatic sorting keeps records consistent and reduces implementation/UI complexity.  
**Impact:** Affects display logic and DAO/service query ordering.  
**Follow-up Actions:** Define sorting rules in wireframe field requirements.

### DEC-013 Use Simplified Additional Info Table for MVP

**Date:** 2026-05-13  
**Type:** Data Model  
**Status:** Approved  
**Context:** During wireframes preparations Additional Info section of Profile page was simplified for MVP
**Selected Option:** Use a simplified Additional Info model for MVP.  
**Rejected Alternatives:** Fully normalized separate tables for every additional profile item.  
**Rationale:** Simplifies MVP implementation while preserving useful AI context.  
**Impact:** Affects profile data model and My Profile scope.  
**Follow-up Actions:** Keep fields clear and migrate to normalized structures only if needed later.

### DEC-014 Remove Resume Details Pages (User and Admin)

**Date:** 2026-05-15
**Type:** Scope
**Status:** Approved
**Context:** Wireframe review revealed that neither user nor admin Resume Details pages provide standalone value. All functions — PDF preview, download, public link copying — can be performed directly from table-level actions (User Home for users, Resumes table for admin) or the post-save success flow after Resume Review.
**Selected Option:** Remove both user and admin Resume Details pages. PDF viewing, download, and public link actions are available from User Home table and the admin Resumes table. Post-save flow offers immediate PDF preview and link copy.
**Rejected Alternatives:** Keeping dedicated Resume Details pages for user or admin.
**Rationale:** Eliminates unnecessary pages that add navigation complexity and implementation effort without incremental value. Admin can view any generated and saved resume PDF the same way as a user — from the list view.
**Impact:**
- **Scope:** Eliminates both user and admin Resume Details pages from MVP.
- **Requirements:** FR-009 is superseded; FR-008 covers PDF and public link actions from User Home.
- **Data Model:** No data model change; SavedResume and PdfFile remain.
- **UI/UX:** User Home table and admin Resumes table provide direct PDF actions; post-save flow shows PDF preview and link copy.
- **Risks:** Reduces UI scope and implementation effort.

### DEC-015 Resume Details as Modal Popup on User Home

**Date:** 2026-05-16
**Type:** UI/UX
**Status:** Approved
**Context:** Early wireframes showed PDF actions as a direct Link to PDF column in the User Home resume table. Refined wireframe design revealed a Details column with an `Open details` button, leading to a modal popup containing PDF link copy, PDF download, and cover letter text.
**Selected Option:** Replace the Link to PDF column with a Details column. Clicking `Open details` opens a modal popup with PDF link copy, PDF download, and cover letter display.
**Rejected Alternatives:** Direct PDF download column; separate Resume Details page; no modal with all actions in table row.
**Rationale:** Modal provides all resume output actions (PDF link, download, cover letter) in one focused view without navigating away from User Home. Cover letter display is included because cover letter is MVP.
**Impact:**
- **Scope:** No new page; modal replaces both the separate Resume Details page (already removed in DEC-014) and the simple PDF column.
- **Requirements:** FR-008 acceptance criteria updated to reflect modal behavior.
- **Data Model:** No change.
- **UI/UX:** User Home table has Details column; clicking opens modal.
- **Risks:** Reduces need for a separate page; modal complexity is manageable.
**Follow-up Actions:** Update FR-008 acceptance criteria and User Home wireframe field requirements.

### DEC-016 Cover Letter Generation Is MVP

**Date:** 2026-05-16
**Type:** Scope
**Status:** Approved
**Context:** During wireframe refinement, the question arose whether cover letter generation should remain part of MVP or be moved to post-MVP. The cover letter feature was already visible in the Resume Review wireframe and in the Resume Details modal.
**Selected Option:** Include cover letter generation in MVP. LLM generates cover letter text as part of the resume generation process. User can edit the generated cover letter before saving. Cover letter is viewable in the Resume Details modal and can be copied.
**Rejected Alternatives:** Post-MVP cover letter generation; manual cover letter entry without AI generation.
**Rationale:** Generating the cover letter at the same time as the resume requires less implementation effort than adding it as a separate post-MVP feature. The LLM already has the vacancy context and profile data — asking it to generate a cover letter as additional output adds minimal complexity.
**Impact:**
- **Scope:** Cover letter moves from stretch/future to MVP.
- **Requirements:** New FR-011 (Cover letter generation) and FR-012 (Cover letter in generation request).
- **Data Model:** Cover letter text field in generation request and saved resume.
- **UI/UX:** Cover letter field in Resume Review; cover letter display in Details modal.
- **Risks:** Slightly increases generation complexity but managed within existing generation flow.
**Follow-up Actions:** Create FR-011 and FR-012. Add cover letter to generation request data model and Resume Review UI requirements.

### DEC-017 HTML-to-PDF Generation on Java Server Side

**Date:** 2026-05-16
**Type:** Architecture
**Status:** Approved
**Context:** PDF generation is required for resume download. Two approaches were considered: generating PDF directly on the Java side, or rendering HTML first and converting to PDF. Wireframe confirmation and technical review favored HTML-first approach.
**Selected Option:** Resume is generated as HTML first using a structured template with replaceable sections. The HTML is then converted to PDF on the Java backend using a PDF generation library. Vue does not handle PDF creation.
**Rejected Alternatives:** Direct PDF generation in Java without HTML intermediate; client-side PDF generation in Vue.
**Rationale:** HTML templates are easier to create, maintain, and review for layout correctness. The structured two-page template ensures consistent output. Java-side conversion keeps PDF logic centralized and avoids browser-dependent behavior.
**Impact:**
- **Scope:** PDF generation approach is defined and stable.
- **Architecture:** PDF library required on Java backend.
- **Data Model:** No change. PDF metadata stored in existing PdfFile entity.
- **UI/UX:** Resume Review generates synchronized HTML preview (not separate PDF preview).
**Follow-up Actions:** Create a separate technical approach document for the two-page HTML template and even content spreading rules.

### DEC-018 Courses and Certificates Section Is Mandatory for MVP

**Date:** 2026-05-16
**Type:** Scope
**Status:** Approved
**Context:** During wireframe review, the question arose whether Courses and Certificates should remain optional or become mandatory for the MVP profile. The target users for the system are professionals with practical experience and completed courses, not complete beginners.
**Selected Option:** Courses and Certificates section is mandatory for MVP. Users must provide at least some course/certificate records for the resume generation to produce meaningful output.
**Rejected Alternatives:** Making Courses optional; removing Courses from MVP.
**Rationale:** The system is designed for users who have courses and practical experience to adapt their resumes for specific vacancies. Excluding or making Courses optional weakens the core value proposition for the target audience.
**Impact:**
- **Scope:** Courses section stays in MVP with mandatory status.
- **Requirements:** FR-006 priority stays Medium but scope confirmed MVP.
- **Data Model:** No change.
- **UI/UX:** Courses section prominently placed in profile flow.
**Follow-up Actions:** Reflect mandatory status in FR-006 notes.

### DEC-019 Two-Page Default HTML Resume Template

**Date:** 2026-05-16
**Type:** Data Model
**Status:** Approved
**Context:** The project needs a consistent resume output format. Wireframes confirmed that a two-page layout is acceptable. A standard template ensures all generated resumes have consistent visual quality and professional appearance.
**Selected Option:** All generated resumes use a standard two-page HTML template. Content is spread evenly across two pages. A separate technical approach document (Approach for Even Spreading A4) explains how sections, courses count, and work experience records are distributed to maintain visual balance.
**Rejected Alternatives:** One-page only template; dynamic page count; no template standard.
**Rationale:** A standard template ensures consistent quality, simplifies PDF generation (DEC-017), and is easier to review and maintain. Two pages provide enough space for profile sections, work experience, education, courses, and projects without feeling cramped.
**Impact:**
- **Scope:** Template approach is defined.
- **Data Model:** No change to entities.
- **Implementation:** Requires HTML template with section placeholders and content distribution logic.
**Follow-up Actions:** Create the Approach for Even Spreading A4 document with page 1 and page 2 section mapping and distribution rules.
**Follow-up Items:**
- Page 1: max 10 most relevant courses (adjusted from 7 to 10 per Resume Template Details v0.1).
- Page 2: if 5+ work experience records, max 5 courses; if fewer than 5 work experience records, max 8 courses.

### DEC-020 Generation Response Status Flow (Draft → Finalized)

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** The data model needs to distinguish between AI-generated content (draft) and user-reviewed/finalized content.
**Selected Option:** Two-phase flow with `response_status` lookup table (DRAFT, FINALIZED) and `resume_generation_response.status_id` FK. Response section tables store AI output and user edits per section.
**Rejected Alternatives:** Single in-memory draft; single flat table.
**Rationale:** Persisting the generation response allows users to revisit and resume interrupted review sessions. Section-level storage provides structured data for template rendering.
**Impact:**
- **Data Model:** New tables: `response_status`, `resume_generation_response`, `generation_response_experience`, `generation_response_education`, `generation_response_course`, `generation_response_project`, `generation_response_skill`.
- **Requirements:** Affects FR-001, FR-011.
**Follow-up Actions:** Map response section tables to Resume Review UI blocks.

### DEC-021 Contact Fields in Contact Detail

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Profile contact data should be separated from account/authentication data. `full_name`, `phone`, and `resume_email` are profile data shown on resumes.
**Selected Option:** `full_name`, `phone`, `resume_email` → `contact_detail`. `users` keeps: `username`, `email`, `password_hash`, `is_privileged`, FKs.
**Rejected Alternatives:** All in `users`; duplication.
**Rationale:** Clean separation: `users` = auth/account control; `contact_detail` = resume profile data.
**Impact:**
- **Data Model:** `users` removes full_name, phone, resume_email. `contact_detail` adds them.
**Follow-up Actions:** Update admin User Details description to JOIN contact_detail.

### DEC-022 Work Format as Junction Table

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Preferred work format is multi-select in wireframes.
**Selected Option:** `work_format` lookup + `user_work_format` junction table.
**Rejected Alternatives:** TEXT comma-separated; JSON array.
**Rationale:** Junction table follows 3NF, queryable, consistent.
**Impact:**
- **Data Model:** New tables: `work_format`, `user_work_format`. Remove `preferred_work_format` from `additional_profile_info`.
**Follow-up Actions:** Define final work format values in seed data.

### DEC-023 UI Language from Browser Locale

**Date:** 2026-05-17
**Type:** Architecture
**Status:** Approved
**Context:** Default interface language should be automatic. Capstone requires i18n with resource files for two languages.
**Selected Option:** Browser locale (Accept-Language header). User overrides via Language Switcher. Session-persisted. UI strings stored in resource files: `messages_en.properties` and `messages_ru.properties` in `src/main/resources/i18n/`. Thymeleaf uses Spring MessageSource (`#{...}`). Vue SPA uses a dedicated i18n library (e.g., vue-i18n) reading the same resource keys.
**Rejected Alternatives:** Default English; manual selection; hardcoded strings in templates.
**Rationale:** Seamless first-visit experience. Resource files provide maintainable translations. Both Thymeleaf and Vue share consistent message keys.
**Impact:**
- **Architecture:** LocaleResolver in Spring MVC. i18n resource files required.
- **Frontend:** Vue i18n library added to package.json. Language switcher in both Thymeleaf and Vue layouts.
**Follow-up Actions:** Implement LocaleChangeInterceptor. Create resource files for EN and RU. Integrate vue-i18n in SPA.

### DEC-024 AI Model Visibility and Privileged Users

**Date:** 2026-05-17
**Type:** Data Model / Security
**Status:** Approved
**Context:** Some models (paid/experimental) should not be available to all users.
**Selected Option:** `ai_model.is_hidden` + `users.is_privileged`. Hidden models visible only to privileged users. Admin sets Visibility dropdown (Visible/Hidden) and Privileged checkbox.
**Rejected Alternatives:** Role-based model access.
**Rationale:** Simple boolean flags sufficient for MVP.
**Impact:**
- **Data Model:** `ai_model.is_hidden`, `users.is_privileged`.
- **UI/UX:** AI Model Details — Visibility dropdown. Admin User Details — Privileged checkbox.
**Follow-up Actions:** Update wireframe descriptions.

### DEC-025 Resume Template Table (Post-MVP Ready)

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Future feature: user-selectable resume templates (ATS-friendly, Human-friendly).
**Selected Option:** `resume_template` table with name, html_file_path, description. Single seed record for MVP. `saved_resume.template_id` FK (nullable for MVP).
**Rejected Alternatives:** No template table; hardcoded.
**Rationale:** DDL-ready prevents migration cycle later. Single seed keeps MVP lean.
**Impact:**
- **Data Model:** New table `resume_template`. `saved_resume.template_id` FK nullable.
**Follow-up Actions:** Seed default template in MVP data.sql.

### DEC-026 LinkedIn URL Field Length

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** LinkedIn vanity URL max 100 characters after linkedin.com/in/.
**Selected Option:** `contact_detail.linkedin_url` varchar(150).
**Rejected Alternatives:** varchar(500).
**Rationale:** Business-aware field sizing.
**Impact:**
- **Data Model:** Field size change only.
**Follow-up Actions:** Add server-side validation.

### DEC-027 Post-MVP Columns in DDL

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Include Post-MVP columns now to avoid future migrations.
**Selected Option:** Include with clear docs/comments. Developer ignores in MVP DAO.
**Rejected Alternatives:** Add later via migration.
**Rationale:** Stable schema, documented intent.
**Impact:**
- **Data Model:** `work_experience.company_url`, `resume_template` table, `saved_resume.template_id` marked Post-MVP.
**Follow-up Actions:** Document Post-MVP columns.

### DEC-028 Resume Generation Response Structure

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** `saved_resume.final_content` as a single text blob violates 3NF. Reviewed content should be stored per-section for template rendering.
**Selected Option:** Structure:
- `resume_generation_response` — professional_summary, professional_aspirations, cover_letter, status_id
- `generation_response_*` tables per section (experience, education, course, project, skill)
- `saved_resume` — user_id, response_id, adaptation_level_id, language_id, title, pdf_file_path, public_code, template_id (no professional_summary/aspirations/cover_letter — those live in response table)
**Rejected Alternatives:** Single `final_content` blob; JSON columns.
**Rationale:** 3NF compliance, per-section editing, template rendering. Cover letter is AI-generated output — logically belongs with the rest of generation response, not with saved_resume metadata.
**Impact:**
- **Data Model:** Multiple new tables. `saved_resume.final_content` removed. `saved_resume.is_public` removed (all resumes are public by design).
- **Requirements:** FR-008, FR-011, FR-012.
**Follow-up Actions:** Map response tables to Resume Review UI sections.

### DEC-029 Resume Language Preferences as FK to Language Table

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Resume language preferences (default and additional resume language) were initially stored as varchar free-text fields. To ensure valid input and simplify AI model integration, dropdown selection from available languages is preferred.
**Selected Option:** Replace `additional_profile_info.default_resume_language` and `additional_profile_info.additional_resume_language` varchar fields with FKs to `language` table: `default_resume_language_id`, `additional_resume_language_id`.
**Rejected Alternatives:** Free-text varchar fields; separate junction table for user languages.
**Rationale:** FK constraint ensures only valid language values are stored. Dropdown on the frontend provides clear options. AI model receives consistent language identifiers.
**Impact:**
- **Data Model:** `additional_profile_info.default_resume_language_id integer ref: language.id`, `additional_profile_info.additional_resume_language_id integer ref: language.id`.
**Follow-up Actions:** Update frontend dropdown values to match language table seed data.

### DEC-030 Resume Content Order and Page Placement

**Date:** 2026-05-17
**Type:** Data Model
**Status:** Approved
**Context:** Generated resume content needs to distinguish between first-page and second-page placement, and maintain a fixed display order. Sort_order semantics were unclear — whether user-controlled or system-controlled.
**Selected Option:**
- Rename `sort_order` to `order_in_resume` in all `generation_response_*` tables (fixed display order, not user-reorderable).
- Add `is_first_page boolean not null default true` to `generation_response_experience` and `generation_response_course` — items on page 1 are primary; items on page 2 go to "Additional work experience" or "Additional courses" sections.
- Add `description` removed from `generation_response_education` and `generation_response_course` (not needed in final resume template — education/courses use compact format).
- Add `updated_at` to `generation_response_education`, `generation_response_project`, `generation_response_skill` for data consistency.
- Make `start_date not null` in `generation_response_experience` and `generation_response_project` (experience/project without start date has no meaning in resume).
- Add `generation_response_education.description` removed (education is summary-only in template).
**Rejected Alternatives:** User-reorderable sort_order; single page without placement distinction.
**Rationale:** Fixed order simplifies template rendering. Page placement flags give the two-page template (DEC-019) the data it needs to distribute content without complex heuristics.
**Impact:**
- **Data Model:** sort_order → order_in_* tables. New columns: `generation_response_experience.is_first_page`, `generation_response_course.is_first_page`, `generation_response_experience.start_date not null`, `generation_response_project.start_date not null`. Removed: `generation_response_education.description`, `generation_response_course.description`.
- **Requirements:** New requirement: generated work experience and course entries can be placed on page 1 (primary, more relevant) or page 2 (additional) of the resume.
**Follow-up Actions:** Add is_first_page logic to generation prompt engineering.

### DEC-031 Default Role Value for Project Participants

**Date:** 2026-05-17
**Type:** Requirement
**Status:** Approved
**Context:** The `project.role` field is optional in the database (wireframes show it as non-mandatory). However, when generating resume content, every project entry needs a role descriptor.
**Selected Option:** If user does not provide a role for a project record, the system defaults to "Participant" / "Участник" at UI/code level. Database schema remains unchanged (optional field).
**Rejected Alternatives:** Making `project.role` required in DB; not providing any default.
**Rationale:** Keeping the DB field optional respects the wireframe design. The code-level default ensures generated resumes never show an empty role field, which would look unprofessional.
**Impact:**
- **Data Model:** No change.
- **Implementation:** Service layer default value logic.
**Follow-up Actions:** Document default value rule in implementation notes.

### DEC-032 Resume Delete from User Home with Confirmation

**Date:** 2026-05-18
**Type:** UI/UX
**Status:** Approved
**Context:** Users need to delete saved resumes from User Home. The delete action must have a confirmation step to prevent accidental deletion. After deletion, the public resume link should no longer work and must return a meaningful HTTP status code.

**Selected Option:** A "Delete this resume" button is placed in the Resume Details modal on User Home. On click, the button changes to a confirmation prompt and a "Confirm deletion" button appears. After confirmation, the resume is soft-deleted (`is_deleted = true` in `saved_resume`). Public access to a deleted resume returns HTTP 410 Gone with a custom message: "Пользователь решил удалить данное резюме. Больше оно не доступно."

**Rejected Alternatives:** Hard-delete (permanent removal); delete action from table row instead of modal; no confirmation step.

**Rationale:** Soft-delete preserves data integrity and allows potential undo. The confirmation step prevents accidental deletion. The HTTP 410 Gone semantically indicates that the resource was intentionally removed, distinguishing from 404 Not Found.

**Impact:**
- **Scope:** New FR-013; adds delete action to existing modal.
- **Requirements:** FR-008 modal gains delete functionality; new FR-013 created.
- **Data Model:** `saved_resume` already has `is_deleted` and `deleted_at`. New field: `public_url_link varchar(200)` for storing ready-made public URL.
- **UI/UX:** Resume Details modal gains delete button with confirmation step.
- **Risks:** Adds new UI complexity but follows existing modal pattern.

**Follow-up Actions:** Create FR-013. Update FR-008 modal description. Add `public_url_link` to saved_resume in ERDs. Implement HTTP 410 handling for public resume endpoint.

### DEC-033 Add professional_title to resume_generation_response

**Date:** 2026-05-18
**Type:** Data Model
**Status:** Approved
**Context:** The generated resume needs a professional title adapted to the target vacancy, which may differ from the user's general `professional_title` in `contact_detail`. The AI model determines the best-fit title based on vacancy requirements.

**Selected Option:** Add `professional_title varchar(250) NOT NULL` to `resume_generation_response`. The AI generates and the user can review/edit this field as part of the generation response.

**Rejected Alternatives:** Reusing `contact_detail.professional_title` (too generic); storing only in generated content without a dedicated field.

**Rationale:** A dedicated field ensures the AI-generated title is stored separately from the user's profile title, enabling vacancy-specific professional positioning.

**Impact:**
- **Data Model:** New field `professional_title` in `resume_generation_response`.
- **Requirements:** FR-001 affected data updated.

**Follow-up Actions:** Update FR-001 affected data. Update ERDs and Data Dictionary. Update TR-003 trace notes.

### DEC-034 Backend Controls Template Rendering

**Date:** 2026-05-20
**Type:** Architecture
**Status:** Approved
**Context:** Resume HTML/PDF generation must be server-side to ensure consistent output and ATS-friendly formatting.
**Selected Option:** Java backend generates final HTML from structured response data and HTML templates, then converts to PDF. Vue does not render final PDF templates.
**Rejected Alternatives:** Client-side PDF generation in Vue.
**Rationale:** Server-side generation ensures consistent layout, selectable text, and ATS-friendly output regardless of browser.
**Impact:** Template rendering responsibility assigned to backend. Vue handles only structured editable forms.

### DEC-035 Resume Review: Vue Shows Editable Review Form Only

**Date:** 2026-05-20
**Type:** Architecture
**Status:** Approved
**Context:** During Resume Review the user needs to edit generated content before final save.
**Selected Option:** Vue shows structured editable fields (text areas, inputs). Final PDF preview is available only after save/finalize.
**Rejected Alternatives:** Live PDF preview during editing; client-side HTML rendering.
**Rationale:** Reduces frontend complexity and keeps PDF generation centralized on backend.
**Impact:** Resume Review in Vue displays editable form, not PDF preview.

### DEC-036 AI Returns Structured JSON

**Date:** 2026-05-20
**Type:** Architecture
**Status:** Approved
**Context:** AI output needs to be parseable into database entities for structured storage and template rendering.
**Selected Option:** AI returns structured JSON matching the generation response contract. Backend parses the JSON and populates `resume_generation_response` and `generation_response_*` tables directly.
**Rejected Alternatives:** Free-form AI text parsed by backend; no structured output contract.
**Rationale:** Structured JSON enables direct entity mapping from AI output, simplifies prompt-to-DB pipeline, and eliminates parsing errors from free-form text.
**Impact:** AI prompt includes JSON schema requirements. Backend validates JSON structure before storage. JSON contract is part of the AI prompt instructions.

### DEC-037 Text Fields May Contain Limited HTML in Generated Content

**Date:** 2026-05-20
**Type:** Data Model
**Status:** Approved
**Context:** AI-generated resume content needs basic formatting (bold, bullets, emphasis) for professional appearance.
**Selected Option:** AI may use approved HTML tags inside JSON text fields. Backend sanitizes using allowlist.
**Rejected Alternatives:** Plain text only; full HTML with no restrictions.
**Rationale:** Limited HTML enables professional formatting without security risk.
**Impact:** Allowlist defined: `<strong>`, `<b>`, `<i>`, `<em>`, `<ul>`, `<ol>`, `<li>`, `<p>`, `<br>`.

### DEC-038 Backend Sanitizes AI-Generated HTML

**Date:** 2026-05-20
**Type:** Security
**Status:** Approved
**Context:** AI-generated HTML may contain unsafe tags, closing tag errors, or unexpected markup.
**Selected Option:** Backend sanitizes all AI-provided HTML using an allowlist before storing or rendering.
**Rejected Alternatives:** Storing raw AI output; client-side sanitization.
**Rationale:** Prevents XSS and template breakage. Centralized control.
**Impact:** Sanitization step added to generation pipeline.

### DEC-039 Fixed Resume Section Order

**Date:** 2026-05-20
**Type:** Requirement
**Status:** Approved
**Context:** Resume sections need consistent ordering for professional appearance and ATS parsing.
**Selected Option:** Resume sections follow fixed order defined in Resume Template Details. AI adjusts content, not section order.
**Rejected Alternatives:** AI determines section order dynamically.
**Rationale:** Consistent ordering improves professional appearance and simplifies template rendering.
**Impact:** Section order enforced by template, not AI.

### DEC-040 One-Page and Two-Page Templates Both in MVP

**Date:** 2026-05-20
**Type:** Scope
**Status:** Approved
**Context:** Different user profiles have different content volumes requiring different page counts.
**Selected Option:** Both template modes included in MVP. Backend decides mode programmatically via Page Profile scoring.
**Rejected Alternatives:** Only one template mode; user chooses mode manually.
**Rationale:** Automatic mode selection provides optimal presentation without user confusion.
**Impact:** Both HTML templates are MVP. Backend selection logic is MVP.

### DEC-041 Page Profile Scoring Is Part of MVP

**Date:** 2026-05-20
**Type:** Scope
**Status:** Approved
**Context:** Content budget and template mode selection depend on Page Profile scoring.
**Selected Option:** Density scoring and content budget selection are MVP features.
**Rejected Alternatives:** Manual content budget; no scoring system.
**Rationale:** Enables automatic content distribution without user intervention.
**Impact:** Page Profile calculation service is MVP.

### DEC-042 PDF Generated Server-Side

**Date:** 2026-05-20
**Type:** Architecture
**Status:** Approved
**Context:** Final PDF must be generated reliably with consistent formatting.
**Selected Option:** HTML-to-PDF conversion on Java backend.
**Rejected Alternatives:** Client-side PDF generation; external PDF service.
**Rationale:** Centralized, stable, and independent of browser capabilities.
**Impact:** PDF generation library required on backend.

### DEC-043 PDF Page Count Is Technically Validated

**Date:** 2026-05-20
**Type:** Requirement
**Status:** Approved
**Context:** Generated PDF must match expected page count (1 or 2).
**Selected Option:** One-page template must produce exactly 1 PDF page; two-page template must produce exactly 2 pages. Validated via PDF library.
**Rejected Alternatives:** No validation; visual inspection only.
**Rationale:** Automated validation catches content overflow bugs before user sees the PDF.
**Impact:** PDF post-generation validation step added.

### DEC-044 Date of Birth Included in Personal Information

**Date:** 2026-05-20
**Type:** Requirement
**Status:** Approved
**Context:** If user provides Date of Birth, it should appear in resume Personal Information.
**Selected Option:** DOB included if provided by user. Code-determined display (no AI involvement).
**Rejected Alternatives:** Always include DOB; never include DOB.
**Rationale:** User-provided data is shown; follows same pattern as other Personal Information fields.
**Impact:** DOB added to Personal Information section fields list.

### DEC-045 Professional Aspirations Always Expected in AI Output

**Date:** 2026-05-20
**Type:** Requirement
**Status:** Approved
**Context:** Aspirations section provides career context for recruiters. Even without user input, it should appear.
**Selected Option:** AI must generate Professional Aspirations even if user input is empty.
**Rejected Alternatives:** Omit Aspirations if user left field empty.
**Rationale:** Aspirations add professional context and fill page 2 space.
**Impact:** AI prompt always requests Aspirations generation.

### DEC-046 AI May Expand Aspirations as Space Filler

**Date:** 2026-05-20
**Type:** Design
**Status:** Approved
**Context:** Page 2 may have empty space if projects and additional WE are minimal.
**Selected Option:** AI may expand Aspirations to fill available space. User reviews and edits before final save.
**Rejected Alternatives:** Fixed-length Aspirations regardless of page density.
**Rationale:** Aspirations act as a content buffer for visual balance.
**Impact:** Aspirations length varies by Page 2 Density per content budget rules.

### DEC-047 Page Navigation Notes on Two-Page Template

**Date:** 2026-05-20
**Type:** UI/UX
**Status:** Approved
**Context:** Multi-page resumes need navigation cues for readers.
**Selected Option:** "See the next page" at bottom of page 1; "See the previous page" at top of page 2.
**Rejected Alternatives:** No navigation notes; watermark-style notes.
**Rationale:** Standard professional resume pattern; low implementation cost.
**Impact:** Notes included in HTML template markup.

### DEC-048 Page 2 Additional WE Compact at High Density

**Date:** 2026-05-20
**Type:** Requirement
**Status:** Approved
**Context:** When page 2 has many additional work experiences, bullet points make it overcrowded.
**Selected Option:** At max density (7+ additional jobs), Additional WE uses one short sentence per job without bullet points.
**Rejected Alternatives:** Always use same format regardless of count.
**Rationale:** Maintains visual balance and readability.
**Impact:** Content budget rule: 6+ jobs → title, company, dates, location only.

### DEC-049 Content Budgets in External YAML Configuration

**Date:** 2026-05-20
**Type:** Architecture
**Status:** Superseded
**Superseded By:** DEC-060 (2026-05-23)
**Context:** Content budgets (sentence counts, bullet limits, skill limits) need to be adjustable without code changes.
**Selected Option:** Sentence counts, bullet limits, skill limits stored in external YAML config file.
**Rejected Alternatives:** Hardcoded values in Java code; database-stored configuration.
**Rationale:** YAML is simple to edit, version-controllable, and independent of database schema.
**Impact:** External config file created; service reads budget values at startup.

### DEC-050 Profile Picture Moved from MVP to POST-MVP

**Date:** 2026-05-20
**Type:** Scope
**Status:** Approved
**Context:** Profile picture is not supported by current HTML templates and adds unnecessary MVP complexity.
**Selected Option:** Move profile picture field from MVP to POST-MVP scope. FR-007 updated; photo_file_path deferred.
**Rejected Alternatives:** Keep optional photo in MVP; remove photo entirely.
**Rationale:** Reduces MVP complexity without losing the feature permanently. Templates can support photos post-MVP.
**Impact:** FR-007 acceptance criteria updated. photo_file_path removed from MVP scope.

### DEC-051 Add Error Handling, Code Quality, and Build NFRs

**Date:** 2026-05-21
**Type:** Process
**Status:** Approved
**Context:** Capstone specification requires comprehensive error handling across all layers, code quality standards, Javadoc documentation, Maven CLI build, and proper repository setup (.gitignore, README.md).
**Selected Option:** Add 10 new NFRs covering: custom exception hierarchy (ControllerException, ServiceException, DaoException), global `@ControllerAdvice` handler, graceful error responses without stack trace exposure, structured logging, package structure with clear DAO/Service separation, Java Code Convention, Javadoc on public methods, Maven CLI build, .gitignore and README.md, and minimal pom.xml dependencies.
**Rejected Alternatives:** Single generic "error handling" NFR; omitting Javadoc requirement; keeping frontend stack as generic "Vue.js".
**Rationale:** Clear and testable NFRs ensure Capstone compliance. Per-layer custom exceptions enable quick failure localization. Explicit DAO/Service separation follows layered architecture best practices.
**Impact:** New NFR-002 through NFR-011 in Requirements Log. Implementation scope extends to error infrastructure, code style enforcement, and build automation.
**Follow-up Actions:** Create NFR-002 through NFR-011. Link related trace rows.

### DEC-052 Use Vue 3 + Vite + PrimeVue for Frontend SPA

**Date:** 2026-05-21
**Type:** Architecture
**Status:** Approved
**Context:** The generic "Vue.js" technology placeholder needed to be resolved to a specific stack. The stack must support responsive design and cross-browser compatibility (Chrome, Firefox, Edge) per Capstone specification.
**Selected Option:** Vue 3 (Composition API) + Vite + PrimeVue. Hybrid approach maintained: Thymeleaf for Landing Page, SPA for authenticated application.
**Rejected Alternatives:** Bootstrap (CSS-only, not Vue-native); pure Vue without component library; Quasar Framework (too heavy for MVP).
**Rationale:** PrimeVue provides Vue-native responsive components with ready themes, responsive/touch-friendly elements, and comprehensive documentation. For a developer new to Vue SPA, this reduces integration overhead and frontend complexity compared to pairing Bootstrap with a separate Vue compatibility layer.
**Impact:** Technology stack updated in Strategic Context and Confirmed Elicitation Results. Cross-browser and responsive requirements are covered by PrimeVue's built-in capabilities.
**Follow-up Actions:** Update Strategic Context technology stack table. Update Confirmed Elicitation Results frontend section. Add trace rows.

### DEC-053 Add DB Layer NFRs

**Date:** 2026-05-21
**Type:** Data Model
**Status:** Approved
**Context:** Capstone specification requires: (1) Service-layer JDBC transaction management using manual `commit()/rollback()`; (2) SQL scripts (schema.sql, data.sql) for database initialization; (3) PreparedStatement for all SQL queries to prevent injection; (4) UTF-8 encoding for Cyrillic support; (5) a custom thread-safe Connection Pool with thorough documentation covering thread-safety mechanism, connection lifecycle, timeout handling, and edge cases.
**Selected Option:** Add NFR-012 (Transaction management), NFR-013 (SQL scripts), NFR-014 (PreparedStatement), NFR-015 (UTF-8), NFR-016 (Custom Connection Pool). Clarify NFR-006 with DAO-to-entity mapping.
**Rejected Alternatives:** Using Spring `@Transactional` (not allowed — must be manual JDBC); using HikariCP or Apache DBCP (not allowed — must be custom implementation); storing plain-text passwords (explicitly forbidden).
**Rationale:** These are mandatory Capstone requirements. Manual Connection Pool with thorough documentation is a Capstone differentiator and must be defensible in code review.
**Impact:** NFR-012–NFR-016 in Requirements Log. NFR-006 updated with DAO-to-entity mapping. Connection Pool requires thorough internal documentation.
**Follow-up Actions:** Create NFR-012 through NFR-016. Add RISK-014. Add trace rows.

### DEC-054 Add UI Security and Dual Validation NFRs

**Date:** 2026-05-21
**Type:** Security
**Status:** Approved
**Context:** Capstone specification requires: form resubmission prevention (F5, back button), XSS protection for user input fields, and dual validation (frontend + backend with Spring @Valid and Jakarta Validation annotations).
**Selected Option:** Add NFR-017 (Form resubmission prevention via PRG pattern and button disable), NFR-018 (User input XSS sanitization via backend sanitizer and Vue template escaping), NFR-019 (Dual validation: PrimeVue frontend + Spring @Valid with @Email, @NotNull, @NotEmpty, @Size annotations on backend).
**Rejected Alternatives:** Single-layer validation (frontend only — can be bypassed; backend only — poor UX).
**Rationale:** Dual validation provides both immediate UX feedback and authoritative server-side checks. XSS sanitization covers the gap between user input and AI output (already covered by DEC-037/038).
**Impact:** NFR-017–NFR-019 added to Requirements Log. PRG pattern affects all POST controllers. Validation annotations affect all entity DTOs.

### DEC-055 Use Vuelidate + Native Vue 3 Validation for Frontend

**Date:** 2026-05-21
**Type:** UI/UX
**Status:** Approved
**Context:** Frontend validation approach needed to be defined. PrimeVue form components have basic validation, but complex rules (cross-field validation, conditional required fields) needed a dedicated validation library.
**Selected Option:** Vuelidate library with native Vue 3 Composition API form validation. PrimeVue components handle basic display; Vuelidate handles validation rules and error states.
**Rejected Alternatives:** jQuery Validation (adds jQuery dependency to a Vue project); PrimeVue built-in validation only (limited for complex rules); no dedicated validation library.
**Rationale:** Vuelidate integrates naturally with Vue 3 Composition API (`reactive`, `computed`), avoids pulling jQuery or Bootstrap JS into the Vue SPA, and provides composable validation rules that are easy to test and maintain.
**Impact:** Frontend validation tool defined. Vuelidate added to `package.json`. NFR-019 notes updated. Confirmed Elicitation Results frontend section updated.

### DEC-056 Document 4 Design Patterns with Rationale

**Date:** 2026-05-21
**Type:** Architecture
**Status:** Approved
**Context:** Capstone requires minimum 4 design patterns with documented rationale. Spring MVC Interceptors, AOP, SOLID, and DRY are also required.
**Selected Option:** Apply and document 4 GoF patterns:
- **Singleton** — single instance of the custom Connection Pool, ensuring all DAO classes share one connection pool.
- **Builder** — `ResumePromptBuilder` constructs complex AI prompts step by step (vacancy context, profile data, content budget, language settings), returning a complete prompt string.
- **Factory Method** — `AiClientFactory` creates either a `MockAiClient` or `OpenRouterAiClient` depending on configuration/environment, isolating creation logic from the calling service.
- **Strategy** — each `AdaptationLevel` (Minimal, Balanced, Maximum) is implemented as a separate strategy class that adjusts AI prompt instructions, allowing selection at runtime.
**Rejected Alternatives:** Formal pattern documentation without code justification; fewer than 4 patterns.
**Rationale:** Each pattern solves a real architectural problem: Singleton prevents redundant Connection Pool instances; Builder handles parameter-rich prompt creation; Factory Method isolates AI provider choice; Strategy separates adaptation algorithms cleanly.
**Impact:** DEC-056 documents the pattern catalog. NFR-021 (Interceptors), NFR-022 (AOP), NFR-023 (SOLID/DRY/reusability) added. ResumePromptBuilder formalized as Builder pattern. Technology stack updated.

### DEC-057 Establish Testing Standards

**Date:** 2026-05-21
**Type:** Process
**Status:** Approved
**Context:** Capstone requires: JUnit 5, 50%+ coverage in Service and DAO layers via JaCoCo, Mockito for mocking, structured tests, positive/negative/boundary scenarios, and TDD approach.
**Selected Option:** Add NFR-024 (50%+ coverage target), NFR-025 (test scenario types), NFR-026 (structure and consistency), NFR-027 (TDD). Update NFR-009 with explicit `mvn test` requirement.
**Rejected Alternatives:** No coverage target; retroactive testing; less than 50% coverage.
**Rationale:** JUnit 5 + Mockito is the standard Capstone test stack. JaCoCo provides visual coverage reports. TDD ensures tests exist for all business logic.
**Impact:** NFR-024–NFR-027 added. JaCoCo plugin required in pom.xml. Test structure and naming standards defined.

### DEC-058 Add dev/prod Profiles, Swagger, Docker Compose

**Date:** 2026-05-21
**Type:** Architecture
**Status:** Approved
**Context:** Capstone project needs Swagger API documentation with restricted prod access, Docker Compose deployment, and separate dev/prod Spring profiles.
**Selected Option:** Add NFR-031 (Swagger/OpenAPI via springdoc-openapi; ADMIN-only access on prod via Spring Security). Add NFR-032 (Docker Compose with backend Tomcat, Vue Nginx, PostgreSQL). Update NFR-028 with dev/prod profile requirement.
**Rejected Alternatives:** No API documentation; single deployment without Docker; single application.yml for all environments.
**Rationale:** Swagger provides essential API documentation for reviewers. Docker Compose guarantees reproducible deployment. Profile separation follows standard Spring practices.
**Impact:** NFR-031, NFR-032 added. NFR-028 updated with profile requirement. Tech stack extended.

### DEC-059 Approve NFR Baseline for Development Handoff

**Date:** 2026-05-21
**Type:** Process
**Status:** Approved
**Context:** All 32 NFRs have been defined with detailed acceptance criteria. Open questions OQ-007 (OpenRouter demo) and OQ-009 (admin logging) were resolved. FR-001, FR-007, FR-011, FR-012, FR-013 acceptance criteria were finalized. The requirements baseline is ready for development handoff.
**Selected Option:** Bulk-approve all NFR-002 through NFR-032 as Approved/Ready. Set FR-001, FR-007, FR-011, FR-012, FR-013 to Approved with clarified acceptance criteria. Close OQ-007 and OQ-009.
**Key decisions confirmed:**
- Mock AI for dev; real OpenRouter for MVP demo (OQ-007).
- Admin logging: only critical actions — role change, block/unblock, forbid/allow generation (OQ-009).
- Languages: English and Russian only. Username: Latin letters, digits, hyphens only (FR-007).
- Cover letter: plain text field, no formatting rules (FR-011/FR-012).
- Resume delete: confirmation dialog text "Are you sure you want to delete this resume?" (FR-013).
**Impact:** All requirements baseline approved and ready for development handoff. Decision Log, Requirements Log, Open Questions Log updated.

### DEC-060 Use DB-Backed Resume Budget Configuration Instead of YAML

**Date:** 2026-05-23
**Type:** Architecture
**Status:** Approved
**Supersedes:** DEC-049
**Context:** The previously approved YAML-based configuration approach is less suitable for this project because YAML is developer-oriented, not admin/data-oriented; runtime changes are less convenient with YAML; the project already uses PostgreSQL and has admin-side concepts for AI models and runtime settings.
**Selected Option:** Resume budget settings stored in PostgreSQL tables. Backend reads DB config before every generation. No cache in MVP. One active config with version_no. PostgreSQL partial unique index prevents multiple active configs.
**Rejected Alternatives:** YAML-based configuration (previous approach); caching for MVP; configurable section order in DB.
**Rationale:** DB-backed configuration is easier to inspect, test, and demonstrate in a portfolio. It avoids hardcoding budget parameters in Java and allows runtime configuration changes without application restart.
**Impact:**
- **Data Model:** 4 new tables: resume_budget_configs, resume_template_selection_rules, resume_work_experience_distribution_rules, resume_section_budget_rules.
- **Resume Template Details:** Section 11 replaced, YAML examples removed, DB-backed config behavior added.
- **Requirements:** NFR-033, NFR-034 added. DEC-049 superseded.
- **ERD/Data Dictionary:** Updated with new entities and fields.
- **Governance:** Change request CR-031 created.
- **Risks:** RISK-015 added for misconfigured budget settings.

### DEC-061 Fixed Resume Section Order Stays in Backend Code

**Date:** 2026-05-23
**Type:** Requirement
**Status:** Approved
**Context:** The decision to move budget configuration to DB raised the question whether section order should also be configurable through DB.
**Selected Option:** Fixed resume section order is implemented in backend rendering code and is not configurable through DB in MVP.
**Rejected Alternatives:** Storing section order in DB; admin panel for section order configuration.
**Rationale:** Section order is fixed and not configurable. There is no requirement to configure section order through admin panel. Storing it in DB adds unnecessary complexity. Section order belongs to template rendering logic, not runtime budget configuration.
**Impact:** Section order explicitly excluded from DB-backed budget configuration scope. Documented in Resume Template Details.

### DEC-062 Use PostgreSQL Partial Unique Index to Protect One Active Config

**Date:** 2026-05-23
**Type:** Data Model
**Status:** Approved
**Context:** Even though backend has fallback logic for multiple active configs, database-level protection is recommended to prevent data integrity issues.
**Selected Option:** Add PostgreSQL partial unique index in Flyway migration: `CREATE UNIQUE INDEX uq_one_active_resume_budget_config ON resume_budget_configs (is_active) WHERE is_active = true;`
**Rejected Alternatives:** Application-level enforcement only; no enforcement.
**Rationale:** Database-level constraint prevents multiple active configs regardless of how data is modified (direct SQL, application, future admin tools). Partial unique index is PostgreSQL-specific and cannot be fully expressed in DBML.
**Impact:** Migration SQL includes partial unique index. DBML notes document the constraint. Data Dictionary mentions it as migration-level constraint.

### DEC-063 Support Bilingual Generation with Language-Specific Responses

**Date:** 2026-06-10  
**Type:** Data Model  
**Status:** Approved  
**Context:** Prototype testing showed that generating English and Russian resumes through two independent AI requests can produce inconsistent wording, emphasis, and section meaning for the same candidate profile. The product needs consistent bilingual output while still allowing users to generate only one language when needed.  
**Selected Option:** Use a single `resume_generation_request` with `language_mode` to represent the user's language choice. Supported values are `English only`, `Russian only`, and `Bilingual`. Move actual generated language to `resume_generation_response.language_id`. A single-language request creates one response row. A bilingual request creates two response rows: one English and one Russian.  
**Rejected Alternatives:** Keep two independent requests for EN/RU; generate one language and then translate it outside the response contract; store two languages in one wide response row; keep `language_id` only on `resume_generation_request`.  
**Rationale:** Language mode belongs to the generation request because it describes what the user asked the system to produce. Actual language belongs to each generated response because generated content is language-specific. This preserves 3NF and allows one request to produce one or two normalized response rows. A bilingual AI response can also enforce semantic consistency between EN and RU versions while still allowing natural localization rather than literal translation.  
**Impact:**  
- `resume_generation_request.language_id` is removed.  
- `resume_generation_request.language_mode` uses controlled values: `English only`, `Russian only`, `Bilingual`.  
- `resume_generation_response.language_id` is added.  
- `resume_generation_response.generation_request_id` is no longer unique by itself.  
- Unique constraint becomes (`generation_request_id`, `language_id`).  
- `resume_generation_request` to `resume_generation_response` relationship changes from 1:1 to 1:N.  
- `resume_generation_response.value_line` is added for language-specific AI positioning text.  
- `ai_usage_log.generation_response_id` is removed because one bilingual API call may produce two responses.  
- `ai_usage_log_response` junction table is added to map one API call to one or more generated responses.  
- ERD, Data Dictionary, Mermaid ERD, PlantUML ERD, prompt contract, and generation flow documentation must be updated.  
**Follow-up Actions:** Update Flyway migrations, Java DAOs/services, response parsing, prompt selection, Resume Review flow, and traceability artifacts to support single-language and bilingual generation modes.

### DEC-064 Use Versioned Modular AI Prompt Configuration

**Date:** 2026-06-11  
**Type:** Architecture  
**Status:** Approved  
**Context:** The resume generation feature needs different prompt behavior depending on language mode, adaptation selection, and cover letter option. A simple table with one full request prompt per combination would require 16 active prompt records at once: single-language/bilingual × minimal/balanced/maximum/all × cover-letter enabled/disabled. This approach would duplicate prompt text, make updates error-prone, and increase the risk of inconsistent AI behavior between scenarios.  
**Selected Option:** Store AI prompts as a versioned modular prompt configuration bundle. Add an active `ai_prompt_config` as the parent prompt set. Store stable global rules in `ai_system_prompt`. Store request prompt fragments separately by language mode, adaptation selection, and cover letter flag using `ai_request_prompt_language`, `ai_request_prompt_adaptation`, and `ai_request_prompt_cover_letter`. Backend assembles the final prompt at runtime from the active config and the current `resume_generation_request` settings. Store the rendered prompt in `ai_prompt_render_log` for debugging and reproducibility.  
**Rejected Alternatives:** Store 16 full request prompts for every language/adaptation/cover-letter combination; keep prompt text fully hardcoded in Java; store only one generic prompt without configuration fragments; add `is_active` flags to every prompt fragment table without a parent config bundle.  
**Rationale:** Modular prompt fragments avoid duplicated instructions and make prompt updates safer. Language-specific, adaptation-specific, and cover-letter-specific instructions are independent configuration concerns and should not be repeated across 16 full prompt rows. A parent prompt config allows the system to switch the whole prompt set as one versioned bundle, reducing inconsistent active states. The design supports 3NF because each prompt fragment table stores one type of prompt rule, while `ai_prompt_config` controls the active version. Rendered prompt logging provides traceability for AI output debugging, QA, and future comparison of prompt versions.  
**Impact:**  
- New AI prompt configuration section added to the Generation group.  
- `ai_prompt_config` added as the versioned parent prompt set.  
- `ai_system_prompt` added for stable system-level model instructions.  
- `ai_request_prompt_language` added for `ENGLISH_ONLY`, `RUSSIAN_ONLY`, and `BILINGUAL` language-mode prompt fragments.  
- `ai_request_prompt_adaptation` added for `MINIMAL`, `BALANCED`, `MAXIMUM`, and `ALL` adaptation-selection prompt fragments.  
- `ai_request_prompt_cover_letter` added for cover-letter enabled/disabled prompt fragments.  
- `ai_prompt_render_log` added to store rendered system/request prompts and optional payload/hash for debugging and reproducibility.  
- `resume_generation_request` should reference the prompt config used for the request.  
- Backend prompt generation should use a Prompt Builder approach: system prompt + language fragment + adaptation fragment + cover-letter fragment + dynamic profile/vacancy/budget payload.  
- PostgreSQL partial unique index should enforce only one active prompt config at a time.  
- ERD, Data Dictionary, Mermaid ERD, PlantUML ERD, Flyway migrations, prompt prototype, and Java generation service documentation must be updated.  
**Follow-up Actions:** Update DBML ERD, Data Dictionary, Mermaid ERD, PlantUML ERD, Flyway migrations, prompt builder logic, Python backend prototype, and OpenCode implementation brief to use the modular prompt configuration model.

### DEC-065 Store System Prompt Separately

**Date:** 2026-06-11
**Type:** Architecture  
**Status:** Approved  
**Context:** Resume generation requires stable global AI instructions that do not depend on user-selected generation settings. These instructions include the model role, JSON-only response rule, hallucination prevention, sourceId preservation, allowed HTML rules, factuality constraints, and general output quality requirements. Keeping these rules inside every request prompt fragment would duplicate text and increase the risk of inconsistent behavior.  
**Selected Option:** Store the stable system-level prompt in a separate `ai_system_prompt` table linked to `ai_prompt_config`. Each active prompt config should have exactly one system prompt. The backend loads this prompt as the system message before assembling the request-specific prompt fragments.  
**Rejected Alternatives:** Keep the system prompt hardcoded in Java; duplicate the same system instructions inside every request prompt; store one large monolithic prompt for every language/adaptation/cover-letter combination.  
**Rationale:** System-level model behavior is independent from language mode, adaptation selection, and cover-letter settings. Storing it separately avoids duplication and keeps the prompt configuration easier to maintain. It also makes it clearer which rules apply globally to all generation scenarios.  
**Impact:**  
- `ai_system_prompt` added to the AI Prompt Configs section.  
- `ai_system_prompt.prompt_config_id` links the system prompt to a specific prompt bundle.  
- Backend Prompt Builder must load the system prompt from the active `ai_prompt_config`.  
- A unique constraint should ensure one system prompt per prompt config.  
- Prompt updates that affect global model behavior can be managed without editing language/adaptation/cover-letter fragments.  
**Follow-up Actions:** Update DBML ERD, Data Dictionary, Flyway migration, Python prototype, and Java Prompt Builder logic to load system prompt from the database.

### DEC-066 Store Language-Specific Prompt Fragments

**Date:** 2026-06-11 
**Type:** Architecture  
**Status:** Approved  
**Context:** Resume generation supports three language modes: English-only, Russian-only, and bilingual. Each mode requires different response rules and JSON contract instructions. Bilingual generation also requires consistency between English and Russian versions while avoiding literal translation. Duplicating language instructions across full request prompts would make prompt updates error-prone.  
**Selected Option:** Store language-mode-specific prompt fragments in `ai_request_prompt_language`, linked to `ai_prompt_config`. The table stores one prompt fragment per language mode: `ENGLISH_ONLY`, `RUSSIAN_ONLY`, and `BILINGUAL`. Backend selects the correct fragment based on `resume_generation_request.language_mode`.  
**Rejected Alternatives:** Use a boolean `bilingual` flag instead of explicit language mode; store language instructions inside 16 full prompt combinations; hardcode language instructions in Java; use one generic language prompt for all modes.  
**Rationale:** `language_mode` is more expressive than a boolean flag because single-language generation still needs to distinguish English-only from Russian-only. Separating language prompt fragments avoids duplication and makes bilingual response behavior easier to update. This also aligns with the data model where `resume_generation_request.language_mode` controls requested generation mode and `resume_generation_response.language_id` stores actual response language.  
**Impact:**  
- `ai_request_prompt_language` added to the AI Prompt Configs section.  
- Expected rows per prompt config: `ENGLISH_ONLY`, `RUSSIAN_ONLY`, `BILINGUAL`.  
- Backend Prompt Builder must select the language fragment by `prompt_config_id` and `language_mode`.  
- Bilingual prompt fragment must describe one-call bilingual JSON generation and semantic consistency between EN/RU outputs.  
- A unique constraint should enforce one language fragment per language mode per prompt config.  
**Follow-up Actions:** Update ERD, Data Dictionary, prompt seed data, Python prototype, and Java Prompt Builder to use language-specific prompt fragments.

### DEC-067 Store Adaptation-Specific Prompt Fragments

**Date:** 2026-06-11
**Type:** Architecture  
**Status:** Approved  
**Context:** The resume generation feature supports several adaptation selections: minimal, balanced, maximum, and all levels. Each selection requires different prompt behavior. Minimal adaptation should preserve more of the original profile wording, balanced adaptation should tailor content without overfitting, maximum adaptation should strongly align the resume with the vacancy, and all-level generation should return multiple variants for user review.  
**Selected Option:** Store adaptation-specific prompt fragments in `ai_request_prompt_adaptation`, linked to `ai_prompt_config`. The table stores one prompt fragment per adaptation selection: `MINIMAL`, `BALANCED`, `MAXIMUM`, and `ALL`. Backend selects the correct fragment based on the generation request settings.  
**Rejected Alternatives:** Hardcode adaptation behavior in Java; store adaptation instructions inside full request prompt combinations; treat `ALL` as a final resume adaptation level; use one generic adaptation prompt for all cases.  
**Rationale:** Adaptation behavior is independent from language mode and cover-letter settings, so it should be stored as a separate prompt fragment. This prevents duplicated adaptation instructions across multiple prompt combinations and makes it easier to refine the generation behavior of one level without affecting others. `ALL` is treated as a generation selection, not as a final saved resume level, because the user later selects one final level for PDF generation.  
**Impact:**  
- `ai_request_prompt_adaptation` added to the AI Prompt Configs section.  
- Expected rows per prompt config: `MINIMAL`, `BALANCED`, `MAXIMUM`, `ALL`.  
- Backend Prompt Builder must select the adaptation fragment by `prompt_config_id` and adaptation selection.  
- `ALL` prompt fragment must instruct the AI to return minimal, balanced, and maximum variants.  
- Final saved resume should still use one selected adaptation level, not `ALL`.  
- A unique constraint should enforce one adaptation fragment per adaptation selection per prompt config.  
**Follow-up Actions:** Update DBML ERD, Data Dictionary, seed data, Python prototype, Java Prompt Builder, and review/export flow documentation to distinguish adaptation selection from final saved adaptation level.

### DEC-068 Store Cover-Letter Prompt Fragments for Enabled/Disabled Mode

**Date:** 2026-06-11
**Type:** Architecture  
**Status:** Approved  
**Context:** Cover letter generation is optional. If the user enables it, the AI must generate editable cover-letter content. If the user disables it, the AI must not generate cover-letter text. Simply omitting a cover-letter prompt fragment when disabled may be risky because the base JSON schema or model behavior could still produce a cover letter.  
**Selected Option:** Store cover-letter-specific prompt fragments in `ai_request_prompt_cover_letter`, linked to `ai_prompt_config`. The table stores two prompt fragments per config: one for `include_cover_letter = true` and one for `include_cover_letter = false`. Backend selects the correct fragment based on the request setting.  
**Rejected Alternatives:** Store only one cover-letter prompt for enabled mode and omit the fragment when disabled; hardcode cover-letter behavior in Java; duplicate cover-letter instructions across all full request prompt combinations.  
**Rationale:** Explicit true/false prompt fragments make the desired AI behavior clear in both cases. The disabled-mode fragment can explicitly instruct the model to return `coverLetter = null` or omit cover-letter content according to the response contract. This reduces accidental cover-letter generation and keeps cover-letter behavior independently maintainable.  
**Impact:**  
- `ai_request_prompt_cover_letter` added to the AI Prompt Configs section.  
- Expected rows per prompt config: `include_cover_letter = true` and `include_cover_letter = false`.  
- Backend Prompt Builder must select the fragment by `prompt_config_id` and request cover-letter flag.  
- Cover-letter output remains stored in `resume_generation_response.cover_letter` when generated.  
- A unique constraint should enforce one cover-letter fragment per true/false mode per prompt config.  
**Follow-up Actions:** Update ERD, Data Dictionary, prompt seed data, Python prototype, Java Prompt Builder, and generation response validation rules to explicitly handle cover-letter enabled and disabled modes.

### DEC-069 Store Rendered Prompt Log for Debugging and Reproducibility

**Date:** 2026-06-10  
**Type:** Architecture  
**Status:** Approved  
**Context:** AI-generated resume output can vary depending on prompt configuration, user profile data, vacancy text, budget rules, language mode, adaptation selection, and cover-letter settings. For debugging and QA, it must be possible to inspect exactly what was sent to the AI model for a specific generation request. Without rendered prompt logging, it is difficult to reproduce output issues or compare results after prompt updates.  
**Selected Option:** Add `ai_prompt_render_log` to store the rendered system prompt, rendered request prompt, optional profile/vacancy payload JSON, and optional prompt hash for each generation request. The log links to both `resume_generation_request` and `ai_prompt_config`.  
**Rejected Alternatives:** Store only prompt fragment IDs without rendered text; rely on application logs; store rendered prompts inside `resume_generation_request`; do not store prompt rendering details.  
**Rationale:** Rendered prompt logging provides strong traceability and makes AI behavior easier to debug. It helps answer why a specific resume was generated in a certain way and which prompt configuration was used. Keeping rendered prompt data in a separate table prevents the generation request table from becoming overloaded and preserves separation between request metadata and diagnostic prompt details. Access to this table should be restricted because rendered prompts may contain user profile and vacancy data.  
**Impact:**  
- `ai_prompt_render_log` added to the AI Prompt Configs section.  
- Render log links to `resume_generation_request` and `ai_prompt_config`.  
- Backend Prompt Builder should save rendered prompts before or during the AI call.  
- Optional `prompt_hash` can support prompt comparison and reproducibility checks.  
- Optional `profile_payload_json` can support debugging of payload construction issues.  
- Sensitive prompt/payload data must not be exposed to normal users and must not be logged in plaintext application logs.  
**Follow-up Actions:** Update DBML ERD, Data Dictionary, Flyway migration, Python prototype, Java Prompt Builder, admin/debugging documentation, and security notes for restricted access to rendered prompt data.

### DEC-070 Store Bilingual Education Profile Fields

**Date:** 2026-06-12  
**Type:** Data Model  
**Status:** Approved  
**Context:** Education records are profile-owned data and are not generated or adapted by the AI response. Prototype testing showed that if Education is entered only in one language, the same text appears in both Russian and English resumes.  
**Selected Option:** Store mandatory bilingual education fields in the profile `education` table: `institution_name_ru`, `institution_name_en`, `degree_ru`, `degree_en`, `field_of_study_ru`, and `field_of_study_en`. Resume rendering selects the correct language-specific fields based on `resume_generation_response.language_id`.  
**Rejected Alternatives:** Let AI translate education during generation; keep one-language education fields; add Education back to AI Review as generated response data.  
**Rationale:** Education is factual profile data and should remain stable, not rewritten by AI. Bilingual profile fields preserve correctness, reduce hallucination risk, and keep the Review page focused on AI-generated content.  
**Impact:** Updates `education` table, My Profile Education form, validation rules, profile API DTOs, rendering logic, Data Dictionary, ERDs, and frontend profile UI.  
**Follow-up Actions:** Update Flyway migrations, Java DAO/DTO/service validation, Vue profile form, seed data, and resume template renderer.

### DEC-071 Add Generated Personal Information Response Table

**Date:** 2026-06-12  
**Type:** Data Model  
**Status:** Approved  
**Context:** Personal Information uses profile values such as location, languages, relocation readiness, business trip readiness, citizenship, and date of birth. Some of these values require localized resume-ready wording, and users must be able to edit them before final PDF generation.  
**Selected Option:** Add `generation_response_personal` as a 1:1 child table of `resume_generation_response`. AI returns localized Personal Information per language/adaptation variant, backend stores it, and Resume Review displays it as editable fields.  
**Rejected Alternatives:** Render Personal Information directly from profile only; make profile fields bilingual for all personal info; store personal info as JSON inside `resume_generation_response`.  
**Rationale:** The table keeps normalized structured data, supports bilingual/adaptation-specific text, and provides a clean editable Review state before final save.  
**Impact:** Adds new generation response table, AI JSON contract section, response parser/persistence changes, Review UI tab, template renderer changes, ERD/Data Dictionary updates.  
**Follow-up Actions:** Update prompt config, OpenRouter parsing, Java DAOs/services, Vue Review flow, and template markers.

### DEC-072 Add Personal Information Tab to Resume Review

**Date:** 2026-06-12  
**Type:** UI/UX  
**Status:** Approved  
**Context:** Resume Review previously excluded Personal Information, but prototype testing showed that localized values must be reviewed and edited before final PDF output.  
**Selected Option:** Add `Personal Information` / `Личная информация` as the last internal tab on `/generate/review`, after Skills. The tab follows the same language/adaptation layout as other Review sections. Education remains outside Review because it is profile-owned bilingual data. Hide `Default resume language` and `Additional resume language` fields from My Profile Additional Info UI for MVP.  
**Rejected Alternatives:** Keep Personal Information uneditable; put Personal Information inside Skills; move Education back into Review.  
**Rationale:** Personal Information is short but visible in final resume. A dedicated Review tab keeps the flow explicit and avoids mixing profile settings with generated resume content.  
**Impact:** Updates Review tabs, i18n labels, frontend mock data, generated response model, and OpenDesign/OpenCode handoff.  
**Follow-up Actions:** Update frontend prototype, Review form components, i18n files, and API contracts.

### DEC-073 Store Generated HTML and PDF Artifacts Under User/Code Folders

**Date:** 2026-06-12  
**Type:** Architecture  
**Status:** Approved  
**Context:** Backend rendering produces filled HTML before server-side PDF conversion. The generated HTML should be persisted for traceability, debugging, future HTML download, and reliable PDF generation.  
**Selected Option:** Store generated artifacts under `generated_results/{username}/{public_code}/`. Each saved resume language version gets its own `public_code` and folder. The folder contains the filled HTML and, after Java implementation, the generated PDF.  
**Rejected Alternatives:** Store only PDF; store all outputs in one flat folder; use request ID only; keep HTML temporary and delete it after PDF conversion.  
**Rationale:** User/code folders are easy to inspect, align with public URL identity, support bilingual outputs, and keep HTML as the canonical intermediate artifact before PDF conversion.  
**Impact:** Adds/uses `saved_resume.html_file_path`, keeps `saved_resume.pdf_file_path`, updates finalization service, export actions, storage structure, and future PDF converter integration.  
**Follow-up Actions:** Implement Java file storage service, safe filename handling, public route validation, HTML download action, and real HTML-to-PDF conversion.


***

*This decision log follows the Information Management Plan structure and conventions for the ResumAIner project. Decisions are recorded with full context for auditability and reuse.*