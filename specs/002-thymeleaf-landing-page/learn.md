# What I Learned: Thymeleaf Landing Page

**Feature**: Professional Landing Page that introduces ResumAIner to first-time visitors — 8 sections, bilingual EN/RU, responsive
**Generated**: 2026-05-31
**Scope**: Full feature
**Implementation status**: 28/28 tasks completed

---

## Key Decisions

### 1. Pure Spring MVC without Spring Boot — Explicit Bean Registration

**What we did**: Every annotated class (`@Controller`, `@ControllerAdvice`) is registered as an explicit `@Bean` in `WebConfig.java`. No `@ComponentScan`.

**Why**: Spring Boot auto-configures a lot. Pure Spring MVC with `@EnableWebMvc` does not. Without component scanning, Spring never finds `@Controller` or `@ControllerAdvice` annotations on the classpath. The `@Bean` method is the only way the class enters the application context.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| `@ComponentScan("com.resumainer")` | Less explicit — any new class with `@Component` would be auto-detected, making the architecture harder to audit. Explicit `@Bean` means every bean is visible in one file. |
| XML bean configuration | Works but mixes Java config with XML — inconsistent with the project's Java-config-only approach. |

**When you'd choose differently**: If the project grows to 30+ beans, component scanning with a well-defined base package becomes more practical than a 200-line `WebConfig.java`. For now, explicit registration is the right call — every bean is findable in one place.

---

### 2. Thymeleaf Instead of JSP

**What we did**: Migrated the entire view layer from JSP to Thymeleaf. Removed `jakarta.servlet.jsp-api`, `jakarta.servlet.jsp.jstl-api` from `pom.xml`. Switched `ViewResolver` from `InternalResourceViewResolver` to `ThymeleafViewResolver`.

**Why**: JSP has limitations that make modern web development harder: no natural expression language integration with Spring, no easy i18n in templates, no template fragments/reusability without custom tags. Thymeleaf's `th:text="#{key}"` syntax directly integrates with Spring's `MessageSource`, making i18n trivial. It also supports natural templating — you can open the HTML in a browser without a server and see the content.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Stay with JSP | Would need JSTL + custom tag libraries for i18n. Thymeleaf was already the project's constitutional choice. |
| FreeMarker | Similar capabilities to Thymeleaf, but Thymeleaf's Spring integration is more mature and the project constitution specified Thymeleaf. |

**When you'd choose differently**: If the team already has deep JSP/JSTL expertise and no i18n requirements, JSP can still work. Thymeleaf is better for i18n-heavy applications.

---

### 3. CookieLocaleResolver + AcceptHeaderLocaleResolver for Language Detection

**What we did**: Used `CookieLocaleResolver` with `setDefaultLocale(Locale.ENGLISH)`. The resolver automatically detects the browser's `Accept-Language` header on first visit and persists the choice in a 1-year `HttpOnly` cookie.

**Why**: Two things needed to work: (1) first-time visitors should see the page in their browser's language without clicking anything, and (2) once they manually switch with `?lang=ru`, the choice should persist across pages. `CookieLocaleResolver` handles both — it reads the browser header initially, then the cookie overrides it after manual switch.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| `SessionLocaleResolver` | Language choice lost when browser closes — bad UX for returning visitors who prefer Russian. |
| URL prefix (`/en/`, `/ru/`) | More complex routing, would require URL rewriting. Overkill for a single landing page. |

**When you'd choose differently**: For a multi-page application with SEO requirements, URL prefix locale (`/en/landing`, `/ru/landing`) is the standard approach because search engines need language-specific URLs.

---

### 4. Self-Hosted Fonts Instead of Google Fonts CDN

**What we did**: Downloaded Inter and Manrope fonts as TTF files (7 files, ~1.6 MB), placed them in `static/fonts/`, and added `@font-face` declarations in CSS. No Google Fonts CDN reference.

**Why**: Every request to Google Fonts leaks the visitor's IP and User-Agent to Google's servers. For EU visitors, this may require GDPR consent for a third-party data transfer. Self-hosting eliminates this entirely — the browser requests everything from our server.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Google Fonts CDN | Fast (CDN), but privacy-hostile. No GDPR consent mechanism on a landing page. |
| System fonts only (`Arial`, `Helvetica`) | Free and private, but the design system specifies Manrope and Inter — they're part of the brand identity. |

**When you'd choose differently**: For a production app that already has a GDPR consent mechanism, Google Fonts CDN is acceptable and faster. Self-host when you don't have consent infrastructure or when privacy is a design constraint.

---

### 5. Bilingual 404/500 Error Pages via Thymeleaf (Not Static HTML)

**What we did**: Error pages are Thymeleaf templates with full header, nav, language switcher, and footer — identical to the landing page design. `GlobalExceptionHandler` catches `NoHandlerFoundException` and `Exception`, forwards to `error/404` or `error/500` views. Static HTML copies in `static/error/` serve as low-level fallbacks.

**Why**: Static error pages can't use i18n. If the landing page has 8 sections with bilingual content, the 404 should too — otherwise it breaks the user experience. But you also need static fallbacks for failures that happen before Thymeleaf initializes (e.g., during application startup).

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Static HTML only | No i18n, no branding, no nav — looks like a different website. |
| Forward to landing page | Confusing UX — user can't tell they hit a 404 (everything looks normal, just different content). |

**When you'd choose differently**: If the app has no i18n, static error pages with proper branding are sufficient and simpler.

---

### 6. `classpath:/templates/` Instead of `/templates/` in SpringResourceTemplateResolver

**What we did**: Set template resolver prefix to `classpath:/templates/` instead of `/templates/`.

**Why**: In pure Spring MVC (no Boot), `SpringResourceTemplateResolver` with prefix `/templates/` resolves files via `ServletContextResource` — it looks for files relative to the webapp root. This works in deployed Tomcat but fails in MockMvc tests, where there's no ServletContext. The `classpath:` prefix uses Spring's resource loader, which resolves from the classpath — works in both environments.

**Alternatives considered**: Using separate resolver beans for production and test contexts. Too complex — the single `classpath:` prefix approach is simpler and works everywhere.

**When you'd choose differently**: If you used Spring Boot, `spring.thymeleaf.prefix=classpath:/templates/` is the default, and this decision is handled for you.

---

### 7. Custom Security Headers Filter Instead of Spring Security

**What we did**: Added a `Filter` bean in `WebConfig` that sets `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, and `Content-Security-Policy` on every response.

**Why**: Spring Security can do this, but adding Spring Security as a dependency for a single landing page with no authentication is overkill. A 30-line `Filter` does exactly what we need.

**Alternatives considered**:
| Approach | Why it wasn't chosen |
|----------|---------------------|
| Spring Security | Heavy dependency (2+ MB) for 4 response headers. Also adds auto-configured features we don't need. |
| Tomcat HTTP header valve | Works but couples security config to the container. We want it in the application code. |

**When you'd choose differently**: As soon as the app needs authentication/authorization, add Spring Security and remove the custom Filter — Spring Security's header writer is more maintainable and feature-complete.

---

## Concepts to Know

### MockMvc Testing for Spring MVC (Non-Boot)

**What it is**: MockMvc is Spring's test framework for controllers — it simulates HTTP requests without starting a real server. In pure Spring MVC, you configure it with `MockMvcBuilders.webAppContextSetup(wac)` where `wac` is the `WebApplicationContext`.

**Where we used it**: `LandingPageControllerTest.java` — tests `GET /`, verifies status 200, checks model attribute `ctaUrl`, and asserts default value `/auth/login`.

**Why it matters**: Without MockMvc, testing controllers requires starting Tomcat, deploying the WAR, and making real HTTP requests. MockMvc runs in milliseconds, is fully deterministic, and catches routing/error issues before deployment.

---

### Bean Registration Without Component Scan

**What it is**: In pure Spring MVC, `@Controller`, `@ControllerAdvice`, and similar annotations do NOT register the class as a bean. Each annotated class needs an explicit `@Bean` method in a `@Configuration` class.

**Where we used it**: `WebConfig.java` — `landingPageController()` and `globalExceptionHandler()` methods.

**Why it matters**: Forgetting to register a `@Controller` gives 404 errors (B1). Forgetting to register `@ControllerAdvice` gives silent failures — the class is never instantiated, and no error is reported. Always check the startup log for `ControllerAdvice beans: none` or `HandlerMapping` with zero mappings.

---

### Locale Resolution Chain in Spring MVC

**What it is**: Spring MVC processes locale in order: (1) `LocaleResolver` bean determines initial locale, (2) `LocaleChangeInterceptor` allows override via request parameter. The `CookieLocaleResolver` persists the result.

**Where we used it**: `WebConfig.java` — `localeResolver()` bean with 1-year `HttpOnly` cookie, `localeChangeInterceptor()` with `?lang=` parameter. Also configured `setIgnoreInvalidLocale(true)` to reject unsupported locales.

**Why it matters**: Without understanding this chain, you might expect `Accept-Language` to always win, or `?lang=` to work on 404 pages (it doesn't — the interceptor runs before the controller, but not before a `NoHandlerFoundException`).

---

### Thymeleaf Natural Templating

**What it is**: Thymeleaf HTML files are valid HTML — they can be opened directly in a browser without a server. The `th:*` attributes are processed server-side, and fall back to their literal values when viewed statically.

**Where we used it**: `landing.html` — every text element has both a `th:text="#{key}"` and a fallback English string. E.g., `<h1 th:text="#{hero.title}">Apply smarter, not harder.</h1>` — the h1 shows "Apply smarter..." when opened directly, and the i18n value when served by the server.

**Why it matters**: This makes frontend debugging easier — designers can open the HTML file without running the app. It also means you always have a default language visible during development.

---

### DispatcherServlet Exception Handling Chain

**What it is**: Spring MVC has multiple levels of exception handling: (1) `HandlerExceptionResolver` beans, (2) `@ControllerAdvice` `@ExceptionHandler` methods, (3) `DefaultHandlerExceptionResolver`. The order matters.

**Where we used it**: `AppInitializer.java` — `setThrowExceptionIfNoHandlerFound(true)` enables step (1) for 404 errors. `GlobalExceptionHandler.java` — `@ExceptionHandler` methods catch the exception at step (2). Both are needed — without the init param, the `DispatcherServlet` never throws for 404s, and the handler never gets called.

**Why it matters**: Debugging error pages is one of the hardest parts of Spring MVC because errors happen during error handling. If you miss any piece of the chain, the error silently falls through to the container's default page.

---

## Architecture Overview

The Landing Page follows a deliberately thin MVC architecture. The controller (`LandingPageController`) is the only Java class — it has one method that adds the externalized CTA URL to the model and returns the view name. The template (`landing.html`) handles all presentation via Thymeleaf, reading text from `messages.properties` or `messages_ru.properties` depending on the active locale.

The configuration layer (`WebConfig.java`) wires everything together: Thymeleaf engine, i18n, locale resolution, security headers, and static resource handling. The error layer (`GlobalExceptionHandler`) catches 404 and 500 errors, forwarding to bilingual Thymeleaf templates.

```
Browser → LandingPageController → landing.html → messages*.properties
                              ↓
                    GlobalExceptionHandler → error/404 or error/500
```

No database, no services, no API endpoints — just a static page with dynamic i18n. This was intentional: the Landing Page is the first thing visitors see, and it must load fast and work reliably regardless of backend state.
