# Tasks: Thymeleaf Landing Page

**Input**: Design documents from `specs/002-thymeleaf-landing-page/`

**Prerequisites**: [plan.md](plan.md) (required), [component-diagram.md](component-diagram.md) (required for architecture context)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format

- `[P]` — Can run in parallel (different files, no dependencies)
- `[TDD]` — Must follow RED-GREEN-REFACTOR (test → fail → implement → pass)
- `[REVIEW]` — Pause for human code review before proceeding
- `[SUBAGENT]` — Can be delegated to a subagent for implementation
- `[US1]` through `[US4]` — Which user story this task belongs to
- File paths are relative to repository root

---

## Phase 1: Setup (Project Scaffold & Dependency Changes)

**Purpose**: Migrate from JSP to Thymeleaf, update dependencies, remove obsolete files

- [ ] T001 [P] Remove JSP/JSTL dependencies from `backend/pom.xml` — delete `jakarta.servlet.jsp-api`, `jakarta.servlet.jsp.jstl-api`; add Thymeleaf Spring 6 dependency:
  ```xml
  <dependency>
      <groupId>org.thymeleaf</groupId>
      <artifactId>thymeleaf-spring6</artifactId>
      <version>3.1.3.RELEASE</version>
  </dependency>
  ```
- [ ] T002 [P] Delete `backend/src/main/webapp/WEB-INF/views/hello.jsp` — JSP is no longer needed
- [ ] T003 [P] Delete `backend/src/main/webapp/WEB-INF/` directory if empty after JSP removal (view resolution now uses Thymeleaf templates from `classpath:/templates/`)
- [ ] T004 [SUBAGENT] Create `backend/src/main/resources/templates/` directory structure for Thymeleaf templates

**Checkpoint**: `./mvnw.cmd clean package` compiles successfully (may fail on controller — expected at this stage)

---

## Phase 2: Foundational (Spring MVC Thymeleaf Configuration)

**Purpose**: Core configuration that MUST be complete before any landing page content can be rendered

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 [P] [SUBAGENT] Create `LandingPageController.java` in `backend/src/main/java/com/resumainer/controller/` — `@Controller` with `@GetMapping("/")`, returns view name `"landing"`, adds model attribute `ctaUrl` from `@Value("${landing.cta.url:/auth/login}")`; add Javadoc per Constitution I.3
- [ ] T006 [SUBAGENT] [REVIEW] Update `WebConfig.java` in `backend/src/main/java/com/resumainer/config/` — add:
  - Remove existing JSP `ViewResolver` (`InternalResourceViewResolver`)
  - Add `SpringResourceTemplateResolver` bean: prefix `/templates/`, suffix `.html`, `TemplateMode.HTML`, cacheable in production
  - Add `SpringTemplateEngine` bean (automatically integrates Spring's `MessageSource`)
  - Add `ThymeleafViewResolver` bean — order after template engine
  - Register `LandingPageController` as `@Bean` (per BUGS.md B1 — `@Controller` alone is invisible in pure Spring MVC)
  - Add `MessageSource` bean: `ReloadableResourceBundleMessageSource` with basename `"messages"`, default encoding `"UTF-8"`
  - Implement `WebMvcConfigurer`:
    * **Locale resolution**: Configure `CookieLocaleResolver` bean — name `"resumainer-lang"`, maxAge 1 year, `HttpOnly`, default locale `Locale.ENGLISH`. This persists the user's language choice across sessions.
    * **Browser detection**: The `CookieLocaleResolver` with `AcceptHeaderLocaleResolver` fallback automatically detects the browser's `Accept-Language` header. If the browser sends `ru` (Russian) as preferred language, locale resolves to Russian on first visit. Any other language → defaults to English.
    * **Locale switching**: Register `LocaleChangeInterceptor` with `paramName="lang"` and `setIgnoreInvalidLocale(true)` — accepts only `?lang=en` or `?lang=ru`. Invalid values (e.g., `?lang=fr`) are silently ignored, keeping the current locale.
    * **Security headers**: Register a `FilterRegistrationBean` (or `@Component` `Filter`) that sets these response headers on every response:
      - `X-Content-Type-Options: nosniff`
      - `X-Frame-Options: DENY`
      - `Referrer-Policy: same-origin`
      - `Content-Security-Policy: default-src 'self'; style-src 'self' 'unsafe-inline'; font-src 'self'; img-src 'self' data:; script-src 'self'`
    * Add `/static/**` resource handler
- [ ] T007 [P] [SUBAGENT] Create `GlobalExceptionHandler.java` in `backend/src/main/java/com/resumainer/exception/` — `@ControllerAdvice` handling `Exception` (500) and `NoHandlerFoundException` (404), returns user-friendly HTML pages without stack traces (per Constitution V.2)

**Checkpoint**: `./mvnw.cmd clean package` compiles successfully — Thymeleaf config is wired

---

## Phase 3: User Story 1 — Landing Page Content (Priority: P1) 🎯 MVP

**Goal**: First-time visitor sees a complete, professional Landing Page at the application root URL

**Independent Test**: Open `http://localhost:8080`, see all 8 sections (Header, Hero, Problem, How It Works, Features, Trust & Control, FAQ, Final CTA) with proper content and styling

- [ ] T008 [P] [US1] Create `landing.html` in `backend/src/main/resources/templates/` — based on `spec_input_files/landing_page.html`; replace all inline text with `th:text="#{key}"` expressions; include Thymeleaf namespace (`xmlns:th="http://www.thymeleaf.org"`); structure:
  - Header with logo, navigation anchors, language switcher, "Get started" CTA
  - Hero section with all copy, CTA button, and product mockup (HTML/CSS-based per FR-017)
  - Problem section with 5 cards
  - How It Works 5-step timeline
  - Features section with 8 cards in 4×2 grid
  - Trust & Control section with message and 3 reassurance cards
  - FAQ section using native `<details>`/`<summary>` with 9 items (no JS)
  - Final CTA card
  - Footer with logo and tagline
- [ ] T009 [P] [US1] Create `landing.css` in `backend/src/main/webapp/static/css/` — implement design tokens per design_dna.md (variables for `--color-bg: #FAF7F0`, `--color-surface: #FFFDF8`, `--color-emerald: #0F8A6A`, etc.), typography (Manrope headings, Inter body), spacing system (4px base), radius tokens, shadows, responsive breakpoints, component styles for all 8 sections
- [ ] T010 [P] [SUBAGENT] [US1] Download and place Pico CSS in `backend/src/main/webapp/static/vendor/pico/pico.min.css` — used as base reset and typography foundation
- [ ] T011 [P] [SUBAGENT] [US1] Download self-hosted Manrope and Inter WOFF2 fonts to `backend/src/main/webapp/static/fonts/` — no Google Fonts CDN (per SEC-002, GDPR-safe)
- [ ] T012 [P] [SUBAGENT] [US1] Copy SVG logo assets from `spec_input_files/` to `backend/src/main/webapp/static/images/logos/` — full logo, monochrome logo, app icon, favicon
- [ ] T013 [P] [US1] Create custom error pages in `backend/src/main/webapp/static/error/` — `404.html` and `500.html` with graceful user-readable messages, no stack traces, consistent with landing page design tokens (per SEC-003)

**Checkpoint**: `docker compose up` → Landing Page visible at `http://localhost:8080` with all 8 sections, proper styling, and working static assets

---

## Phase 4: User Story 2 — Bilingual Language Switching (Priority: P2)

**Goal**: Visitor can switch Landing Page language between English and Russian

**Independent Test**: Click EN/RU in the language switcher — all visible text changes to the selected language

- [ ] T014 [P] [SUBAGENT] [US2] Create `messages.properties` in `backend/src/main/resources/` — based on `spec_input_files/messages.properties`; all ~50 message keys for 8 sections (hero, problem, how-it-works, features, trust, faq, cta, header, footer, mockup chips)
- [ ] T015 [P] [SUBAGENT] [US2] Create `messages_ru.properties` in `backend/src/main/resources/` — based on `spec_input_files/messages_ru.properties`; complete Russian translations for all keys
- [ ] T016 [US2] Update `landing.html` — ensure all text uses `th:text="#{key}"` (no hardcoded text remains); add language switcher with active state via `th:classappend="${#locale.language == 'en'} ? 'active' : ''"`; configure switcher buttons with `th:onclick="|window.location='?lang=en'|"` and `?lang=ru`

**Checkpoint**: Open page → EN is default. Click RU → all text switches to Russian. Click EN → back to English. Reload with `?lang=ru` → Russian persists.

---

## Phase 5: User Story 3 — Responsive Layout (Priority: P3)

**Goal**: Landing Page displays correctly on mobile, tablet, and desktop

**Independent Test**: Resize browser to mobile (<640px), tablet (640-1023px), and desktop (1024px+) — layout adapts correctly without horizontal overflow

- [ ] T017 [P] [SUBAGENT] [US3] Update `landing.css` — implement responsive breakpoints:
  - Desktop (1024px+): two-column Hero, 3/2 problem grid, horizontal timeline, 4-column features, two-column trust
  - Tablet (640-1023px): stacked Hero, 2-column grids, reduced spacing
  - Mobile (<640px): single column, vertical timeline, collapsed nav, adequate tap targets
  - Responsive `clamp()` values for hero heading font size
- [ ] T018 [P] [SUBAGENT] [US3] Update `landing.html` — add mobile hamburger menu toggle (CSS-only or minimal JS), ensure FAQ accordion works on mobile, verify no horizontal overflow

**Checkpoint**: Open page at 320px, 768px, 1280px widths — layout displays correctly at all sizes

---

## Phase 6: User Story 4 — Navigation and CTA (Priority: P3)

**Goal**: Visitor can navigate to sections using header links and click CTA to start authentication

**Independent Test**: Click each nav link → smooth scroll to correct section. Click CTA → redirected to auth flow

- [ ] T019 [P] [SUBAGENT] [US4] Update `landing.html` — ensure header anchors: `#how-it-works`, `#features`, `#faq` match section IDs; add smooth scroll behavior via CSS `scroll-behavior: smooth`
- [ ] T020 [P] [SUBAGENT] [US4] Add `landing.cta.url` to `application.properties` — default value `/auth/login` (externalized, configurable per profile per SEC-001)
- [ ] T021 [P] [SUBAGENT] [US4] Update `landing.html` — all "Get started" and "Create your profile" CTA buttons use `th:href="${ctaUrl}"` pointing to configurable URL; verify no "Log in" button exists anywhere on the page (FR-016)

**Checkpoint**: Click "Get started" → browser navigates to /auth/login (or configured URL). Click nav links → smooth scroll to sections.

---

## Phase 7: Cleanup & Testing

**Purpose**: Remove obsolete code, verify everything works together

- [ ] T022 [P] [SUBAGENT] Remove `HelloWorldController.java` from `backend/src/main/java/com/resumainer/controller/` — no longer needed (root URL is served by `LandingPageController`)
- [ ] T023 [P] [SUBAGENT] Remove `HelloWorldControllerTest.java` from `backend/src/test/java/com/resumainer/controller/` — test for removed controller
- [ ] T024 [P] [SUBAGENT] Remove `hello.jsp` from `backend/src/main/webapp/WEB-INF/views/` (verify T002 already handled this)
- [ ] T025 [TDD] Create `LandingPageControllerTest.java` in `backend/src/test/java/com/resumainer/controller/` — JUnit 5 + MockMvc:
  - `getLandingPage_returns200()` — status 200, view name "landing"
  - `getLandingPage_containsCtaUrlAttribute()` — model contains "ctaUrl"
  - `getLandingPage_withRussianLocale_showsRussianContent()` — simulate `?lang=ru` locale
  - Follow Arrange-Act-Assert pattern per Constitution II.2
- [ ] T026 [REVIEW] Update `application.properties` — add `landing.cta.url=/auth/login`; ensure `spring.web.encoding.charset=UTF-8` is set for i18n
- [ ] T027 [REVIEW] Update `application-dev.properties` — verify no debug leaks
- [ ] T028 [REVIEW] Run full verification: `./mvnw.cmd clean package` + `docker compose up` + browser check for all 4 user stories; verify page load time under 3 seconds with no visible layout shift (per SC-005)

**Checkpoint**: Feature complete — all acceptance criteria satisfied

---

## Execution Wave DAG

```
Wave 0 (parallel):     T001  T002  T003
Wave 1:                T004                         (depends on T001, T002, T003)
Wave 2 (parallel):     T005  T006  T007              (depends on T004)
Wave 3 (build gate):   [mvnw clean package]          (depends on T005, T006, T007)
Wave 4 (parallel):     T008  T009  T010  T011  T012  T013  (depends on Wave 3)
Wave 5 (MVP gate):     [Landing Page visible]        (depends on T008-T013)
Wave 6 (parallel):     T014  T015                    (depends on Wave 5)
Wave 7:                T016                          (depends on T014, T015)
Wave 8:                T017  T018                    (depends on Wave 5)
Wave 9:                T019  T020  T021              (depends on Wave 5)
Wave 10 (parallel):    T022  T023  T024              (depends on Wave 5 — safe to clean up old code)
Wave 11:               T025                          (depends on T005, T006)
Wave 12:               T026  T027                    (depends on Wave 5)
Wave 13 (final gate):  T028                          (depends on all)
```

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **US1 (Phase 3, P1, MVP)**: Depends on Foundational
- **US2 (Phase 4, P2)**: Depends on US1 (landing page must exist to switch language)
- **US3 (Phase 5, P3)**: Depends on US1 (CSS is part of landing page)
- **US4 (Phase 6, P3)**: Depends on US1 (navigation/CTA are in the template)
- **Cleanup & Testing (Phase 7)**: Depends on all user stories

### Parallel Opportunities

- T001 + T002 + T003 — All pom.xml and file removal changes
- T005 + T006 + T007 — Controller + Config + Exception handler are independent
- T008 + T009 + T010 + T011 + T012 + T013 — Template + CSS + Pico + Fonts + Logos + Error pages
- T014 + T015 — English + Russian message files
- T017 + T018 — CSS responsive + template mobile adjustments
- T019 + T020 + T021 — Nav + config + CTA
- T022 + T023 + T024 — Cleanup of old files
- T026 + T027 — Config updates

### Implementation Strategy (MVP First)

1. **Waves 0-3**: Setup + Foundational → App compiles with Thymeleaf
2. **Waves 4-5**: US1 (Landing Page content) → **MVP**: Page visible at localhost:8080
3. **STOP and VALIDATE**: Test US1 independently with browser
4. **Wave 6-7**: US2 (Bilingual i18n) — language switching
5. **Wave 8**: US3 (Responsive) — mobile/tablet/desktop
6. **Wave 9**: US4 (Nav & CTA) — interactive elements
7. **Waves 10-12**: Cleanup + Tests + Config finalization
8. **Wave 13**: Final verification gate
