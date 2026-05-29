<!--
  Sync Impact Report (v1.0.0)
  Version change: (template) → 1.0.0
  Modified principles:
    - [PRINCIPLE_1_NAME] → I. Code Quality & Maintainability
    - [PRINCIPLE_2_NAME] → II. Testing Excellence
    - [PRINCIPLE_3_NAME] → III. User Experience Consistency
    - [PRINCIPLE_4_NAME] → IV. Performance & Reliability
    - [PRINCIPLE_5_NAME] → V. Security by Design
  Added sections:
    - Technology Stack & Architecture Constraints
    - Development Workflow & Quality Gates
    - Governance (amendment procedure, versioning policy, compliance)
  Removed sections: (none)
  Templates requiring updates:
    - .specify/templates/plan-template.md → ⚠ pending (Constitution Check section is generic)
    - .specify/templates/spec-template.md → ⚠ pending (no principle-specific sections yet)
    - .specify/templates/tasks-template.md → ⚠ pending (no constitution-driven task categories)
  Follow-up TODOs: (none)
-->

# ResumAIner Constitution

## Core Principles

### I. Code Quality & Maintainability

Every code change MUST preserve or improve the long-term maintainability of the
codebase. Code quality is not negotiable and is verified through automated and
manual review.

- **Package Structure**: Code MUST follow the standard Java layered architecture:
  `controller/`, `service/`, `dao/`, `model/`, `config/`, `util/`. Each layer
  has a single responsibility and communicates only with the layer below.
- **Java Code Convention**: All Java code MUST follow standard Java naming and
  formatting conventions. Class names are PascalCase, methods and variables are
  camelCase, constants are UPPER_SNAKE_CASE.
- **Javadoc**: All public service methods MUST have Javadoc comments explaining
  purpose, parameters, return values, and thrown exceptions. Internal/private
  methods SHOULD have inline comments when logic is non-trivial.
- **SOLID & DRY**: All architecture decisions MUST respect SOLID principles.
  Duplication MUST be eliminated through extraction, not copy-paste. A rejected
  alternative must be documented when a DRY violation is knowingly accepted.
- **Minimal Dependencies**: The `pom.xml` MUST contain only required
  dependencies. Every added dependency MUST be justified. No speculative or
  convenience libraries.
- **Design Patterns**: The project MUST document and apply at least 4 GoF design
  patterns (Singleton for Connection Pool, Builder for AI prompt construction,
  Factory Method for mock/real AI client creation, Strategy for adaptation level
  selection) with rationale in the Decision Log.
- **Maven CLI Build**: `mvn clean package` MUST succeed without IDE assistance.
  The build MUST run on a clean checkout.

### II. Testing Excellence

Testing is the primary mechanism for verifying correctness and preventing
regression. Tests are written first, are meaningful, and produce measurable
coverage.

- **Test Framework**: All tests MUST use JUnit 5 with Mockito for isolation.
- **Coverage Target**: Service and DAO layers MUST achieve at least 50% line
  coverage measured by JaCoCo. Coverage reports MUST be generated during the
  Maven build lifecycle.
- **TDD for Business Logic**: New business logic MUST follow Test-Driven
  Development: write a failing test first, then implement, then refactor. This
  applies to all new service and DAO methods.
- **Scenario Coverage**: Tests MUST cover positive paths, negative paths (error
  conditions), and boundary cases. Happy-path-only tests are insufficient.
- **Test Structure**: Every test MUST follow Arrange-Act-Assert structure. Test
  names MUST follow the `methodName_scenario_expectedResult` convention.
- **Determinism**: Tests MUST be deterministic and isolated. No test depends on
  another test's state. No test depends on external services unless mocked.
- **Mock AI Provider**: All AI-dependent tests MUST use the Mock AI provider.
  Real OpenRouter calls MUST NOT be made during automated testing.

### III. User Experience Consistency

The user experience MUST be consistent across all screens, languages, and
interaction patterns. Every user-facing interaction follows the same rules.

- **Internationalization**: All user-facing strings MUST be externalized into
  resource files (`messages_en.properties`, `messages_ru.properties`) for both
  Thymeleaf (Landing Page) and Vue SPA. No hardcoded UI text is permitted.
- **Dual Validation**: Every form MUST validate input on both frontend
  (Vuelidate + Vue 3 native validation for immediate feedback) and backend
  (Spring `@Valid` with Jakarta Validation annotations `@NotNull`, `@NotEmpty`,
  `@Email`, `@Size` as the authoritative check).
- **PRG Pattern (Post-Redirect-Get)**: All form submission endpoints MUST
  implement the Post-Redirect-Get pattern to prevent duplicate submissions.
  Submit buttons MUST be disabled after the first click.
- **Card List + Add/Edit Pattern**: Repeatable profile sections MUST use the
  card list with Add/Edit form pattern consistently. No mixing of different
  interaction patterns for the same data type.
- **Automatic Sorting**: All repeatable profile sections MUST be sorted
  automatically by the system. Users do not manually order records.
- **Error Messages**: All error messages MUST be user-readable, consistent in
  tone, and never expose stack traces or internal system details. Validation
  errors MUST appear inline next to the relevant field or as toast messages.
- **Pagination**: All data tables with more than 10 rows MUST implement
  pagination. This applies to User Home, Admin Users, Admin Resumes, and Admin
  AI Models views.
- **Empty States**: Every data view MUST display a meaningful empty state
  message when no data exists, guiding the user to the next action.

### IV. Performance & Reliability

Performance and reliability are cross-cutting concerns that affect every layer
from database to frontend rendering.

- **Database Access**: All database queries MUST use `PreparedStatement` to
  prevent SQL injection and enable query plan reuse. Raw string concatenation
  for SQL is forbidden.
- **Connection Pool**: The custom thread-safe Connection Pool MUST be thoroughly
  documented covering thread-safety mechanism, connection lifecycle, timeout
  handling, and edge cases. Pool health MUST be observable through logging.
- **Transaction Management**: All critical business operations (registration,
  generation + save) MUST use manual JDBC transaction management
  (`commit()`/`rollback()`) at the Service layer. Transaction boundaries MUST
  be explicit and documented.
- **Pagination Performance**: All list queries MUST use SQL-level pagination
  (LIMIT/OFFSET) and corresponding indexes. No in-memory pagination is
  permitted.
- **UTF-8 Encoding**: The database, all database connections, and all text
  columns MUST use UTF-8 encoding to support Cyrillic characters.
- **PDF Generation**: The backend MUST generate PDFs server-side using HTML
  templates. Post-generation validation MUST verify the page count (one page vs
  two pages). PDF rendering MUST be tested for A4 layout with selectable text.
- **Resume Budget Configuration**: Resume budget configuration (sentence counts,
  bullet limits, skill limits) MUST be stored in PostgreSQL and read before
  every generation. Configuration changes MUST take effect without application
  restart.
- **Content Budget Enforcement**: AI-generated content MUST respect the active
  budget configuration. The system MUST validate output size against budgets
  before presenting to the user.

### V. Security by Design

Security MUST be integrated into every feature from design through
implementation. Security is not an afterthought.

- **Password Storage**: All passwords MUST be hashed using BCrypt. No plain-text
  or reversible encryption is permitted.
- **API Key Protection**: Saved API keys MUST always be masked in the UI.
  Administrators can replace or delete a key, but MUST NOT view the full key
  after saving. API keys MUST NOT appear in logs.
- **XSS Sanitization**: All user-generated input MUST be sanitized against XSS
  on input. AI-generated HTML content MUST be sanitized using an allowlist of
  approved tags before storage and rendering.
- **Error Safety**: All error responses MUST return graceful, user-readable
  messages. Stack traces and internal system details MUST NOT be exposed to the
  client. Validation errors are logged at WARN level for monitoring.
- **Access Control**: Admin-only endpoints MUST enforce authorization on the
  backend. Frontend-only hiding of admin controls is insufficient. Public resume
  links MUST expose only the finalized PDF output — no profile data, drafts, or
  usage data.
- **Public Link Safety**: Soft-deleted resumes MUST return HTTP 410 Gone for
  their public links. Public PDF links MUST NOT require authentication.
- **Log Safety**: No secrets, API keys, or personal data beyond what is
  necessary MUST be written to logs. Logback configuration MUST specify the log
  format, rotation, and level policy.
- **Dual Validation (Security)**: Backend validation is the authoritative
  security boundary. Frontend validation improves UX but MUST NOT be trusted for
  security.

## Technology Stack & Architecture Constraints

The following technology decisions are mandatory and MUST NOT be changed without
a governance amendment.

| Layer             | Technology                                                                | Constraint                                       |
| ----------------- | ------------------------------------------------------------------------- | ------------------------------------------------ |
| Language          | Java 21 LTS                                                               | Required                                         |
| Web Framework     | Spring MVC (no Spring Boot)                                               | Required                                         |
| Data Access       | Plain JDBC with custom thread-safe Connection Pool                        | Required. No ORM, JPA, Hibernate, or Spring Data |
| Database          | PostgreSQL (3NF normalized)                                               | Required                                         |
| Migrations        | Flyway (versioned SQL scripts)                                            | Required                                         |
| Frontend SPA      | Vue 3 (Composition API) + Vite + PrimeVue                                 | Required                                         |
| Landing Page      | Thymeleaf                                                                 | Required                                         |
| AI Integration    | OpenRouter API behind `AiClientFactory` interface                         | Required. Mock AI for dev/test                   |
| PDF Generation    | Server-side HTML-to-PDF                                                   | Required. Not on frontend                        |
| Logging           | SLF4J + Logback                                                           | Required                                         |
| API Documentation | Swagger/OpenAPI (springdoc-openapi)                                       | ADMIN-only in production                         |
| Testing           | JUnit 5 + Mockito + JaCoCo                                                | Required. 50%+ coverage on Service/DAO           |
| Build             | Maven                                                                     | Required. `mvn clean package` must work          |
| Deployment        | Docker Compose (3 containers: backend Tomcat, frontend Nginx, PostgreSQL) | Required                                         |
| Profiles          | `dev` and `prod` Spring profiles                                          | Required                                         |

Additional architecture constraints:
- Database MUST be normalized to 3NF. Denormalization requires documented
  justification and governance approval.
- Transactions MUST be managed manually via JDBC `commit()`/`rollback()` at the
  Service layer.
- Spring MVC HandlerInterceptors MUST handle request logging and authorization.
- Spring AOP with AspectJ MUST handle service-layer cross-cutting concerns
  (logging, monitoring).
- AI integration MUST be isolated behind a service interface. Mock AI provider
  MUST be available for development and testing at all times.
- Vue MUST NOT render final PDF templates. The backend controls all final
  rendering.
- All text fields MUST use UTF-8 encoding.

## Development Workflow & Quality Gates

Every feature follows the Spec Kit memory-first workflow:

```
constitution → specify → clarify → plan → tasks → implement → review → commit
```

### Quality Gates

Each phase has a mandatory pass/fail gate:

1. **Spec Gate**: The specification MUST reference the relevant requirements
   from `requirements_log.md` and trace to acceptance criteria. No spec proceeds
   without traceability.

2. **Plan Gate**: The plan MUST include a Constitution Check section verifying
   that the proposed implementation respects all active principles. Any
   violation MUST be documented with justification and a rejected simpler
   alternative.

3. **Tasks Gate**: Tasks MUST be categorized to reflect constitution-driven
   concerns: code quality tasks (Javadoc, structure), testing tasks (unit,
   integration, coverage), UX tasks (i18n, validation), security tasks (XSS,
   access control), and performance tasks (pagination, transaction boundaries).

4. **Implementation Gate**: Before marking any task complete, the implementer
   MUST verify:
   - Tests pass (all existing + new)
   - No secrets committed
   - No stack traces exposed
   - i18n strings externalized
   - `PreparedStatement` used for all queries
   - Pagination applied where applicable
   - Error handling covers positive, negative, and boundary cases

5. **Review Gate**: Every implementation MUST be reviewed against the
   constitution principles before commit. Violations MUST block the commit.

### Commit Standards

- Commit messages MUST follow Conventional Commits format:
  `type(scope): description`
- Commits MUST be scoped to a single logical change.
- No commit MAY contain secrets, API keys, or sensitive configuration.

## Governance

### Amendment Procedure

1. **Proposal**: Any principle, constraint, or governance rule MAY be proposed
   for amendment via a change request in `decision_log.md` and
   `change_request_log.md`.
2. **Review**: The proposed amendment MUST be reviewed for impact on all
   existing principles and downstream artifacts.
3. **Approval**: The amendment MUST be approved by the project owner before
   taking effect.
4. **Documentation**: The amendment MUST be recorded in `DECISIONS.md` with
   rationale, rejected alternatives, and impact analysis.
5. **Propagation**: All affected templates (plan, spec, tasks) MUST be updated
   to reflect the amendment.

### Versioning Policy

This constitution follows semantic versioning:

- **MAJOR** (1.x.x → 2.0.0): Backward-incompatible principle removals or
  redefinitions.
- **MINOR** (1.0.x → 1.1.0): New principle or section added, or materially
  expanded guidance.
- **PATCH** (1.0.0 → 1.0.1): Clarifications, wording refinements, typo fixes.

### Compliance Review

- All PRs and feature implementations MUST be checked for constitution
  compliance during the Review Gate.
- Complexity MUST be justified when it violates a principle. The justification
  MUST be documented in the plan's Constitution Check section.
- This constitution supersedes all other practices where conflicts exist.
- Any exception to a principle MUST be documented, approved, and time-boxed.

**Version**: 1.0.0 | **Ratified**: 2026-05-29 | **Last Amended**: 2026-05-29