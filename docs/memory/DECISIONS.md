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
