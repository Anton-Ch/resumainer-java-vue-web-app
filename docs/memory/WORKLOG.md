# Worklog

Use concise high-value entries only.
This is not a changelog. Do not record routine releases, version bumps, or implementation summaries.

## Template

---

---

---

### 2026-06-02 - Feature 003 Planning and Security Review Completed

**Milestone**: Feature 003 (Vue Auth Page) reaches Spec + Plan + Tasks + Security Review complete.

**What was achieved**:
- Specification: 6 user stories, 28 FR, 10 SC, 3 clarifications rounds
- Plan: 4 phases (Backend, Frontend, Docker, Integration) with hybrid UUID/BIGSERIAL strategy
- Tasks: 63 tasks with [TDD], [P], [SUBAGENT], [REVIEW] execution markers
- Diagrams: component diagram, system design, software architecture
- Security review: 11 findings (1 High, 5 Medium, 3 Low, 2 Info)
- Applied fixes: session regeneration on login (SEC-002), CSRF cookie-to-header filter (SEC-003)
- Phase 1 (Setup) implemented: pom.xml dependencies, Vue 3 + Vite scaffold, Docker Compose PostgreSQL 17, application.properties

**Next phase**: Phase 2 — Flyway migrations, Model/DTO classes, DAO layer.

### 2026-05-31 - Second Feature MVP Achieved: Thymeleaf Landing Page

**Status**
Active

**Milestone**
Feature 002-thymeleaf-landing-page reaches MVP.

**What was achieved**
Full Landing Page with 8 sections (Header, Hero, Problem, How It Works, Features, Trust & Control, FAQ, Final CTA), bilingual EN/RU i18n with browser auto-detection and ?lang= parameter switching, responsive layout, self-hosted fonts (Inter + Manrope, 7 TTF files), SVG brand assets (4 logos), custom bilingual error pages (404/500) with full branding, and MockMvc controller tests (3 tests, all pass). Migrated from JSP to Thymeleaf. All 28 tasks complete. BUILD SUCCESS.

**Why this is durable**
Establishes the Thymeleaf + i18n + responsive design pattern for all future Thymeleaf views. Documents the migration from JSP to Thymeleaf and the self-hosted font approach (SEC-002).

**Evidence**
Commit `f8ec657` — 22 files, 1288 insertions. Branch `feat/002-thymeleaf-landing-page`. Build: 3 tests pass, WAR created.

**Where to look next**
backend/src/main/resources/templates/landing.html, backend/src/main/webapp/static/css/landing.css, backend/src/main/resources/messages.properties

### 2026-05-30 - First Feature MVP Achieved: Hello World Tomcat

**Milestone**: Feature `001-hello-world-tomcat` reaches MVP.

**What was achieved**: Full end-to-end validation: `git clone → mvnw clean package → docker compose up → browser shows Hello World page with server time`. Spring MVC 6.2.11 + Jakarta EE 10 on Tomcat 10.1, deployed in Docker via multi-stage build (Maven → Tomcat, non-root user). Unit test (MockMvc, standalone setup) passing.

**Key lessons captured**:
- D1: Servlet initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml)
- D2: Maven Wrapper at same level as pom.xml
- D3: Docker Tomcat health check uses bash /dev/tcp (not nc)
- B1: @Controller must be registered as explicit @Bean or via @ComponentScan
- JaCoCo 0.8.13+ required for Java 21 class file support (0.8.12 fails)

**Evidence**
docker compose up → http://localhost:8080 → 200 OK with ResumAIner Hello World page.
mvnw clean package → BUILD SUCCESS with 1 passing test.

### YYYY-MM-DD - Summary

- why this is durable
- what future mistake it prevents
- evidence
- where future contributors should look

## Example

### 2026-03-15 - Pagination cursor must be opaque to clients

- **Why durable**: three features so far have tried to expose raw database offsets as pagination cursors, each time creating breaking changes when the underlying query changes
- **Future mistake prevented**: next time a feature adds pagination, the implementer will know to use opaque cursors from the start
- **Evidence**: specs 018, 024, and 031 all required pagination rework; see DECISIONS.md entry on API pagination
- **Where to look**: `src/api/pagination.ts`, `docs/memory/DECISIONS.md`

## Counter-Example (do not write entries like this)

> ### 2026-03-15 - Updated pagination
>
> - Changed pagination to use cursors
> - Deployed to staging

This is a changelog entry, not a durable lesson. It records what happened, not what was learned.
