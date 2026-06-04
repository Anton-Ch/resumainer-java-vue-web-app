# AGENTS.md

## Project state

Active development. Feature 001 (Hello World Tomcat) — complete and merged to main.
Feature 002 (Thymeleaf Landing Page) — specification approved, plan ready.

## Project Snapshot

ResumAIner is a Java + Vue web application for AI-assisted resume adaptation.

Core product flow:

1. User maintains a structured professional profile.
2. User provides target vacancy/company information.
3. AI generates adapted resume and cover letter content.
4. User reviews and edits generated structured data.
5. User saves final resume.
6. System generates printable/selectable-text PDF.
7. User shares public PDF link with recruiters.

## Implementation repository:

- Backend: `backend/`
- Frontend: `frontend/`
- Specs: `specs/`
- Spec Kit config: `.specify/`
- OpenCode config: `.opencode/`
- Project memory: `docs/memory/`
- Business analysis: `ba-docs/`

## Branches

| Branch | Contents |
|---|---|
| `main` | Only README.md |
| `chore/spec-kit-configuration` | Spec Kit depricated tooling for Claude (not used) |
| `chore/spec-kit-setup` (HEAD) | Only README.md (same as main) |

## Key files (on `main` branch)

- `README.md` — project overview and planned stack

## Key files (on `chore/spec-kit-setup` branch)

- `AGENTS.md` — agent instructions and shared project rules
- `.specify/` — Spec Kit extensions, templates, workflows
- `.specify/memory/constitution.md` — project constitution (unfilled)
- `.specify/memory/workflow.md` — authoritative memory-first workflow
- `.specify/superpowers.yml` — superpowers skill detection results
- `.specify/branch-convention.yml` — Git branch naming convention
- `.specify/extensions.yml` — 12 registered extensions, auto-execute hooks
- `docs/memory/` — durable memory store (INDEX, PROJECT_CONTEXT, ARCHITECTURE, DECISIONS, BUGS, WORKLOG)
- `specs/` — feature specifications root


## Methodology

Spec-Driven Development with GitHub Spec Kit. The flow is:
```
constitution → specify → plan → tasks → implement
```

Each feature must follow the memory-first workflow in `.specify/memory/workflow.md`.


## Planned stack

Backend: Java / Spring MVC / Plain JDBC / DAO / PostgreSQL / Flyway / HTML-to-PDF
Frontend: Vue 3 / Vite / REST API
Infra: Docker / Docker Compose / VPS
AI: OpenRouter API (DeepSeek in dev), Mock AI provider for tests

## Workflow Stack

Use this stack:

- OpenCode as agent harness
- Spec Kit as source-of-truth workflow
- SuperSpec / Superpowers as execution discipline extension to Spec Kit
- Memory Hub for durable project memory
- GitHub Flow for branches and PRs
- Conventional Commit style for commits

Default feature flow:

~~~text
constitution -> specify -> clarify -> plan -> tasks -> implement -> review -> commit
~~~

Spec Kit controls the process.

SuperSpec supports implementation and review.

Agents provide specialization.

## Known quirks

- No `pom.xml`, `package.json`, `docker-compose.yml` exist yet
- Constitution and memory store are unfilled

## First milestone

> Java Spring config + Hello World Tomcat page in Docker

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the implementation
plan at `specs/003-vue-auth-page/plan.md`.
<!-- SPECKIT END -->

### Spec Kit

You MUST follow the memory-first workflow defined in `.specify/memory/workflow.md` and proactively execute `/speckit.memory-md.prepare-context` before planning.

---

# Shared ResumAIner Agent Rules

These shared rules apply to all project-specific OpenCode agents.

Role-specific agents should keep only their unique domain rules and inherit common project rules from this section.

## Shared Workflow Context

All ResumAIner agents work inside:

- OpenCode
- Spec Kit
- SuperSpec / Superpowers
- Java + Vue monorepo
- GitHub Flow
- Memory-first workflow

Spec Kit remains the source of truth.

SuperSpec strengthens execution discipline but does not replace Spec Kit.

## Shared Source of Truth Order

Before changing files, use this priority:

1. Active Spec Kit feature `spec.md`
2. Active `plan.md`
3. Active `tasks.md`
4. Spec Kit constitution
5. Approved wireframes / UI field requirements, when relevant
6. Project memory:
   - `docs/memory/PROJECT_CONTEXT.md`
   - `docs/memory/ARCHITECTURE.md`
   - `docs/memory/DECISIONS.md`
   - `docs/memory/BUGS.md`
7. `AGENTS.md`
8. Existing code in current repository files
9. User's current request
10. BA source package only if needed

Do not load the full business analysis dump by default. Use lazy loading.
If you are sure that the task is complex and you need full Business Analysis context for this - read file "ba-docs/complete_business_analysis.md" otherwise first use "ba-index.md" file as a BA documentation catalogue router for you to understand which files you need to read (make a list of them if more than 1 file going to be read), and only then go to specific files required to you.

If sources conflict, stop and ask.

Do not silently choose.

Do not read the full business analysis dump by default. Use summarized memory/docs first unless the user asks for deeper source review.

## Technical Constraints

### Backend:

- Java 21
- Spring MVC
- Spring Core
- Servlets if needed
- Plain JDBC
- Custom thread-safe Connection Pool
- DAO pattern
- PostgreSQL
- Flyway
- Maven
- SLF4J + Logback
- JUnit 5 + Mockito + JaCoCo
- Swagger/OpenAPI, admin-only in production
- HTML-to-PDF generation on backend

### Frontend:

- Vue 3
- Vite
- REST API integration
- structured forms for generated resume review
- bilingual English/Russian UI

### Infrastructure:

- Docker
- Docker Compose
- VPS deployment target
- backend + frontend + database containers

### AI:

- OpenRouter API
- DeepSeek model route for development experiments
- Mock AI provider fallback for stable local testing

### Strict constraints:

- Never ever use Spring Boot under no circumstances.
- No Hibernate, JPA, Spring Data JPA, or ORM.
- Do not replace plain JDBC requirement.
- Do not move PDF template rendering to Vue.
- Do not make the public recruiter flow require login.


## Shared Spec Kit / SuperSpec Behavior

When working through Spec Kit or SuperSpec:

- respect active feature boundaries;
- work only on the selected task group;
- do not skip specification or planning steps;
- do not implement tasks absent from `tasks.md` unless explicitly asked;
- do not skip `spec`, `plan`, or `tasks` stages for real features;
- use SuperSpec execution discipline when `/speckit.superpowers.execute` is active;
- use `/speckit.memory-md.prepare-context` before planning when memory exists;
- stop after one logical task group if the user asks for controlled step-by-step execution;
- separate cross-layer work clearly by agent responsibility.

Default execution pattern:

1. Read active `spec.md`, `plan.md`, and `tasks.md`.
2. Identify tasks that belong to the current agent scope.
3. State assumptions briefly.
4. Implement only the selected task group.
5. Run or suggest relevant verification.
6. Summarize files changed and verification result.
7. Capture durable lessons only when useful.

## Shared Implementation Method

All ResumAIner agents must use this implementation method when executing tasks, especially during `/speckit.superpowers.execute` or any code/file implementation/editing work.

### Documentation-First Implementation

Before implementing important code, configuration, properties, integration logic, framework-specific behavior, or non-trivial files, consult the available Context7 MCP server documentation.

Use Context7 to verify current official or near-official best practices for the specific technology, component, or library being used.

This is required before work involving, for example:

* Spring MVC configuration;
* Java language or library usage;
* Maven configuration;
* Flyway migrations;
* PostgreSQL-specific behavior;
* JDBC behavior;
* Vue 3 / Vite implementation;
* Docker / Docker Compose configuration;
* security-sensitive code;
* validation behavior;
* testing framework setup;
* PDF generation libraries;
* OpenRouter or external API integration.

The agent must not rely only on own AI model memory when current documentation is available through Context7.

### Context7 During Troubleshooting

During implementation, actively use Context7 again when:

* tests fail unexpectedly;
* build errors occur;
* framework behavior is unclear;
* configuration does not work as expected;
* bugs appear after changes;
* generated code does not behave as intended;
* there is uncertainty about the correct API, annotation, option, command, or lifecycle behavior.

When troubleshooting, prefer this order:

1. Check project source of truth: active `spec.md`, `plan.md`, `tasks.md`, constitution, and memory.
2. Check current project code and configuration.
3. Check Context7 documentation for the relevant technology.
4. Only then rely on general model knowledge or assumptions.

### KISS Principle Implementation Rule

Follow KISS principle (Keep It Simple, Stupid): keep implementation simple, direct, and understandable because most systems and processes work best if they are kept simple rather than complicated.

This means:

* prefer straightforward code over clever overengineered abstractions;
* avoid speculative architecture;
* avoid unnecessary layers, factories, helpers, services, or utilities bringing low value;
* avoid introducing libraries without clear current value;
* keep components focused on one responsibility (Single Responsibility Principle);
* keep configuration readable for a beginner;
* prefer explicitness over hidden magic;
* implement only what the active task requires.

KISS must never be used as an excuse for weak quality.

Do not simplify away:

* security;
* validation;
* testability;
* error handling;
* data integrity;
* clear naming;
* maintainability;
* project constraints;
* Spec Kit acceptance criteria.

The target is simple and clear professional code, not primitive code.

### Implementation Discipline

Before writing or editing files, the agent should briefly identify:
1. what documentation or Context7 topic is relevant;
2. what source-of-truth project files control the task;
3. what minimal implementation path satisfies the requirement;
4. what verification command or check will prove the change works.

During implementation:
* make surgical changes;
* touch only necessary files;
* keep changes inside the active task boundary;
* do not refactor unrelated code;
* do not introduce broad abstractions unless the current task clearly needs them;
* re-check documentation when errors suggest incorrect API or framework usage.

After implementation:
* summarize files changed;
* explain what changed;
* show verification results or recommended verification commands;
* mention risks, assumptions, and follow-ups.

## Shared Project Domain Summary

ResumAIner is an AI-assisted resume adaptation platform.

Backend-owned capabilities include:

- user registration and login;
- BCrypt password storage;
- roles: `USER`, `ADMIN`;
- user status and generation permission;
- structured user profile data;
- resume generation requests;
- mock AI provider first;
- real OpenRouter provider behind the same interface;
- structured AI JSON output;
- generated resume review data;
- saved finalized resumes;
- cover letter generation;
- public resume links that open PDF directly;
- soft-delete with public link returning HTTP 410;
- admin user/resume/AI model management;
- AI usage logging;
- DB-backed resume budget configuration;
- PDF generation from backend-rendered HTML templates;
- Swagger/OpenAPI, ADMIN-only in production;
- dev/prod profiles;
- Docker Compose with backend, frontend, database.

Frontend-owned capabilities include:

- User Home with integrated saved resume listing;
- My Profile with structured profile sections;
- Generate Resume flow;
- Resume Review structured editable form;
- cover letter editing in Resume Review;
- post-save PDF actions;
- User Home details modal with PDF link copy, PDF download, and cover letter display;
- Admin Home;
- Admin Users table;
- Admin User Details;
- Admin Resumes table with PDF actions;
- Admin AI Models table;
- Admin AI Model Details with masked API key behavior;
- bilingual English/Russian UI;
- pagination for long tables;
- visible search/filter fields only when defined by specs/wireframes.

Important scope decisions:

- Landing Page is handled by Thymeleaf, not Vue, unless explicitly changed.
- Public resume link opens finalized PDF directly.
- There is no recruiter Vue portal.
- There is no separate Resume History page.
- User settings are integrated into My Profile.
- Vue does not render final PDF templates.

Core data entities include:

- `users`
- `role`
- `user_status`
- `user_permission`
- `contact_detail`
- `work_experience`
- `education`
- `project`
- `course_certificate`
- `additional_profile_info`
- `work_format`
- `user_work_format`
- `ai_model`
- `resume_generation_request`
- `resume_generation_response`
- `generation_response_experience`
- `generation_response_education`
- `generation_response_course`
- `generation_response_project`
- `generation_response_skill`
- `saved_resume`
- `ai_usage_log`
- `resume_template`
- `resume_budget_configs`
- `resume_template_selection_rules`
- `resume_work_experience_distribution_rules`
- `resume_section_budget_rules`

## Shared Security Baseline

All agents must treat these areas as security-sensitive:

- authentication;
- authorization;
- admin functionality;
- API key storage, masking, replacement, deletion;
- public PDF links;
- HTML sanitization;
- generated resume content;
- cover letter content;
- user profile data;
- AI model settings;
- AI usage logs;
- file/PDF access.

Mandatory rules:

- Never log secrets.
- Never commit secrets.
- Never expose full API keys after saving.
- Validate input on the backend even if frontend validates too.
- Do not rely on frontend-only authorization.
- Public links expose only finalized PDF output.
- Soft-deleted resumes must not expose PDF.
- AI-generated HTML must be sanitized/allowlisted before safe rendering.
- Return graceful errors without leaking stack traces.

## Shared Karpathy-Style Operating Rules

### Think Before Coding

Before implementation:

- state assumptions;
- surface uncertainty;
- name trade-offs;
- ask when requirements are ambiguous.

### Simplicity First

- Minimum code that solves the requirement.
- No speculative abstractions.
- No unnecessary libraries.
- No “future-proofing” without current value.
- If code becomes large, simplify before continuing.

### Surgical Changes

- Touch only files needed for the task.
- Do not refactor unrelated code.
- Match existing style.
- Remove only dead code introduced by your change.
- Mention unrelated issues instead of fixing them silently.

### Goal-Driven Execution

Before editing:

- define success criteria;
- define verification command or manual check;
- after editing, say what changed and how it was verified.

## Shared Collaboration Style

When talking to the user:

- default to Russian explanations;
- keep answers concise and practical;
- warn clearly about risks;
- do not flatter;
- teach when useful;
- explain technical concepts simply when asked;
- use English for code, comments, commit messages, file content, and repository documentation.

When producing implementation output:

1. Short summary.
2. Files touched.
3. What changed.
4. How to verify.
5. Risks or follow-ups.

For complex tasks, ask before broad changes.

## Shared Success Criteria

A good agent answer or change is:

- aligned with active spec/plan/tasks;
- compliant with project constraints;
- simple enough for a Capstone project;
- professional enough for portfolio review;
- understandable to the user;
- testable;
- secure by default;
- documented where needed;
- not overengineered.

## Agent Routing Guidance

When the current task clearly belongs to a specialized project agent, suggest switching to the right agent before continuing (unless suitable agent already in use).

Do not switch agents automatically.  
Ask or recommend the switch clearly and briefly.

### Primary Agent Routing

| Task Type | Recommended Agent |
|---|---|
| Java backend, Spring MVC, JDBC, DAO, services, controllers, backend architecture | `java_agent` |
| Vue 3, frontend forms, UI pages, client-side validation, routing, API integration UI | `vue_agent` |
| Cross-layer tasks touching backend + frontend together | `software_engineering_team_lead` |
| SQL, PostgreSQL, schema design, migrations, queries, indexes, data consistency | `software_engineering_team_lead` |
| Testing strategy, test coverage, test cases, verification workflow | `software_engineering_team_lead` |
| Docker, deployment, integration, environment setup, build pipeline | `software_engineering_team_lead` |
| Security, authentication, authorization, API keys, public links, HTML/PDF safety | `software_engineering_team_lead` |
| Architecture trade-offs spanning multiple layers | `software_engineering_team_lead` |

### Git Subagent Routing

For Git-related work, suggest using Opencode subagent:

~~~text
@git_commit_pr_assistant
~~~

Use it for:

- commit message preparation;
- commit body formatting;
- deciding whether to split commits;
- PR title and description;
- MR title and description;
- reviewing `git status`, `git diff`, branch state, and staged files;
- creating commits, PRs, or MRs only after explicit user approval.

### Agent Selection Message Recommendation Style

Use short recommendations.

Good examples:

~~~text
This is mainly a frontend task. I recommend switching to `vue_agent` before implementation.
~~~

~~~text
This is mainly a backend task. I recommend switching to `java_agent` before implementation.
~~~

~~~text
This touches backend, frontend, testing, and integration. I recommend using `software_engineering_team_lead`.
~~~

~~~text
This is a Git/PR task. I recommend calling `@git_commit_pr_assistant`.
~~~

### Important Rule

The active agent must stay aligned with:

1. Spec Kit constitution
2. active feature spec
3. active plan
4. active tasks
5. shared project rules from this `AGENTS.md`

Specialized agents may improve execution quality, but they must not override project-level rules.

## Implementation Behavior

### Before editing:

1. Identify active feature/spec/task context if missing it.
2. State assumptions briefly.
3. Confirm scope if unclear.
4. Touch only necessary files.
5. Avoid unrelated refactoring.
6. Prefer simple maintainable code.
7. Define verification before or after changes.

### After editing:

1. List files changed.
2. Explain what changed short and clear.
3. Explain how to verify.
4. Mention risks/follow-ups.
5. Suggest memory capture only for durable lessons.

## External Context Loading
 
Do not preload all project documentation.
 
Use focused reading:
 
- active feature files first;
- memory docs second;
- BA source only when specific context is missing;
- README for repository workflow and stack;
- AGENTS.md for shared agent rules.
 
When a referenced file is needed, read only the relevant file or section.
 
---