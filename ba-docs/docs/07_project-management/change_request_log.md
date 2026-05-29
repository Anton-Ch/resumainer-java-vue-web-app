# Change Request Log

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-10  
**Last Updated:** 2026-05-23  
**Author:** Anton  
**Version:** 16.0  
**Status:** Active  
**Related BABOK Area:** 3.3 Plan Business Analysis Governance  

---

## 1. Description

This document tracks non-trivial **changes** to requirements, scope, architecture, data model, UI/UX, deployment, security, and project documentation.

It helps keep the project baseline controlled and explains why meaningful changes were introduced.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Use this log for meaningful changes only.
- Do not use this log for typos, small formatting edits, or minor wording improvements.
- Each change request must have a unique ID: `CR-001`, `CR-002`, `CR-003`.
- If a change is approved, update affected artifacts and mark the request as `Implemented` or `Closed`.
- If a change is valuable but not suitable for MVP, use `Postponed`.
- If a change affects a major project decision, create or update a Decision Log entry.

### 2.2 Change Type Values

| Value | Meaning | When to Use |
|---|---|---|
| Scope | Change affects MVP, stretch goals, post-MVP, or future scope | Adding/removing feature from MVP |
| Requirement | Change affects FR/NFR, user story, or acceptance criteria | Changing behavior or validation |
| Architecture | Change affects layers, frameworks, integrations, or system structure | Switching backend/frontend approach |
| Data Model | Change affects entities, tables, fields, or relationships | Adding table or field |
| UI/UX | Change affects screens, navigation, workflow, or layout | Changing resume review flow |
| Deployment | Change affects Docker, VPS, domain, server, or environment | Adding Flyway container |
| Security | Change affects auth, roles, permissions, secrets, or public access | Changing public link behavior |
| Process | Change affects BA workflow, governance, traceability, or planning | Adding readiness checklist |
| Documentation | Change affects documentation structure or repository organization | Updating repo paths |

### 2.3 Impact Values

| Value | Meaning                                                          | When to Use |
|-------|------------------------------------------------------------------|-------------|
| Low   | Documentation only or small isolated change                      | No major rework |
| Medium| Affects one area or several related artifacts                    | Moderate rework |
| High  | Affects multiple areas, implementation, or review strategy       | Significant rework |
| Critical| Affects core architecture, MVP viability, or capstone compliance | Must be handled urgently |

### 2.4 Decision Values

| Value | Meaning |
|-------|---------|
| Pending | Not yet decided |
| Approved | Accepted for implementation |
| Rejected | Not accepted |
| Postponed | Moved to later phase |
| Needs More Info | Cannot be decided without clarification |

### 2.5 Status Values

| Value | Meaning |
|-------|---------|
| Draft | Request is captured but not ready for review |
| Open | Request is ready for review |
| Approved | Request is approved but not implemented |
| Implemented | Change was applied |
| Closed | Change is complete and needs no further action |
| Rejected | Request was rejected |
| Postponed | Request was moved to later phase |

## 3. Summary Table

| CR ID  | Date       | Title                                                      | Type          | Requester   | Affected Area                       | Impact                     | Decision | Status      |
| ------ | ---------- | ---------------------------------------------------------- | ------------- | ----------- | ----------------------------------- | -------------------------- | -------- | ----------- |
| CR-001 | 2026-05-10 | Update repository structure in planning documents          | Documentation | BA          | Information Management, Governance  | Low                        | Approved | Implemented |
| CR-002 | 2026-05-11 | Update UI/UX terminology from Dashboard to Home            | UI/UX         | BA          | Elicitation, sitemap, UI docs       | Medium                     | Approved | Implemented |
| CR-003 | 2026-05-11 | Remove separate User Settings page from MVP                | Scope         | BA          | My Profile, navigation              | Medium                     | Approved | Implemented |
| CR-004 | 2026-05-11 | Add Resume Details page to user flow                       | Scope         | BA          | Resume flow, details page           | Medium                     | Approved | Implemented |
| CR-005 | 2026-05-11 | Refine admin page map                                      | Scope         | BA          | Admin pages                         | Medium                     | Approved | Implemented |
| CR-006 | 2026-05-11 | Change public resume behavior to direct PDF opening        | UI/UX         | BA          | Public access, PDF delivery         | Medium                     | Approved | Implemented |
| CR-007 | 2026-05-12 | Integrate resume listing into User Home                    | Scope         | BA          | User Home, navigation               | Medium                     | Approved | Implemented |
| CR-008 | 2026-05-12 | Update downstream UI requirements from elicitation results | Requirement   | BA          | UI/UX requirements, sitemap, flows  | Medium                     | Approved | Implemented |
| CR-009 | 2026-05-13 | Apply wireframe field findings to My Profile               | UI/UX         | BA          | My Profile, profile data fields     | Medium                     | Approved | Implemented |
| CR-010 | 2026-05-13 | Update Generate Resume fields from wireframe findings      | UI/UX         | BA          | Generate Resume, generation request | Medium                     | Approved | Implemented |
| CR-011 | 2026-05-13 | Replace full API key visibility with masked key handling   | Security      | BA          | AI Model Details, logging, admin UI | High                       | Approved | Implemented |
| CR-012 | 2026-05-13 | Clean validation inconsistencies from wireframe notes      | Requirement   | BA          | Validation rules, error messages    | Medium                     | Approved | Implemented |
| CR-013 | 2026-05-15 | Remove Resume Details pages (user and admin)                | Scope         | BA          | User and admin page maps, FR-009    | Medium                     | Approved | Implemented |
| CR-014 | 2026-05-16 | Replace PDF column with Details modal on User Home         | UI/UX         | BA          | User Home table, Resume Details modal, FR-008 | Medium                | Approved | Draft       |
| CR-015 | 2026-05-16 | Add Cover Letter generation and editing to MVP             | Scope         | BA          | Generate Resume, Resume Review, FR-001, new FR-011, FR-012 | Medium    | Approved | Draft       |
| CR-016 | 2026-05-16 | Expand Additional Info fields in My Profile                | Requirement   | BA          | My Profile, FR-007, Wireframe Field Requirements | Low                  | Approved | Draft       |
| CR-017 | 2026-05-18 | Add resume delete from User Home and public_url_link field | Requirement   | BA          | Requirements Log, ERD, Data Dictionary, Traceability Matrix, Risk Register, Decision Log | Medium | Approved | Implemented |
| CR-018 | 2026-05-18 | Add professional_title to resume_generation_response       | Requirement   | BA          | Requirements Log, Decision Log, ERD, Data Dictionary, Traceability Matrix | Low | Approved | Draft       |
| CR-019 | 2026-05-20 | Move profile picture from MVP to POST-MVP                  | Scope         | BA          | Requirements Log (FR-007), Wireframe Field Requirements, Decision Log | Low | Approved | Draft       |
| CR-020 | 2026-05-21 | Add error handling, custom exceptions, and logging NFRs    | Requirement   | BA          | Requirements Log, Decision Log, Risk Register, Traceability Matrix    | Medium | Approved | Draft       |
| CR-021 | 2026-05-21 | Add code quality, package structure, and build NFRs        | Requirement   | BA          | Requirements Log, Decision Log, Strategic Context, Traceability Matrix | Medium | Approved | Draft       |
| CR-022 | 2026-05-21 | Define frontend stack as Vue 3 + Vite + PrimeVue           | Architecture  | BA          | Decision Log, Strategic Context, Confirmed Elicitation Results, Traceability Matrix | Medium | Approved | Draft       |
| CR-023 | 2026-05-21 | Add DB layer NFRs: transactions, SQL scripts, connection pool, PreparedStatement, UTF-8 | Requirement | BA | Requirements Log, Decision Log, Strategic Context, Traceability Matrix, Risk Register | Medium | Approved | Draft |
| CR-024 | 2026-05-21 | Add UI security and dual validation NFRs | Requirement | BA | Requirements Log, Decision Log, Traceability Matrix | Medium | Approved | Draft |
| CR-025 | 2026-05-21 | Refine logging stack, add consistent logging NFR, add Vuelidate | Requirement | BA | Requirements Log, Decision Log, Strategic Context, Confirmed Elicitation Results | Low | Approved | Draft |
| CR-026 | 2026-05-21 | Document design patterns, add interceptors, AOP, SOLID NFRs | Requirement | BA | Requirements Log, Decision Log, Strategic Context, Traceability Matrix, Resume Template Details | Medium | Approved | Draft |
| CR-027 | 2026-05-21 | Add testing NFRs: coverage, scenarios, structure, TDD | Requirement | BA | Requirements Log, Decision Log, Strategic Context, Traceability Matrix | Medium | Approved | Draft |
| CR-028 | 2026-05-21 | Add external configuration and Javadoc style reference | Requirement | BA | Requirements Log, Decision Log | Low | Approved | Draft |
| CR-029 | 2026-05-21 | Add pagination NFR and i18n resource file requirements | Requirement | BA | Requirements Log, Decision Log, Confirmed Elicitation Results | Medium | Approved | Draft |
| CR-030 | 2026-05-21 | Add dev/prod profiles, Swagger, Docker Compose NFRs | Requirement | BA | Requirements Log, Decision Log, Strategic Context | Medium | Approved | Draft |
| CR-031 | 2026-05-23 | Replace YAML budget configuration with DB-backed config | Architecture | Project Owner | Resume Template Details, DBML ERD, Data Dictionary, Mermaid ERD, PlantUML ERD, Requirements Log, Decision Log, Traceability Matrix, Risk Register | Medium | Approved | Implemented |

## 4. Details

### CR-001 Update Repository Structure in Planning Documents

**Date:** 2026-05-10  
**Type:** Documentation  
**Requester:** Business Analyst  
**Status:** Implemented  
**Description:** Update planning artifacts to match the actual BA repository structure.  
**Reason:** Earlier drafts used placeholder paths.  
**Affected Artifacts:** `information_management_plan.md`, `governance_plan.md`  
**Impact Assessment:** Low. Documentation only.  
**Decision:** Approved  
**Resolution Date:** 2026-05-10  
**Follow-up Actions:** Keep future artifacts aligned with repository structure.

### CR-002 Update UI/UX Terminology from Dashboard to Home

**Date:** 2026-05-11  
**Type:** UI/UX  
**Status:** Implemented  
**Description:** Replace Dashboard terminology with `User Home` and `Admin Home`.  
**Reason:** Home page wording is clearer for main post-login pages.  
**Affected Artifacts:** `confirmed_elicitation_results.md`, `sitemap.md`, wireframes, UI docs  
**Impact Assessment:** Medium. Affects terminology across artifacts.  
**Decision:** Approved  
**Resolution Date:** 2026-05-11
**Follow-up Actions:** Ensure all future documents use "User Home" and "Admin Home" terminology consistently.
### CR-003 Remove Separate User Settings Page from MVP

**Date:** 2026-05-11  
**Type:** Scope  
**Status:** Implemented  
**Description:** Move settings into My Profile.  
**Reason:** Reduces page count and navigation complexity.  
**Affected Artifacts:** My Profile, sitemap, user flow  
**Impact Assessment:** Medium. Simplifies navigation.  
**Decision:** Approved  
**Resolution Date:** 2026-05-11
**Follow-up Actions:** Implement My Profile with integrated settings tabs/sections for interface language, resume language preferences, account settings, etc.
### CR-004 Add Resume Details Page to User Flow

**Date:** 2026-05-11  
**Type:** Scope  
**Status:** Implemented  
**Description:** Add Resume Details page for selected saved resume.  
**Reason:** Users need a focused place to view PDF, download it, and copy recruiter link.  
**Affected Artifacts:** Resume flow, sitemap, wireframes  
**Impact Assessment:** Medium. Adds one necessary user page.  
**Decision:** Approved  
**Resolution Date:** 2026-05-11
**Follow-up Actions:** Implement Resume Details page with PDF preview/download, public link copying, and navigation back to history.
### CR-005 Refine Admin Page Map

**Date:** 2026-05-11  
**Type:** Scope  
**Status:** Implemented  
**Description:** Confirm admin pages: Admin Home, Users, User Details, Resumes, Resume Details, AI Models, AI Model Details.  
**Reason:** Admin needs management pages for users, resumes, usage, and AI models.  
**Affected Artifacts:** Admin sitemap, admin wireframes, UI requirements  
**Impact Assessment:** Medium. Expands admin scope but supports required oversight.  
**Decision:** Approved  
**Resolution Date:** 2026-05-11
**Follow-up Actions:** Implement all admin pages: Admin Home, Users, User Details, Resumes, Resume Details (admin), AI Models, AI Model Details.
### CR-006 Change Public Resume Behavior to Direct PDF Opening

**Date:** 2026-05-11  
**Type:** UI/UX  
**Status:** Implemented  
**Description:** Public recruiter link opens PDF directly.  
**Reason:** Recruiters need fast viewing, printing, text copying, and saving.  
**Affected Artifacts:** Public access flow, PDF delivery, Resume Details  
**Impact Assessment:** Medium. Simplifies recruiter flow.  
**Decision:** Approved  
**Resolution Date:** 2026-05-11
**Follow-up Actions:** Implement public URL routing to serve PDF files directly with appropriate caching and access controls.
### CR-007 Integrate Resume Listing into User Home

**Date:** 2026-05-12  
**Type:** Scope  
**Status:** Implemented  
**Description:** Remove separate Resume History page and integrate resume listing table into User Home.  
**Reason:** Faster access and fewer navigation steps.  
**Affected Artifacts:** User Home, navigation, sitemap, wireframes  
**Impact Assessment:** Medium. Simplifies page map.  
**Decision:** Approved  
**Resolution Date:** 2026-05-12
**Follow-up Actions:** Implement User Home with integrated searchable/sortable table with filter of user's resumes including PDF download and details access.
### CR-008 Update Downstream UI Requirements from Elicitation Results

**Date:** 2026-05-12  
**Type:** Requirement  
**Status:** Implemented  
**Description:** Align UI/UX requirements, sitemap, and flows with confirmed elicitation decisions.  
**Reason:** Prevents contradictions between elicitation and design artifacts.  
**Affected Artifacts:** UI/UX requirements, sitemap, user flows, wireframes  
**Impact Assessment:** Medium. Improves consistency.  
**Decision:** Approved  
**Resolution Date:** 2026-05-12
**Follow-up Actions:** Use updated requirements as basis for wireframe creation and UI implementation.
### CR-009 Apply Wireframe Field Findings to My Profile

**Date:** 2026-05-13  
**Type:** UI/UX  
**Status:** Implemented  
**Description:** Add concrete fields, validation, and section structure from wireframe preparation to My Profile documentation.  
**Reason:** Wireframes clarified actual fields needed for profile data entry.  
**Affected Artifacts:** `confirmed_elicitation_results.md`, `wireframe_field_requirements.md`, readiness checklist, traceability matrix  
**Impact Assessment:** Medium. Adds field-level clarity.  
**Decision:** Approved  
**Resolution Date:** 2026-05-13
**Follow-up Actions:** Use updated My Profile field requirements as the basis for profile wireframes, validation logic, and test case preparation.
### CR-010 Update Generate Resume Fields from Wireframe Findings

**Date:** 2026-05-13  
**Type:** UI/UX  
**Status:** Implemented  
**Description:** Add `Additional comments for AI` and clean Generate Resume validation rules.  
**Reason:** Wireframe notes clarified generation input fields and removed unrelated validation items.  
**Affected Artifacts:** Generate Resume, generation request fields, readiness checklist, traceability matrix  
**Impact Assessment:** Medium. Improves form accuracy.  
**Decision:** Approved  
**Resolution Date:** 2026-05-13
**Follow-up Actions:** Update Generate Resume wireframes, validation scenarios, and traceability links to reflect the finalized generation input fields.
### CR-011 Replace Full API Key Visibility with Masked Key Handling

**Date:** 2026-05-13  
**Type:** Security  
**Status:** Implemented  
**Description:** Replace full API key display with masked key display. Admin can replace or delete the key but cannot view it in full after saving.  
**Reason:** API keys are secrets and should not be exposed or logged.  
**Affected Artifacts:** Decision Log, Risk Register, AI Model Details requirements  
**Impact Assessment:** High. Reduces security risk.  
**Decision:** Approved  
**Resolution Date:** 2026-05-13
**Follow-up Actions:** Apply masked API key handling in UI design, implementation requirements, security test cases, and risk mitigation tracking.
### CR-012 Clean Validation Inconsistencies from Wireframe Notes

**Date:** 2026-05-13  
**Type:** Requirement  
**Status:** Implemented  
**Description:** Resolve inconsistencies in required fields, validation rules, and error messages found during wireframe review.  
**Reason:** Work Experience description and Education start year are required; profile picture is optional; Generate Resume validation must match actual fields.  
**Affected Artifacts:** `wireframe_field_requirements.md`, `confirmed_elicitation_results.md`, readiness checklist  
**Impact Assessment:** Medium. Improves requirement quality and testability.  
**Decision:** Approved  
**Resolution Date:** 2026-05-13
**Follow-up Actions:** Use the cleaned validation rules as the source of truth for UI validation, error message design, QA checks, and acceptance criteria.

### CR-013 Remove Resume Details Pages from MVP (User and Admin)

**Date:** 2026-05-15  
**Type:** Scope  
**Requester:** Business Analyst  
**Status:** Implemented  
**Description:** Remove both user and admin Resume Details pages from MVP. PDF viewing, download, and public link copying are handled directly from User Home table and the post-save flow after Resume Review for users. Admin can view generated and saved resume PDFs the same way — from the Resumes table without a separate detail page.  
**Reason:** Neither page provides standalone value. All functions (PDF preview, download, public link access) are accessible from list/table views — User Home for users, Resumes table for admin.  
**Affected Artifacts:** `requirements_log.md`, `confirmed_elicitation_results.md`, `decision_log.md`, `traceability_matrix.md`, `risk_register.md`, `sitemap.md`  
**Impact Assessment:** Medium. Eliminates two pages from MVP but all functions remain available through table-level actions.  
**Decision:** Approved  
**Resolution Date:** 2026-05-15
**Follow-up Actions:** Mark FR-009 as Superseded. Remove Resume Details from user and admin page maps in Confirmed Elicitation Results. Update sitemap to remove both user and admin Resume Details entries. Supersede TR-011.

### CR-014 Replace PDF Column with Details Modal on User Home

**Date:** 2026-05-16
**Type:** UI/UX
**Requester:** Business Analyst
**Status:** Draft
**Description:** Replace the Link to PDF column in the User Home resume table with a Details column. Each row shows an `Open details` button that opens a modal popup containing: (1) public PDF link for copying, (2) PDF download button, (3) cover letter text for copying.
**Reason:** Wireframe refinement showed that a modal provides all resume output actions in one place without cluttering the table or requiring a separate page. Cover letter display is included because cover letter is now MVP.
**Affected Artifacts:** `requirements_log.md` (FR-008), `wireframe_field_requirements.md`, `traceability_matrix.md` (TR-010), `decision_log.md` (DEC-015)
**Impact Assessment:** Medium. Changes User Home table column structure and adds modal component.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Update FR-008 acceptance criteria to reflect modal behavior. Update Wireframe Field Requirements. Add cover letter to modal content.

### CR-015 Add Cover Letter Generation and Editing to MVP

**Date:** 2026-05-16
**Type:** Scope
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add cover letter generation to MVP. LLM generates cover letter text as part of the resume generation process. User can view and edit the cover letter in Resume Review before saving.
**Reason:** Cover letter was already visible in wireframes. Generating alongside the resume is less effort than post-MVP. LLM already has the context needed.
**Affected Artifacts:** `requirements_log.md` (FR-001, new FR-011, FR-012), `decision_log.md` (DEC-016), `traceability_matrix.md`, `wireframe_field_requirements.md`
**Impact Assessment:** Medium. Adds cover letter field to generation flow but reuses existing infrastructure.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create FR-011 and FR-012. Update FR-001. Add cover letter to Generate Resume and Resume Review field requirements. Add trace rows.

### CR-016 Expand Additional Info Fields in My Profile

**Date:** 2026-05-16
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add new fields to Additional Info section: Date of Birth, Ready for relocation (dropdown), Ready for business trips (dropdown), Preferred work format (checkbox group: full-time, part-time, offline, remote, hybrid, on-site project based).
**Reason:** Wireframes show these fields are needed for complete profile data supporting resume generation and candidate positioning.
**Affected Artifacts:** `requirements_log.md` (FR-007), `wireframe_field_requirements.md` (Section 4.6), `traceability_matrix.md` (TR-009)
**Impact Assessment:** Low. Expands existing section without adding new pages.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Update FR-007 description and acceptance criteria. Add validation rules to Wireframe Field Requirements. Update TR-009 trace notes.

### CR-017 Add Resume Delete from User Home and public_url_link Field

**Date:** 2026-05-18
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Implemented
**Description:** Add user-facing resume delete capability from User Home and add `public_url_link` varchar(200) field to `saved_resume` table for storing ready-made public resume URLs.

**Reason:** Users need to delete saved resumes directly from User Home. The delete button ("Delete this resume") is placed in the Open Details modal. After clicking, the button changes to a confirmation prompt with a new "Confirm deletion" button. Additionally, `public_url_link` is needed to store the generated public link for direct access. The existing `is_deleted` boolean field in `saved_resume` (default: false) is set to true on deletion to deactivate the link. If a recruiter or external visitor accesses a deleted resume link, the system returns HTTP 410 Gone with a custom page stating "Пользователь решил удалить данное резюме. Больше оно не доступно."

**Affected Artifacts:** `requirements_log.md`, `dbml_erd.md`, `mermaid_erd.md`, `plantuml_erd.puml`, `data_dictionary.md`, `traceability_matrix.md`, `risk_register.md`, `decision_log.md`

**Impact Assessment:** Medium. Adds new user-facing delete flow, a new DB field, and custom HTTP 410 handling.

**Decision:** Approved

**Resolution Date:** 2026-05-18

**Follow-up Actions:** Create FR-013. Update ERD files with `public_url_link`. Update Data Dictionary. Add trace row TR-017. Add risk RISK-011. [Completed 2026-05-18]

### CR-018 Add professional_title to resume_generation_response

**Date:** 2026-05-18
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add `professional_title` varchar(250) NOT NULL field to `resume_generation_response` table. The AI model generates the most relevant professional title matching the target vacancy and stores it in this field.

**Reason:** The generated resume needs a professional title that is specifically adapted to the target vacancy, distinct from the user's general `professional_title` in `contact_detail`. The AI model determines the best-fit title based on the vacancy requirements.

**Affected Artifacts:** `requirements_log.md` (FR-001), `dbml_erd.md`, `mermaid_erd.md`, `plantuml_erd.puml`, `data_dictionary.md`, `traceability_matrix.md` (TR-003), `decision_log.md`

**Impact Assessment:** Low. Adds one field to existing table; uses existing AI generation flow.

**Decision:** Approved

**Resolution Date:** N/A

**Follow-up Actions:** Update FR-001 affected data. Add DEC-033. Update ERDs and Data Dictionary. Update TR-003 trace notes.

### CR-019 Move Profile Picture from MVP to POST-MVP

**Date:** 2026-05-20
**Type:** Scope
**Requester:** Business Analyst
**Status:** Draft
**Description:** Move profile picture (photo_file_path) from MVP scope to POST-MVP. Profile picture is not supported by current HTML templates and is not required for resume generation.

**Reason:** The profile picture field (optional in FR-007) is not used by current one-page or two-page HTML templates. Keeping it in MVP creates unnecessary UI and data handling complexity without delivering resume output value. Deferred to POST-MVP when templates may support photos.

**Affected Artifacts:** `requirements_log.md` (FR-007), `wireframe_field_requirements.md`, `decision_log.md` (DEC-050)

**Impact Assessment:** Low. Removes optional field from MVP scope.

**Decision:** Approved

**Resolution Date:** N/A

**Follow-up Actions:** Update FR-007 acceptance criteria. Remove photo_file_path from MVP scope.

### CR-020 Add Error Handling, Custom Exceptions, and Logging NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFRs for system-wide error handling: global `@ControllerAdvice`, per-layer custom exceptions (`ControllerException`, `ServiceException`, `DaoException`), graceful error responses (no Java stack traces exposed to Vue frontend), and structured logging of all errors via SLF4J/Log4j2.
**Reason:** Mandatory per Capstone specification: all errors must be handled at controller, service, and DAO layers, logged, and displayed to the user in a readable format.
**Affected Artifacts:** `requirements_log.md` (new NFR-002–NFR-005), `decision_log.md`, `risk_register.md`, `traceability_matrix.md`
**Impact Assessment:** Medium. Cross-cutting infrastructure NFRs. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-002, NFR-003, NFR-004, NFR-005. Add DEC-051. Add RISK-013.

### CR-021 Add Code Quality, Package Structure, and Build NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFRs for code quality and build automation: package structure (`controller`, `service`, `dao`, `model`, `config`, `util`); clear DAO/Service separation — no business logic in DAO; Java Code Convention; Javadoc on all public interface and service methods; Maven CLI build (`mvn clean package`); `.gitignore` and `README.md` in repository; minimal and stable `pom.xml` dependencies.
**Reason:** Mandatory per Capstone specification covering package organization, code conventions, Javadoc documentation, build automation, and repository setup.
**Affected Artifacts:** `requirements_log.md` (new NFR-006–NFR-011), `decision_log.md`, `strategic_context_and_gap_analysis.md`, `traceability_matrix.md`
**Impact Assessment:** Medium. Quality and process NFRs. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-006 through NFR-011. Add DEC-051. Update technology stack notes.

### CR-022 Define Frontend Stack as Vue 3 + Vite + PrimeVue

**Date:** 2026-05-21
**Type:** Architecture
**Requester:** Business Analyst
**Status:** Draft
**Description:** Replace generic "Vue.js" reference with explicit frontend stack: Vue 3 (Composition API) + Vite + PrimeVue. The hybrid approach (Thymeleaf for Landing Page, SPA for authenticated app) is kept. PrimeVue provides Vue-native responsive components with ready themes and responsive/touch-friendly elements, covering the cross-browser and responsive design requirement. Cross-browser support targets Chrome, Firefox, and Edge.
**Reason:** Cross-browser compatibility and responsive design are mandatory per Capstone. PrimeVue is chosen over Bootstrap because it provides Vue-native components, reducing integration complexity for a developer new to Vue SPA.
**Affected Artifacts:** `decision_log.md` (DEC-052), `strategic_context_and_gap_analysis.md`, `confirmed_elicitation_results.md`, `traceability_matrix.md`
**Impact Assessment:** Medium. Defines the frontend stack precisely. Documentation-only — no code written yet.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Add DEC-052. Update technology stack in Strategic Context and Confirmed Elicitation Results.

### CR-023 Add DB Layer NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFRs for the database access layer: Service-layer transaction management via `connection.commit()/rollback()`; custom thread-safe Connection Pool implemented manually (no HikariCP or similar libraries); SQL scripts (schema.sql for DDL, data.sql for seed data) for manual DB initialization; PreparedStatement for all SQL queries with a strict ban on string concatenation; UTF-8 encoding for database, connection, and all text columns to support Cyrillic. Additionally, clarify that each DAO class maps to a single table/entity and implements full CRUD where applicable.
**Reason:** Mandatory per Capstone specification covering JDBC transaction management, manual Connection Pool implementation, SQL script initialization, SQL injection prevention, and UTF-8 encoding for Cyrillic support.
**Affected Artifacts:** `requirements_log.md` (NFR-012–NFR-016, NFR-006 update), `decision_log.md` (DEC-053, DEC-001 update), `strategic_context_and_gap_analysis.md`, `traceability_matrix.md`, `risk_register.md`
**Impact Assessment:** Medium. 5 new NFRs plus NFR-006 clarification. Establishes critical DB access standards. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-012 through NFR-016. Update NFR-006 with DAO-to-entity mapping. Add DEC-053. Update DEC-001 with Connection Pool detail.

### CR-024 Add UI Security and Dual Validation NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFRs for UI/UX security and validation: frontend protection against form resubmission (PRG pattern, button disable on submit); XSS sanitization for user input fields (profile, vacancy, generation settings); dual validation — frontend (PrimeVue form validation) and backend (Spring @Valid with Jakarta Validation annotations @Email, @NotNull, @NotEmpty, @Size).
**Reason:** Mandatory per Capstone specification covering form resubmission prevention, XSS protection, input validation on both layers, and Spring validation annotations.
**Affected Artifacts:** requirements_log.md (NFR-017–NFR-019), decision_log.md (DEC-054), traceability_matrix.md
**Impact Assessment:** Medium. 3 new NFRs. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-017, NFR-018, NFR-019. Add DEC-054. Add trace rows.

### CR-025 Refine Logging Stack, Add Consistent Logging NFR, Add Vuelidate

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Switch logging implementation from Log4j2 to Logback (SLF4J facade kept). Add NFR-020 for consistent log format across all application layers. Add DEC-055 confirming Vuelidate + native Vue validation as the frontend validation approach. Update NFR-005 with acceptance criteria for log consistency and validation-error logging as security events.
**Reason:** Logback is lighter and simpler than Log4j2 for a Capstone project. Vuelidate pairs naturally with Vue 3 Composition API and avoids jQuery dependency. Validation-error logging helps detect suspicious behavior per Capstone specification.
**Affected Artifacts:** `requirements_log.md` (NFR-005, NFR-020), `decision_log.md` (DEC-055), `strategic_context_and_gap_analysis.md`, `confirmed_elicitation_results.md`
**Impact Assessment:** Low. Log stack change is implementation-only. Vuelidate is a documentation update.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Update Strategic Context: SLF4J + Logback. Add DEC-055. Create NFR-020. Update NFR-005 acceptance criteria. Add Vuelidate to Confirmed Elicitation Results frontend section.

### CR-026 Document Design Patterns, Add Interceptors, AOP, SOLID NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Document 4 applied design patterns with rationale: Singleton (Connection Pool), Builder (AI prompt construction), Factory Method (mock vs real AI client), Strategy (adaptation level / AI model selection). Add NFR-021 (Spring MVC Interceptors for logging and authorization), NFR-022 (AOP for cross-cutting concerns), NFR-023 (SOLID + DRY principles and reusability). Formalize ResumePromptBuilder as a Builder pattern instance in Resume Template Details.
**Reason:** Mandatory per Capstone specification: minimum 4 design patterns with justification, Spring MVC Interceptors, AOP, SOLID, and DRY principles.
**Affected Artifacts:** `requirements_log.md` (NFR-021–NFR-023), `decision_log.md` (DEC-056), `strategic_context_and_gap_analysis.md`, `resume_template_details_and_logic.md`, `traceability_matrix.md`
**Impact Assessment:** Medium. 3 new NFRs and pattern documentation. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Add DEC-056. Create NFR-021, NFR-022, NFR-023. Update Resume Template Details to formalize Builder pattern. Update tech stack.

### CR-027 Add Testing NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add testing NFRs: JUnit 5 + Mockito as the test framework; 50%+ coverage in Service and DAO layers measured by JaCoCo; positive, negative, and boundary test scenarios; structured and consistent test practices (src/test/java, descriptive naming, @Test/@BeforeEach/@AfterEach); and Test-Driven Development approach. Also update NFR-009 to explicitly require `mvn test` execution.
**Reason:** Mandatory per Capstone specification covering JUnit 5, 50%+ coverage, Mockito, JaCoCo reports, test structure, and TDD.
**Affected Artifacts:** `requirements_log.md` (NFR-009, NFR-024–NFR-027), `decision_log.md` (DEC-057), `strategic_context_and_gap_analysis.md`, `traceability_matrix.md`
**Impact Assessment:** Medium. 4 new NFRs plus NFR-009 update. Defines testing standards before implementation starts.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Update NFR-009 with mvn test requirement. Create NFR-024, NFR-025, NFR-026, NFR-027. Add DEC-057. Update tech stack testing line. Add BG-008 and trace rows.

### CR-028 Add External Configuration and Javadoc Style Reference

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFR-028 requiring all configurable parameters (DB URL, credentials, localization settings, AI model defaults) to be externalized in `application.yml`. Update NFR-008 notes to reference the Oracle How to Write Doc Comments guide and Google Java Style Guide Section 7 as Javadoc style standards.
**Reason:** External configuration is a quality requirement per Capstone specification. Javadoc style reference provides a clear standard beyond "must have Javadoc."
**Affected Artifacts:** `requirements_log.md` (NFR-008 notes, new NFR-028)
**Impact Assessment:** Low. One new NFR and one notes update. No scope change.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-028. Update NFR-008 notes.

### CR-029 Add Pagination NFR and i18n Resource File Requirements

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFR-029 requiring pagination for all long lists (User Home resume table, Admin Users, Admin Resumes, Admin AI Models). Add NFR-030 requiring i18n resource files (messages_en.properties, messages_ru.properties) for both Thymeleaf (Landing Page) and Vue (authenticated SPA), loaded via Spring MessageSource with a Vue i18n library. Update Confirmed Elicitation Results to remove "if feasible" qualifier from pagination references.
**Reason:** Required to achieve maximum Capstone evaluation scores for pagination (5 points) and localization (5 points).
**Affected Artifacts:** requirements_log.md (NFR-029, NFR-030), decision_log.md (DEC-023 update), confirmed_elicitation_results.md
**Impact Assessment:** Medium. 2 new NFRs. Makes pagination mandatory. Formalizes i18n resource file structure.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Create NFR-029, NFR-030. Update DEC-023. Update Confirmed Elicitation Results pagination references.

### CR-030 Add dev/prod Profiles, Swagger, Docker Compose NFRs

**Date:** 2026-05-21
**Type:** Requirement
**Requester:** Business Analyst
**Status:** Draft
**Description:** Add NFR-031 (Swagger/OpenAPI REST API documentation; access restricted to ADMIN role on prod via Spring Security), NFR-032 (Docker Compose deployment with 3 containers: Java Spring MVC Tomcat, Vue frontend, PostgreSQL). Update NFR-028 to require dev and prod profile separation (application-dev.yml, application-prod.yml).
**Reason:** Swagger documents REST endpoints for reviewers and portfolio. Docker Compose ensures reproducible deployment. Profile separation enables environment-specific configuration without hardcoded values.
**Affected Artifacts:** requirements_log.md (NFR-028 update, NFR-031, NFR-032), decision_log.md (DEC-058), strategic_context_and_gap_analysis.md
**Impact Assessment:** Medium. 2 new NFRs, NFR-028 update, tech stack extension. MVP scope unchanged.
**Decision:** Approved
**Resolution Date:** N/A
**Follow-up Actions:** Update NFR-028 with profile requirement. Create NFR-031, NFR-032. Add DEC-058. Update tech stack.

### CR-031 Replace YAML Budget Configuration with DB-Backed Config

**Date:** 2026-05-23
**Type:** Architecture
**Requester:** Project Owner
**Status:** Implemented
**Description:** Replace the previously approved YAML-based resume budget configuration with DB-backed configuration stored in PostgreSQL. This affects the Resume Template Details document (Section 11), data model (4 new tables + fields in resume_generation_request), and related artifacts.
**Reason:** YAML is developer-oriented, not admin/data-oriented. The project already uses PostgreSQL and has admin-side concepts. DB-backed configuration is easier to inspect, test, and demonstrate in a portfolio.
**Affected Artifacts:** `resume_template_details_and_logic.md`, `dbml_erd.md`, `mermaid_erd.md`, `plantuml_erd.puml`, `data_dictionary.md`, `requirements_log.md`, `decision_log.md`, `change_request_log.md`, `traceability_matrix.md`, `risk_register.md`
**Impact Assessment:** Medium. Replaces existing YAML approach with DB-backed approach. New tables and fields required. Existing YAML-specific requirements are superseded.
**Decision:** Approved
**Resolution Date:** 2026-05-23
**Follow-up Actions:** Create DEC-060, DEC-061, DEC-062. Add NFR-033, NFR-034. Update all ERDs and Data Dictionary. Update section 11 in Resume Template Details. Add RISK-015.