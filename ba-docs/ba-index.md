# BA Index — ResumAIner

**Purpose:** lightweight navigation layer for AI agents and developers working on the ResumAIner implementation.

This file helps an agent find the right Business Analysis / System Analysis artifact without loading the full BA package or guessing by folder names.

**Generated from:** `complete_business_analysis.md`  
**Recommended location:** repository root or `docs/ba-index.md`  
**Last generated:** 2026-05-29

---

## 1. Agent Reading Protocol

1. Start with this file before reading BA artifacts.
2. Use the **Quick Routing Table** to choose the smallest relevant source file.
3. Read only the selected source artifact and any directly linked source-of-truth artifact.
4. Do not load `complete_business_analysis.md` during normal implementation work.
5. If a requirement, screen, workflow, or data model changes, update the source artifact and the related tracking artifacts listed here.
6. If artifacts conflict, prefer approved current-state artifacts over older planning or elicitation artifacts.

---

## 2. Source-of-Truth Priority

Use this order when artifacts overlap:

| Need | Primary source | Secondary source |
|---|---|---|
| Current MVP scope and acceptance criteria | `docs/02_requirements/requirements_log.md` | `docs/08_traceability/traceability_matrix.md` |
| Business reason / problem / goals | `docs/01_project-overview/strategic_context_and_gap_analysis.md` | `docs/01_project-overview/business_goals_and_kpis.md` |
| User/admin/recruiter process flow | `docs/03_processes-and-workflows/user_workflows.md` | `docs/05_ui-ux/wireframes_detailed_description.md` |
| Database tables and fields | `docs/04_domain-and-data-model/data_dictionary.md` | `docs/04_domain-and-data-model/dbml_erd.md` |
| UI fields, validation, and messages | `docs/05_ui-ux/wireframe_field_requirements.md` | `docs/05_ui-ux/wireframes_detailed_description.md` |
| Resume generation, JSON contract, template budgets | `docs/05_ui-ux/resume_template_details_and_logic.md` | HTML templates in `docs/05_ui-ux/` |
| Final rationale for accepted choices | `docs/09_decisions/decision_log.md` | `docs/07_project-management/change_request_log.md` |
| Change history | `docs/07_project-management/change_request_log.md` | `docs/09_decisions/decision_log.md` |
| Unresolved or previously resolved questions | `docs/07_project-management/open_questions_log.md` | `docs/09_decisions/decision_log.md` |
| Risk and mitigation | `docs/07_project-management/risk_register.md` | `docs/07_project-management/ba-planning-and-monitoring/governance_plan.md` |

---

## 3. Quick Routing Table

| Agent question | Read first | Also read if needed |
|---|---|---|
| What is this product and stack? | `README.md` | `strategic_context_and_gap_analysis.md` |
| Why does this project exist? | `strategic_context_and_gap_analysis.md` | `business_goals_and_kpis.md` |
| What exactly must be implemented? | `requirements_log.md` | `traceability_matrix.md`, `decision_log.md` |
| What are the acceptance criteria? | `requirements_log.md` | `traceability_matrix.md` |
| How does the user/admin flow work? | `user_workflows.md` | `wireframes_detailed_description.md` |
| What screens exist? | `wireframes_detailed_description.md` | `confirmed_elicitation_results.md` |
| What fields and validations should a screen have? | `wireframe_field_requirements.md` | `requirements_log.md`, `data_dictionary.md` |
| What DB tables/columns are needed? | `data_dictionary.md` | `dbml_erd.md` |
| What ERD format should I use? | `dbml_erd.md` for dbdiagram.io | `mermaid_erd.md`, `plantuml_erd.puml` |
| How should resume generation output be structured? | `resume_template_details_and_logic.md` | `one_pager_template.html`, `two_pager_template.html` |
| How should PDF/resume layout behave? | `resume_template_details_and_logic.md` | HTML templates |
| Why was a design choice made? | `decision_log.md` | `change_request_log.md` |
| What changed from earlier versions? | `change_request_log.md` | `decision_log.md` |
| Is a question still open? | `open_questions_log.md` | `decision_log.md` |
| What risks should implementation watch? | `risk_register.md` | `governance_plan.md` |
| How should BA docs be updated after a change? | `governance_plan.md` | `information_management_plan.md` |

---

## 4. Common Implementation Routes

### 4.1 Implement a functional requirement

Read in this order:

1. `docs/02_requirements/requirements_log.md`
2. `docs/08_traceability/traceability_matrix.md`
3. Related workflow in `docs/03_processes-and-workflows/user_workflows.md`
4. Related UI fields in `docs/05_ui-ux/wireframe_field_requirements.md`
5. Related data entities in `docs/04_domain-and-data-model/data_dictionary.md`
6. Related decisions in `docs/09_decisions/decision_log.md`

### 4.2 Implement a screen

Read in this order:

1. `docs/05_ui-ux/wireframes_detailed_description.md`
2. `docs/05_ui-ux/wireframe_field_requirements.md`
3. Related requirement in `docs/02_requirements/requirements_log.md`
4. Related workflow in `docs/03_processes-and-workflows/user_workflows.md`
5. Related database entities in `docs/04_domain-and-data-model/data_dictionary.md`

### 4.3 Implement database or DAO logic

Read in this order:

1. `docs/04_domain-and-data-model/data_dictionary.md`
2. `docs/04_domain-and-data-model/dbml_erd.md`
3. Related requirement in `docs/02_requirements/requirements_log.md`
4. Relevant architectural decisions in `docs/09_decisions/decision_log.md`

### 4.4 Implement resume generation and PDF export

Read in this order:

1. `docs/05_ui-ux/resume_template_details_and_logic.md`
2. `docs/05_ui-ux/one_pager_template.html`
3. `docs/05_ui-ux/two_pager_template.html`
4. Related generation requirements in `docs/02_requirements/requirements_log.md`
5. Related data entities in `docs/04_domain-and-data-model/data_dictionary.md`

---

## 5. Folder Map

| Folder | Purpose |
|---|---|
| `docs/01_project-overview/` | Business context, goals, KPIs, constraints, target state. |
| `docs/02_requirements/` | Current requirement baseline and elicitation artifacts. |
| `docs/03_processes-and-workflows/` | User/admin/recruiter workflows and scenario logic. |
| `docs/04_domain-and-data-model/` | ERD, database entities, fields, relationships. |
| `docs/05_ui-ux/` | Wireframes, field requirements, resume template logic, HTML layouts. |
| `docs/07_project-management/` | Governance, change control, risks, questions, BA planning. |
| `docs/08_traceability/` | Requirement-to-source/design/test traceability. |
| `docs/09_decisions/` | Approved decisions and rationale. |

---

## 6. Artifact Index

### 6.1 Repository Overview

#### `README.md`

**Summary:** High-level repository overview, product summary, educational context, documentation status, MVP scope, target stack, and implementation direction.

**Use this file for:**
1. Understanding the whole project quickly.
2. Confirming the intended Java/Vue/Spring MVC/JDBC/PostgreSQL stack.
3. Explaining the project in portfolio, onboarding, or handoff contexts.

**Update this file when:**
1. MVP scope, repository status, or target implementation stack changes.
2. A major deliverable is added, removed, or moved.
3. The implementation repository or portfolio packaging direction changes.

---

### 6.2 Project Overview

#### `docs/01_project-overview/strategic_context_and_gap_analysis.md`

**Summary:** Strategic foundation: current state, pain points, root cause, target state, capabilities, constraints, and value assessment.

**Use this file for:**
1. Understanding why the product exists.
2. Checking whether a feature supports the core business need.
3. Explaining scope boundaries and technology constraints.

**Update this file when:**
1. The business problem or target state changes.
2. Major technology constraints change.
3. New strategic value, capability, or gap is discovered.

#### `docs/01_project-overview/business_goals_and_kpis.md`

**Summary:** SMART business goals and measurable KPIs, including resume generation time, reliability, code quality, DB layer quality, UI security, observability, architecture patterns, testing, i18n, deployment, and configurable resume budgets.

**Use this file for:**
1. Translating business goals into measurable implementation targets.
2. Checking whether technical work supports Capstone success criteria.
3. Defining release/demo verification criteria.

**Update this file when:**
1. A KPI target, metric, or deadline changes.
2. A new measurable goal is added.
3. Implementation scope changes enough to affect success measurement.

---

### 6.3 Requirements and Elicitation

#### `docs/02_requirements/requirements_log.md`

**Summary:** Main requirements baseline: business, stakeholder, functional, non-functional, data, UI, security, testing, deployment, and acceptance criteria.

**Use this file for:**
1. Finding what must be implemented.
2. Checking acceptance criteria before coding or testing.
3. Determining priority, scope, readiness, and requirement status.

**Update this file when:**
1. A requirement is added, removed, changed, superseded, or clarified.
2. Acceptance criteria change.
3. Implementation reveals a missing or invalid requirement.

#### `docs/02_requirements/elicitation/confirmed_elicitation_results.md`

**Summary:** Approved results of UI/UX elicitation: page scope, MVP classification, page-level decisions, role-based needs, and implementation guidance.

**Use this file for:**
1. Checking approved UI/UX scope from elicitation.
2. Understanding why specific screens or blocks exist.
3. Comparing current implementation with confirmed page-level expectations.

**Update this file when:**
1. New elicitation results are confirmed.
2. Page scope changes due to stakeholder or mentor feedback.
3. A previous elicitation result is replaced by a newer confirmed decision.

#### `docs/02_requirements/elicitation/ui_ux_elicitation_plan.md`

**Summary:** Plan for how UI/UX elicitation was conducted: objectives, respondent segments, scope, technique, controlled values, and analysis method.

**Use this file for:**
1. Understanding the elicitation method and scoring logic.
2. Re-running or extending UI/UX elicitation.
3. Explaining why the questionnaire was structured this way.

**Update this file when:**
1. The elicitation method changes.
2. New respondent segments or decision rules are added.
3. The questionnaire scope is expanded or reduced.

#### `docs/02_requirements/elicitation/ui_ux_questionnaire.md`

**Summary:** Full role-based UI/UX questionnaire for registered users, recruiters, admins, and developer/technical review.

**Use this file for:**
1. Reviewing raw elicitation questions.
2. Creating follow-up stakeholder questions.
3. Validating whether a screen decision had a question behind it.

**Update this file when:**
1. New elicitation questions are added.
2. Existing questions become obsolete.
3. A new stakeholder role or screen needs structured questions.

---

### 6.4 Processes and Workflows

#### `docs/03_processes-and-workflows/user_workflows.md`

**Summary:** Main user/admin/recruiter workflows with preconditions, success scenarios, postconditions, and extensions.

**Use this file for:**
1. Implementing end-to-end flows.
2. Checking edge cases and alternate paths.
3. Designing controller/service orchestration and integration tests.

**Update this file when:**
1. A user journey changes.
2. A new workflow or extension path is added.
3. Implementation changes the order of user/system actions.

---

### 6.5 Domain and Data Model

#### `docs/04_domain-and-data-model/data_dictionary.md`

**Summary:** Field-level database dictionary for entities, columns, data types, meanings, and constraints.

**Use this file for:**
1. Creating Flyway migrations and SQL tables.
2. Implementing DAO models, DTO mapping, and validation alignment.
3. Checking the meaning of each database field.

**Update this file when:**
1. A table, column, constraint, or relationship changes.
2. Field meaning or allowed values change.
3. A requirement introduces new persistent data.

#### `docs/04_domain-and-data-model/dbml_erd.md`

**Summary:** DBML source for dbdiagram.io and the most implementation-friendly ERD representation.

**Use this file for:**
1. Generating or reviewing the database diagram.
2. Translating the data model into SQL migrations.
3. Checking entity relationships quickly.

**Update this file when:**
1. The database model changes.
2. A relationship, FK, enum/reference table, or cardinality changes.
3. The Data Dictionary is updated with structural changes.

#### `docs/04_domain-and-data-model/mermaid_erd.md`

**Summary:** Mermaid ERD version for markdown-friendly documentation and quick visual review.

**Use this file for:**
1. Viewing the ERD inside Markdown-compatible tools.
2. Explaining entity groups and relationships in documentation.
3. Adding lightweight diagrams to README or portfolio docs.

**Update this file when:**
1. DBML or Data Dictionary changes structurally.
2. Entity groups or relationship labels need correction.
3. Portfolio documentation needs an updated diagram.

#### `docs/04_domain-and-data-model/plantuml_erd.puml`

**Summary:** PlantUML ERD source for tools that support PlantUML rendering.

**Use this file for:**
1. Rendering ERD in PlantUML-based environments.
2. Maintaining an alternative diagram format.
3. Producing architecture-style documentation views.

**Update this file when:**
1. DBML or Data Dictionary changes structurally.
2. PlantUML output no longer matches the approved data model.
3. A new diagram convention is introduced.

---

### 6.6 UI/UX and Resume Template Logic

#### `docs/05_ui-ux/wireframes_detailed_description.md`

**Summary:** Detailed role-based screen descriptions, visible blocks, actions, validation expectations, empty states, and navigation logic.

**Use this file for:**
1. Implementing screens and navigation.
2. Checking what each screen should show.
3. Understanding visitor, user, admin, and recruiter UI flows.

**Update this file when:**
1. A screen layout or navigation path changes.
2. A visible block, action, modal, or empty state changes.
3. Wireframes are revised or replaced.

#### `docs/05_ui-ux/wireframe_field_requirements.md`

**Summary:** Field-level UI requirements: profile sections, input fields, validation rules, error messages, sorting, and draft data model notes.

**Use this file for:**
1. Implementing forms and frontend validation.
2. Aligning backend validation with UI expectations.
3. Checking exact fields for My Profile and Generate Resume.

**Update this file when:**
1. A field is added, removed, renamed, or revalidated.
2. Error messages or validation rules change.
3. A UI field needs a new database mapping.

#### `docs/05_ui-ux/resume_template_details_and_logic.md`

**Summary:** Resume template and generation logic: AI JSON contract, HTML formatting expectations, one-page/two-page rules, content budgets, profile requirements, template mode decision rules, and backend mapping assumptions.

**Use this file for:**
1. Implementing AI prompt construction and response parsing.
2. Implementing resume review, PDF generation, and HTML rendering.
3. Enforcing content budgets for summaries, skills, jobs, courses, and page limits.

**Update this file when:**
1. AI response JSON structure changes.
2. Resume layout, budget, or page-mode rules change.
3. PDF/template rendering behavior changes.

#### `docs/05_ui-ux/one_pager_template.html`

**Summary:** HTML layout template for one-page resume output.

**Use this file for:**
1. Implementing or testing one-page PDF rendering.
2. Checking CSS/layout expectations for compact resume output.
3. Comparing generated resume data with final visual structure.

**Update this file when:**
1. One-page resume layout changes.
2. CSS or print/PDF behavior changes.
3. Template placeholders no longer match backend data.

#### `docs/05_ui-ux/two_pager_template.html`

**Summary:** HTML layout template for two-page resume output.

**Use this file for:**
1. Implementing or testing two-page PDF rendering.
2. Checking layout rules for longer resumes.
3. Ensuring page 2 follows approved compact content rules.

**Update this file when:**
1. Two-page resume layout changes.
2. CSS or print/PDF behavior changes.
3. Template placeholders no longer match backend data.

---

### 6.7 BA Planning, Governance, and Information Management

#### `docs/07_project-management/ba-planning-and-monitoring/project_approach_decision.md`

**Summary:** Methodology decision explaining why the project uses a hybrid approach: predictive for architecture/skeleton and adaptive for features/AI logic.

**Use this file for:**
1. Understanding the project delivery approach.
2. Explaining why some parts are planned upfront and others remain adaptive.
3. Aligning implementation workflow with BA planning assumptions.

**Update this file when:**
1. The project methodology changes.
2. Predictive/adaptive boundaries change.
3. A major delivery strategy decision is revised.

#### `docs/07_project-management/ba-planning-and-monitoring/stakeholder_engagement_plan.md`

**Summary:** Stakeholder identification, engagement strategy, communication plan, and communication protocols.

**Use this file for:**
1. Understanding stakeholder roles and expected communication.
2. Planning feedback or review activities.
3. Explaining whose perspective a requirement serves.

**Update this file when:**
1. A stakeholder group is added, removed, or reclassified.
2. Communication cadence or protocol changes.
3. Review responsibilities change.

#### `docs/07_project-management/ba-planning-and-monitoring/governance_plan.md`

**Summary:** Governance rules, RACI, change management process, decision/change log usage, de-scoping rule, conflict resolution, and documentation update workflows.

**Use this file for:**
1. Deciding how to process requirement changes.
2. Updating documentation after adding/removing/changing requirements.
3. Resolving conflicts between artifacts.

**Update this file when:**
1. Change control rules change.
2. RACI or authority rules change.
3. Documentation maintenance workflow changes.

#### `docs/07_project-management/ba-planning-and-monitoring/information_management_plan.md`

**Summary:** Repository information structure, artifact status model, lifecycle, naming conventions, versioning, decision management, traceability, and quality criteria.

**Use this file for:**
1. Deciding where a new artifact belongs.
2. Naming files and maintaining documentation consistency.
3. Understanding status, versioning, and traceability rules.

**Update this file when:**
1. Repository structure changes.
2. Naming, versioning, or artifact status rules change.
3. New artifact categories are introduced.

#### `docs/07_project-management/ba-planning-and-monitoring/ba_process_improvement_plan.md`

**Summary:** Improvement plan for the BA process: readiness checklist, feasibility filter, de-scoping, decision log discipline, open question management, traceability, metrics, and improvement backlog.

**Use this file for:**
1. Improving the documentation process itself.
2. Checking whether BA work remains lean and useful.
3. Identifying process risks and improvement actions.

**Update this file when:**
1. A BA process weakness is discovered.
2. New improvement actions or metrics are added.
3. Review cadence or quality expectations change.

---

### 6.8 Project Management Logs

#### `docs/07_project-management/change_request_log.md`

**Summary:** Controlled log of requested, accepted, rejected, implemented, or closed changes.

**Use this file for:**
1. Understanding how the scope evolved.
2. Tracking why a requirement, screen, data model, or workflow changed.
3. Linking implementation changes to approved change records.

**Update this file when:**
1. A requirement, scope item, workflow, UI element, or data model changes.
2. A change is proposed, approved, rejected, implemented, or closed.
3. A decision causes downstream documentation updates.

#### `docs/07_project-management/open_questions_log.md`

**Summary:** Log of open, answered, closed, or deferred questions affecting scope, requirements, UI/UX, data, or implementation.

**Use this file for:**
1. Checking whether an ambiguity has already been resolved.
2. Recording uncertainty before making a decision.
3. Preventing silent assumptions during implementation.

**Update this file when:**
1. A new uncertainty appears.
2. A question is answered or closed.
3. An answer creates a new decision, requirement, or change request.

#### `docs/07_project-management/risk_register.md`

**Summary:** Risk list with categories, probability, impact, severity, response strategy, mitigation, status, and ownership.

**Use this file for:**
1. Checking implementation risks before starting complex work.
2. Planning mitigation for AI, PDF, security, Vue/Spring integration, scope, and deployment risks.
3. Tracking risk status during development.

**Update this file when:**
1. A new risk is discovered.
2. Probability, impact, mitigation, or status changes.
3. A risk becomes an issue or is closed.

---

### 6.9 Traceability and Decisions

#### `docs/08_traceability/traceability_matrix.md`

**Summary:** Requirements Traceability Matrix connecting requirements to business goals, workflows, UI, data model, decisions, risks, and test coverage.

**Use this file for:**
1. Checking impact of a requirement change.
2. Connecting implementation tasks to requirements and tests.
3. Verifying that important requirements are covered by design and test plans.

**Update this file when:**
1. A requirement is added, changed, removed, or superseded.
2. UI/data/workflow/test coverage changes.
3. A decision or change request affects a requirement.

#### `docs/09_decisions/decision_log.md`

**Summary:** Approved decision log covering scope, architecture, technology, UI/UX, data model, security, AI integration, resume templates, and implementation constraints.

**Use this file for:**
1. Understanding why a design or scope choice was made.
2. Avoiding repeated debates during implementation.
3. Resolving conflicts between older and newer artifacts.

**Update this file when:**
1. A new decision is made.
2. An existing decision is superseded or corrected.
3. A technical or scope choice needs rationale for future agents/developers.

---

## 7. Minimal Update Rules for Agents

### 7.1 If a new feature is added

Update in this order:

1. `change_request_log.md`
2. `decision_log.md` if a decision is needed
3. `requirements_log.md`
4. `user_workflows.md` if flow changes
5. `wireframe_field_requirements.md` and/or `wireframes_detailed_description.md` if UI changes
6. `data_dictionary.md` and ERD files if persistence changes
7. `traceability_matrix.md`
8. `risk_register.md` if risk changes
9. This `ba-index.md` if a new artifact or routing rule is introduced

### 7.2 If an existing feature is changed

Update in this order:

1. `change_request_log.md`
2. `decision_log.md` if rationale changed
3. Source artifact: requirement, workflow, UI, data, or template logic
4. `traceability_matrix.md`
5. `risk_register.md` if risk changed
6. This `ba-index.md` if routing or artifact meaning changed

### 7.3 If a feature is removed or de-scoped

Update in this order:

1. `change_request_log.md`
2. `decision_log.md`
3. Mark related requirements as removed/superseded instead of silently deleting them
4. Update workflows, UI, data model, and template logic if affected
5. Update `traceability_matrix.md`
6. Update `README.md` if MVP scope/status changes
7. This `ba-index.md` if routing changes

---

## 8. Context-Saving Rules for AI Agents

1. Do not read the whole BA package unless the task is broad project review.
2. For implementation tasks, read the smallest artifact set from Section 4.
3. Prefer current baseline files over historical elicitation/planning files.
4. Treat HTML templates as layout references, not requirement sources.
5. Treat ERD diagram files as structural references; use Data Dictionary for field meaning.
6. Treat Decision Log as rationale; use Requirements Log as implementation baseline.
7. When unsure, check Open Questions before inventing assumptions.
8. When changing anything, update traceability and change history.

---

## 9. One-Sentence Agent Summary

ResumAIner BA documentation is organized so that `requirements_log.md` defines what to build, `user_workflows.md` defines how users move through it, `data_dictionary.md` defines what must be stored, `wireframe_field_requirements.md` defines what users enter and validate, `resume_template_details_and_logic.md` defines AI/PDF behavior, and `decision_log.md` plus `change_request_log.md` explain why the current baseline exists.
