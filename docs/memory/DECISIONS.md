# Technical Decisions (`docs/memory/`)

This file stores durable technical and implementation decisions. For governance-level decisions or project standards, see `.specify/memory/DECISIONS.md`.

## Entry Lifecycle

Each decision follows this lifecycle:

```
Active → Needs Review → Superseded → (pruned)
```

- **Active**: The decision is current and must be honored by all features and AI agents.
- **Needs Review**: Implementation reality or new context suggests this decision may be outdated. It should still be honored until reviewed and explicitly changed.
- **Superseded**: A newer decision has replaced this one. Keep it for historical context until the next audit, then consider pruning.
- **Pruned**: During an audit, remove superseded entries that no longer provide historical value. This keeps the file focused.

### When to change status

| Current Status | Change To    | When                                                                                                       |
| -------------- | ------------ | ---------------------------------------------------------------------------------------------------------- |
| Active         | Needs Review | Verified implementation or tests contradict the decision, or recurring features follow a different pattern |
| Active         | Superseded   | A newer decision explicitly replaces this one                                                              |
| Needs Review   | Active       | Team confirms the decision still holds after review                                                        |
| Needs Review   | Superseded   | Team confirms a replacement decision                                                                       |
| Superseded     | _(remove)_   | Audit finds no remaining historical value                                                                  |

### Rules

- Never delete an Active decision without replacing or superseding it.
- Never silently ignore a decision. If it feels wrong, mark it Needs Review and resolve it.
- Keep at most 3–5 Superseded entries for context. Prune older ones during audits.

---

## Template

### YYYY-MM-DD - Decision title

**Status**
Active | Superseded | Needs review

**Why this is durable**
What cross-feature choice is likely to matter again?

**Decision**
What was decided and what boundary does it create?

**Tradeoffs**
What was gained, what was made harder, and when should this be reconsidered?

**Future mistake prevented**
What likely incorrect approach does this rule out?

**Evidence**
Diff, tests, review, incident, or repeated implementation evidence.

**Where to look next**
Files, modules, or specs future maintainers should inspect.

---

### 2026-05-30 - Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml)

**Status**
Active

**Why this is durable**
Tomcat 10.1+ uses Jakarta EE 10 (jakarta.servlet.* namespace). web.xml with javax.* namespace causes class loading conflicts. This decision applies to every servlet/controller feature in the project.

**Decision**
Use `AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer` for servlet initialization instead of web.xml. The initializer:
- Registers DispatcherServlet with "/" mapping
- Loads WebConfig (@Configuration, @EnableWebMvc) as servlet config class
- Is auto-discovered by Tomcat via ServletContainerInitializer SPI
- Provides compile-time type safety (vs string-based class names in web.xml)

**Tradeoffs**
- Gained: Type-safe configuration, one less XML file, Jakarta EE 10 compatible, compile-time errors instead of runtime failures
- Made harder: Requires understanding of Servlet 3.0+ SPI mechanism
- Reconsider: If the project migrates to embedded containers (future consideration)

**Future mistake prevented**
Adding a web.xml with javax.servlet.* declarations would cause incompatibility errors with Tomcat 10.1+ and Spring MVC 6.x.

**Evidence**
Spring Framework 6.2 reference docs confirm this is the standard approach for Jakarta EE environments. Spec review feedback identified the javax/jakarta namespace conflict.

**Where to look next**
backend/src/main/java/com/resumainer/initializer/AppInitializer.java

---

### 2026-05-30 - Maven Wrapper Must Be at Same Directory Level as pom.xml

**Status**
Active

**Why this is durable**
Maven Wrapper scripts (mvnw, mvnw.cmd) determine the project root directory. They look for pom.xml in the same directory. If pom.xml is elsewhere (e.g., backend/), running mvnw from the project root fails with "The goal you specified requires a project to execute but there is no POM in this directory."

**Decision**
Place mvnw, mvnw.cmd, and .mvn/wrapper/ in the same directory as pom.xml. For this project: all in backend/ alongside backend/pom.xml.

**Tradeoffs**
- Gained: mvnw clean package works immediately, no -f flag needed, matches Maven Wrapper documentation and community convention.
- Made harder: Developer must cd backend/ before running mvnw, or use cd backend && ./mvnw ... from project root (minor inconvenience).
- Reconsider: If the project restructures to a multi-module build with a parent pom.xml at root, the wrapper should move to root level.

**Future mistake prevented**
Putting mvnw at the project root while pom.xml lives in a subdirectory causes confusing "no POM in this directory" errors. Developers waste time debugging build configuration when the issue is simply directory mismatch.

**Evidence**
Maven Wrapper documentation (maven.apache.org/wrapper/): "mvnw must be placed in the same directory as pom.xml." Spec review flagged the inconsistency during Context7 documentation check.

**Where to look next**
backend/mvnw, backend/pom.xml, and any future multi-module restructure.

---

### 2026-05-30 - Docker Tomcat: Use bash /dev/tcp Instead of nc for TCP Health Checks

**Status**
Active

**Why this is durable**
The official tomcat Docker image does not include netcat (nc). Any script that relies on nc -z for TCP connectivity checks will fail silently in containers built from this image. This applies to all future Docker services that need startup ordering.

**Decision**
Use bash's built-in TCP pseudo-device instead of nc for port availability checks:
`timeout 1 bash -c "echo >/dev/tcp/$HOST/$PORT" 2>/dev/null`
This works in any bash 4+ environment without additional packages.

**Tradeoffs**
- Gained: No need to install netcat in the container (smaller image, fewer dependencies).
- Made harder: The /dev/tcp syntax is bash-specific and not POSIX sh-compatible. The script must use #!/usr/bin/env bash, not sh.
- Reconsider: If the project migrates to a minimal base image that lacks bash entirely (e.g., Alpine with ash only), install netcat or use a different tool.

**Future mistake prevented**
Adding a wait-for-it.sh script that uses nc will silently hang or fail in Tomcat-based containers, causing confusing startup failures that are hard to debug inside a running container.

**Evidence**
Docker build and run test: nc not found in tomcat:10.1.28-jdk21-temurin-jammy. Script using nc -z timed out repeatedly. Replaced with bash /dev/tcp — worked immediately.

**Where to look next**
docker/scripts/wait-for-it.sh

---

### 2026-05-30 - Docker Dev Workflow: Rebuild Image After Code Changes

**Status**
Active

**Why this is durable**
When running a Java application in Docker, the WAR/JAR is baked into the image during docker build. docker compose up -d without rebuilding uses the cached image with the old code. Developers waste time debugging why changes don't take effect.

**Decision**
After any Java source code change, follow this workflow:
1. mvnw clean package (rebuilds WAR locally)
2. docker compose build --no-cache (rebuilds image with new WAR — no-cache ensures Maven runs)
3. docker compose up -d (starts fresh container)

Alternatively use the --build flag: docker compose up -d --build

**Tradeoffs**
- Gained: Guarantees the running container reflects the latest code. No stale-code debugging.
- Made harder: Full rebuild takes ~2-3 minutes. For rapid iteration without Docker changes, run mvnw test or start Tomcat locally.
- Reconsider: For frontend code (HTML, JS, CSS) that doesn't need compilation, bind mounts can be used instead of rebuilds.

**Future mistake prevented**
Running docker compose up -d after changing Java code shows the old behavior. Developer assumes the fix didn't work and wastes time debugging the wrong version.

**Evidence**
Changed HelloWorldController to show active profile via @Value. docker compose up -d still showed "default" for 3 iterations. Only after docker compose build --no-cache did the new WAR take effect.

**Where to look next**
docker/Dockerfile, docker/docker-compose.yml

---

### 2026-05-31 - Error pages use Thymeleaf templates for i18n support, static fallback for low-level failures

**Status**
Active

**Why this is durable**
Every feature that adds i18n will need error pages in all supported languages. This decision defines the pattern: Thymeleaf templates with full branding for most errors, static HTML only for failures before the template engine is available.

**Decision**
Error pages (404, 500) are implemented as Thymeleaf templates in `templates/error/` with full header, nav bar, language switcher, and footer matching the landing page design. `GlobalExceptionHandler` forwards to Thymeleaf views (`error/404`, `error/500`). Static HTML copies in `static/error/` are kept as low-level fallbacks for failures that occur before Thymeleaf initialization.

**Tradeoffs**
- Gained: Bilingual error pages, consistent branding, language switcher works on error pages
- Made harder: Two copies to maintain (Thymeleaf + static fallback)
- Reconsider: If the project drops i18n or all errors go through Thymeleaf, static fallbacks can be removed

**Future mistake prevented**
Creating new static error pages without i18n support, or forgetting to keep static fallbacks for low-level failures.

**Where to look next**
backend/src/main/resources/templates/error/404.html, backend/src/main/resources/templates/error/500.html, backend/src/main/java/com/resumainer/exception/GlobalExceptionHandler.java

---

### 2026-05-31 - Custom 404 requires DispatcherServlet config + bean registration

**Status**
Active

**Why this is durable**
Every feature that adds i18n or custom error pages needs this configuration. Without it, 404 errors fall through to the servlet container default page.

**Decision**
Custom 404 Thymeleaf templates require two changes:
1. `AppInitializer`: override `createDispatcherServlet()` and call `dispatcherServlet.setThrowExceptionIfNoHandlerFound(true)` so Spring MVC throws `NoHandlerFoundException` for unhandled URLs.
2. Register the `@ControllerAdvice` handler (e.g. `GlobalExceptionHandler`) as an explicit `@Bean` in `WebConfig` (see B5).

**Tradeoffs**
- Gained: Custom bilingual 404 pages with full branding, nav, and language switch
- Made harder: Two configuration points to remember (initializer + bean)
- Reconsider: If the project adds Spring Boot later, both steps become automatic

**Future mistake prevented**
Adding custom error pages without configuring the DispatcherServlet to throw exceptions for unhandled URLs.

**Where to look next**
backend/src/main/java/com/resumainer/initializer/AppInitializer.java, backend/src/main/java/com/resumainer/config/WebConfig.java

---

### 2026-06-02 - Hybrid PK strategy: gen_random_uuid() for entities, BIGSERIAL for lookups

**Status**
Active

**Why this is durable**
Every new table added in future features needs to follow this PK strategy. Choosing the wrong PK type for a new table would cause inconsistency.

**Decision**
Use PostgreSQL built-in `gen_random_uuid()` (UUID v4) for entity table primary keys (`users`, `contact_detail`, and all future business entity tables). Use `BIGSERIAL` (GENERATED BY DEFAULT AS IDENTITY) for lookup/reference tables (`role`, `user_status`, `user_permission`, `language`). No custom UUID generators or extensions.

**Tradeoffs**
- Gained: Zero setup (built-in), UUID prevents enumeration on entity PKs exposed in URLs, BIGSERIAL is more efficient for small lookup tables (2-6 rows)
- Made harder: FKs from entity tables to lookup tables are mixed type (UUID → BIGINT), but this is transparent at the JDBC level
- Reconsider: If a future feature needs distributed ID generation, consider switching to UUID v7 with a Java library

**Future mistake prevented**
Adding auto-increment to entity tables (enumeration vulnerability) or adding UUID to lookup tables (unnecessary overhead).

**Evidence**
Feature 003 spec, plan, and data-model.md all updated to use hybrid strategy. Context7 research confirmed PostgreSQL 17 has no built-in UUID v7.

---

### 2026-06-02 - CSRF via OWASP cookie-to-header pattern in pure Spring MVC

**Status**
Active

**Why this is durable**
Every feature with form submissions needs CSRF protection. Without Spring Security, there is no built-in CSRF filter. This pattern must be reused for all future POST/PUT/DELETE endpoints.

**Decision**
Implement CSRF via custom `CsrfFilter` (extends OncePerRequestFilter):
1. On login: generate token via SecureRandom, store in HttpSession, set as non-HTTP-only cookie `XSRF-TOKEN`
2. Vue SPA reads token from `document.cookie`, sends as `X-CSRF-Token` header
3. CsrfFilter validates header matches session token for POST/PUT/DELETE
4. Exclude `/api/auth/*` (unauthenticated) and `/api/public/**` (future) from validation
5. Register via `AppInitializer.getServletFilters()` — NOT FilterRegistrationBean (see B6)

**Tradeoffs**
- Gained: OWASP-recommended pattern for SPAs, no additional dependencies, works alongside existing SameSite=Lax cookies
- Made harder: Requires custom filter + Vue interceptor (vs Spring Security's auto-configuration)
- Reconsider: If the project adds Spring Security in the future, replace CsrfFilter with Spring Security's built-in CSRF protection

**Future mistake prevented**
Assuming SameSite=Lax alone is sufficient for CSRF protection, or trying to add Spring Security just for CSRF.

**Evidence**
OWASP CSRF Cheat Sheet confirms SameSite is "defense-in-depth" not a replacement. SEC-003 from Feature 003 security review.

---

### 2026-06-03 - JDK version must match project target to avoid build and test failures

**Status**
Active

**Why this is durable**
pom.xml targets Java 21 (`<release>21</release>`, `<maven.compiler.source>21</maven.compiler.source>`). Installing a newer JDK (23+) causes subtle failures: Mockito 5.x inline mock maker cannot self-attach because JDK 23+ disables the Attach API. Every developer setting up this project will face this if their system JDK doesn't match.

**Decision**
The installed JDK version must match the project target version (Java 21). Do NOT use workarounds like `-Djdk.attach.allowAttachSelf=true` or manually adding `-javaagent` paths — these are fragile and version-specific. Instead:
1. Install Eclipse Temurin JDK 21 (winget: EclipseAdoptium.Temurin.21.JDK)
2. Set JAVA_HOME to the JDK 21 installation path
3. Verify: `java -version` shows &quot;21.0.x&quot;, not &quot;25.0.x&quot; or later

**Tradeoffs**
- Gained: Mockito works out of the box, no argLine hacks, predictable test behavior
- Made harder: Developer must install and configure a specific JDK version instead of using the latest system JDK
- Reconsider: When the project upgrades its Java target version, update the dev JDK accordingly

**Future mistake prevented**
Wasting hours debugging Mockito failures, adding fragile argLine workarounds, or accidentally compiling with a different Java version than the project target.

**Evidence**
Phase 2 implementation: first test run failed with &quot;Failed to load interface org.mockito.plugins.MockMaker&quot; on JDK 25. After installing JDK 21 and setting JAVA_HOME, all 26 tests passed immediately with only a warning about future self-attach deprecation.

**Where to look next**
README.md (setup instructions), backend/pom.xml (`<release>21</release>`), CI/CD configuration

---

### 2026-06-03 - DAO connection-accepting overloads for JDBC transaction support

**Status**
Active

**Why this is durable**
Registration requires creating User + ContactDetail atomically. The standard DAO pattern (each method opens/closes its own Connection via DataSource) cannot support multi-table transactions. This pattern will repeat for every future feature that needs atomic multi-table operations.

**Decision**
Each DAO that participates in transactions MUST provide two `create` (or equivalent) methods:
1. `create(Entity entity)` — auto-managed connection (convenience, uses DataSource internally)
2. `create(Entity entity, Connection conn)` — uses an existing connection (called from service layer within a transaction)

The service layer manages the transaction:
1. Gets a Connection from DataSource
2. Sets autoCommit(false)
3. Calls DAO methods with the shared Connection
4. Commits or rollbacks
5. Closes the Connection

**Tradeoffs**
- Gained: Clean transaction support without ORM, minimal code duplication, backward compatible
- Made harder: Each transactional DAO needs two overloads per transactional method
- Reconsider: If the project adds Spring transaction management (@Transactional via AOP), these overloads become unnecessary

**Future mistake prevented**
Adding multi-table operations without transaction support, or trying to use Spring's @Transactional (which requires AOP configuration in pure Spring MVC).

**Evidence**
AuthService.register() needed atomic User + ContactDetail creation. UserDao and ContactDetailDao were extended with connection-accepting create() overloads. AuthService manages the transaction lifecycle. All 44 tests pass.

**Where to look next**
backend/src/main/java/com/resumainer/dao/UserDao.java, ContactDetailDao.java, backend/src/main/java/com/resumainer/service/AuthService.java

---

### 2026-06-03 - PrimeVue 4 Form with Zod resolver: standard validation pattern

**Status**
Active

**Why this is durable**
Every Vue SPA feature that accepts user input needs form validation. PrimeVue 4 introduced a new Form component with resolver-based validation. The pattern is non-obvious and differs from PrimeVue 3. All future forms must follow this pattern for consistency.

**Decision**
All form validation in the Vue SPA uses PrimeVue 4 Form component with Zod resolver:

1. Define a Zod schema for the form data
2. Wrap with `zodResolver` from `@primevue/forms/resolvers/zod`
3. Pass as `:resolver` prop to `<Form>`
4. Use `<FormField v-slot="$field" name="fieldName">` for each field
5. Show errors via `<Message v-if="$field?.invalid" severity="error" size="small" variant="simple">`
6. Handle submit via `@submit="onSubmit"` which receives `{ valid, values }`

Import pattern:
```typescript
import { Form, FormField } from '@primevue/forms'
import { zodResolver } from '@primevue/forms/resolvers/zod'
import { z } from 'zod'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Message from 'primevue/message'
```

Components are imported individually (not from a barrel), following PrimeVue 4 tree-shaking conventions.

**Tradeoffs**
- Gained: Declarative validation with Zod schemas, field-level error messages, consistent across all forms, tree-shakeable imports
- Made harder: Requires understanding Zod schema API and PrimeVue 4 Form resolver pattern
- Reconsider: If the project switches to a different form library (e.g., VeeValidate), the resolver pattern changes

**Future mistake prevented**
Using PrimeVue 3 patterns (old InputText + manual validation) on PrimeVue 4, or using v-model directly without Form resolver. These patterns would bypass PrimeVue 4's built-in validation state management.

**Evidence**
LoginForm.vue and RegisterForm.vue successfully implement PrimeVue 4 Form + Zod resolver with email, password, confirmPassword, and rememberMe fields. All validation works with bilingual error messages.

**Where to look next**
frontend/src/components/LoginForm.vue, frontend/src/components/RegisterForm.vue

---

### 2026-06-03 - All user-facing strings must use i18n $t() — no hardcoded text in templates

**Status**
Active

**Why this is durable**
During manual testing, hardcoded English strings were found in AuthPage.vue (info panel text, subtitles) and in LoginForm/RegisterForm (Zod validation messages). These were not caught during implementation because they were "invisible" — the page looked correct in English, but switching to Russian revealed untranslated text. Every future feature with UI will have the same risk.

**Decision**
Every user-facing string in every Vue component MUST use `$t('key')` or `t('key')` from vue-i18n. This includes:

1. **Visible text in templates** — headings, paragraphs, labels, placeholders, button text, link text
2. **Info/instructional text** — info panels, help text, empty states, tooltips
3. **Validation messages** — Zod schema error messages, inline form errors
4. **Alt text and aria-labels** — for images, icons, accessibility

The ONLY exceptions are:
- Product name "ResumAIner" (must not be translated)
- Brand taglines that are English-only by design
- Technical/error codes (e.g., "auth.invalidCredentials")

**Tradeoffs**
- Gained: Bilingual pages work correctly in both languages, no hidden English strings, easier to add more languages later
- Made harder: Requires discipline to never use a literal string, slightly more verbose templates
- Reconsider: If the project drops i18n support, hardcoded strings would be simpler

**Future mistake prevented**
Adding a new Vue component with hardcoded strings that look correct in English but break the bilingual experience. This is invisible during development (dev always uses English) and only caught during manual testing with language switch.

**Evidence**
Manual testing found 6 hardcoded strings in AuthPage.vue (info panel, subtitle), 8 hardcoded Zod messages in LoginForm/RegisterForm, and duplicate toggle text caused by i18n key design. All fixed by moving strings to en.json/ru.json and using $t().

**Where to look next**
frontend/src/views/AuthPage.vue, frontend/src/components/LoginForm.vue, frontend/src/components/RegisterForm.vue, frontend/src/i18n/en.json, frontend/src/i18n/ru.json

---

### 2026-06-03 - Mandatory manual integration testing phase after all implementation phases

**Status**
Active

**Why this is durable**
Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction.

Every future feature will have the same blind spots that unit tests can't cover: Docker deployment, real database interaction, browser rendering, i18n correctness, cross-component integration, and session management.

**Decision**
After ALL implementation phases of any feature, there MUST be a mandatory integration testing phase with the following checklist:

1. **Rebuild Docker** — `docker compose build --no-cache` then `docker compose up -d`
2. **Verify all containers healthy** — `docker compose ps` — db, app, frontend all running
3. **API smoke test** — POST register, POST login, GET status, POST logout via curl/PowerShell
4. **Frontend smoke test** — Open http://localhost in browser, verify page loads
5. **Main user flow** — Register a new user → Login → Verify redirect to home page
6. **Logout** → Verify redirect to login page
7. **Login with registered user** → Verify redirect to home page
8. **i18n verification** — Switch to Russian → Verify ALL text is translated on every page (including validation messages!)
9. **Edge cases** — Login with wrong password (401), login with non-existent email (401), submit empty forms (validation errors shown), register duplicate email (409)
10. **API error responses** — Verify error messages are user-friendly (not stack traces)

**Tradeoffs**
- Gained: Catches deployment issues, i18n problems, integration bugs, and DX issues that unit tests miss
- Made harder: Takes 15-30 minutes per feature, requires Docker running, requires manual browser interaction (or Playwright automation)
- Reconsider: For trivial features with no DB/UI changes, a lighter check may suffice

**Future mistake prevented**
Implementing a complete feature that looks perfect in unit tests but fails completely when deployed. This applies to ALL features, regardless of their complexity.

**Evidence**
Feature 003 had 62 unit tests all passing, but 6 critical bugs were found during manual integration testing: API 500 (DataSource + Flyway), unresponsive registration (same root cause), non-functional Russian validation messages, hardcoded English text, duplicate toggle links, and wrong brand logo.

**Where to look next**
Docker Compose configuration, CI/CD pipeline, feature completion checklist

---

### 2026-06-04 - Separate @Configuration for infrastructure beans via @ComponentScan

**Status**
Active

**Why this is durable**
Every feature that adds infrastructure beans (DataSource, caching, messaging) needs a consistent pattern for configuration organization. Without a clear pattern, WebConfig becomes a dumping ground for unrelated beans.

**Decision**
Infrastructure beans belong in separate `@Configuration` classes (e.g., `DataSourceConfig`) discovered via `@ComponentScan("com.resumainer")`. WebConfig stays focused on web MVC configuration (interceptors, view resolvers, message sources, security filters).

This replaces the previous pattern of lumping all `@Bean` methods into WebConfig. The component scan also eliminates the need for explicit `@Bean` methods for `@Repository`/`@Service`/`@Controller`/`@ControllerAdvice` annotated classes.

**Tradeoffs**
- Gained: Clean separation — infrastructure config is independently testable and replaceable
- Gained: WebConfig stays focused on web concerns (~230 lines instead of ~320)
- Gained: Adding new infrastructure (cache, messaging) follows the same pattern — new @Configuration class
- Made harder: Must add @Repository/@Service annotations to all DAOs and services (was optional before)
- Reconsider: If project grows to 10+ @Configuration classes, consider grouping by domain or using @Import for explicit ordering

---

### 2026-06-04 - Controller tests: standalone MockMvc over full Spring context when no DB needed

**Status**
Active

**Why this is durable**
Adding infrastructure beans (DataSource, Flyway) to the Spring context breaks any test that loads `@ContextConfiguration(classes = WebConfig.class)` because the DataSource initialization requires a real PostgreSQL connection. Controller tests that don't need database access should use standalone MockMvc setup to avoid this dependency.

**Decision**
Use `MockMvcBuilders.standaloneSetup(controller)` for controller tests that:
- Test request mapping, view resolution, model attributes
- Don't interact with database, services, or infrastructure beans
- Need to mock service layer dependencies

Use `@ContextConfiguration(classes = WebConfig.class)` only for integration tests that:
- Need the full Spring context (interceptors, filters, property resolution)
- Test end-to-end behavior through all layers

**Impact on @Value properties**
When using standalone setup, `@Value` annotations on controllers are not processed by Spring. Controllers must use constructor injection for `@Value` properties to remain testable:

```java
// Instead of field injection:
// @Value("${landing.cta.url:/auth/login}")
// private String ctaUrl;

// Use constructor injection:
private final String ctaUrl;

public LandingPageController(@Value("${landing.cta.url:/auth/login}") String ctaUrl) {
    this.ctaUrl = ctaUrl;
}
```

Test then creates the controller directly:
```java
LandingPageController controller = new LandingPageController("/auth/login");
mockMvc = standaloneSetup(controller).build();
```

**Tradeoffs**
- Gained: Tests don't require PostgreSQL or any infrastructure to run
- Gained: Tests run faster (standalone setup in ~150ms vs full context in ~3s)
- Gained: Easy to inject mock services for controller testing
- Made harder: Must use constructor injection for @Value (good practice anyway)
- Reconsider: If a controller starts needing many infrastructure beans, consider whether standalone setup is still appropriate

---

### 2026-06-06 — PrimeVue DataTable lazy mode for server-paginated APIs

**Status**: Active

**Why this is durable**: When backend paginates with SQL LIMIT/OFFSET, PrimeVue DataTable MUST use `:lazy="true"`. Client-side mode loads ALL records into browser memory and defeats server-side search/filter/sort. This mistake is easy to make (lazy is not the default) and hard to catch early.

**Decision**: All future features with server-paginated DataTable MUST use lazy mode: `:lazy="true"`, `:totalRecords`, `@page`/`@sort`/`@filter` callbacks that trigger backend API calls. Do NOT use client-side mode when the backend paginates.

**Tradeoffs**:
- Gained: server-side pagination, no data duplication, efficient filtering
- Made harder: client-side instant search across all records (requires debounced backend calls instead)
- Reconsider: For tiny datasets (&lt;50 rows, no backend pagination), client-side mode is acceptable

---

### 2026-06-06 — Independent block loading for resilient page architecture

**Status**: Active

**Why this is durable**: User Home page loads data from two independent API sources (profile summary + saved resumes). If one fails, the other block remains functional. This pattern prevents cascading failures and improves perceived performance.

**Decision**: Dashboard-like pages with multiple independent data sources MUST use separate API endpoints per block, and each block MUST handle loading/error states independently. A failure in one block MUST NOT block other blocks from rendering.

**Tradeoffs**:
- Gained: partial page functionality during API failures, independent loading
- Made harder: more API calls (two instead of one), more complex state management
- Reconsider: If blocks share the same data source, a single endpoint is simpler and appropriate

---

### 2026-06-06 — Maven compiler -parameters flag required in Spring MVC 6 for controller parameter name resolution

**Status**: Active

**Why this is durable**: Spring Framework 6.x requires the Java compiler `-parameters` flag to discover method parameter names via reflection. Without it, `@RequestParam`, `@PathVariable`, and `@SessionAttribute` annotations that omit explicit `name`/`value` attributes fail with: "Name for argument type not specified, and parameter name information not available via reflection." This affects every new controller added to the project.

**Decision**: The `maven-compiler-plugin` MUST include `<parameters>true</parameters>` in its configuration. This is configured in `pom.xml`.

**Tradeoffs**:
- Gained: Spring MVC controllers can use concise `@RequestParam String search` without explicit `name` attribute
- Gained: Consistent with Spring 6.x best practices (officially recommended in Spring 6.0 release notes)
- Made harder: None — this is a one-time configuration change
- Reconsider: If migrating to a different DI framework that doesn't need parameter names

---

### 2026-06-06 — PrimeVue 4: ToastService and ConfirmationService require app.use() plugin installation

**Status**: Active

**Why this is durable**: In PrimeVue 4, Toast and ConfirmDialog components require their corresponding services to be installed as Vue plugins via `app.use()`. Just placing `<Toast />` and `<ConfirmDialog />` in the template is not sufficient — without `app.use(ToastService)` and `app.use(ConfirmationService)`, the `useToast()` and `useConfirm()` composables throw "No PrimeVue Toast/Confirmation provided!".

**Decision**: All PrimeVue 4 projects MUST install both services in `main.ts`:
```typescript
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
app.use(ToastService)
app.use(ConfirmationService)
```

**Tradeoffs**:
- Gained: useToast() and useConfirm() work globally
- Made harder: None — one-time setup
- Reconsider: If PrimeVue changes this pattern in a future version

---

### 2026-06-06 — PrimeVue 4: Tooltip is a global directive requiring explicit registration

**Status**: Active

**Why this is durable**: In PrimeVue 4, the v-tooltip directive is NOT auto-registered by `app.use(PrimeVue)`. It must be explicitly registered via `app.directive('tooltip', Tooltip)`. Without this, `v-tooltip.top="..."` in templates silently fails — no tooltips appear, no errors in console, just invisible functionality.

**Decision**: All PrimeVue 4 projects MUST register the Tooltip directive:
```typescript
import Tooltip from 'primevue/tooltip'
app.directive('tooltip', Tooltip)
```

**Tradeoffs**:
- Gained: v-tooltip works globally in all components
- Made harder: One extra import line per project
- Reconsider: If PrimeVue auto-registers directives in a future version

---

### 2026-06-06 — PrimeVue 4: PrimeIcons is a separate package requiring explicit CSS import

**Status**: Active

**Why this is durable**: In PrimeVue 4, PrimeIcons (`pi pi-*` classes) are NOT included with the core PrimeVue package. They require a separate `npm install primeicons` and explicit CSS import `import 'primeicons/primeicons.css'` in `main.ts`. Without this, all PrimeIcons render as invisible unicode characters — no errors, just blank space where icons should be.

**Decision**: Every PrimeVue 4 project setup MUST include:
1. `npm install primeicons`
2. `import 'primeicons/primeicons.css'` in main.ts
3. Vite handles font file paths correctly regardless of `base` config

**Tradeoffs**:
- Gained: All PrimeIcons render correctly
- Made harder: One extra dependency and import
- Reconsider: None — this is the documented PrimeVue 4 setup

---

### 2026-06-08 - Manual JDBC transaction methods must catch Exception, not just SQLException

**Status**
Active

**Why this is durable**
DAO connection-overloads declare throws SQLException, but DAO auto-managed methods wrap SQLException in RuntimeException. In pure Spring MVC without @Transactional, manual transaction blocks that only catch SQLException will miss RuntimeExceptions thrown by auto-managed DAO methods, causing the transaction to leak without rollback.

**Decision**
Manual JDBC transaction methods (getConnection → setAutoCommit(false) → operations → commit/rollback) MUST catch Exception (not just SQLException) to ensure rollback on all error types. This applies to all ProfileService and future Service classes that manage JDBC transactions manually.

**Tradeoffs**
- Gained: Reliable rollback on all error types, including DAO RuntimeExceptions
- Made harder: Slightly broader catch block
- Reconsider: If Spring's TransactionTemplate is adopted, this pattern is replaced entirely

---

### 2026-06-08 - Sort field names must be mapped between frontend and backend in lazy DataTable

**Status**
Active

**Why this is durable**
PrimeVue DataTable lazy mode sends sortField from Column field prop directly to server. The frontend uses camelCase field names (courseName, startDate) but the DB uses snake_case column names (name, start_date). Without explicit mapping, sort requests fail with IllegalArgumentException and data disappears when user clicks a column header to sort.

This complements D17 (PrimeVue DataTable lazy mode) which mandates lazy loading but does not cover field name mapping.

**Decision**
Every DAO with pagination and sort support MUST:
1. Define ALLOWED_SORT_FIELDS containing both frontend field names (camelCase) and DB column names (snake_case)
2. Implement a mapping from frontend field names to DB column names in validateSortField()
3. Add SORT_FIELDS_NEEDING_MAP with the frontend-only names that need translation
4. Test that sorting by each column returns correct results

Example mapping pattern:
- courseName -> name
- startDate -> start_date
- endDate -> end_date
- courseFocus -> course_focus

**Tradeoffs**
- Gained: Sort works correctly regardless of naming convention differences
- Made harder: DAO needs an additional mapping structure and switch/case or Map
- Reconsider: If frontend and DB column names are identical, mapping is not needed

---

### 2026-06-12 - HTML-first generation pipeline with deferred PDF conversion

**Status**
Active

**Why this is durable**
HTML-to-PDF conversion requires independent library evaluation (Flying Saucer, OpenPDF, wkhtmltopdf), A4 layout validation, Cyrillic font support, and selectable text verification. Bundling it with AI generation would make the feature too large and risky. This split pattern may apply to other composite features.

**Decision**
HTML is the canonical generated artifact in feat/007-resume-generation. PDF conversion is deferred to a separate feature (feat/008-pdf-conversion). PdfGenerationService is defined only as an interface boundary with a NoOp stub. The Export DTO carries pdfAvailable=false and placeholder URLs until the PDF feature is implemented.

**Tradeoffs**
- Gained: Feature can be completed, tested, and merged without waiting for PDF library selection. HTML artifact is usable independently.
- Made harder: Frontend must handle placeholder PDF/public-link actions. Backend must maintain nullable pdf_file_path and clear "not available" responses.
- Reconsider: If a future PDF library proves trivial to integrate, the two features could be merged.

---

### 2026-06-12 - Vue composable state must be module-level singleton, not per-component-instance

**Status**
Active

**Why this is durable**
Multi-page wizards that share state across routes must use module-level state in composables. Otherwise each route page creates a fresh state instance on mount, losing all data from previous steps.

**Decision**
When a Vue composable is used across multiple pages in a wizard flow (e.g., Vacancy -> Settings -> Review -> Export), the reactive state MUST be defined at the module level (outside the exported function), not inside the function body. Otherwise each page that calls useGenerateResumeFlow() gets an independent state ref, causing shared data (like requestId) to reset to defaults on navigation.

Wrong pattern:
```ts
export function useGenerateResumeFlow() {
  const state = ref({ requestId: null })
  return { state }
}
```

Correct pattern:
```ts
const state = ref({ requestId: null })
export function useGenerateResumeFlow() {
  return { state }
}
```

**Tradeoffs**
- Gained: Shared state across wizard pages works correctly. requestId and settings persist when navigating between steps.
- Made harder: Page refresh loses wizard state (acceptable for MVP). Can be mitigated with sessionStorage later.
- Reconsider: If the app needs SSR or multiple independent wizards simultaneously, this pattern needs revisiting.

---

### 2026-06-13 - Backend-generated opaque updateKey for review/save pattern

**Status**
Active

**Why this is durable**
Every feature that involves multi-section review/editing (generated resume, profile, admin forms) needs a safe way for frontend to send back edited field values. The naive approach (frontend constructs field paths) leads to format mismatches, SQL injection risks, and tight coupling to backend column names.

**Decision**
When the backend exposes editable fields in a review DTO, every field variant MUST include an opaque `updateKey` string. The frontend MUST NOT construct update keys manually — it must reuse the `updateKey` returned by the review endpoint.

Format: `sectionKey:recordId:fieldName:adaptationCode`
- sectionKey: section identifier (e.g., professional_positioning, work_experience)
- recordId: UUID of the owning record (response or child row)
- fieldName: frontend-friendly field name (mapped to DB column by backend allowlist)
- adaptationCode: MINIMAL, BALANCED, MAXIMUM (or omitted for non-adaptive fields)

Backend parsing: split by `:`, validate against section-aware allowlist (`ALLOWED_REVIEW_FIELDS_BY_SECTION`), map fieldName to DB column via per-section column map (never raw SQL concatenation). Example: `professional_positioning:<responseId>:professionalTitle:BALANCED` → `UPDATE resume_generation_response SET professional_title = ? WHERE id = ?`.

**Tradeoffs**
- Gained: Frontend never constructs save keys, never knows DB column names. Backend controls key format and field allowlist. Field renames don't require frontend changes. SQL injection through field names eliminated.
- Made harder: updateKey is opaque — frontend cannot reason about which section a field belongs to without parsing. But that's intentional.
- Reconsider: If the number of sections grows beyond 10-15, consider using a more structured format (JSON path, dot-notation) instead of colon-delimited. For current needs, colon format is simpler.

---

### 2026-06-13 - Frontend adapter pattern for hierarchical backend DTO to flat view model

**Status**
Active

**Why this is durable**
When the backend returns a hierarchical DTO (language → section → record → fieldVariants) but the frontend UI expects a flat view model (GeneratedVariant[] per language×adaptation), the transformation should live in a dedicated frontend adapter module (`utils/generateReviewAdapter.ts`), not in the component or service layer. This isolates the mapping logic, makes it testable independently of Vue rendering, and allows the backend and frontend view models to evolve independently.

**Decision**
Place all DTO-to-view-model transformation and reverse-mapping (view-model-to-update-payload) in a single adapter module under `frontend/src/utils/`. The adapter exports two functions:
1. `adaptGenerationReviewDto(dto)` — backend → frontend view model
2. `buildReviewUpdatePayload(model)` — frontend view model → backend save payload

Rules:
- The adapter is the ONLY place that knows both the backend DTO shape and the frontend view model shape.
- Components receive only the view model, never raw backend DTO.
- Services send only the save payload, never raw component state.
- The adapter must preserve opaque updateKey values across both directions.

Partially inspired by the adapter pattern from the prototype's mock service (`generateMockService.ts`), but reimplemented for real backend DTOs without mock data.

**Tradeoffs**
- Gained: Backend can change DTO structure without touching Vue components. Components remain focused on rendering, not data transformation. Mapping logic is testable via unit tests on the adapter module.
- Made harder: Two data shape definitions (backend DTO types + view model types) must be maintained. Adapter needs updates when either side changes.
- Reconsider: If the backend and frontend view models converge in the future, the adapter can be simplified or removed. For now, they serve different purposes (backend: normalized, database-oriented; frontend: UI-oriented, flat).

---

### 2026-06-17 - Verify external API parameters against official docs before implementation

**Status**
Active

**Why this is durable**
The OpenRouter `reasoning` parameter (`effort`, `exclude`, `max_tokens`, `enabled`) controls whether AI models return reasoning tokens or final content. Guessing parameter names (e.g., `reasoning_effort` vs `reasoning.effort`, or `suppress_reasoning` vs `exclude`) would produce silent failures — the API would accept unknown parameters without error but not apply them. The exact parameter structure was verified against official OpenRouter documentation at `openrouter.ai/docs/guides/best-practices/reasoning-tokens` before implementation.

**Decision**
Always use Context7 or official API documentation to verify exact parameter names, supported values, and placement (body vs extra_body) before adding provider-specific configuration. For OpenRouter: the `reasoning` object goes at the top level of the request body, not inside `extra_body`. Options: `effort` (OpenAI-style: xhigh/high/medium/low/minimal/none), `max_tokens` (Anthropic-style), `exclude` (boolean), or `enabled` (boolean).

**Tradeoffs**
- Gained: Correct parameter names prevent silent configuration failures. Shape diagnostics confirm reasoning tokens are excluded.
- Made harder: Slightly slower implementation — must check docs instead of guessing.
- Reconsider: If OpenRouter changes their API, docs should be re-checked.

**Evidence**
6 unit tests verify the request body includes `"reasoning"`, `"effort":"none"`, `"exclude":true`. Playwright E2E confirms `messageKeys` changed from `[role, content, refusal, reasoning, reasoning_details]` to `[role, content, refusal]`.

---

### 2026-06-17 - GENERATION_ALREADY_IN_PROGRESS returns HTTP 409 Conflict, not 500

**Status**
Active

**Why this is durable**
When a user starts generation while another request is already processing, the backend must reject the new request. Returning HTTP 500 (Internal Server Error) is semantically wrong — it suggests a server fault when the real issue is a client-side conflict. HTTP 409 Conflict correctly signals "the request could not be completed due to a conflict with the current state of the resource." Additionally, the blocked request must be marked `failed` with a clear error message to prevent stale `pending` rows that block future generations.

**Decision**
1. `ResumeGenerationService.generate()`: When the same request is already processing, throw `AiClientException("GENERATION_ALREADY_IN_PROGRESS")` instead of `IllegalStateException`. When another request is processing, call `requestDao.updateStatus(requestId, userId, "failed", errMsg, false)` before throwing.
2. `GenerateResumeController`: Map `GENERATION_ALREADY_IN_PROGRESS` to `HttpStatus.CONFLICT` (409).
3. Frontend: Disable Generate button during loading (`:disabled="state.isLoading"`, `:loading="state.isLoading"`), add `isLoading` guard in `handleGenerate()`.

**Tradeoffs**
- Gained: Correct HTTP semantics. Blocked requests don't stay pending forever. Frontend prevents double-submit.
- Made harder: Controller must distinguish `GENERATION_ALREADY_IN_PROGRESS` from other AI errors (real provider failures still return 500).
- Reconsider: If the frontend needs to poll for generation completion, 409 could be used as "try again later" signal.

**Evidence**
Backend diagnostics showed `Completed 500 INTERNAL_SERVER_ERROR` for blocked requests. 11 lifecycle tests + 5 controller tests verify 409 behavior and that blocked pending requests are marked failed.

---

### 2026-06-22 — Frontend download/open service must consume backend-provided DTO URLs, not reconstruct URLs from IDs

**Status**
Active

**Why this is durable**
The frontend service `generateResumeService.ts` had methods `downloadPdf(savedResumeId)`, `openPdf(savedResumeId)`, `downloadHtml(savedResumeId)` that constructed URLs from a `RESUME_BASE` constant and the saved resume ID. Meanwhile the backend DTO `SavedResumeExportDto` already carried `pdfDownloadUrl`, `pdfOpenUrl`, `htmlDownloadUrl`, and `publicUrlLink` — fully resolved canonical URLs. The ID-based construction bypassed backend route changes, ignored the `?disposition=inline` parameter, and created fragile coupling where any backend route rename would break the frontend.

**Decision**
Frontend download/open methods MUST accept the URL string directly from the DTO. Change `downloadPdf(savedResumeId: number): Promise<Blob>` to `downloadPdfByUrl(pdfDownloadUrl: string): Promise<Blob>`. Validate URL is non-empty before fetch. For inline PDF, use `window.open(url, '_blank', 'noopener,noreferrer')` instead of blob+createObjectURL round-trip.

**Tradeoffs**
- Gained: frontend no longer owns URL construction; backend can rename routes without frontend changes; public link uses absolute URL from `window.location.origin`
- Made harder: service methods must guard against empty/missing DTO URLs (fallback to error toast)
- Reconsider: when backend adds download auth tokens or signed URLs, the DTO contract already supports it

---

### 2026-06-22 — Budget drift from proven spike capacity causes production PDF fitting failures

**Status**
Active

**Why this is durable**
The V12.1 spike proved EC-016 with exactly 8 total work experience records (3+5 split) and 3 projects. Production seed config silently drifted to allow 10 work items (3+7 split) and 4 projects. This excess content entered the PDF pipeline where the fitting engine and content validation were never tested for that volume, causing `PAGE2:MISSING_TEXTS` and `PAGE2:PAGE_EMPTY` failures at runtime. The automated tests passed because they used mock data, not real dense profiles.

**Decision**
Any seed/config migration that increases content budget beyond spike-proven limits MUST simultaneously update the fitting engine, page planner, renderer, content expectation builder, and validation service to handle the new capacity. Budget parity between spike and production is a release gate — not a feature request.

**Fix applied**
Migration V36 restored V12.1 parity: EC-016 page2_jobs=5 (was 7), page2_max_additional_jobs=5 (was 7), max_projects=3 (was 4). `PagePlanBuilder` added `resolvePage2ProjectCount()` to cap projects to configured budget. `FeedbackFitEngine` and `ContentExpectationBuilder` were updated to match the restored limits.

**Tradeoffs**
- Gained: stable PDF generation for the proven EC-016 scenario
- Made harder: dense profiles with 9+ source work items are capped to 8 rendered
- Reconsider: when fitting engine is upgraded for 3-page layouts, the budgets can be expanded with matching fitting+validation updates
