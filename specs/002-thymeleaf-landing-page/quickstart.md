# Quickstart — Thymeleaf Landing Page

## Prerequisites
- Java 21, Maven Wrapper (`backend/mvnw.cmd`)
- PostgreSQL running (Docker Compose)
- Branch: `feat/002-thymeleaf-landing-page`

## Implementation Steps

### Step 1: Setup i18n Resource Files
- Copy `spec_input_files/messages.properties` → `backend/src/main/resources/messages.properties`
- Copy `spec_input_files/messages_ru.properties` → `backend/src/main/resources/messages_ru.properties`
- Configure `MessageSource` bean in `WebConfig.java`

### Step 2: Configure Thymeleaf in WebConfig
- Add `SpringTemplateEngine` bean with `TemplateEngineMessageSource`
- Add `ThymeleafViewResolver` bean with `.html` suffix

### Step 3: Add LocaleChangeInterceptor
- Register `LocaleChangeInterceptor` in `WebMvcConfigurer.addInterceptors()`
- Default locale: English. Switch via `?lang=en` or `?lang=ru`

### Step 3b: Add Error Handling
- Create `GlobalExceptionHandler.java` with `@ControllerAdvice` 
- Handle 404 (`NoHandlerFoundException`) and 500 (`Exception`) with user-friendly templates
- No stack traces exposed — return graceful messages in HTML
- Create `src/main/webapp/static/error/404.html` and `500.html`

### Step 4: Create LandingPageController
- `@Controller` class with `@GetMapping("/")`
- Add model attribute for page title
- Add `@Value("${landing.cta.url:/auth/login}")` for CTA target URL (externalized)
- Add `landing.cta.url` to `application.properties`, `application-dev.properties`, `application-prod.properties`
- **CRITICAL**: Register as `@Bean` in `WebConfig.java` (per BUGS.md B1 — @Controller alone is invisible)

### Step 5: Create landing.html Thymeleaf Template
- Based on `spec_input_files/landing_page.html` — replace inline text with `th:text="#{key}"`
- All 8 sections: Header, Hero, Problem, How It Works, Features, Trust & Control, FAQ, Final CTA
- FAQ: `<details>` / `<summary>` (no JS needed)
- Language switcher: `th:onclick="|window.location='?lang=en'|"`, active class via `th:classappend`

### Step 6: Create Static Assets
- `src/main/webapp/static/css/landing.css` — design tokens and component styles
- Copy SVG logos from `spec_input_files/` to `src/main/webapp/static/images/logos/`
- Fonts (Manrope + Inter): self-hosted WOFF2 in `src/main/webapp/static/fonts/` — no Google Fonts CDN (privacy: no third-party requests, GDPR-safe)
- `src/main/webapp/static/error/404.html` — custom 404 page (no stack traces)
- `src/main/webapp/static/error/500.html` — custom 500 page (no stack traces)

### Step 7: Root URL Conflict Resolution
- `HelloWorldController` currently maps `GET /`. Landing page replaces it.
- Option: Move HelloWorldController to `/hello` or remove (feature complete).
- Decision: **Option A** — Landing at `/`, remove HelloWorld mapping.

### Step 8: Create Controller Test
- `LandingPageControllerTest.java` — MockMvc test for `GET /`
- Test: HTTP 200, view name, model attributes
- i18n test with `?lang=ru` parameter

### Step 9: Build & Verify
- `cd backend && .\mvnw.cmd clean package` — builds successfully
- `docker compose up` — Landing Page visible at `http://localhost:8080`
- Verify all 8 sections, language switching, responsive layout, FAQ toggle

## Test Commands
```powershell
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```
