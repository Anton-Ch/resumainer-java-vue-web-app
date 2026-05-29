# Requirements Log

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-13  
**Last Updated:** 2026-05-23  
**Author:** Anton  
**Version:** 18.0  
**Status:** Active  
**Related BABOK Area:** 5.1 Trace Requirements / 5.3 Prioritize Requirements / 6.2 Specify and Model Requirements  

---

## 1. Description

This document is the main requirements register for the ResumAIner Capstone project.

It consolidates requirement tracking and requirement readiness checks in one lightweight artifact. Each requirement includes classification, scope, priority, status, acceptance criteria, affected areas, and implementation readiness checks.

This document replaces the separate working `requirement_readiness_checklist.md` artifact for this project. Readiness checks are now maintained inside each requirement detail section.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Use this document as the main source of truth for requirements tracking.
- Each requirement must have a stable unique ID.
- Do not delete historical requirements. Change their status instead.
- Keep requirements concise, testable, and traceable.
- Use only the controlled values defined in this document.
- Add new requirements to the Summary Table and create a corresponding entry in Details.
- Use the Readiness Check section to decide whether a requirement is ready for MVP implementation.
- Link important requirement changes to the Change Request Log.
- Link major requirement decisions to the Decision Log.
- Link implemented or planned verification to test cases when they become available.

### 2.2 ID Types

The requirement ID types follow the BABOK requirements classification schema.

| ID Prefix | Requirement Type           | Meaning                                                                                                       | When to Use                                                                                  |
| --------- | -------------------------- | ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| BR        | Business Requirement       | High-level business need, goal, or desired business outcome                                                   | Use for project goals and business value statements                                          |
| STK       | Stakeholder Requirement    | Need of a specific stakeholder or stakeholder group                                                           | Use when a user/admin/recruiter need must be captured before solution detail                 |
| FR        | Functional Requirement     | Solution behavior, capability, screen action, workflow, or system function                                    | Use for what the system must do                                                              |
| NFR       | Non-Functional Requirement | Quality, constraint, security, performance, usability, maintainability, deployment, or compliance requirement | Use for how well the system must work or under what constraints                              |
| TRN       | Transition Requirement     | Temporary requirement needed to move from current state to future state                                       | Use for migration, setup, deployment preparation, initial data, or one-time transition needs |

Note: BABOK defines Solution Requirements as a major requirement class. In this project, Solution Requirements are tracked through `FR` and `NFR` because this is more practical for development, testing, and traceability.

### 2.3 Priority Values

| Value | Meaning |
|---|---|
| High | Important for MVP success or core product value |
| Medium | Useful for MVP or important for polish, but not core-critical |
| Low | Nice to have or low urgency |

### 2.4 Scope Values

| Value | Meaning |
|---|---|
| MVP | Required for the first working version |
| MVP Stretch | Useful if time allows, but not required for MVP success |
| Post-MVP | Planned after MVP |
| Future Scope | Long-term idea |
| Out of Scope | Explicitly excluded |

### 2.5 Requirement Status Values

| Value | Meaning |
|---|---|
| Draft | Requirement is captured but not yet reviewed |
| Reviewed | Requirement was checked but not yet approved |
| Approved | Requirement is accepted for the current baseline |
| Implemented | Requirement is implemented in the application |
| Verified | Requirement is implemented and tested/confirmed |
| Postponed | Requirement is moved to later phase |
| Rejected | Requirement is not accepted |
| Superseded | Requirement was replaced by another requirement |

### 2.6 Readiness Values

| Value | Meaning |
|---|---|
| Ready | Requirement is clear enough for implementation planning |
| Needs Clarification | Requirement has gaps that should be resolved before implementation |
| Blocked | Requirement cannot move forward until an issue is resolved |
| Postponed | Requirement is intentionally moved out of current implementation scope |
| N/A | Readiness check does not apply |

### 2.7 Readiness Check Values

| Value | Meaning |
|---|---|
| Yes | Check is satisfied |
| No | Check is not satisfied |
| Partial | Check is partly satisfied but needs clarification |
| N/A | Check does not apply |

### 2.8 Source Values

| Value | Meaning |
|---|---|
| Project Vision | Requirement comes from initial product vision |
| Elicitation Results | Requirement comes from confirmed elicitation results |
| Wireframe Review | Requirement comes from wireframe preparation or field-level review |
| Technical Constraint | Requirement comes from architecture or implementation constraints |
| Security Review | Requirement comes from security/privacy review |
| Governance Decision | Requirement comes from Decision Log or Change Request Log |
| Capstone Constraint | Requirement comes from Capstone expectations or delivery constraints |

## 3. Summary Table

| ID      | Type               | Title                                            | Source               | Priority   | Scope   | Status   | Readiness               |
| ------- | ------------------ | ------------------------------------------------ | -------------------- | ---------- | ------- | -------- | ----------------------- |
| BR-001  | Business           | Reduce manual resume adaptation effort           | Project Vision       | High       | MVP     | Approved | Ready                   |
| STK-001 | Stakeholder        | Recruiter can open shared resume without account | Elicitation Results  | High       | MVP     | Approved | Ready                   |
| FR-001  | Functional         | Generate AI-assisted resume draft                | Project Vision       | High       | MVP     | Approved | Needs Clarification     |
| FR-002  | Functional         | Manage contact details                           | Wireframe Review     | High       | MVP     | Approved | Ready                   |
| FR-003  | Functional         | Manage work experience                           | Wireframe Review     | High       | MVP     | Approved | Ready                   |
| FR-004  | Functional         | Manage projects and volunteering                 | Wireframe Review     | High       | MVP     | Approved | Ready                   |
| FR-005  | Functional         | Manage education                                 | Wireframe Review     | High       | MVP     | Approved | Ready                   |
| FR-006  | Functional         | Manage courses and certificates                  | Wireframe Review     | Medium     | MVP     | Approved | Ready                   |
| FR-007  | Functional         | Manage additional profile info and settings      | Wireframe Review     | Medium     | MVP     | Approved | Needs Clarification     |
| FR-008  | Functional         | View saved resumes on User Home                  | Elicitation Results  | High       | MVP     | Approved | Ready                   |
| FR-009  | Functional         | View resume details and PDF actions (superseded) | Governance Decision  | Low        | Post-MVP | Superseded | N/A       |
| FR-010  | Functional         | Admin manages AI model details                   | Elicitation Results  | Medium     | MVP     | Approved | Ready                   |
| FR-011  | Functional         | Generate and edit cover letter                   | Governance Decision  | Medium     | MVP     | Draft    | Needs Clarification     |
| FR-012  | Functional         | Include cover letter in generation request       | Governance Decision  | Medium     | MVP     | Approved | Ready                   |
| FR-013  | Functional         | Delete saved resume from User Home               | Governance Decision  | Medium     | MVP     | Approved | Ready                   |
| NFR-001 | Non-Functional     | Mask and protect saved API keys                  | Security Review      | High       | MVP     | Approved | Ready                   |
| NFR-002 | Non-Functional     | Define custom exception hierarchy per layer       | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-003 | Non-Functional     | Implement global exception handler                | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-004 | Non-Functional     | Graceful error responses without stack trace      | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-005 | Non-Functional     | Log all errors with structured logging            | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-006 | Non-Functional     | Organize code in standard package structure       | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-007 | Non-Functional     | Follow Java Code Convention                       | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-008 | Non-Functional     | Add Javadoc to all public service methods         | Capstone Constraint  | Medium     | MVP     | Approved | Ready                   |
| NFR-009 | Non-Functional     | Enable Maven CLI build without IDE                | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-010 | Non-Functional     | Include .gitignore and README.md in repository    | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-011 | Non-Functional     | Keep pom.xml dependencies minimal and stable      | Capstone Constraint  | Medium     | MVP     | Approved | Ready                   |
| NFR-012 | Non-Functional     | Implement Service-layer transaction management    | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-013 | Non-Functional     | Create SQL scripts for DB initialization          | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-014 | Non-Functional     | Prevent SQL injection via PreparedStatement       | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-015 | Non-Functional     | Use UTF-8 encoding for database and connections   | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-016 | Non-Functional     | Implement custom thread-safe Connection Pool      | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-017 | Non-Functional     | Prevent form resubmission on frontend             | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-018 | Non-Functional     | Sanitize user input against XSS                   | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-019 | Non-Functional     | Implement dual validation (frontend + backend)    | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-020 | Non-Functional     | Use consistent log format across all layers       | Capstone Constraint  | Medium     | MVP     | Approved | Ready                   |
| NFR-021 | Non-Functional     | Use Spring MVC Interceptors for cross-cutting concerns | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-022 | Non-Functional     | Use AOP for cross-cutting logic                   | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-023 | Non-Functional     | Follow SOLID, DRY principles and ensure reusability | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-024 | Non-Functional     | Achieve 50%+ test coverage in Service and DAO layers | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-025 | Non-Functional     | Cover positive, negative, and boundary test scenarios | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-026 | Non-Functional     | Maintain structured, consistent, and readable tests   | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-027 | Non-Functional     | Apply Test-Driven Development approach               | Capstone Constraint  | Medium     | MVP     | Approved | Ready                   |
| NFR-028 | Non-Functional     | Externalize configuration in application.yml          | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-029 | Non-Functional     | Implement pagination for all long lists                | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-030 | Non-Functional     | Provide i18n resource files for Thymeleaf and Vue      | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-031 | Non-Functional     | Document REST API with Swagger/OpenAPI                 | Capstone Constraint  | Medium     | MVP     | Approved | Ready                   |
| NFR-032 | Non-Functional     | Define Docker Compose deployment with 3 containers     | Capstone Constraint  | High       | MVP     | Approved | Ready                   |
| NFR-033 | Non-Functional     | DB-backed resume budget configuration                  | Governance Decision  | High       | MVP     | Approved | Ready                   |
| NFR-034 | Non-Functional     | Active config fallback and versioning                  | Governance Decision  | Medium     | MVP     | Approved | Ready                   |
| TRN-001 | Transition         | Prepare initial active AI model configuration    | Technical Constraint | Medium     | MVP     | Draft    | Needs Clarification     |
| XX-XXX  | [Requirement Type] | [Requirement title]                              | [Source]             | [Priority] | [Scope] | Draft    | [Requirement Readiness] |

## 4. Details

### BR-001 Reduce Manual Resume Adaptation Effort

**Type:** Business Requirement  
**Source:** Project Vision  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The product shall reduce the amount of manual work required to adapt a resume for a specific vacancy.

**Business Value:**  
This is the core reason for the product. The system should help users generate relevant resume drafts faster than manual rewriting.

**Acceptance Criteria:**
- User can provide profile data.
- User can provide vacancy information.
- User can generate an adapted resume draft.
- User can review, edit, save, and download the final resume.

**Affected UI:**  
User Home, My Profile, Generate Resume, Resume Review.

**Affected Data:**  
Profile data, ResumeGenerationRequest, GeneratedResumeDraft, SavedResume (pdf_file_path).

**Related Artifacts:**  
Project Vision, Confirmed Elicitation Results, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
This requirement is implemented through a set of lower-level functional and non-functional requirements.

### STK-001 Recruiter Can Open Shared Resume Without Account

**Type:** Stakeholder Requirement  
**Source:** Elicitation Results  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
Recruiters and external viewers need to open a shared resume link without registration or login.

**Business Value:**  
The public link makes the generated resume useful outside the system and supports real job application workflows.

**Acceptance Criteria:**
- Recruiter can open a public resume link without authentication.
- Public link opens the saved PDF directly.
- PDF text is selectable.
- Recruiter can view, copy text, print, and save the PDF.
- Private profile data, drafts, token usage, and admin data are not exposed.

**Affected UI:**  
Public PDF Resume Link.

**Affected Data:**  
Saved Resume, pdf file, public resume code/link.

**Related Artifacts:**  
Confirmed Elicitation Results, Decision Log, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
This stakeholder requirement is supported by public access and PDF-related functional requirements.

### FR-001 Generate AI-Assisted Resume Draft

**Type:** Functional Requirement  
**Source:** Project Vision  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall generate an AI-assisted resume draft based on user profile data, vacancy information, selected language, adaptation level, and selected AI model.

**Business Value:**  
This requirement supports the core product value: reducing manual resume adaptation effort.

**Acceptance Criteria:**
- User can submit required generation fields.
- System validates required fields before generation.
- System creates a resume generation request.
- System generates a draft using a mock AI provider during development and testing.
- System uses real OpenRouter integration for MVP demo; both implementations coexist behind the same interface (AiClientFactory).
- System displays the generated draft for user review.
- System handles empty output, timeout, unavailable provider, and inactive model errors.

**Affected UI:**  
Generate Resume, Resume Review.

**Affected Data:**  
ResumeGenerationRequest, GeneratedResumeDraft, AiModel, AiUsageLog, resume_generation_response (professional_title).

**Related Artifacts:**  
Confirmed Elicitation Results, Wireframe Field Requirements, Open Questions Log, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**  
Requirement is feasible if AI integration is isolated behind an interface and mock generation is implemented first.

### FR-002 Manage Contact Details

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to create, view, update, and save contact details in My Profile.

**Business Value:**  
Contact details provide candidate identity and communication information for generated resumes.

**Acceptance Criteria:**
- User can enter full name, email, phone, and location.
- User can optionally enter professional title, LinkedIn URL, portfolio URL, Telegram, and WhatsApp.
- System validates required fields, email format, URL format, and length limits.
- Saved contact details can be used in resume generation.

**Affected UI:**  
My Profile / Contact Details.

**Affected Data:**  
ContactDetails.

**Related Artifacts:**  
Wireframe Field Requirements, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
This is a core profile section for MVP.

### FR-003 Manage Work Experience

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to add, edit, delete, and view work experience records in My Profile.

**Business Value:**  
Work experience is one of the most important sources for resume generation.

**Acceptance Criteria:**
- User can add a work experience record.
- User can edit an existing record.
- User can delete a record.
- Job title, company name, start date, and role/job description are required.
- End date is optional for current role.
- End date cannot be earlier than start date.
- Work experience records are sorted automatically.

**Affected UI:**  
My Profile / Work Experience.

**Affected Data:**  
WorkExperience.

**Related Artifacts:**  
Wireframe Field Requirements, Decision Log, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Repeatable section uses card list + Add/Edit form pattern.

### FR-004 Manage Projects and Volunteering

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to add, edit, delete, and view project and volunteering records in My Profile.

**Business Value:**  
Projects and volunteering help demonstrate practical experience and portfolio value.

**Acceptance Criteria:**
- User can add, edit, and delete project records.
- Project name and description are required.
- Role, start date, end date, and project URL are optional.
- End date cannot be earlier than start date.
- Project records are sorted automatically.

**Affected UI:**  
My Profile / Projects & Volunteering.

**Affected Data:**  
Project.

**Related Artifacts:**  
Wireframe Field Requirements, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Volunteering is handled together with projects for MVP simplicity.

**Default Role Value:** If the user does not specify a role for a project entry, the system defaults to "Participant" at the UI/code level (DEC-031).

### FR-005 Manage Education

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to add, edit, delete, and view education records in My Profile.

**Business Value:**  
Education is a standard resume section and supports candidate background context.

**Acceptance Criteria:**
- User can add, edit, and delete education records.
- Institution name, degree/qualification, and start year are required.
- Field of study, end year, location, description, and GPA/grade are optional.
- End year cannot be earlier than start year.
- Education records are sorted automatically.

**Affected UI:**  
My Profile / Education.

**Affected Data:**  
Education.

**Related Artifacts:**  
Wireframe Field Requirements, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Start year is required based on confirmed elicitation decision.

### FR-006 Manage Courses and Certificates

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to add, edit, delete, and view courses and certificates in My Profile.

**Business Value:**  
Courses and certificates support professional development evidence, especially for career transition and junior roles.

**Acceptance Criteria:**
- User can add, edit, and delete course/certificate records.
- Course/certificate name, provider/issuer, and start date are required.
- End date, credential URL, skills/topics, and description are optional.
- End date cannot be earlier than start date.
- Records are sorted automatically.

**Affected UI:**  
My Profile / Courses & Certificates.

**Affected Data:**  
CourseCertificate.

**Related Artifacts:**  
Wireframe Field Requirements, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
This section is important for portfolio and learning evidence.
Courses section is mandatory for MVP per DEC-018. Page distribution: page 1 shows max 7 most relevant courses; page 2 — if 5+ work experience records exist, max 5 courses; if fewer than 5 work experience records, max 8 courses.

### FR-007 Manage Additional Profile Info and Settings

**Type:** Functional Requirement  
**Source:** Wireframe Review  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow a registered user to manage additional profile information and basic settings inside My Profile.

**Business Value:**  
Additional info provides useful AI context and keeps user settings in one place without a separate settings page.

**Acceptance Criteria:**
- User can enter optional skills, languages, professional aspirations, achievements, and general AI context.
- User can set default resume language (English, Russian) and optional additional resume language.
- User can manage URL-friendly username.
- User can enter date of birth.
- User can select Ready for relocation (dropdown: Yes / No / Not specified).
- User can select Ready for business trips and rotational schedule (dropdown: Yes / No / Not specified).
- User can select Preferred work format (checkbox group: full-time, part-time, offline, remote, hybrid, on-site project based).
- Username must be unique and URL-friendly: only Latin letters (a-z, A-Z), digits (0-9), and hyphens (-) allowed.
- Date of birth must be a valid date.

**Affected UI:**  
My Profile / Additional Info.

**Affected Data:**  
AdditionalProfileInfo, User.

**Related Artifacts:**  
Wireframe Field Requirements, Decision Log (DEC-044, DEC-050), Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**  
Needs final dropdown values for languages and username validation rules.

### FR-008 View Saved Resumes on User Home

**Type:** Functional Requirement  
**Source:** Elicitation Results  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall show saved/generated resumes in a searchable and sortable table on User Home.

**Business Value:**  
Users need quick access to all generated resumes without a separate Resume History page.

**Acceptance Criteria:**
- User Home shows saved resumes in a searchable and sortable table.
- Table includes a Details column with an `Open details` button for each resume row.
- Clicking `Open details` opens a modal popup on User Home.
- Modal contains: (1) public PDF resume link for copying, (2) PDF download button, (3) cover letter text for copying, (4) "Delete this resume" button.
- Clicking "Delete this resume" changes the button text to a confirmation prompt and reveals a "Confirm deletion" button.
- After confirming deletion, the resume is soft-deleted and removed from the table.
- Empty state is shown when no resumes exist.
- No-results state is shown when search returns no matches.

**Affected UI:**  
User Home, Resume Details modal.

**Affected Data:**  
SavedResume (pdf_file_path), CoverLetter.

**Related Artifacts:**  
Confirmed Elicitation Results, Decision Log (DEC-015, DEC-016), Change Request Log (CR-014), Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Partial
- Testable: Partial

**Notes:**  
This requirement replaced a separate Resume History page. It now provides resume actions through a Details column modal per DEC-015/CR-014. Cover letter display added to modal because cover letter is MVP (DEC-016). The direct PDF download from table was replaced with Details column + modal approach.

### FR-009 View Resume Details and PDF Actions (Superseded)

**Type:** Functional Requirement  
**Source:** Governance Decision  
**Priority:** Low  
**Scope:** Post-MVP  
**Status:** Superseded  
**Readiness:** N/A  

**Description:**  
This requirement is superseded. The Resume Details page was removed. PDF viewing, download, and public link copying are handled directly from User Home (FR-008) and the post-save flow after Resume Review.

**Business Value:**  
N/A — requirement is superseded.

**Acceptance Criteria:**
- N/A. Requirement is superseded by FR-008.

**Affected UI:**  
N/A.

**Affected Data:**  
N/A.

**Related Artifacts:**  
Decision Log — DEC-014; Change Request Log — CR-013.

**Readiness Check:**
- Business value clear: N/A
- Acceptance criteria clear: N/A
- Technically feasible: N/A
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: N/A

**Notes:**  
Superseded by DEC-014 / CR-013 (2026-05-15). Resume Details page removed from MVP. PDF and public link actions are provided by FR-008 (User Home) and the post-save success flow.

### FR-010 Admin Manages AI Model Details

**Type:** Functional Requirement  
**Source:** Elicitation Results  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall allow an admin to view and manage AI model details.

**Business Value:**  
Admin needs control over available AI models used for resume generation.

**Acceptance Criteria:**
- Admin can view AI model details.
- Admin can edit display name and provider base URL.
- Admin can replace API key.
- Admin can delete API key.
- Admin can activate or deactivate model.
- Saved API key is masked and cannot be viewed in full after saving.

**Affected UI:**  
AI Models, AI Model Details.

**Affected Data:**  
AiModel.

**Related Artifacts:**  
Confirmed Elicitation Results, Decision Log, Risk Register, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Security behavior is also covered by NFR-001.

### NFR-001 Mask and Protect Saved API Keys

**Type:** Non-Functional Requirement  
**Source:** Security Review  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The system shall protect saved API keys by masking them in the UI, preventing full key display after saving, and avoiding key exposure in logs.

**Business Value:**  
Protects secrets and prevents accidental exposure of provider credentials.

**Acceptance Criteria:**
- Saved API key is shown only as masked value.
- Admin can replace API key.
- Admin can delete API key.
- Admin cannot view saved API key in full after saving.
- API key is not logged.
- Error messages do not expose API key values.

**Affected UI:**  
AI Model Details.

**Affected Data:**  
AiModel.

**Related Artifacts:**  
Decision Log, Risk Register, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
This requirement supports DEC-008 and closes the API key exposure risk.

### NFR-002 Define Custom Exception Hierarchy Per Layer

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall define custom exception classes for each architectural layer: `ControllerException`, `ServiceException`, and `DaoException`. Each exception must include the originating layer, error context, and cause. This enables quick failure localization during development, testing, and debugging.

**Business Value:**  
Per-layer exceptions improve debugging speed and error traceability. The exception class itself identifies the failing layer without inspecting the stack trace.

**Acceptance Criteria:**
- `ControllerException` is thrown for controller-level errors (validation, binding, unauthorized access).
- `ServiceException` is thrown for business logic errors.
- `DaoException` is thrown for data access errors (SQL failures, connection issues).
- Each exception stores the original cause and a meaningful error message.
- Each exception stores a reference to the layer that originated it.

**Affected UI:**  
N/A (cross-cutting infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-020), Risk Register.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of the system-wide error handling strategy. Implemented alongside NFR-003, NFR-004, and NFR-005.

### NFR-003 Implement Global Exception Handler

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall implement a global exception handler using Spring's `@ControllerAdvice`. The handler catches exceptions from all controllers, delegates to the logging system, and returns standardized error responses to the Vue frontend.

**Business Value:**  
A single point of error handling prevents scattered try-catch blocks and ensures consistent error responses across the entire application.

**Acceptance Criteria:**
- `@ControllerAdvice` class handles all uncaught exceptions from controllers.
- Handler maps `ControllerException` → HTTP 4xx with user-friendly message.
- Handler maps `ServiceException` → HTTP 500 with user-friendly message.
- Handler maps `DaoException` → HTTP 500 with user-friendly message.
- Handler maps unclassified exceptions → HTTP 500 with generic message.
- Handler does not expose Java stack traces in the response body.

**Affected UI:**  
N/A (cross-cutting infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-020).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Works together with NFR-002 and NFR-005.

### NFR-004 Graceful Error Responses Without Stack Trace

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall never expose Java stack traces, SQL queries, or internal error details to the Vue frontend. All error responses returned to the client must contain only user-friendly messages. Internal error details must be logged server-side only.

**Business Value:**  
Prevents information leakage and provides a professional user experience. Stack traces in API responses are a security concern and look unprofessional.

**Acceptance Criteria:**
- JSON error response contains only: `message` (user-friendly), `errorCode` (optional), `timestamp`.
- JSON error response does NOT contain: `exception`, `trace`, `path`, `status` (unless mapped to a readable message).
- HTML error pages (if used) show a generic error message without technical details.
- All original exception details are logged server-side before returning the response.

**Affected UI:**  
All screens (global error behavior)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Risk Register, Change Request Log (CR-020).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Complements NFR-003 (global handler) and NFR-005 (logging).

### NFR-005 Log All Errors with Structured Logging

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall log all actions and errors through SLF4J/Logback with consistent format across all layers. ERROR level for system faults (database failures, external API errors). WARN level for validation failures and business rule violations. INFO level for successful operations. Log messages must include enough context to diagnose issues without exposing secrets or stack traces to the client. Validation errors are logged at WARN level to enable detection of suspicious behavior or tampering attempts.

**Business Value:**  
Structured logging with consistent format enables debugging, monitoring, and audit. Validation-error logging helps detect suspicious user behavior or attempted attacks.

**Acceptance Criteria:**
- ERROR level logs include: exception type, message, layer, timestamp, request context (user ID, action).
- WARN level logs include: validation failure details, business rule violations, field name, submitted value pattern.
- Log entries never contain: plaintext passwords, full API keys, personally identifiable information (PII).
- Log format is consistent across Controller, Service, and DAO layers (same timestamp format, delimiters, context keys).
- Validation errors from backend are logged at WARN level with field name and violation type to support security monitoring.

**Affected UI:**  
N/A (cross-cutting infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-020).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Builds on the existing SLF4J + Logback technology stack choice.

### NFR-006 Organize Code in Standard Package Structure

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The project shall follow a standard layered package structure: `controller`, `service`, `dao`, `model`, `config`, `util`. Each layer has a clear responsibility. DAO classes contain only data access logic. Service classes contain only business logic. No business logic is placed in DAO classes.

**Business Value:**  
Clear package structure improves code readability, maintainability, and team onboarding. Layer separation prevents business logic from leaking into data access code.

**Acceptance Criteria:**
- Package structure: `com.ainalyst.resumainer.controller`, `com.ainalyst.resumainer.service`, `com.ainalyst.resumainer.dao`, `com.ainalyst.resumainer.model`, `com.ainalyst.resumainer.config`, `com.ainalyst.resumainer.util`.
- Controller classes handle HTTP requests and responses only.
- Service classes contain business logic and orchestration.
- DAO classes contain SQL queries and data access only — no business logic.
- Each DAO class maps to a single table/entity and implements full CRUD operations for that entity where applicable (create, read, update, delete). If certain CRUD operations are not meaningful for the entity (e.g., read-only lookup tables), they may be omitted with justification.

**Affected UI:**  
N/A (source code organization)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: No (code review only)

**Notes:**  
Part of CR-021. Follows layered architecture best practices.

### NFR-007 Follow Java Code Convention

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
All Java source code shall follow the Java Code Convention (Oracle standard): consistent indentation, meaningful naming (camelCase for variables/methods, PascalCase for classes), brace placement, and single-statement-per-line.

**Business Value:**  
Consistent code style improves readability and code review quality. Required per Capstone specification.

**Acceptance Criteria:**
- Class names use PascalCase.
- Method and variable names use camelCase.
- Constants use UPPER_SNAKE_CASE.
- Indentation is consistent (4 spaces per level).
- No unused imports or variables.
- No commented-out code.

**Affected UI:**  
N/A (source code style)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: No (code review only)

**Notes:**  
Code review and IDE formatter (e.g., Checkstyle) can enforce this automatically.

### NFR-008 Add Javadoc to All Public Service Methods

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
All public methods in service and DAO interfaces shall include Javadoc comments describing the method purpose, parameters (`@param`), return value (`@return`), and thrown exceptions (`@throws`).

**Business Value:**  
Javadoc provides in-code documentation for other developers and reviewers. This is explicitly required by the Capstone specification.

**Acceptance Criteria:**
- Every public interface method has a Javadoc block.
- Each Javadoc includes: description, @param for each parameter, @return for non-void methods, @throws for declared exceptions.
- Service implementation methods inherit interface Javadoc where possible.

**Affected UI:**  
N/A (code documentation)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: No (code review only)

**Notes:**  
Part of CR-021. Focus on service and DAO interfaces; internal utility methods may omit Javadoc where the code is self-explanatory. Follow the Oracle How to Write Doc Comments guide and Google Java Style Guide Section 7 as Javadoc style standards.

### NFR-009 Enable Maven CLI Build Without IDE

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The project must build successfully from the command line using `mvn clean package` without requiring any IDE configuration. The build must produce a deployable WAR or JAR file.

**Business Value:**  
CLI build is required for CI/CD, code review (Capstone repository), and portfolio demonstration. It proves the project is properly configured and environment-independent.

**Acceptance Criteria:**
- `mvn clean package` completes without errors and runs all tests as part of the build lifecycle.
- `mvn test` can be run standalone to execute only the test phase without a full build.
- Build produces a WAR or JAR artifact in the `target/` directory.
- All unit tests pass during the build.
- Build works on a clean checkout without IDE-specific files.

**Affected UI:**  
N/A (build configuration)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-021. Maven wrapper (`mvnw`) may be included for environment independence.

### NFR-010 Include .gitignore and README.md in Repository

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The repository shall contain a `.gitignore` file excluding IDE files, build output, runtime artifacts, and secrets. It shall also contain a `README.md` describing the project purpose, technology stack, build instructions, and run instructions.

**Business Value:**  
Required per Capstone specification. A proper `.gitignore` prevents accidental commits of IDE files, build artifacts, and secrets. A README provides essential project context.

**Acceptance Criteria:**
- `.gitignore` excludes: `target/`, `*.iml`, `.idea/`, `.vscode/`, `*.log`, `.env`, application-local properties with secrets.
- `README.md` includes: project name, purpose, technology stack, prerequisites, build steps, run instructions.
- Both files are present in the repository root.

**Affected UI:**  
N/A (repository root files)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (file presence check)

**Notes:**  
Part of CR-021. README is a portfolio artifact and should be well-written.

### NFR-011 Keep pom.xml Dependencies Minimal and Stable

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The `pom.xml` shall include only directly used dependencies. Unused, deprecated, or unstable libraries must be excluded. Dependency versions should use stable releases.

**Business Value:**  
Minimal dependencies reduce build time, security surface, and risk of version conflicts. Required per Capstone specification.

**Acceptance Criteria:**
- Every dependency in `pom.xml` is actually imported or used in the codebase.
- No snapshot or beta dependencies unless explicitly justified.
- No duplicate or conflicting transitive dependencies.
- Dependency tree can be verified with `mvn dependency:tree`.

**Affected UI:**  
N/A (build configuration)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-021).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (dependency tree review)

**Notes:**  
Part of CR-021. Review `pom.xml` periodically as dependencies are added.

### NFR-012 Implement Service-Layer Transaction Management

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall manage database transactions at the Service layer using standard JDBC transaction control: `connection.setAutoCommit(false)`, `connection.commit()`, and `connection.rollback()`. Transactions are required for critical business operations to ensure data integrity. The Transaction Manager is implemented manually — no Spring `@Transactional` or declarative transaction management is used.

**Business Value:**  
Transaction management ensures data integrity. Manual JDBC transaction control demonstrates deep understanding of database concepts as required by the Capstone.

**Acceptance Criteria:**
- Each Service method that performs multiple DAO operations wraps them in a single transaction.
- Transaction starts with `connection.setAutoCommit(false)`.
- Transaction commits with `connection.commit()`.
- Transaction rollback on any exception with `connection.rollback()`.
- Connection is returned to the pool in a finally block regardless of success or failure.
- Minimum transaction-critical operations: user registration, resume generation request + response save.

**Affected UI:**  
N/A (cross-cutting infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-053), Change Request Log (CR-023).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Part of CR-023. Transactions are managed through the custom Connection Pool, not through frameworks.

### NFR-013 Create SQL Scripts for DB Initialization

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The repository shall include `schema.sql` containing all DDL statements (CREATE TABLE, indexes, constraints, foreign keys) required to recreate the database from scratch, and `data.sql` containing seed data for lookup tables (role, user_status, user_permission, response_status, language, adaptation_level, work_format). Both files must be runnable against PostgreSQL without modifications.

**Business Value:**  
SQL scripts provide a documented, repeatable way to initialize the database. Required per Capstone specification.

**Acceptance Criteria:**
- `schema.sql` contains CREATE TABLE statements for all 25+ entities.
- `schema.sql` includes primary keys, foreign keys, NOT NULL constraints, unique constraints, and indexes.
- `data.sql` inserts seed data for all lookup tables.
- Both files can be executed sequentially against an empty PostgreSQL database.
- Scripts are idempotent where possible.

**Affected UI:**  
N/A (database setup)

**Affected Data:**  
All entities

**Related Artifacts:**  
Decision Log (DEC-053), Change Request Log (CR-023), ERD, Data Dictionary.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Part of CR-023. Flyway migrations may also be used for versioned schema changes; schema.sql and data.sql serve as the canonical DDL source.

### NFR-014 Prevent SQL Injection via PreparedStatement

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
All SQL queries must use `PreparedStatement` with parameterized placeholders (`?`). String concatenation for building SQL queries is strictly forbidden. This applies to all DAO classes across the entire project.

**Business Value:**  
Prevents SQL injection attacks. Required per Capstone specification and fundamental security best practice.

**Acceptance Criteria:**
- Every SQL query in every DAO class uses `PreparedStatement`.
- Parameters are passed via `setString()`, `setInt()`, `setDate()`, etc.
- No SQL query uses string concatenation or interpolation for parameter values.
- Dynamic query building (e.g., optional filters) uses `PreparedStatement` with conditional `?` placeholders.
- Code review enforces this rule across all DAO classes.

**Affected UI:**  
N/A (data access layer)

**Affected Data:**  
All entities

**Related Artifacts:**  
Decision Log (DEC-053), Change Request Log (CR-023).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (code review + static analysis)

**Notes:**  
Part of CR-023. Enforced through code review and, optionally, static analysis tools.

### NFR-015 Use UTF-8 Encoding for Database and Connections

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The database, all database connections, and all text columns must use UTF-8 encoding to support Cyrillic characters (Russian, Kazakh) and other Unicode content in resumes, profile data, and vacancy descriptions.

**Business Value:**  
UTF-8 support is required because the application stores and displays Russian-language resume content and UI text. Required per Capstone specification.

**Acceptance Criteria:**
- PostgreSQL database is created with UTF-8 encoding (`ENCODING 'UTF8'`).
- Connection URL includes `?characterEncoding=UTF-8` or equivalent parameter.
- All VARCHAR and TEXT columns store and retrieve UTF-8 data correctly.
- Cyrillic characters in profile data, vacancy descriptions, and generated resumes are stored and displayed without corruption.
- SQL scripts use UTF-8 encoding.

**Affected UI:**  
All screens that display text data

**Affected Data:**  
All text columns across all entities

**Related Artifacts:**  
Decision Log (DEC-053), Change Request Log (CR-023).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Part of CR-023. Configured at database creation and connection pool initialization.

### NFR-016 Implement Custom Thread-Safe Connection Pool

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall implement a custom thread-safe Connection Pool manually. No third-party connection pool libraries (HikariCP, Apache DBCP, C3P0, Tomcat JDBC Pool) are allowed. The implementation must be thoroughly documented so it can be explained and defended during code review. Documentation must include: pool initialization, connection lifecycle, thread-safety mechanism, timeout handling, and connection validation.

**Business Value:**  
Manual Connection Pool implementation is a mandatory Capstone requirement. It demonstrates deep understanding of JDBC connection management, thread safety, and resource lifecycle.

**Acceptance Criteria:**
- Custom Connection Pool class with configurable initial size, maximum size, and timeout.
- Thread-safe connection acquisition and release (e.g., using `BlockingQueue`, `Semaphore`, or synchronized blocks with `wait()/notify()`).
- Connections are validated before being returned to a caller.
- Idle connections are periodically validated or evicted.
- Connections are properly closed and removed from the pool on database errors.
- Pool blocks or throws when maximum size is reached and no connection is available within timeout.
- Pool is gracefully shut down on application stop.
- Internal documentation (Javadoc or design notes) explains: why a custom pool is needed, thread-safety approach, connection lifecycle, timeout handling, and edge cases.

**Affected UI:**  
N/A (infrastructure layer)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-053), Change Request Log (CR-023), Risk Register.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-023. This is a key Capstone differentiator. The pool implementation must be well-documented for code review defense.

### NFR-017 Prevent Form Resubmission on Frontend

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall prevent duplicate form submissions on all user-facing forms. Submit buttons must be disabled immediately after the first click. The browser back button must not re-submit a previously completed form (PRG — Post/Redirect/Get pattern). This applies to registration, login, resume generation, profile editing, and any other user submission forms.

**Business Value:**  
Prevents duplicate resumes, duplicate account registrations, and user confusion. Required per Capstone specification.

**Acceptance Criteria:**
- Submit button is disabled after first click; shows loading state.
- Browser F5 refresh does not re-submit the last form.
- Browser back button after form submission does not show "Confirm Form Resubmission" dialog.
- Post/Redirect/Get (PRG) pattern is implemented on the backend for form submissions.
- User is redirected to a new page (or the same page with GET) after successful submission.

**Affected UI:**  
Register, Login, My Profile (all sections), Generate Resume, Resume Review (Save & Create), AI Model Details (all admin forms).

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-054), Change Request Log (CR-024).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-024. Backend implements PRG pattern via `RedirectView` or `redirect:` prefix. Frontend disables button via PrimeVue `:disabled` binding.

### NFR-018 Sanitize User Input Against XSS

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall sanitize all user-provided text input to prevent Cross-Site Scripting (XSS) attacks. This applies to all profile fields (skills, experience descriptions, etc.), vacancy description input, generation settings, and any other user-entered text that may be displayed in the UI or stored and later rendered.

**Business Value:**  
User input fields are a common XSS attack vector. Sanitization prevents injected scripts from executing in other users' browsers or the admin panel.

**Acceptance Criteria:**
- User input is sanitized on input (server-side before storage).
- User input is escaped on output (in Vue templates via Vue's built-in escaping).
- `<script>`, `onerror`, `onclick`, `onload`, `javascript:` and other dangerous patterns are stripped or escaped.
- Admin panel does not render user input as raw HTML.
- Backend uses an allowlist-based sanitizer for any user-provided HTML (or strips all HTML from user input).
- Vue's default template escaping (`{{ }}`) prevents XSS in rendered text.

**Affected UI:**  
My Profile (all text fields), Generate Resume (vacancy description, company description, additional comments), Admin User Details, Admin AI Model Details.

**Affected Data:**  
All user-editable text fields across all profile and generation entities.

**Related Artifacts:**  
Decision Log (DEC-054), Change Request Log (CR-024).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Part of CR-024. AI-generated HTML sanitization is already covered by DEC-037 and DEC-038. This NFR covers user input sanitization separately.

### NFR-019 Implement Dual Validation (Frontend + Backend)

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall implement validation on both frontend and backend layers. Frontend validation provides immediate user feedback. Backend validation is the authoritative check and uses Spring Framework's `@Valid` annotation with Jakarta Bean Validation annotations (`@Email`, `@NotNull`, `@NotEmpty`, `@Size`). Backend validation must check NOT NULL and NOT EMPTY for all required fields across all entities. Backend validation is enforced even if frontend validation is bypassed.

**Business Value:**  
Dual validation prevents invalid data from reaching the database regardless of frontend state. Required per Capstone specification.

**Acceptance Criteria:**
- Frontend validation: required fields show inline error messages before submission.
- Backend validation uses `@Valid` on all POST/PUT controller method parameters.
- Required fields use `@NotNull` / `@NotEmpty` annotations.
- Email fields use `@Email` annotation.
- String length constraints use `@Size` annotation.
- Backend rejects invalid input with HTTP 400 and field-level error messages.
- Backend validation covers all entity fields marked as Required in Wireframe Field Requirements.

**Affected UI:**  
Register, Login, My Profile (all sections), Generate Resume, AI Model Details, Admin User Details.

**Affected Data:**  
All entities with required fields.

**Related Artifacts:**  
Decision Log (DEC-054), Change Request Log (CR-024), Wireframe Field Requirements.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Part of CR-024. Wireframe Field Requirements document is the definitive source for field-level validation rules. Frontend validation uses Vuelidate library with native Vue 3 Composition API form handling (DEC-055).

### NFR-020 Use Consistent Log Format Across All Layers

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
All log entries across Controller, Service, and DAO layers must follow a consistent format. The format includes: timestamp (ISO 8601), log level, logger name, thread, message, and contextual data (user ID, action, layer). This ensures log readability and enables effective searching and filtering.

**Business Value:**  
Consistent log format makes debugging, monitoring, and log analysis predictable across the entire application.

**Acceptance Criteria:**
- Log pattern is defined once in `logback.xml` and applied application-wide.
- Timestamp format: ISO 8601 (e.g., `2026-05-21T14:30:00.123+06:00`).
- Each log entry includes: level, thread, logger/class name, message, and any additional context.
- Controller, Service, and DAO layers use the same format without per-layer customization.
- MDC (Mapped Diagnostic Context) is used to inject user ID and request ID into log entries.

**Affected UI:**  
N/A (cross-cutting)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-051), Change Request Log (CR-025), NFR-005.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-025. Works together with NFR-005 (structured error logging). Consistent format applies to both normal operations and error conditions.

### NFR-021 Use Spring MVC Interceptors for Cross-Cutting Concerns

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall use Spring MVC `HandlerInterceptor` implementations for cross-cutting concerns that execute before or after controller method calls. Minimum interceptors: request logging interceptor (log incoming requests, execution time, and response status) and authentication/authorization interceptor (verify user is authenticated and has required role for the endpoint).

**Business Value:**  
Interceptors separate cross-cutting logic from controller code, keeping controllers focused on request handling.

**Acceptance Criteria:**
- `RequestLoggingInterceptor` logs HTTP method, URI, execution time, and response status for each request.
- `AuthInterceptor` checks authentication before secured endpoints; redirects to login if not authenticated.
- `AdminInterceptor` checks ADMIN role before admin endpoints; returns 403 if unauthorized.
- Interceptors are registered in Spring configuration and applied to the appropriate URL patterns.
- Public endpoints (Landing Page, public resume link, login, register) are excluded from auth interceptor.

**Affected UI:**  
All pages (cross-cutting)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-056), Change Request Log (CR-026).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-026. Interceptors complement AOP (NFR-022) — interceptors handle web-layer concerns; AOP handles service-layer concerns.

### NFR-022 Use AOP for Cross-Cutting Logic

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall use Spring AOP (Aspect-Oriented Programming) with AspectJ annotations for cross-cutting concerns at the Service and DAO layers. AOP aspects shall be used for logging method entry/exit and execution time, and optionally for security checks and transaction boundary logging.

**Business Value:**  
AOP keeps cross-cutting logic out of business code, reducing duplication and improving maintainability. Required per Capstone specification.

**Acceptance Criteria:**
- At least one `@Aspect` class exists in the project.
- `LoggingAspect` logs method entry, exit, and execution time for all Service methods.
- Aspects use `@Before`, `@AfterReturning`, or `@Around` pointcuts as appropriate.
- Pointcut expressions are defined in a reusable manner.
- Aspect logic can be enabled or disabled via configuration without changing business code.

**Affected UI:**  
N/A (service layer cross-cutting)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-056), Change Request Log (CR-026).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-026. AOP handles service-layer logging and monitoring; web-layer concerns are handled by Interceptors (NFR-021).

### NFR-023 Follow SOLID, DRY Principles and Ensure Reusability

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system architecture shall follow SOLID and DRY principles. Each class has a single responsibility. Dependencies are inverted (abstractions, not concretions). Duplication is avoided through abstraction and reuse. Key modules (Service layer, DAO layer, UI components) must be designed for reusability across different parts of the application.

**Business Value:**  
SOLID and DRY produce maintainable, testable, and extensible code. Reusability reduces development time and improves consistency.

**Acceptance Criteria:**
- Single Responsibility: each class has one clearly defined purpose.
- Open/Closed: core modules are open for extension but closed for modification.
- Dependency Inversion: Service layer depends on DAO interfaces, not implementations.
- DRY: business logic and validation rules are not duplicated across layers.
- Reusable frontend components: common UI patterns (form fields, tables, modals) are extracted into shared Vue components.
- Reusable backend components: DAO base class or utility methods reduce repetitive JDBC code.

**Affected UI:**  
All layers (cross-cutting architecture principle)

**Affected Data:**  
All entities

**Related Artifacts:**  
Decision Log (DEC-056), Change Request Log (CR-026).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: No (code review only)

**Notes:**  
Part of CR-026. Enforced through code review and refactoring cycles during development.

### NFR-024 Achieve 50%+ Test Coverage in Service and DAO Layers

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The project must achieve at least 50% line coverage in Service and DAO layers, measured by JaCoCo. Tests use JUnit 5 with Mockito for dependency isolation. Coverage reports are generated during the Maven build and must be reviewable.

**Business Value:**  
Required per Capstone specification. Ensures business logic and data access code is verified.

**Acceptance Criteria:**
- JaCoCo plugin is configured in `pom.xml`.
- `mvn test` generates a JaCoCo coverage report.
- Service layer achieves at least 50% line coverage.
- DAO layer achieves at least 50% line coverage.
- Coverage threshold is checked during build (optional warning, not a hard fail for MVP).
- Report is accessible at `target/site/jacoco/index.html`.

**Affected UI:**  
N/A (testing infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-057), Change Request Log (CR-027).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (JaCoCo report)

**Notes:**  
Part of CR-027. Coverage is measured per layer, not globally. Coverage target applies to MVP-delivered code only.

### NFR-025 Cover Positive, Negative, and Boundary Test Scenarios

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
Tests must cover three scenario types: positive scenarios (expected inputs produce correct results), negative scenarios (invalid inputs produce appropriate errors), and boundary values (edge cases for field lengths, date ranges, numeric limits). Validation logic and business rules must be tested explicitly.

**Business Value:**  
Comprehensive scenario coverage catches defects before demo. Required per Capstone specification.

**Acceptance Criteria:**
- Each Service method has at least one positive test and one negative test.
- Each DAO method has at least one positive test.
- Boundary value tests exist for fields with length limits (e.g., max 100 chars for `full_name`).
- Validation logic tests cover: required field missing, invalid email format, invalid date range.
- Business rule tests cover: sorting rules, profile minimum requirements, page placement logic.

**Affected UI:**  
N/A (testing)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-057), Change Request Log (CR-027).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-027. Tests should follow the naming pattern: `methodName_scenario_expectedResult`.

### NFR-026 Maintain Structured, Consistent, and Readable Tests

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
Test classes must be placed in `src/test/java/` mirroring the production package structure. Test names must be descriptive and indicate the method or behavior under test. JUnit annotations (@Test, @BeforeEach, @AfterEach) are used consistently. Tests within a class follow a consistent pattern (arrange-act-assert or given-when-then). Test data is consistent and reusable across related tests.

**Business Value:**  
Consistent and readable tests improve maintainability and make test failures easier to diagnose.

**Acceptance Criteria:**
- Test class location mirrors production class: `src/test/java/com/ainalyst/resumainer/service/UserServiceTest.java`.
- Each test method name describes the scenario: `createUser_withValidData_returnsUserId`, `createUser_withDuplicateEmail_throwsException`.
- `@BeforeEach` sets up common test fixtures.
- `@AfterEach` cleans up where needed.
- Tests follow arrange-act-assert pattern with clear section separation.
- No hardcoded magic values in test assertions — use named constants or test data builders.
- Test data is consistent: same test user, same test vacancy used across related tests.

**Affected UI:**  
N/A (testing)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-057), Change Request Log (CR-027).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (code review)

**Notes:**  
Part of CR-027. Test consistency is enforced through code review.

### NFR-027 Apply Test-Driven Development Approach

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The project shall follow Test-Driven Development (TDD) where practical: write a failing test first, then implement the minimum code to make it pass, then refactor. TDD is applied to Service and DAO layer development.

**Business Value:**  
TDD produces testable code by design, catches regressions early, and ensures every piece of business logic has a corresponding test.

**Acceptance Criteria:**
- Service and DAO classes are developed test-first where feasible.
- Each new Service method has a corresponding test written before or alongside the implementation.
- Test coverage trends upward during development, not added retroactively.
- TDD is documented in development notes or commit messages where applicable.

**Affected UI:**  
N/A (development process)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-057), Change Request Log (CR-027).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: No (process requirement)

**Notes:**  
Part of CR-027. TDD is recommended but may be relaxed for simple getter/setter or CRUD boilerplate.

### NFR-028 Externalize Configuration in application.yml

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
All configurable application parameters must be externalized into an `application.yml` file. This includes database connection URL, credentials, connection pool settings, localization defaults, AI model defaults (provider base URL, model code, max tokens), and logging configuration. No hardcoded environment-specific values in Java source code. The project shall define separate Spring profiles for development and production via `application-dev.yml` and `application-prod.yml`.

**Business Value:**  
External configuration enables environment-independent deployment and simplifies setup for reviewers. Profile separation keeps development settings (local DB, debug logging) isolated from production settings.

**Acceptance Criteria:**
- `application.yml` exists in `src/main/resources/` as the base configuration.
- `application-dev.yml` contains development-specific overrides.
- `application-prod.yml` contains production-specific overrides.
- Database URL, username, password are defined in profile-specific files.
- Connection pool settings (initial size, max size, timeout) are configurable per profile.
- Logging level and pattern are configurable per profile.
- AI provider base URL, model code, and API key are configurable per profile.
- Secrets (DB password, API keys) are not hardcoded — use `${ENV_VARIABLE}` placeholders.
- Active profile is set via `SPRING_PROFILES_ACTIVE` environment variable or JVM argument.

**Affected UI:**  
N/A (configuration infrastructure)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (CR-028).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes (file presence + value loading)

**Notes:**  
Part of CR-028. Environment-specific secrets (DB passwords, API keys) use `${ENV_VARIABLE}` placeholders in `application.yml` with actual values provided via environment variables or `.env` file.

### NFR-029 Implement Pagination for All Long Lists

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall implement pagination for all data tables displaying multiple records. This includes the User Home resume table, Admin Users table, Admin Resumes table, and Admin AI Models table. Each page must show a configurable number of items, display page navigation controls, and indicate total items and current page.

**Business Value:**  
Pagination improves page load performance and user experience for long lists. Required for maximum Capstone evaluation score.

**Acceptance Criteria:**
- User Home resume table displays paginated results (e.g., 10 items per page).
- Admin Users, Admin Resumes, and Admin AI Models tables display paginated results.
- Pagination controls include: Previous, page numbers, Next.
- Current page is visually highlighted.
- Total number of items and current page range are displayed.
- Page size is configurable via backend or application.yml default.
- Pagination works correctly with search and filter applied.

**Affected UI:**  
User Home, Admin Users, Admin Resumes, Admin AI Models.

**Affected Data:**  
N/A (pagination is a query/UI concern)

**Related Artifacts:**  
Change Request Log (CR-029).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes (LIMIT/OFFSET queries)
- Testable: Yes

**Notes:**  
Part of CR-029. Pagination is mandatory for all list views — not optional. Backend implements LIMIT/OFFSET pagination; frontend PrimeVue components handle pagination display.

### NFR-030 Provide i18n Resource Files for Thymeleaf and Vue

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall support at least two interface languages (English and Russian) through resource files. For the Thymeleaf Landing Page, messages are loaded via Spring MessageSource from `.properties` files. For the Vue SPA, messages are loaded via a Vue i18n library from corresponding resource files. Both layers must provide consistent translations for all UI strings.

**Business Value:**  
i18n with resource files is required for maximum Capstone evaluation score. Proper resource file structure ensures translations are maintainable and consistent.

**Acceptance Criteria:**
- `messages_en.properties` and `messages_ru.properties` exist in `src/main/resources/i18n/`.
- Thymeleaf Landing Page reads translations via Spring MessageSource (`#{...}` syntax).
- Vue SPA reads translations from i18n resource files served by the backend or bundled at build time.
- Language switcher (top-right UI element) switches between EN and RU.
- All user-facing text is externalized — no hardcoded UI strings in Thymeleaf templates or Vue components.
- Default language follows browser locale (DEC-023); user override persists in session.

**Affected UI:**  
All pages (Landing Page, Register, Login, User Home, My Profile, Generate Resume, Resume Review, Admin pages).

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-023), Change Request Log (CR-029).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-029. Thymeleaf uses Spring's built-in i18n. Vue uses a dedicated i18n library (e.g., vue-i18n). Both reference the same message keys for consistency.

### NFR-031 Document REST API with Swagger/OpenAPI

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall provide REST API documentation via Swagger/OpenAPI. All backend controller endpoints must be documented with request parameters, response formats, and HTTP status codes. In the production environment, Swagger UI access must be restricted to users with the ADMIN role via Spring Security.

**Business Value:**  
Swagger documentation enables reviewers to explore endpoints, simplifies frontend-backend integration, and demonstrates API design quality.

**Acceptance Criteria:**
- Swagger/OpenAPI dependency (springdoc-openapi) is configured in pom.xml.
- Swagger UI is accessible at `/swagger-ui.html` or `/api-docs` in dev mode.
- In production, Swagger UI requires authentication with ADMIN role; non-admin users receive 403.
- Controller endpoints are annotated with `@Operation` and `@ApiResponse` summaries.
- API documentation includes request/response schemas.
- Swagger can be disabled entirely via a configuration flag in prod profile.

**Affected UI:**  
N/A (API documentation)

**Affected Data:**  
N/A

**Related Artifacts:**  
Decision Log (DEC-058), Change Request Log (CR-030).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-030. Uses springdoc-openapi for Spring MVC. Prod access restriction configured via Spring Security filter chain.

### NFR-032 Define Docker Compose Deployment with 3 Containers

**Type:** Non-Functional Requirement  
**Source:** Capstone Constraint  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The project shall include a `docker-compose.yml` file defining at least three containers: a Java Spring MVC application running on Tomcat, a Vue frontend served via Nginx or similar, and a PostgreSQL database. The Docker Compose configuration must support starting the entire application stack with a single command.

**Business Value:**  
Docker Compose provides a reproducible, portable deployment environment required for Capstone review and portfolio demonstration.

**Acceptance Criteria:**
- `docker-compose.yml` exists in the repository root.
- Three services defined: `backend` (Java Tomcat), `frontend` (Vue), `database` (PostgreSQL).
- Backend container builds from a `Dockerfile` in the project root.
- Frontend container builds from a `Dockerfile` in the Vue project directory.
- Database container uses the official PostgreSQL image.
- Database initialization runs schema.sql and data.sql automatically.
- Containers communicate via an internal Docker network.
- Environment variables (DB URL, credentials, active profile) are passed to containers.
- `docker compose up` starts the full stack without manual steps.

**Affected UI:**  
N/A (deployment infrastructure)

**Affected Data:**  
All entities

**Related Artifacts:**  
Decision Log (DEC-058), Change Request Log (CR-030), Strategic Context.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: N/A
- Testable: Yes

**Notes:**  
Part of CR-030. Flyway migrations run inside the backend container on startup. Vue frontend is served via Nginx or similar and proxies API calls to the backend.

### NFR-033 DB-Backed Resume Budget Configuration

**Type:** Non-Functional Requirement  
**Source:** Governance Decision  
**Priority:** High  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall store resume budget configuration in PostgreSQL instead of YAML files. Budget settings must be readable before every resume generation without requiring Java code changes or application restart.

**Business Value:**  
DB-backed configuration is easier to inspect, test, and demonstrate in a portfolio. It avoids hardcoding budget parameters in Java and allows runtime configuration changes.

**Acceptance Criteria:**
- Resume budget configuration is stored in PostgreSQL tables: `resume_budget_configs`, `resume_template_selection_rules`, `resume_work_experience_distribution_rules`, `resume_section_budget_rules`.
- Backend reads the active/newest config from DB before each generation.
- PostgreSQL partial unique index prevents more than one active config.
- Config values control: template selection, work experience distribution, section budgets, sentence counts, bullet limits, skill limits, project limits, and course limits.

**Affected UI:**  
N/A (backend infrastructure)

**Affected Data:**  
resume_budget_configs, resume_template_selection_rules, resume_work_experience_distribution_rules, resume_section_budget_rules

**Related Artifacts:**  
Decision Log, Change Request Log, DBML ERD, Data Dictionary, Resume Template Details

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Replaces the previously planned YAML-based configuration approach (DEC-049). YAML-based configuration is no longer used.

### NFR-034 Active Config Fallback and Versioning

**Type:** Non-Functional Requirement  
**Source:** Governance Decision  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall implement safe fallback behavior for budget configuration selection and version tracking.

**Business Value:**  
Fallback logic prevents generation failures when configuration is misconfigured. Version tracking enables traceability of which config version was used for each generation.

**Acceptance Criteria:**
- If one active config exists, use it.
- If multiple active configs exist, use the newest by `updated_at DESC, id DESC`.
- If no active config exists, use the newest config as fallback.
- If no config exists at all, backend throws a clear configuration error.
- `version_no` is incremented when config settings change.
- Generation request stores `budget_config_id` and `budget_config_version_used`.
- No cache is used for MVP — changes affect future generations immediately.
- No full config history/version tables required for MVP.

**Affected UI:**  
N/A (backend infrastructure)

**Affected Data:**  
resume_generation_request (budget_config_id, budget_config_version_used), resume_budget_configs

**Related Artifacts:**  
Decision Log, Change Request Log, Resume Template Details

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Yes
- Technically feasible: Yes
- UI/workflow identified: N/A
- Data impact identified: Yes
- Testable: Yes

**Notes:**  
Active config fallback applies to the config selection query, not to individual missing values. If a required rule row is missing from the active config, the backend should handle it gracefully.

### FR-011 Generate and Edit Cover Letter

**Type:** Functional Requirement  
**Source:** Governance Decision  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall generate a cover letter alongside the resume draft and allow the user to review and edit it before saving the final version.

**Business Value:**  
Cover letter reduces the effort required to apply for a vacancy. Generating it alongside the resume is more efficient than adding it post-MVP since the LLM already has the vacancy and profile context.

**Acceptance Criteria:**
- System generates cover letter text as part of the resume generation process.
- Cover letter text is displayed in the Resume Review screen alongside generated resume fields.
- User can edit the generated cover letter before saving.
- Saved resume includes the final cover letter version.
- Cover letter text is viewable in the Resume Details modal on User Home.
- User can copy cover letter text from the modal.

**Affected UI:**  
Resume Review, Resume Details modal, Generate Resume.

**Affected Data:**  
ResumeGenerationRequest (cover_letter field), SavedResume (cover_letter field).

**Related Artifacts:**  
Decision Log (DEC-016), Change Request Log (CR-015), Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**  
Cover letter generation reuses the same AI generation flow as resume draft. The LLM receives the same vacancy and profile context and produces cover letter as additional output. Cover letter format and length rules should be defined during implementation.

### FR-012 Include Cover Letter in Generation Request

**Type:** Functional Requirement  
**Source:** Governance Decision  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready

**Description:**  
The system shall include cover letter generation as part of the resume generation request. The generation request shall instruct the AI to produce a cover letter alongside the adapted resume content.

**Business Value:**  
Cover letter generation must be explicitly requested as part of the AI generation call. Without this requirement, the AI would not produce cover letter output.

**Acceptance Criteria:**
- Generation request includes a flag or instruction for cover letter generation.
- AI prompt includes cover letter generation instructions.
- Cover letter output is stored separately from resume content.
- System handles cases where cover letter generation fails while resume generation succeeds.

**Affected UI:**  
Generate Resume (optional toggle for cover letter generation), Resume Review.

**Affected Data:**  
ResumeGenerationRequest (include_cover_letter).

**Related Artifacts:**  
Decision Log (DEC-016), FR-011, Traceability Matrix.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**  
This requirement works together with FR-011. The generation request should include cover letter as a requested output alongside the resume adaptation.

**Generated Content Page Placement:** Generated work experience entries and course entries are placed on page 1 (primary, more relevant) or page 2 (additional) of the resume. Page placement is controlled by `is_first_page` flag in `generation_response_experience` and `generation_response_course` tables. AI model assigns relevance and placement during generation (DEC-030).

### TRN-001 Prepare Initial Active AI Model Configuration

**Type:** Transition Requirement  
**Source:** Technical Constraint  
**Priority:** Medium  
**Scope:** MVP  
**Status:** Approved  
**Readiness:** Ready  

**Description:**  
The project shall include initial AI model configuration data required for the MVP to run after deployment.

**Business Value:**  
The system needs at least one active AI model configuration for resume generation to work in demo or production-like environment.

**Acceptance Criteria:**
- Initial model configuration can be inserted through migration or seed data.
- At least one active AI model exists after setup.
- API key is provided through secure configuration, not committed to Git.
- Mock model/provider is available for stable demo flow.

**Affected UI:**  
AI Models, AI Model Details.

**Affected Data:**  
AiModel.

**Related Artifacts:**  
Technical Constraints, Deployment Plan, Decision Log.

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**  
Needs final setup approach for environment variables, seed data, and demo mode.

### FR-013 Delete Saved Resume from User Home

**Type:** Functional Requirement
**Source:** Governance Decision
**Priority:** Medium
**Scope:** MVP
**Status:** Approved
**Readiness:** Ready

**Description:**
The system shall allow a registered user to delete a saved resume from User Home. The delete action is initiated from the Resume Details modal on User Home. After soft-delete, the public resume link returns HTTP 410 Gone with the message "Пользователь решил удалить данное резюме. Больше оно не доступно."

**Business Value:**
Users need to remove outdated or unwanted resumes. Soft-delete ensures data is not permanently lost and the public link is properly deactivated.

**Acceptance Criteria:**
- User Home's Resume Details modal displays a "Delete this resume" button.
- Clicking "Delete this resume" shows a confirmation prompt: "Are you sure you want to delete this resume?" and reveals a "Confirm deletion" button.
- Clicking "Confirm deletion" soft-deletes the resume (sets `is_deleted = true` and `deleted_at` timestamp in `saved_resume`).
- After deletion, the resume row is removed from the User Home table.
- Accessing a deleted resume's public URL returns HTTP status code 410 Gone.
- The 410 page displays the message: "Пользователь решил удалить данное резюме. Больше оно не доступно."
- Private profile data, drafts, and other user data are not affected by the delete action.

**Affected UI:**
User Home (Resume Details modal).

**Affected Data:**
SavedResume (is_deleted, deleted_at).

**Related Artifacts:**
Decision Log (DEC-032), Change Request Log (CR-017), Traceability Matrix (TR-017).

**Readiness Check:**
- Business value clear: Yes
- Acceptance criteria clear: Partial
- Technically feasible: Yes
- UI/workflow identified: Yes
- Data impact identified: Yes
- Testable: Partial

**Notes:**
This requirement works together with FR-008. The Resume Details modal described in FR-008 gains a delete action. The existing `is_deleted` and `deleted_at` fields in `saved_resume` table are used for soft-delete. The `public_url_link` field in `saved_resume` stores the ready-made public resume URL.

### FR-999 [Requirement Title Template]

**Type:** [Functional Requirement / Non Functional Requirement / Transition Requirement / Stakeholder Requirement / Business Requirement]  
**Source:** [Project Vision / Elicitation Results / Wireframe Review / Technical Constraint / Security Review / Governance Decision / Capstone Constraint]  
**Priority:** [High / Medium / Low]  
**Scope:** [MVP / MVP Stretch / Post-MVP / Future Scope / Out of Scope]  
**Status:** [Draft]  
**Readiness:** [Needs Clarification  / Ready]

**Description:**  
[Write a concise requirement description.]

**Business Value:**  
[Explain why this requirement matters.]

**Acceptance Criteria:**
- [Criterion 1]
- [Criterion 2]
- [Criterion 3]

**Affected UI:**  
[Related screen/page/section or N/A.]

**Affected Data:**  
[Related entity/table/data object or N/A.]

**Related Artifacts:**  
[Decision Log, Change Request Log, Traceability Matrix, Risk Register, etc.]

**Readiness Check:**
- Business value clear: [Yes / No / Partial / N/A]
- Acceptance criteria clear: [Yes / No / Partial / N/A]
- Technically feasible: [Yes / No / Partial / N/A]
- UI/workflow identified: [Yes / No / Partial / N/A]
- Data impact identified: [Yes / No / Partial / N/A]
- Testable: [Yes / No / Partial / N/A]

**Notes:**  
[Additional notes, assumptions, gaps, or follow-up actions.]
