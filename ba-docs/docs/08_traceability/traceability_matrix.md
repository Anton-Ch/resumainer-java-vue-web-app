# Requirements Traceability Matrix

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-10  
**Last Updated:** 2026-05-23  
**Author:** Anton  
**Version:** 16.0  
**Status:** Active  
**Related BABOK Area:** 3.4 Plan Business Analysis Information Management  

---

## 1. Description

This document maintains **traceability** between business objectives, requirements, workflows, UI screens, data entities, test cases, and implementation references.

The goal is to ensure that project scope remains controlled and that each important requirement can be traced from business value to implementation and verification.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Add a row for each important requirement or requirement group.
- Keep traceability lightweight and useful.
- Do not block development because of excessive traceability detail.
- Update implementation references after development starts.
- Use `N/A` when a column is not applicable.
- Use stable IDs for business objectives, requirements, use cases, and tests.

### 2.2 Requirement Type Values

| Value | Meaning |
|---|---|
| BR | Business Requirement |
| STK | Stakeholder Requirement |
| FR | Functional Requirement |
| NFR | Non-Functional Requirement |
| TRN | Transition Requirement |

### 2.3 Trace Status Values

| Value | Meaning |
|---|---|
| Draft | Trace row is incomplete or not reviewed |
| Reviewed | Trace row was checked |
| Approved | Trace row is stable enough for baseline |
| Implemented | Related functionality is implemented |
| Verified | Related test or review confirms implementation |
| Superseded | Trace row was replaced by a newer one |

### 2.4 Test Coverage Values

| Value | Meaning |
|---|---|
| Not Started | No test case identified yet |
| Planned | Test case is planned but not implemented |
| Covered | Test case exists or manual test is documented |
| Not Applicable | Testing does not apply directly |


## 3. Summary Table

| Trace ID | Business Objective | Requirement ID | Requirement Type | Use Case / Workflow | UI Screen | Data Entity | Service / Component | Test Case | Test Coverage | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| TR-001 | BO-001 | BR-001 | BR | End-to-end resume adaptation workflow | User Home, My Profile, Generate Resume, Resume Review | Profile data, ResumeGenerationRequest, GeneratedResumeDraft, SavedResume | ProfileService, ResumeGenerationService, SavedResumeService, PdfGenerationService | TC-001 | Planned | Approved |
| TR-002 | BO-004 | STK-001 | STK | Recruiter opens public resume link | Public PDF Resume Link | SavedResume, PublicCode | PublicResumeService, PdfGenerationService | TC-002 | Planned | Approved |
| TR-003 | BO-001 | FR-001 | FR | Generate AI-assisted resume draft | Generate Resume, Resume Review | ResumeGenerationRequest, GeneratedResumeDraft, AiModel, AiUsageLog | ResumeGenerationService, AiClient, AiUsageLogService | TC-003 | Planned | Draft |
| TR-004 | BO-002 | FR-002 | FR | Complete contact profile | My Profile / Contact Details | ContactDetails | ProfileService, ContactDetailsDao | TC-004 | Planned | Approved |
| TR-005 | BO-002 | FR-003 | FR | Manage work experience | My Profile / Work Experience | WorkExperience | ProfileService, WorkExperienceDao | TC-005 | Planned | Approved |
| TR-006 | BO-002 | FR-004 | FR | Manage projects and volunteering | My Profile / Projects & Volunteering | Project | ProfileService, ProjectDao | TC-006 | Planned | Approved |
| TR-007 | BO-002 | FR-005 | FR | Manage education | My Profile / Education | Education | ProfileService, EducationDao | TC-007 | Planned | Approved |
| TR-008 | BO-002 | FR-006 | FR | Manage courses and certificates | My Profile / Courses & Certificates | CourseCertificate | ProfileService, CourseCertificateDao | TC-008 | Planned | Approved |
| TR-009 | BO-002 | FR-007 | FR | Manage additional profile info and settings | My Profile / Additional Info | AdditionalProfileInfo | ProfileService, AdditionalInfoDao | TC-009 | Planned | Draft |
| TR-010 | BO-003 | FR-008 | FR | View saved resumes on User Home | User Home | SavedResume | SavedResumeService, PdfGenerationService | TC-010 | Planned | Approved |
| TR-011 | BO-003 | FR-009 | FR | View resume PDF and actions from User Home (superseded) | User Home | SavedResume, PublicCode | SavedResumeService, PdfGenerationService, PublicResumeService | TC-011 | Not Applicable | Superseded |
| TR-012 | BO-005 | FR-010 | FR | Admin manages AI model details | AI Model Details | AiModel | AiModelService, AiModelDao | TC-012 | Planned | Approved |
| TR-013 | BO-005 | NFR-001 | NFR | Protect saved API keys | AI Model Details | AiModel | AiModelService, Security/Logging Components | TC-013 | Planned | Approved |
| TR-014 | BO-005 | TRN-001 | TRN | Prepare initial active AI model configuration | AI Models, AI Model Details | AiModel | AiModelService, Migration/Seed Script | TC-014 | Not Started | Draft |
| TR-015 | BO-001 | FR-011 | FR | Generate and edit cover letter | Resume Review, Resume Details modal | SavedResume (cover_letter), ResumeGenerationRequest (cover_letter) | ResumeGenerationService, AiClient | TC-015 | Not Started | Draft |
| TR-016 | BO-001 | FR-012 | FR | Include cover letter in generation request | Generate Resume | ResumeGenerationRequest (include_cover_letter) | ResumeGenerationService | TC-016 | Not Started | Draft |
| TR-017 | BO-003 | FR-013 | FR | Delete saved resume from User Home | User Home (Resume Details modal) | SavedResume (is_deleted, deleted_at) | SavedResumeService | TC-017 | Not Started | Draft |
| TR-018 | BO-006 | NFR-002 | NFR | Custom exception hierarchy | All layers (cross-cutting) | N/A | GlobalExceptionHandler, ServiceException, DaoException | TC-018 | Not Started | Draft |
| TR-019 | BO-006 | NFR-003 | NFR | Global exception handler | All controllers | N/A | GlobalExceptionHandler | TC-019 | Not Started | Draft |
| TR-020 | BO-006 | NFR-004 | NFR | Graceful error responses | All screens | N/A | GlobalExceptionHandler, ApiErrorResponse | TC-020 | Not Started | Draft |
| TR-021 | BO-006 | NFR-005 | NFR | Structured error logging | All layers (cross-cutting) | N/A | LoggingAspect, Services, DAOs | TC-021 | Not Started | Draft |
| TR-022 | BO-007 | NFR-006 | NFR | Standard package structure | N/A (code organization) | N/A | Entire project | TC-022 | Not Started | Draft |
| TR-023 | BO-007 | NFR-007 | NFR | Java Code Convention | N/A (code style) | N/A | Entire project | TC-023 | Not Started | Draft |
| TR-024 | BO-007 | NFR-008 | NFR | Javadoc on public service methods | N/A (code documentation) | N/A | Service and DAO interfaces | TC-024 | Not Started | Draft |
| TR-025 | BO-007 | NFR-009 | NFR | Maven CLI build | N/A (build) | N/A | pom.xml, Maven wrapper | TC-025 | Not Started | Draft |
| TR-026 | BO-007 | NFR-010 | NFR | .gitignore and README.md | N/A (repo root) | N/A | Repository root files | TC-026 | Not Started | Draft |
| TR-027 | BO-007 | NFR-011 | NFR | Minimal dependencies | N/A (build) | N/A | pom.xml | TC-027 | Not Started | Draft |
| TR-028 | BO-008 | NFR-012 | NFR | Service-layer transaction management | N/A (cross-cutting) | N/A | Service layer, ConnectionPool | TC-028 | Not Started | Draft |
| TR-029 | BO-008 | NFR-013 | NFR | SQL scripts for DB initialization | N/A (DB setup) | All entities | schema.sql, data.sql | TC-029 | Not Started | Draft |
| TR-030 | BO-008 | NFR-014 | NFR | Prevent SQL injection via PreparedStatement | N/A (data access) | N/A | All DAO classes | TC-030 | Not Started | Draft |
| TR-031 | BO-008 | NFR-015 | NFR | UTF-8 encoding for database | N/A (DB config) | All text columns | ConnectionPool, DB config | TC-031 | Not Started | Draft |
| TR-032 | BO-008 | NFR-016 | NFR | Custom thread-safe Connection Pool | N/A (infrastructure) | N/A | ConnectionPool | TC-032 | Not Started | Draft |
| TR-033 | BO-009 | NFR-017 | NFR | Form resubmission prevention | All forms | N/A | Controllers (PRG pattern), PrimeVue forms | TC-033 | Not Started | Draft |
| TR-034 | BO-009 | NFR-018 | NFR | User input XSS sanitization | My Profile, Generate Resume | All user-editable text fields | Validator/Sanitizer service | TC-034 | Not Started | Draft |
| TR-035 | BO-009 | NFR-019 | NFR | Dual validation frontend + backend | All forms | All entities with required fields | Controllers (@Valid), PrimeVue forms | TC-035 | Not Started | Draft |
| TR-036 | BO-010 | NFR-020 | NFR | Consistent log format | N/A (cross-cutting) | N/A | Logback config, all layers | TC-036 | Not Started | Draft |
| TR-037 | BO-011 | NFR-021 | NFR | Spring MVC Interceptors | All pages | N/A | HandlerInterceptors (logging, auth) | TC-037 | Not Started | Draft |
| TR-038 | BO-011 | NFR-022 | NFR | AOP for cross-cutting logic | N/A (service layer) | N/A | LoggingAspect | TC-038 | Not Started | Draft |
| TR-039 | BO-011 | NFR-023 | NFR | SOLID, DRY, and reusability | N/A (architecture) | All entities | All layers | TC-039 | Not Started | Draft |
| TR-040 | BO-012 | NFR-024 | NFR | Test coverage 50%+ Service+DAO | N/A (testing) | N/A | JaCoCo, JUnit 5, Mockito | TC-040 | Not Started | Draft |
| TR-041 | BO-012 | NFR-025 | NFR | Test scenarios (positive, negative, boundary) | N/A (testing) | N/A | Service, DAO test classes | TC-041 | Not Started | Draft |
| TR-042 | BO-012 | NFR-026 | NFR | Test structure and consistency | N/A (testing) | N/A | Test classes in src/test/java | TC-042 | Not Started | Draft |
| TR-043 | BO-012 | NFR-027 | NFR | Test-Driven Development approach | N/A (process) | N/A | All Service and DAO code | TC-043 | Not Started | Draft |
| TR-044 | BO-013 | NFR-029 | NFR | Pagination for all long lists | User Home, Admin tables | N/A | Backend LIMIT/OFFSET, PrimeVue pagination | TC-044 | Not Started | Draft |
| TR-045 | BO-013 | NFR-030 | NFR | i18n resource files | All pages | N/A | Spring MessageSource, vue-i18n | TC-045 | Not Started | Draft |
| TR-046 | BO-014 | NFR-031 | NFR | Swagger/OpenAPI documentation | N/A (API docs) | N/A | springdoc-openapi, Spring Security | TC-046 | Not Started | Draft |
| TR-047 | BO-014 | NFR-032 | NFR | Docker Compose deployment | N/A (infra) | All entities | docker-compose.yml, Dockerfiles | TC-047 | Not Started | Draft |
| TR-048 | BO-015 | NFR-033 | NFR | DB-backed budget configuration | N/A (backend) | resume_budget_configs, resume_template_selection_rules, resume_work_experience_distribution_rules, resume_section_budget_rules | ResumeBudgetConfigService | TC-048 | Not Started | Approved |
| TR-049 | BO-015 | NFR-034 | NFR | Active config fallback and versioning | N/A (backend) | resume_generation_request (budget_config_id), resume_budget_configs | ResumeBudgetConfigService | TC-049 | Not Started | Approved |
| TR-999 | BO-XXX | FR-XXX | FR | [Use case] | [Screen] | [Entity] | [Component] | TC-XXX | Not Started | Draft |

## 4. Details

### TR-001 Business Value Trace

**Business Objective:** BO-001 Reduce manual resume adaptation effort  
**Requirement ID:** BR-001  
**Requirement Type:** BR  
**Use Case / Workflow:** End-to-end resume adaptation workflow  
**UI Screen:** User Home, My Profile, Generate Resume, Resume Review  
**Data Entity:** Profile data, ResumeGenerationRequest, GeneratedResumeDraft, SavedResume  
**Service / Component:** ProfileService, ResumeGenerationService, SavedResumeService, PdfGenerationService  
**Test Case:** TC-001  
**Status:** Approved  
**Traceability Notes:** This trace connects the main business requirement with the complete MVP value chain: profile data, vacancy input, generated draft, review/edit, saved resume, PDF download (from User Home), and public sharing.  
**Gaps / Follow-up:** Define final end-to-end demo test after implementation plan is created.

### TR-002 Recruiter Public Resume Access

**Business Objective:** BO-004 Support recruiter-friendly resume sharing  
**Requirement ID:** STK-001  
**Requirement Type:** STK  
**Use Case / Workflow:** Recruiter opens public resume link  
**UI Screen:** Public PDF Resume Link  
**Data Entity:** SavedResume, PublicCode  
**Service / Component:** PublicResumeService, PdfGenerationService  
**Test Case:** TC-002  
**Status:** Approved  
**Traceability Notes:** This trace connects recruiter needs with direct public PDF access without registration. It also supports the privacy rule that only the saved resume PDF is exposed.  
**Gaps / Follow-up:** Define public URL validation and not-found/private/deleted resume behavior.

### TR-003 AI Resume Generation

**Business Objective:** BO-001 Reduce manual resume adaptation effort  
**Requirement ID:** FR-001  
**Requirement Type:** FR  
**Use Case / Workflow:** Generate AI-assisted resume draft  
**UI Screen:** Generate Resume, Resume Review  
**Data Entity:** ResumeGenerationRequest, GeneratedResumeDraft, AiModel, AiUsageLog, ResumeGenerationResponse (professional_title)  
**Service / Component:** ResumeGenerationService, AiClient, AiUsageLogService  
**Test Case:** TC-003  
**Status:** Draft  
**Traceability Notes:** This trace connects the core resume generation feature with vacancy input, model selection, draft generation, and review flow. The AI returns structured JSON (DEC-036) matching the generation response contract — backend parses JSON and populates entities directly. The AI generates a `professional_title` specific to the target vacancy, distinct from the user's `professional_title` in `contact_detail`. Text fields may contain limited HTML (`<strong>`, `<b>`, `<i>`, etc.) for formatting — backend sanitizes via allowlist (DEC-037, DEC-038). Resume sections follow fixed order defined in Resume Template Details (DEC-039).  
**Gaps / Follow-up:** Define final acceptance criteria for mock AI generation, real OpenRouter integration, timeout handling, and empty response handling.

### TR-004 Contact Details

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-002  
**Requirement Type:** FR  
**Use Case / Workflow:** Complete contact profile  
**UI Screen:** My Profile / Contact Details  
**Data Entity:** ContactDetails  
**Service / Component:** ProfileService, ContactDetailsDao  
**Test Case:** TC-004  
**Status:** Approved  
**Traceability Notes:** Contact details provide candidate identity and contact information for generated resumes.  
**Gaps / Follow-up:** Confirm final username/public URL validation rules if username is stored with contact or account settings.

### TR-005 Work Experience

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-003  
**Requirement Type:** FR  
**Use Case / Workflow:** Manage work experience  
**UI Screen:** My Profile / Work Experience  
**Data Entity:** WorkExperience  
**Service / Component:** ProfileService, WorkExperienceDao  
**Test Case:** TC-005  
**Status:** Approved  
**Traceability Notes:** Work experience is a core resume source section and must support add/edit/delete, required description, validation, and automatic sorting.  
**Gaps / Follow-up:** Confirm final employment type values only if employment type is added to MVP.

### TR-006 Projects and Volunteering

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-004  
**Requirement Type:** FR  
**Use Case / Workflow:** Manage projects and volunteering  
**UI Screen:** My Profile / Projects & Volunteering  
**Data Entity:** Project  
**Service / Component:** ProfileService, ProjectDao  
**Test Case:** TC-006  
**Status:** Approved  
**Traceability Notes:** Projects and volunteering support practical experience, portfolio positioning, and career-change evidence.  
**Gaps / Follow-up:** Confirm whether project type is needed in MVP.

### TR-007 Education

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-005  
**Requirement Type:** FR  
**Use Case / Workflow:** Manage education  
**UI Screen:** My Profile / Education  
**Data Entity:** Education  
**Service / Component:** ProfileService, EducationDao  
**Test Case:** TC-007  
**Status:** Approved  
**Traceability Notes:** Education supports formal background in resume generation. Start year is required.  
**Gaps / Follow-up:** Confirm valid year range rules.

### TR-008 Courses and Certificates

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-006  
**Requirement Type:** FR  
**Use Case / Workflow:** Manage courses and certificates  
**UI Screen:** My Profile / Courses & Certificates  
**Data Entity:** CourseCertificate  
**Service / Component:** ProfileService, CourseCertificateDao  
**Test Case:** TC-008  
**Status:** Approved  
**Traceability Notes:** Courses and certificates support professional development and career-change evidence.  
**Gaps / Follow-up:** Confirm whether certificate type is needed in MVP.

### TR-009 Additional Info and Settings

**Business Objective:** BO-002 Maintain structured user profile data  
**Requirement ID:** FR-007  
**Requirement Type:** FR  
**Use Case / Workflow:** Manage additional profile info and settings  
**UI Screen:** My Profile / Additional Info  
**Data Entity:** AdditionalProfileInfo  
**Service / Component:** ProfileService, AdditionalInfoDao  
**Test Case:** TC-009  
**Status:** Draft  
**Traceability Notes:** Additional Info stores simplified profile context and user preferences for MVP, including skills, languages, aspirations, achievements, resume language settings, general AI context, and username. Profile picture moved to POST-MVP per DEC-050/CR-019.  
**Gaps / Follow-up:** Confirm language dropdown values, username uniqueness rules, and final field length limits.

### TR-010 User Home Resume Listing

**Business Objective:** BO-003 Reuse and manage saved resume versions  
**Requirement ID:** FR-008  
**Requirement Type:** FR  
**Use Case / Workflow:** View saved resumes on User Home  
**UI Screen:** User Home  
**Data Entity:** SavedResume  
**Service / Component:** SavedResumeService, PdfGenerationService  
**Test Case:** TC-010  
**Status:** Approved  
**Traceability Notes:** User Home replaces the separate Resume History page and provides saved resume table, search, sorting, pagination, and a Details column. Clicking `Open details` opens a modal popup with PDF link copy, PDF download, and cover letter text per DEC-015/CR-014.  
**Gaps / Follow-up:** Define minimum search/sort behavior and pagination threshold.

### TR-011 Resume Details and PDF Actions (Superseded)

**Business Objective:** BO-003 Reuse and manage saved resume versions  
**Requirement ID:** FR-009 (Superseded)  
**Requirement Type:** FR  
**Use Case / Workflow:** View resume PDF and actions from User Home  
**UI Screen:** User Home  
**Data Entity:** SavedResume, PublicCode  
**Service / Component:** SavedResumeService, PdfGenerationService, PublicResumeService  
**Test Case:** TC-011  
**Status:** Superseded  
**Traceability Notes:** This trace is superseded. The Resume Details page was removed per DEC-014 / CR-013. PDF actions (download, public link copy) are handled directly from User Home (TR-010) and the post-save success flow.  
**Gaps / Follow-up:** Verify that TR-010 covers all required PDF actions from User Home.

### TR-012 Admin AI Model Management

**Business Objective:** BO-005 Support controlled AI model configuration  
**Requirement ID:** FR-010  
**Requirement Type:** FR  
**Use Case / Workflow:** Admin manages AI model details  
**UI Screen:** AI Model Details  
**Data Entity:** AiModel  
**Service / Component:** AiModelService, AiModelDao  
**Test Case:** TC-012  
**Status:** Approved  
**Traceability Notes:** AI Model Details allows admin to view and manage model metadata, provider settings, active status, and API key replacement/deletion.  
**Gaps / Follow-up:** Define exact validation rules for provider base URL, model code, and active/inactive model behavior.

### TR-013 API Key Protection

**Business Objective:** BO-005 Support controlled AI model configuration  
**Requirement ID:** NFR-001  
**Requirement Type:** NFR  
**Use Case / Workflow:** Protect saved API keys  
**UI Screen:** AI Model Details  
**Data Entity:** AiModel  
**Service / Component:** AiModelService, Security/Logging Components  
**Test Case:** TC-013  
**Status:** Approved  
**Traceability Notes:** Saved API keys must be masked, never logged, and available only for replacement or deletion after saving.  
**Gaps / Follow-up:** Define storage/encryption approach and logging exclusions during implementation.

### TR-014 Initial AI Model Configuration

**Business Objective:** BO-005 Support controlled AI model configuration  
**Requirement ID:** TRN-001  
**Requirement Type:** TRN  
**Use Case / Workflow:** Prepare initial active AI model configuration  
**UI Screen:** AI Models, AI Model Details  
**Data Entity:** AiModel  
**Service / Component:** AiModelService, Migration/Seed Script  
**Test Case:** TC-014  
**Status:** Draft  
**Traceability Notes:** The system needs at least one configured active AI model before generation can be demonstrated reliably.  
**Gaps / Follow-up:** Confirm whether seed data is created through SQL migration, admin UI, or manual setup before demo.

### TR-015 Cover Letter Generation

**Business Objective:** BO-001 Reduce manual resume adaptation effort
**Requirement ID:** FR-011
**Requirement Type:** FR
**Use Case / Workflow:** Generate and edit cover letter
**UI Screen:** Resume Review, Resume Details modal
**Data Entity:** SavedResume (cover_letter), ResumeGenerationRequest (cover_letter)
**Service / Component:** ResumeGenerationService, AiClient
**Test Case:** TC-015
**Status:** Draft
**Traceability Notes:** Cover letter is generated alongside resume draft. User can edit cover letter text in Resume Review before saving. Cover letter is viewable in the Resume Details modal on User Home per DEC-016/CR-015.
**Gaps / Follow-up:** Define cover letter generation prompt, max length, and whether cover letter generation is always on or toggleable.

### TR-016 Cover Letter in Generation Request

**Business Objective:** BO-001 Reduce manual resume adaptation effort
**Requirement ID:** FR-012
**Requirement Type:** FR
**Use Case / Workflow:** Include cover letter in generation request
**UI Screen:** Generate Resume
**Data Entity:** ResumeGenerationRequest (include_cover_letter)
**Service / Component:** ResumeGenerationService
**Test Case:** TC-016
**Status:** Draft
**Traceability Notes:** Generation request must include cover letter generation instruction for the AI. Cover letter output is stored separately from resume content.
**Gaps / Follow-up:** Define prompt format for cover letter generation and error handling if cover letter fails but resume succeeds.

### TR-017 Resume Delete from User Home

**Business Objective:** BO-003 Reuse and manage saved resume versions
**Requirement ID:** FR-013
**Requirement Type:** FR
**Use Case / Workflow:** Delete saved resume from User Home
**UI Screen:** User Home (Resume Details modal)
**Data Entity:** SavedResume (is_deleted, deleted_at)
**Service / Component:** SavedResumeService
**Test Case:** TC-017
**Status:** Draft
**Traceability Notes:** Resume delete action is initiated from the Resume Details modal. Soft-delete sets `is_deleted = true`. Public URL returns HTTP 410 Gone for deleted resumes.
**Gaps / Follow-up:** Define exact confirmation UI behavior and 410 page design.

### TR-018 Custom Exception Hierarchy

**Business Objective:** BO-006 Ensure system reliability and error transparency  
**Requirement ID:** NFR-002  
**Requirement Type:** NFR  
**Use Case / Workflow:** Custom exception hierarchy  
**UI Screen:** N/A (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** GlobalExceptionHandler, ServiceException, DaoException  
**Test Case:** TC-018  
**Status:** Draft  
**Traceability Notes:** Per-layer custom exceptions (ControllerException, ServiceException, DaoException) enable quick failure localization. Part of CR-020 error handling package.  
**Gaps / Follow-up:** Define exception class hierarchy and constructors during implementation.

### TR-019 Global Exception Handler

**Business Objective:** BO-006 Ensure system reliability and error transparency  
**Requirement ID:** NFR-003  
**Requirement Type:** NFR  
**Use Case / Workflow:** Global exception handler  
**UI Screen:** N/A (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** GlobalExceptionHandler  
**Test Case:** TC-019  
**Status:** Draft  
**Traceability Notes:** `@ControllerAdvice` catches all uncaught exceptions and returns standardized error responses. Works with NFR-002 and NFR-005.  
**Gaps / Follow-up:** Define error response JSON structure.

### TR-020 Graceful Error Responses

**Business Objective:** BO-006 Ensure system reliability and error transparency  
**Requirement ID:** NFR-004  
**Requirement Type:** NFR  
**Use Case / Workflow:** Graceful error responses  
**UI Screen:** All screens (global error behavior)  
**Data Entity:** N/A  
**Service / Component:** GlobalExceptionHandler, ApiErrorResponse  
**Test Case:** TC-020  
**Status:** Draft  
**Traceability Notes:** No Java stack traces exposed to Vue frontend. All technical details logged server-side.  
**Gaps / Follow-up:** Define error response DTO fields.

### TR-021 Structured Error Logging

**Business Objective:** BO-006 Ensure system reliability and error transparency  
**Requirement ID:** NFR-005  
**Requirement Type:** NFR  
**Use Case / Workflow:** Structured error logging  
**UI Screen:** N/A (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** LoggingAspect, Services, DAOs  
**Test Case:** TC-021  
**Status:** Draft  
**Traceability Notes:** SLF4J/Log4j2 with ERROR, WARN, INFO levels. Log messages include context without exposing secrets.  
**Gaps / Follow-up:** Define log format standards and sensitive data filters.

### TR-022 Standard Package Structure

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-006  
**Requirement Type:** NFR  
**Use Case / Workflow:** Standard package structure  
**UI Screen:** N/A (code organization)  
**Data Entity:** N/A  
**Service / Component:** Entire project  
**Test Case:** TC-022  
**Status:** Draft  
**Traceability Notes:** Package structure: controller, service, dao, model, config, util. Clear DAO/Service separation — no business logic in DAO.  
**Gaps / Follow-up:** Define base package name.

### TR-023 Java Code Convention

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-007  
**Requirement Type:** NFR  
**Use Case / Workflow:** Java Code Convention  
**UI Screen:** N/A (code style)  
**Data Entity:** N/A  
**Service / Component:** Entire project  
**Test Case:** TC-023  
**Status:** Draft  
**Traceability Notes:** Oracle Java Code Convention standards: naming, formatting, braces, indentation.  
**Gaps / Follow-up:** Configure IDE formatter or Checkstyle rules.

### TR-024 Javadoc on Public Service Methods

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-008  
**Requirement Type:** NFR  
**Use Case / Workflow:** Javadoc documentation  
**UI Screen:** N/A (code documentation)  
**Data Entity:** N/A  
**Service / Component:** Service and DAO interfaces  
**Test Case:** TC-024  
**Status:** Draft  
**Traceability Notes:** All public interface and service methods include @param, @return, @throws.  
**Gaps / Follow-up:** Define minimum Javadoc completeness standard.

### TR-025 Maven CLI Build

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-009  
**Requirement Type:** NFR  
**Use Case / Workflow:** Maven CLI build  
**UI Screen:** N/A (build)  
**Data Entity:** N/A  
**Service / Component:** pom.xml, Maven wrapper  
**Test Case:** TC-025  
**Status:** Draft  
**Traceability Notes:** `mvn clean package` completes without IDE. Build produces WAR/JAR artifact.  
**Gaps / Follow-up:** Verify build on clean checkout during implementation.

### TR-026 .gitignore and README.md

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-010  
**Requirement Type:** NFR  
**Use Case / Workflow:** Repository setup  
**UI Screen:** N/A (repo root)  
**Data Entity:** N/A  
**Service / Component:** Repository root files  
**Test Case:** TC-026  
**Status:** Draft  
**Traceability Notes:** .gitignore excludes IDE/build/secrets. README.md covers project, tech stack, build/run instructions.  
**Gaps / Follow-up:** Draft README.md during implementation.

### TR-027 Minimal Dependencies

**Business Objective:** BO-007 Ensure code quality and maintainability  
**Requirement ID:** NFR-011  
**Requirement Type:** NFR  
**Use Case / Workflow:** Dependency management  
**UI Screen:** N/A (build)  
**Data Entity:** N/A  
**Service / Component:** pom.xml  
**Test Case:** TC-027  
**Status:** Draft  
**Traceability Notes:** Only directly used, stable dependencies in pom.xml. No snapshots or beta versions.  
**Gaps / Follow-up:** Review dependency tree before implementation baseline.

### TR-028 Service-Layer Transaction Management

**Business Objective:** BO-008 Ensure robust database access layer  
**Requirement ID:** NFR-012  
**Requirement Type:** NFR  
**Use Case / Workflow:** Service-layer transaction management  
**UI Screen:** N/A (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** Service layer, ConnectionPool  
**Test Case:** TC-028  
**Status:** Draft  
**Traceability Notes:** Manual JDBC transaction control via connection.commit()/rollback() in Service layer. No Spring @Transactional.  
**Gaps / Follow-up:** Define transaction boundaries for each critical operation.

### TR-029 SQL Scripts for DB Initialization

**Business Objective:** BO-008 Ensure robust database access layer  
**Requirement ID:** NFR-013  
**Requirement Type:** NFR  
**Use Case / Workflow:** SQL scripts for DB initialization  
**UI Screen:** N/A (DB setup)  
**Data Entity:** All entities  
**Service / Component:** schema.sql, data.sql  
**Test Case:** TC-029  
**Status:** Draft  
**Traceability Notes:** schema.sql for DDL, data.sql for seed data. Runable against empty PostgreSQL.  
**Gaps / Follow-up:** Generate scripts from ERD during implementation.

### TR-030 SQL Injection Prevention

**Business Objective:** BO-008 Ensure robust database access layer  
**Requirement ID:** NFR-014  
**Requirement Type:** NFR  
**Use Case / Workflow:** Prevent SQL injection via PreparedStatement  
**UI Screen:** N/A (data access)  
**Data Entity:** N/A  
**Service / Component:** All DAO classes  
**Test Case:** TC-030  
**Status:** Draft  
**Traceability Notes:** All SQL queries use PreparedStatement with ? placeholders. String concatenation forbidden.  
**Gaps / Follow-up:** Enforce via code review checklist.

### TR-031 UTF-8 Encoding

**Business Objective:** BO-008 Ensure robust database access layer  
**Requirement ID:** NFR-015  
**Requirement Type:** NFR  
**Use Case / Workflow:** UTF-8 encoding for database  
**UI Screen:** N/A (DB config)  
**Data Entity:** All text columns  
**Service / Component:** ConnectionPool, DB configuration  
**Test Case:** TC-031  
**Status:** Draft  
**Traceability Notes:** UTF-8 encoding for DB, connections, and all text columns. Supports Cyrillic.  
**Gaps / Follow-up:** Configure at database creation and connection pool initialization.

### TR-032 Custom Thread-Safe Connection Pool

**Business Objective:** BO-008 Ensure robust database access layer  
**Requirement ID:** NFR-016  
**Requirement Type:** NFR  
**Use Case / Workflow:** Custom thread-safe Connection Pool  
**UI Screen:** N/A (infrastructure)  
**Data Entity:** N/A  
**Service / Component:** ConnectionPool  
**Test Case:** TC-032  
**Status:** Draft  
**Traceability Notes:** Manual implementation. No third-party pool libraries. Must be thoroughly documented for code review.  
**Gaps / Follow-up:** Document thread-safety mechanism, connection lifecycle, timeout handling, and edge cases.

### TR-033 Form Resubmission Prevention

**Business Objective:** BO-009 Ensure UI/UX security and validation integrity  
**Requirement ID:** NFR-017  
**Requirement Type:** NFR  
**Use Case / Workflow:** Form submission prevention  
**UI Screen:** All forms (Register, Login, My Profile, Generate Resume, Admin forms)  
**Data Entity:** N/A  
**Service / Component:** Controllers (PRG pattern), PrimeVue form components  
**Test Case:** TC-033  
**Status:** Draft  
**Traceability Notes:** PRG pattern prevents duplicate submissions on F5/Back. Button disable prevents double-clicks.  
**Gaps / Follow-up:** Confirm PRG implementation in all POST controllers.

### TR-034 User Input XSS Sanitization

**Business Objective:** BO-009 Ensure UI/UX security and validation integrity  
**Requirement ID:** NFR-018  
**Requirement Type:** NFR  
**Use Case / Workflow:** User input XSS mitigation  
**UI Screen:** My Profile, Generate Resume, Admin forms  
**Data Entity:** All user-editable text fields  
**Service / Component:** Input sanitizer, Vue template escaping  
**Test Case:** TC-034  
**Status:** Draft  
**Traceability Notes:** User input sanitized server-side on input; Vue's built-in escaping handles output. Covers gap not addressed by DEC-037/038 (which covers AI output only).  
**Gaps / Follow-up:** Define sanitization allowlist or approach.

### TR-035 Dual Validation Frontend + Backend

**Business Objective:** BO-009 Ensure UI/UX security and validation integrity  
**Requirement ID:** NFR-019  
**Requirement Type:** NFR  
**Use Case / Workflow:** Dual validation  
**UI Screen:** All forms  
**Data Entity:** All entities with required fields  
**Service / Component:** Controllers (@Valid), PrimeVue form validation  
**Test Case:** TC-035  
**Status:** Draft  
**Traceability Notes:** Frontend provides immediate UX feedback; backend enforces via @Valid with @Email, @NotNull, @NotEmpty, @Size. Backend is authoritative — enforces even if frontend is bypassed.  
**Gaps / Follow-up:** Map all Wireframe Field Requirements to DTO annotations.

### TR-036 Consistent Log Format

**Business Objective:** BO-010 Ensure consistent observability practices  
**Requirement ID:** NFR-020  
**Requirement Type:** NFR  
**Use Case / Workflow:** Log format consistency  
**UI Screen:** N/A (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** Logback config, all layers  
**Test Case:** TC-036  
**Status:** Draft  
**Traceability Notes:** Single log pattern (ISO 8601, level, thread, logger, message) applied across all layers via logback.xml. MDC injects user ID and request ID context.  
**Gaps / Follow-up:** Define exact log pattern and MDC keys.

### TR-037 Spring MVC Interceptors

**Business Objective:** BO-011 Apply professional architecture and design patterns  
**Requirement ID:** NFR-021  
**Requirement Type:** NFR  
**Use Case / Workflow:** Request logging and authorization  
**UI Screen:** All pages (cross-cutting)  
**Data Entity:** N/A  
**Service / Component:** HandlerInterceptor implementations (logging, auth)  
**Test Case:** TC-037  
**Status:** Draft  
**Traceability Notes:** RequestLoggingInterceptor logs HTTP method, URI, execution time, response status. AuthInterceptor checks authentication before secured endpoints. AdminInterceptor checks ADMIN role.  
**Gaps / Follow-up:** Define URL patterns for each interceptor.

### TR-038 AOP for Cross-Cutting Logic

**Business Objective:** BO-011 Apply professional architecture and design patterns  
**Requirement ID:** NFR-022  
**Requirement Type:** NFR  
**Use Case / Workflow:** AOP logging and monitoring  
**UI Screen:** N/A (service layer)  
**Data Entity:** N/A  
**Service / Component:** LoggingAspect (Spring AOP + AspectJ)  
**Test Case:** TC-038  
**Status:** Draft  
**Traceability Notes:** At least one @Aspect class with @Before, @AfterReturning, or @Around pointcuts for Service methods. Complements NFR-021 (interceptors handle web layer, AOP handles service layer).  
**Gaps / Follow-up:** Define pointcut expressions.

### TR-039 SOLID, DRY, and Reusability

**Business Objective:** BO-011 Apply professional architecture and design patterns  
**Requirement ID:** NFR-023  
**Requirement Type:** NFR  
**Use Case / Workflow:** Architecture principles  
**UI Screen:** N/A (architecture)  
**Data Entity:** All entities  
**Service / Component:** All layers  
**Test Case:** TC-039  
**Status:** Draft  
**Traceability Notes:** SOLID enforced via SRP (single-purpose classes), DIP (interface-based dependencies), OCP (extensible modules). DRY via reusable DAO utilities and shared Vue components.  
**Gaps / Follow-up:** Enforce via code review checklist.

### TR-040 Test Coverage 50%+ Service and DAO

**Business Objective:** BO-012 Ensure testing quality and coverage  
**Requirement ID:** NFR-024  
**Requirement Type:** NFR  
**Use Case / Workflow:** Coverage measurement  
**UI Screen:** N/A (testing)  
**Data Entity:** N/A  
**Service / Component:** JaCoCo, JUnit 5, Mockito  
**Test Case:** TC-040  
**Status:** Draft  
**Traceability Notes:** 50%+ line coverage in Service and DAO layers measured by JaCoCo. Report at target/site/jacoco/index.html.  
**Gaps / Follow-up:** Configure JaCoCo in pom.xml before development starts.

### TR-041 Test Scenarios

**Business Objective:** BO-012 Ensure testing quality and coverage  
**Requirement ID:** NFR-025  
**Requirement Type:** NFR  
**Use Case / Workflow:** Test scenario types  
**UI Screen:** N/A (testing)  
**Data Entity:** N/A  
**Service / Component:** Service and DAO test classes  
**Test Case:** TC-041  
**Status:** Draft  
**Traceability Notes:** Positive, negative, and boundary tests for Service and DAO methods. Validation and business rule consistency checked.  
**Gaps / Follow-up:** Define test scenario checklist.

### TR-042 Test Structure and Consistency

**Business Objective:** BO-012 Ensure testing quality and coverage  
**Requirement ID:** NFR-026  
**Requirement Type:** NFR  
**Use Case / Workflow:** Test organization  
**UI Screen:** N/A (testing)  
**Data Entity:** N/A  
**Service / Component:** Test classes in src/test/java  
**Test Case:** TC-042  
**Status:** Draft  
**Traceability Notes:** Mirror production package structure. Descriptive naming. arrange-act-assert pattern. Consistent test data.  
**Gaps / Follow-up:** Define naming convention examples.

### TR-043 Test-Driven Development

**Business Objective:** BO-012 Ensure testing quality and coverage  
**Requirement ID:** NFR-027  
**Requirement Type:** NFR  
**Use Case / Workflow:** TDD approach  
**UI Screen:** N/A (process)  
**Data Entity:** N/A  
**Service / Component:** All Service and DAO code  
**Test Case:** TC-043  
**Status:** Draft  
**Traceability Notes:** Test-first approach for Service and DAO development. Tests written before or alongside implementation code.  
**Gaps / Follow-up:** Apply to new Service methods; may relax for simple CRUD boilerplate.

### TR-044 Pagination for All Long Lists

**Business Objective:** BO-013 Deliver complete UI and localization readiness  
**Requirement ID:** NFR-029  
**Requirement Type:** NFR  
**Use Case / Workflow:** Paginated data browsing  
**UI Screen:** User Home, Admin Users, Admin Resumes, Admin AI Models  
**Data Entity:** N/A  
**Service / Component:** Backend LIMIT/OFFSET queries, PrimeVue pagination component  
**Test Case:** TC-044  
**Status:** Draft  
**Traceability Notes:** Pagination is mandatory for all list views. Configurable page size. Works with search and filters.  
**Gaps / Follow-up:** Define default page size.

### TR-045 i18n Resource Files

**Business Objective:** BO-013 Deliver complete UI and localization readiness  
**Requirement ID:** NFR-030  
**Requirement Type:** NFR  
**Use Case / Workflow:** Internationalization  
**UI Screen:** All pages (Landing Page, User app, Admin pages)  
**Data Entity:** N/A  
**Service / Component:** Spring MessageSource, vue-i18n  
**Test Case:** TC-045  
**Status:** Draft  
**Traceability Notes:** Resource files for EN and RU. Thymeleaf uses Spring MessageSource. Vue uses vue-i18n library. Language switcher in both layers.  
**Gaps / Follow-up:** Create initial resource files during implementation.

### TR-046 Swagger/OpenAPI Documentation

**Business Objective:** BO-014 Ensure deployment and documentation readiness  
**Requirement ID:** NFR-031  
**Requirement Type:** NFR  
**Use Case / Workflow:** REST API documentation  
**UI Screen:** N/A (API docs)  
**Data Entity:** N/A  
**Service / Component:** springdoc-openapi, Spring Security  
**Test Case:** TC-046  
**Status:** Draft  
**Traceability Notes:** Swagger UI at /swagger-ui.html in dev; ADMIN-only access in prod. All controller endpoints annotated with @Operation.  
**Gaps / Follow-up:** Configure springdoc-openapi in pom.xml.

### TR-047 Docker Compose Deployment

**Business Objective:** BO-014 Ensure deployment and documentation readiness  
**Requirement ID:** NFR-032  
**Requirement Type:** NFR  
**Use Case / Workflow:** Containerized deployment  
**UI Screen:** N/A (infrastructure)  
**Data Entity:** All entities  
**Service / Component:** docker-compose.yml, Dockerfiles  
**Test Case:** TC-047  
**Status:** Draft  
**Traceability Notes:** 3 containers: backend (Tomcat), frontend (Vue/Nginx), database (PostgreSQL). Flyway runs on startup. Single command deployment.  
**Gaps / Follow-up:** Create Dockerfiles during implementation.

### TR-048 DB-Backed Budget Configuration

**Business Objective:** BO-015 Ensure configurable resume budget constraints  
**Requirement ID:** NFR-033  
**Requirement Type:** NFR  
**Use Case / Workflow:** DB-backed budget configuration  
**UI Screen:** N/A (backend infrastructure)  
**Data Entity:** resume_budget_configs, resume_template_selection_rules, resume_work_experience_distribution_rules, resume_section_budget_rules  
**Service / Component:** ResumeBudgetConfigService  
**Test Case:** TC-048  
**Status:** Approved  
**Traceability Notes:** Resume budget configuration is stored in PostgreSQL instead of YAML. Four new tables store config identity, template selection rules, work experience distribution rules, and section budget rules. Backend reads active/newest config before every generation.  
**Gaps / Follow-up:** Define initial seed data for MVP budget configuration.

### TR-049 Active Config Fallback and Versioning

**Business Objective:** BO-015 Ensure configurable resume budget constraints  
**Requirement ID:** NFR-034  
**Requirement Type:** NFR  
**Use Case / Workflow:** Active config fallback and versioning  
**UI Screen:** N/A (backend infrastructure)  
**Data Entity:** resume_generation_request (budget_config_id, budget_config_version_used), resume_budget_configs  
**Service / Component:** ResumeBudgetConfigService  
**Test Case:** TC-049  
**Status:** Approved  
**Traceability Notes:** Backend implements multi-level fallback: prefer active config → newest active → newest any → configuration error. Generation request stores config ID and version for traceability.  
**Gaps / Follow-up:** Define error handling for missing required rule rows.

### TR-999 [Trace Item Title Template]

**Business Objective:** BO-XXX  
**Requirement ID:** FR-XXX  
**Requirement Type:** [BR / STK / FR / NFR / TRN]  
**Use Case / Workflow:** [Use case or workflow]  
**UI Screen:** [Related screen]  
**Data Entity:** [Related entity/table]  
**Service / Component:** [Related service/component]  
**Test Case:** TC-XXX  
**Status:** Draft  
**Traceability Notes:** [Explain why these items are connected]  
**Gaps / Follow-up:** [Missing links or next steps]

---

*This traceability matrix follows the Information Management Plan structure and conventions for the ResumAIner project.*