# Implementation Plan: Thymeleaf Landing Page

**Branch**: `feat/002-thymeleaf-landing-page` | **Date**: 2026-05-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-thymeleaf-landing-page/spec.md`

## Summary

Implement a professional Landing Page that introduces ResumAIner to first-time visitors. The page consists of 8 sections (Header, Hero, Problem, How It Works, Features, Trust & Control, FAQ, Final CTA) using Thymeleaf, Pico CSS base, and custom CSS with Warm Professional / Ink + Sand + Emerald design system. Supports bilingual EN/RU via Spring i18n, responsive layout, native HTML FAQ, and CTA buttons pointing to the Vue SPA authentication flow.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring MVC 6.x, Thymeleaf (Spring MVC integration), Pico CSS (base reset/typography), custom `landing.css` (design tokens and component styles), SLF4J + Logback

**Storage**: N/A — static presentation layer, no persistent data. i18n messages in `messages.properties` / `messages_ru.properties`.

**Testing**: JUnit 5 + Mockito + Spring MVC Test (MockMvc) for controller test

**Target Platform**: Modern browsers (Chrome, Firefox, Safari, Edge) on desktop, tablet, and mobile. Tomcat 10.1+ server.

**Project Type**: Web application — Thymeleaf Landing Page (public, no auth required)

**Performance Goals**: Page loads and displays all content within 3 seconds on standard broadband connection. No visual layout shift after initial render.

**Constraints**: No "Log in" button anywhere. No JavaScript required for core functionality (FAQ works natively). No stock images, no AI-hype visuals. All text externalized in i18n resource files. Responsive on mobile/tablet/desktop. CTA URL must be externalized in `application.properties` (not hardcoded). Fonts must be self-hosted WOFF2 (no Google Fonts CDN in production). Custom error pages for 404/500 without stack traces.

**External Config**:
- `landing.cta.url` (default: `/auth/login`) — CTA button target, externalized in `application.properties` per profile
- Fonts: self-hosted Manrope + Inter as WOFF2 in `static/fonts/` — no Google Fonts CDN in production
- Error pages: custom 404 and 500 templates via `@ControllerAdvice`, no stack traces

**Scale/Scope**: Single page, 8 sections, ~50 i18n message keys per language, 2 languages (EN, RU). Estimated 300-500 lines of Thymeleaf template, 400-600 lines of CSS.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Code Quality & Maintainability
- **Package Structure**: ✅ Controller goes to `controller/`, config to `config/`, i18n to `resources/`. Follows layered architecture.
- **Java Code Convention**: ✅ Standard Java naming conventions apply.
- **Javadoc**: ✅ Public controller method needs Javadoc.
- **SOLID & DRY**: ✅ Single responsibility — controller handles routing only. Template manages presentation.
- **Minimal Dependencies**: ✅ Pico CSS is lightweight (minimal). No unnecessary libraries.
- **Design Patterns**: ✅ Not applicable for this feature (no new GoF patterns introduced).
- **Maven CLI Build**: ✅ `mvn clean package` must succeed.

### II. Testing Excellence
- **Test Framework**: ✅ JUnit 5 + Mockito + MockMvc for controller test.
- **Coverage Target**: ✅ Controller test required. Landing page is thin — coverage applies to controller logic.
- **TDD for Business Logic**: ✅ No new business logic — presentation only. Controller is thin.
- **Scenario Coverage**: ✅ Positive: HTTP 200 with landing page. Negative: 404 handling.
- **Test Structure**: ✅ Arrange-Act-Aassert with descriptive method names.

### III. User Experience Consistency
- **Internationalization**: ✅ CRITICAL — all text MUST be externalized. No hardcoded strings in template. Spec FR-011 confirmed.
- **Dual Validation**: ✅ Not applicable — no forms on landing page.
- **PRG Pattern**: ✅ Not applicable — no forms.
- **Error Messages**: ✅ Landing page 404/500 must show graceful message without stack traces.
- **Empty States**: ✅ Not applicable — no data views.
- **Pagination**: ✅ Not applicable.

### IV. Performance & Reliability
- **Database Access**: ✅ N/A — no database access.
- **UTF-8 Encoding**: ✅ i18n resource files must use UTF-8 encoding.
- **Content Budget Enforcement**: ✅ N/A — no AI-generated content.

### V. Security by Design
- **Error Safety**: ✅ Must not expose stack traces on 404/500.
- **Log Safety**: ✅ No secrets to log in this feature.
- **Public Link Safety**: ✅ No public resume links in this feature.
- **Dual Validation (Security)**: ✅ N/A — no forms.

**Gate Status**: ✅ PASS — all applicable principles satisfied. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/002-thymeleaf-landing-page/
├── spec.md              # Feature specification (Approved)
├── plan.md              # This file
├── research.md          # Phase 0 output (if needed)
├── data-model.md        # Phase 1 output (if applicable)
├── quickstart.md        # Phase 1 output
├── memory-synthesis.md  # Memory synthesis for context
├── doc-synthesis.md     # Doc synthesis
├── checklists/
│   └── requirements.md  # Quality checklist
└── spec_input_files/    # Design reference files
```

### Source Code (repository root)

```text
backend/src/main/java/com/resumainer/
├── config/
│   └── WebConfig.java              # [MODIFY] Register LandingPageController bean
├── controller/
│   ├── HelloWorldController.java   # [EXISTING → DELETE] Removed (replaced by LandingPageController)
│   └── LandingPageController.java  # [NEW] GET / — renders landing.html, reads landing.cta.url from @Value
├── exception/
│   └── GlobalExceptionHandler.java # [NEW] @ControllerAdvice — custom 404/500, no stack traces
└── ... (other packages not affected by this feature)

backend/src/main/resources/
├── templates/
│   └── landing.html                # [NEW] Thymeleaf template for Landing Page
├── messages.properties             # [NEW/COPY FROM REFERENCE] English i18n
├── messages_ru.properties          # [NEW/COPY FROM REFERENCE] Russian i18n
├── application.properties          # [MODIFY] Register Thymeleaf config (if not already)
├── application-dev.properties      # [MAY MODIFY] If needed
└── application-prod.properties     # [MAY MODIFY] If needed

backend/src/main/webapp/
└── static/
    ├── css/
    │   ├── landing.css             # [NEW] Custom Landing Page CSS
    │   └── (vendor/pico/ if local) # [NEW] Pico CSS if self-hosted
    ├── fonts/                      # [NEW] Self-hosted Manrope + Inter WOFF2 (no CDN)
    ├── images/
    │   └── logos/                  # [NEW] SVG logo assets
    └── error/
        ├── 404.html                # [NEW] Custom 404 page (no stack traces)
        └── 500.html                # [NEW] Custom 500 page (no stack traces)

backend/src/test/java/com/resumainer/
└── controller/
    └── LandingPageControllerTest.java  # [NEW] MockMvc test
```

**Structure Decision**: Single backend module with standard Spring MVC structure. No frontend module changes — this is a Thymeleaf template served from the backend.

## Complexity Tracking

No constitution violations — all principles pass. Complexity tracking not required.

## Phase 0: Research

No NEEDS CLARIFICATION — all technical decisions are clear:
- **i18n**: Spring `MessageSource` with `ReloadableResourceBundleMessageSource`, `LocaleChangeInterceptor` via URL param `?lang=en|ru`
- **Thymeleaf**: Spring MVC integration with `ThymeleafViewResolver`
- **Pico CSS**: Minimal version as base reset + design tokens overridden in `landing.css`
- **Controller**: `LandingPageController` with `@GetMapping("/")` — must be registered as `@Bean` in `WebConfig` (per B1 from memory)
- **Landing page at root**: Replaces existing `HelloWorldController` root mapping — HelloWorldController is removed entirely (feature 001 complete)

### Research Required

1. **Root URL conflict**: Resolved — **Option A chosen**. Landing Page serves at `/`. HelloWorldController and its associated JSP view (`hello.jsp`) are removed. All JSP dependencies (`jakarta.servlet.jsp-api`, `jakarta.servlet.jsp.jstl-api`) are removed from `pom.xml`. The application migrates entirely from JSP to Thymeleaf.

**Decision**: Option A — Landing Page at root. HelloWorldController removed.

*(Research file not needed — single straightforward decision documented here.)*

## Phase 1: Design & Contracts

### Data Model
No persistent data entities. This feature uses only i18n message keys (static content).

### Contracts
No external API contracts. Thymeleaf renders server-side HTML.

### Quickstart
See [quickstart.md](quickstart.md) for implementation steps.
