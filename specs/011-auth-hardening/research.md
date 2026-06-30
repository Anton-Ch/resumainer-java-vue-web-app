# Research Notes: Auth Hardening and Spring Security Migration

**Date**: 2026-06-30  
**Status**: Complete (no NEEDS CLARIFICATION remaining)  
**Plan**: `plan.md`

## Key Technical Decisions

### Spring Security Integration in Non-Boot Application

- **Decision**: Use explicit `SecurityFilterChain` registration via `AbstractSecurityWebApplicationInitializer` or manual `FilterRegistrationBean`-equivalent in `AppInitializer.getServletFilters()`.
- **Rationale**: Spring Boot auto-configuration is forbidden. The project uses pure Spring MVC with `AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer`. Spring Security filters must be registered explicitly in the servlet context.
- **Alternatives considered**: Spring Boot starters (rejected — project constraint), web.xml (rejected — project uses Jakarta Servlet API).
- **Bug prevention**: `FilterRegistrationBean` is Spring Boot API and will cause compilation errors. Use `getServletFilters()` instead. (Source: BUGS.md)

### CSRF Migration

- **Decision**: Replace custom `CsrfFilter` with Spring Security CSRF using `CookieCsrfTokenRepository`. Use cookie `XSRF-TOKEN` and header `X-XSRF-TOKEN`.
- **Rationale**: The existing CSRF decision (D-CSRF-001) explicitly anticipates this migration. Spring Security provides production-grade CSRF with the same cookie-to-header pattern.
- **Bug prevention**: All frontend API services must use the shared HTTP client pattern. CSRF header change from `X-CSRF-Token` to `X-XSRF-TOKEN` must be applied to ALL unsafe-request paths, not just auth service. (Source: BUGS.md L554-580)

### Password Encoding

- **Decision**: Use Spring Security `BCryptPasswordEncoder`.
- **Rationale**: Constitution mandates BCrypt for all password storage. Spring Security `BCryptPasswordEncoder` provides a standard implementation.
- **Alternatives considered**: `SCryptPasswordEncoder`, `Pbkdf2PasswordEncoder` (rejected — BCrypt is project standard).

### OAuth2 Provider

- **Decision**: Use Spring Security OAuth2 Client for Google OAuth2 login. OIDC principal for user info extraction.
- **Rationale**: Spring Security provides built-in OAuth2/OIDC support. No custom OAuth2 client implementation needed.
- **Constraint**: Only Google OAuth2. No multi-provider support.

### Email Delivery

- **Decision**: Use Resend API with bilingual (EN/RU) templates. Dev fallback: log email links when API key absent.
- **Rationale**: Resend has a free tier suitable for portfolio deployment.
- **Constraint**: Production must NOT silently no-op on missing API key. (FR-118)

### Captcha

- **Decision**: Cloudflare Turnstile. Dev bypass via `dev-captcha-pass` token.
- **Rationale**: Free tier, no visual challenge required, simple server-side verification.
- **Constraint**: prod must reject dev bypass. Missing secret must cause safe failure. (FR-113, FR-114)

### Remember-Me

- **Decision**: Spring Security persistent remember-me with standard `persistent_logins` table.
- **Rationale**: Spring Security built-in pattern. No custom remember-me framework.

### Auth Token Storage

- **Decision**: Hash all verification/reset tokens before storage. Raw token in email link only. One simple `auth_tokens` table with explicit types.
- **Rationale**: Industry security standard. No custom token framework needed.

### Endpoint Contract

- **Decision**: Keep frontend SPA-facing JSON auth endpoints. No Spring Security default HTML login pages.
- **Rationale**: Existing Vue SPA consumes JSON. Migration must preserve this contract.

## Non-Boot Spring Security Setup

Pure Spring MVC requires:
1. Add `spring-security-web`, `spring-security-config`, `spring-security-core`, `spring-security-crypto`, `spring-security-oauth2-client`, `spring-security-oauth2-jose` to `pom.xml`.
2. Create `SecurityConfig` class with `@Configuration` and `@EnableWebSecurity`.
3. Register `SecurityFilterChain` bean.
4. Wire into servlet context via `AbstractSecurityWebApplicationInitializer` or by adding `DelegatingFilterProxy` registration in `AppInitializer`.
5. Do NOT use `@SpringBootApplication`, `@EnableAutoConfiguration`, or Spring Boot starters.

## Testing Approach

- Backend: JUnit 5 + Mockito + `@WithMockUser` / `SecurityMockMvcRequestPostProcessors` for Spring Security tests.
- Frontend: Vitest + Vue Test Utils for new auth pages/components.
- E2E: Playwright MCP for browser evidence.
- Key lesson: MockMvc standalone creates fresh session per perform(). Use Spring Security test annotations in WebApplicationContext-based tests.

## Auth Event Logging

Decision from brainstorming (2026-06-30):
- Log: failed login attempts (email, IP, timestamp, reason), successful logins, email verification completions, password reset completions.
- Never log: passwords, raw tokens, token hashes, password hashes, API keys, secrets.
- PII exposure limited to email and IP for operational debugging. (FR-141 through FR-146)
