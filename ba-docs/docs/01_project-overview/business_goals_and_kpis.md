# Business Goals and KPIs

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-18
**Last Updated:** 2026-05-23
**Author:** Anton
**Version:** 10.0
**Status:** Approved
**Related BABOK Area:** 6.2 Define Future State (SMART Goals)

***

## 1. Description

This document defines the measurable business goals for the ResumAIner project. Each goal follows the **SMART** criteria — Specific, Measurable, Achievable, Relevant, and Time-bound. These goals translate the business need (BR-001) into quantifiable targets that will be used to evaluate project success.

## 2. Goal Overview

| ID     | Goal Title                                                            | Linked Business Need |
| ------ | --------------------------------------------------------------------- | -------------------- |
| BG-001 | Reduce resume adaptation time from manual hours to AI-powered minutes | BR-001               |
| BG-002 | Ensure system reliability and error transparency                      | Capstone Constraint  |
| BG-003 | Ensure code quality and maintainability                               | Capstone Constraint  |
| BG-004 | Ensure robust database access layer                                    | Capstone Constraint  |
| BG-005 | Ensure UI/UX security and validation integrity                         | Capstone Constraint  |
| BG-006 | Ensure consistent observability practices                              | Capstone Constraint  |
| BG-007 | Apply professional architecture and design patterns                    | Capstone Constraint  |
| BG-008 | Ensure testing quality and coverage                                     | Capstone Constraint  |
| BG-009 | Deliver complete UI and localization readiness                          | Capstone Constraint  |
| BG-010 | Ensure deployment and documentation readiness                           | Capstone Constraint  |
| BG-011 | Ensure configurable resume budget constraints                           | Governance Decision  |

## 3. SMART Goal Details

### BG-001: Reduce Resume Adaptation Time

#### Description

Users currently spend 2-3 hours manually adapting each resume using fragmented tools (Word, ChatGPT, email). ResumAIner should reduce this to under 10 minutes by providing structured profile storage and AI-powered vacancy-specific generation, enabling users to apply to more vacancies with professionally adapted resumes.

#### SMART Verification

| Criteria       | Assessment                                                                                                                                               |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **S**pecific   | Reduce the time required to produce a vacancy-adapted resume                                                                                             |
| **M**easurable | Tracked as "time from vacancy input to ready-to-save resume" (minutes)                                                                                   |
| **A**chievable | AI-powered generation with structured profile data can produce results in under 10 minutes; manual review/edit is the remaining user-controlled variable |
| **R**elevant   | Directly addresses the core business need (BR-001) — reducing manual adaptation effort                                                                   |
| **T**ime-bound | Target deadline aligned with Capstone project delivery                                                                                                   |

#### KPIs

| #   | KPI                     | Metric                                       | Baseline (Current)                           | Target (Goal)                      | Deadline   |
| --- | ----------------------- | -------------------------------------------- | -------------------------------------------- | ---------------------------------- | ---------- |
| 1   | Resume adaptation time  | Time to generate an adapted resume (minutes) | 120-180 minutes (manual)                     | <10 minutes                        | 2026-06-30 |
| 2   | Multilingual generation | Languages supported for automated generation | 1-2 (manual translation or separate version) | 2 (Russian and English, automated) | 2026-06-30 |

#### Measurement Approach

| KPI                     | How to Measure                                                                                  | Frequency           |
| ----------------------- | ----------------------------------------------------------------------------------------------- | ------------------- |
| Resume adaptation time  | Track from "Generate Resume" button click to draft preview displayed; exclude user editing time | Per-generation      |
| Multilingual generation | Language options available and functional in the generation flow                                | Verified at release |

### BG-002: Ensure System Reliability and Error Transparency

#### Description

The system must handle all errors consistently across controller, service, and DAO layers. Custom exceptions per layer enable quick failure localization. Errors are logged with sufficient context and returned to the user in a readable format without exposing stack traces.

#### SMART Verification

| Criteria       | Assessment |
| -------------- | ---------- |
| **S**pecific   | Handle, log, and report errors consistently across all application layers |
| **M**easurable | Count of uncovered try-catch blocks (target: zero); presence of per-layer custom exceptions |
| **A**chievable | Cross-cutting infrastructure concern; can be implemented once and applied everywhere |
| **R**elevant   | Directly required by Capstone specification and professional code quality standards |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Custom exception coverage | Layers with dedicated exception class | 0 | 3 (controller, service, DAO) | 2026-06-30 |
| 2 | Stack trace exposure | Percentage of errors exposing trace to client | Unknown | 0% | 2026-06-30 |

### BG-003: Ensure Code Quality and Maintainability

#### Description

The codebase follows standard package structure, Java Code Convention, Javadoc on public methods, Maven CLI build capability, proper repository setup (.gitignore, README.md), and minimal dependencies. These quality standards are required by the Capstone specification.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Establish package structure, code style, Javadoc, CLI build, and repository standards |
| **M**easurable | Package structure follows convention; `mvn clean package` succeeds; .gitignore and README.md present |
| **A**chievable | Standard developer practices; single configuration effort |
| **R**elevant | Required per Capstone specification for professional code presentation |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Package structure compliance | Code organized by layer (controller/service/dao/model/config/util) | None | Full compliance | 2026-06-30 |
| 2 | CLI build success | `mvn clean package` completes without IDE | No project yet | Success | 2026-06-30 |
| 3 | Repository completeness | .gitignore and README.md present | No project yet | Both present | 2026-06-30 |

### BG-004: Ensure Robust Database Access Layer

#### Description

The database access layer must follow enterprise-grade practices: custom thread-safe Connection Pool with thorough documentation, Service-layer transaction management via manual JDBC commit/rollback, SQL scripts for complete database initialization, PreparedStatement for all queries, and UTF-8 encoding for Cyrillic support. These requirements are mandated by the Capstone specification.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Implement custom thread-safe Connection Pool, transaction management, SQL scripts, PreparedStatement, and UTF-8 encoding |
| **M**easurable | Connection Pool operational with documented internals; `mvn clean package` includes SQL scripts; code review confirms PreparedStatement-only queries; UTF-8 data round-trips correctly |
| **A**chievable | Standard DAO infrastructure; Connection Pool is a single well-scoped component |
| **R**elevant | Directly required by Capstone specification and security best practices |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Connection Pool documentation | Internal docs covering thread-safety, lifecycle, timeouts | None | Complete and review-ready | 2026-06-30 |
| 2 | Transaction coverage | Critical operations wrapped in manual transactions | None | 100% (register, generate+save) | 2026-06-30 |
| 3 | SQL injection prevention | DAO methods using PreparedStatement | None | 100% | 2026-06-30 |

### BG-005: Ensure UI/UX Security and Validation Integrity

#### Description

The system must protect against form resubmission (PRG pattern, button disable), sanitize user input against XSS attacks, and enforce dual validation — frontend validation for immediate UX feedback and backend validation with Spring @Valid and Jakarta Validation annotations (@Email, @NotNull, @NotEmpty, @Size) as the authoritative check.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Implement PRG pattern, XSS sanitization, and dual frontend/backend validation across all forms |
| **M**easurable | All POST endpoints use PRG; all user input is sanitized; all required DTO fields have @NotNull/@NotEmpty |
| **A**chievable | Standard Spring/PrimeVue features; single implementation pass across all forms |
| **R**elevant | Required per Capstone specification covering form resubmission, XSS, and validation |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Form resubmission prevention | POST endpoints implementing PRG | None | 100% | 2026-06-30 |
| 2 | User input sanitization coverage | User-facing text fields sanitized on input | None | 100% | 2026-06-30 |
| 3 | Backend validation completeness | Required DTO fields with @NotNull/@NotEmpty | None | 100% | 2026-06-30 |

### BG-006: Ensure Consistent Observability Practices

#### Description

All application layers must use a consistent log format with ISO 8601 timestamps, MDC context (user ID, request ID), and unified log levels. Logback replaces Log4j2 as a lighter logging implementation. Validation errors are logged at WARN level to support security monitoring.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Define and apply a single log format pattern across Controller, Service, and DAO layers using SLF4J + Logback |
| **M**easurable | Log format consistency verified by inspecting log output from each layer; logback.xml defines one pattern |
| **A**chievable | Single configuration file (logback.xml) applied application-wide |
| **R**elevant | Consistent logging is required for debugging, monitoring, and audit |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Log format consistency | Layers using the same log pattern | None | 100% (Controller, Service, DAO) | 2026-06-30 |
| 2 | MDC context coverage | Log entries containing user ID and request ID | None | 100% of ERROR and WARN entries | 2026-06-30 |

### BG-007: Apply Professional Architecture and Design Patterns

#### Description

The system must apply at least 4 documented GoF design patterns with clear rationale: Singleton (Connection Pool), Builder (AI prompt construction), Factory Method (mock vs real AI client creation), Strategy (adaptation level selection). Spring MVC Interceptors handle request logging and authorization. AOP handles service-layer cross-cutting concerns. SOLID and DRY principles guide all architecture decisions.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Apply 4 GoF patterns, Spring MVC Interceptors, AOP, SOLID, DRY |
| **M**easurable | Pattern catalog documented in Decision Log; interceptor and aspect classes exist; code review confirms SOLID/DRY compliance |
| **A**chievable | Standard enterprise patterns for a Spring MVC application |
| **R**elevant | Required per Capstone specification |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Documented patterns | GoF patterns with documented rationale | None | 4 (Singleton, Builder, Factory Method, Strategy) | 2026-06-30 |
| 2 | Interceptor coverage | Request paths covered by logging and auth interceptors | None | 100% of secured endpoints | 2026-06-30 |
| 3 | AOP aspect count | @Aspect classes implementing cross-cutting logic | None | 1+ aspect class | 2026-06-30 |

### BG-008: Ensure Testing Quality and Coverage

#### Description

The project must achieve 50%+ line coverage in Service and DAO layers using JUnit 5 and Mockito, measured by JaCoCo. Tests cover positive, negative, and boundary scenarios. Tests are structured, consistent, and readable. TDD approach is applied for new business logic.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Achieve 50%+ test coverage in Service and DAO layers; cover positive, negative, and boundary scenarios |
| **M**easurable | JaCoCo coverage report; code review confirms scenario coverage and test structure |
| **A**chievable | Standard testing practices with JUnit 5 + Mockito + JaCoCo |
| **R**elevant | Required per Capstone specification |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Service layer coverage | Line coverage percentage | 0% | 50%+ | 2026-06-30 |
| 2 | DAO layer coverage | Line coverage percentage | 0% | 50%+ | 2026-06-30 |
| 3 | Test structure compliance | Tests following naming convention and arrange-act-assert | None | 100% code review pass | 2026-06-30 |

### BG-009: Deliver Complete UI and Localization Readiness

#### Description

All data tables must implement pagination for performance and usability. The interface must support English and Russian languages via resource files (messages_en.properties, messages_ru.properties) for both Thymeleaf (Landing Page) and Vue SPA. All user-facing strings must be externalized.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Implement pagination on all list views; provide i18n resource files for EN and RU for both Thymeleaf and Vue |
| **M**easurable | Pagination controls visible on all data tables; resource files exist; language switcher functions correctly |
| **A**chievable | Standard PrimeVue pagination component; standard Spring/Vue i18n patterns |
| **R**elevant | Required for maximum Capstone evaluation scores |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Pagination coverage | List views with pagination | 0 | 4 (User Home, Admin Users, Admin Resumes, Admin AI Models) | 2026-06-30 |
| 2 | i18n resource files | .properties files for supported languages | 0 | 2 (EN, RU) | 2026-06-30 |

### BG-010: Ensure Deployment and Documentation Readiness

#### Description

The project must include Swagger/OpenAPI REST documentation with ADMIN-only access in production, Docker Compose with 3 containers (backend Tomcat, Vue Nginx, PostgreSQL), and separate dev/prod Spring profiles.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Implement Swagger, Docker Compose, and dev/prod profiles |
| **M**easurable | Swagger UI accessible; docker compose up starts full stack; profile-specific configs exist |
| **A**chievable | Standard tools: springdoc-openapi, Docker Compose, Spring profile mechanism |
| **R**elevant | Required for portfolio demonstration and reproducible deployment |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Docker Compose readiness | Containers defined | 0 | 3 (backend, frontend, database) | 2026-06-30 |
| 2 | Swagger availability | API documented endpoints | 0 | All controllers | 2026-06-30 |

### BG-011: Ensure Configurable Resume Budget Constraints

#### Description

Resume budget configuration (sentence counts, bullet limits, skill limits, job distribution rules) must be stored in PostgreSQL and configurable without Java code changes or application restart. The backend reads the active configuration before every resume generation.

#### SMART Verification

| Criteria | Assessment |
|---|---|
| **S**pecific | Store budget configuration in PostgreSQL; backend reads before each generation |
| **M**easurable | Config changes take effect on next generation without code/restart change |
| **A**chievable | DB-backed configuration with active config fallback and versioning |
| **R**elevant | Enables runtime budget tuning without developer intervention |
| **T**ime-bound | Verified before final Capstone submission |

#### KPIs

| # | KPI | Metric | Baseline (Current) | Target (Goal) | Deadline |
|---|---|---|---|---|---|
| 1 | Budget config tables created | Tables | 0 | 4 | 2026-06-30 |
| 2 | Config readiness delay | Generations without config | Unlimited | None (error returned) | 2026-06-30 |

## 4. Dependencies and Assumptions

| Dependency / Assumption                                        | Impact on Goal                                                                        |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| AI API (OpenRouter) must be accessible and responsive          | Generation time depends on API response speed; target of <10 min includes API latency |
| User must complete structured profile before generation        | Goal assumes profile data is already entered, first setup time is separate            |
| AI output quality must be sufficient for meaningful adaptation | If AI output requires heavy editing, effective time savings are reduced               |

***

*This document is part of the ResumAIner business analysis portfolio. Goals and KPIs should be reviewed after MVP delivery to validate assumptions and refine targets for future iterations.*
