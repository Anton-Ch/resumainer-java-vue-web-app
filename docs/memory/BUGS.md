# Recurring Bug Patterns (`docs/memory/`)

This file stores durable implementation bug patterns and their mitigations. For systemic, high-risk, or governance-level patterns, see `.specify/memory/BUGS.md`.

## Template
### YYYY-MM-DD - Bug / Failure Pattern
**Status**
Active | Monitored | Retired

**Symptoms**
What was observed?

**Root Cause**
What actually caused it?

**Future mistake prevented**
What change pattern should future work avoid?

**Evidence**
Failing test, production incident, review finding, or verified fix.

**Prevention / Detection**
How should future work avoid it and how can we catch it sooner?

**Where to look next**
Files, modules, logs, or checks maintainers should inspect.

---

### 2026-05-30 - @Controller Without Registration Is Invisible to Spring MVC

**Status**
Active

**Symptoms**
DispatcherServlet initializes successfully (no errors), but all HTTP requests return 404 with "No endpoint GET /..." even though the mapping exists in the controller. HandlerMapping shows zero mappings.

**Root Cause**
In Spring MVC (non-Boot) with @Configuration + @EnableWebMvc, the @Controller annotation alone does NOT register the controller as a Spring bean. Without @ComponentScan or an explicit @Bean method in a @Configuration class, the controller class is never instantiated by the Spring container.

**Future mistake prevented**
When adding a new controller without component scan, always register it as an explicit @Bean in WebConfig or add @ComponentScan for the controller package. Never assume @Controller alone is sufficient.

**Evidence**
HelloWorldController with @Controller and @GetMapping("/") returned 404 despite successful DispatcherServlet startup. Adding `@Bean public HelloWorldController helloWorldController()` to WebConfig resolved the issue immediately (200 OK).

**Prevention / Detection**
- Check HandlerMapping logs for registered mappings after startup
- Verify that all @Controller classes have corresponding @Bean methods in the config, or add @ComponentScan
- Tomcat log "No endpoint GET /..." with no prior mapping registration is the telltale symptom

**Where to look next**
backend/src/main/java/com/resumainer/config/WebConfig.java

---

### 2026-05-30 - Spring MVC (non-Boot): Use @Value for Profile, Not Environment.getActiveProfiles()

**Status**
Active

**Symptoms**
Controller displays "Active Profile: default" even though spring.profiles.active=dev is set in application.properties. The @Value("${spring.profiles.active:default}") annotation resolves correctly, but Environment.getActiveProfiles() returns an empty array.

**Root Cause**
In pure Spring MVC (without Spring Boot), Environment.getActiveProfiles() is populated only through programmatic profile activation in a WebApplicationInitializer or ApplicationContextInitializer. The spring.profiles.active property from a .properties file is read by PropertySources and available via @Value, but it does NOT activate profiles in the Environment.

**Future mistake prevented**
Always use @Value("${spring.profiles.active:default}") to read the active profile value in Spring MVC (non-Boot) controllers. Do not rely on environment.getActiveProfiles() unless profiles are activated programmatically.

**Evidence**
HelloWorldController showed "default" despite spring.profiles.active=dev in application.properties. After switching from Environment.getActiveProfiles() to @Value("${spring.profiles.active:default}"), the page correctly displayed "dev".

**Prevention / Detection**
- Verify profile display immediately in the first controller test
- For pure Spring MVC, always use @Value for profile-dependent logic
- Programmatic activation via context.getEnvironment().setActiveProfiles() is also valid but requires changes in AppInitializer

**Where to look next**
backend/src/main/java/com/resumainer/controller/HelloWorldController.java

---

### 2026-05-31 - SpringResourceTemplateResolver: ServletContext prefix fails in MockMvc tests

**Status**
Active

**Symptoms**
MockMvc tests throw `FileNotFoundException: Could not open ServletContext resource [/templates/landing.html]` even though the template file exists at `src/main/resources/templates/landing.html`. In deployed Tomcat, the same prefix (`/templates/`) works fine.

**Root Cause**
`SpringResourceTemplateResolver` with prefix `/templates/` resolves templates via `ServletContextResource`, which looks for files relative to the webapp root. In MockMvc tests, there is no ServletContext with those resources — templates live on the classpath, not in the webapp directory.

**Future mistake prevented**
Using `classpath:/templates/` as prefix instead of `/templates/` ensures templates resolve correctly in both contexts: deployed Tomcat (via Spring's classpath resource loader) and MockMvc tests.

**Evidence**
LandingPageControllerTest failed with TemplateInputException. Changing prefix from `/templates/` to `classpath:/templates/` fixed all 3 tests immediately (BUILD SUCCESS).

**Prevention / Detection**
Always use `classpath:/templates/` (or `classpath:/` prefix) for SpringResourceTemplateResolver in pure Spring MVC projects. The `/WEB-INF/templates/` convention only works in Servlet containers.

**Where to look next**
backend/src/main/java/com/resumainer/config/WebConfig.java

---

### 2026-05-31 - Shell scripts with CRLF line endings fail in Linux Docker containers

**Status**
Active

**Symptoms**
Docker container repeatedly restarts with exit code 127. Logs show:
`/usr/bin/env: 'bash\r': No such file or directory`

**Root Cause**
On Windows, Git checks out or creates shell scripts with CRLF (`\r\n`) line endings. The Linux kernel's shebang (`#!`) parser treats `\r` as part of the interpreter path — `bash\r` is not a valid executable. This affects any `.sh` script copied into a Docker image: entrypoints, health checks, wait-for scripts.

**Future mistake prevented**
All shell scripts destined for Linux Docker containers must have LF (`\n`) line endings. Fix with PowerShell: `(Get-Content script.sh -Raw) -replace "\r\n", "\n"` or with `sed -i 's/\r$//' script.sh` in Git Bash / WSL.

**Evidence**
wait-for-it.sh failed with `bash\r: No such file or directory` in Docker. Fixed by converting to LF line endings. Container started successfully.

**Prevention / Detection**
Add a `.gitattributes` file or a pre-commit hook to normalize shell scripts to LF. Or add a Dockerfile RUN step: `sed -i 's/\r$//' /path/to/*.sh`.

**Where to look next**
docker/scripts/wait-for-it.sh, .specify/scripts/bash/*.sh, any new Docker shell scripts

---

### 2026-05-31 - All Spring stereotype annotations require explicit @Bean in pure Spring MVC

**Status**
Superseded-by-ComponentScan

**Symptoms**
`@ControllerAdvice` with `@ExceptionHandler` methods never gets invoked. Log shows: `ControllerAdvice beans: none` during DispatcherServlet init. 404 errors show Tomcat default page instead of custom Thymeleaf template.

**Root Cause**
In pure Spring MVC with `@Configuration` + `@EnableWebMvc` and no `@ComponentScan`, Spring does NOT scan the classpath for stereotype annotations. This applies to ALL annotation-driven Spring beans: `@Controller` (B1), `@ControllerAdvice`, `@RestController`, `@Service`, `@Repository`, `@Component`.

**Resolution (Feature 004)**
Added `@ComponentScan("com.resumainer")` to `WebConfig.java` combined with `@Repository`, `@Service`, `@Controller`, `@ControllerAdvice` annotations on all components. This eliminated the need for explicit `@Bean` methods for DAOs, services, controllers, and exception handlers.

**What changed**
- Before: 20+ explicit `@Bean` methods in WebConfig for every DAO, service, and controller
- After: `@ComponentScan("com.resumainer")` + stereotype annotations — no explicit beans needed
- All 97 tests pass with the new approach

**Why explicit @Bean was the wrong fix**
The original workaround (adding `@Bean` methods) treated the symptom, not the cause. The actual missing piece was `@ComponentScan`. Using explicit `@Bean` methods is still valid for infrastructure beans (Flyway, interceptors, filters) but should NOT be the default pattern for annotated components.

**Prevention / Detection**
When adding a new annotated class:
1. First approach: add `@Repository`/`@Service`/`@Controller`/`@ControllerAdvice` — `@ComponentScan` will discover it
2. Only if component scan doesn't apply (e.g., third-party classes): use explicit `@Bean`
3. Check startup log for expected bean registration

**Where to look next**
`@ComponentScan("com.resumainer")` in `WebConfig.java`. DAO classes in `com.resumainer.dao` with `@Repository`. Service classes in `com.resumainer.service` with `@Service`.

---

### 2026-06-02 - FilterRegistrationBean is Spring Boot API — use getServletFilters() in pure Spring MVC

**Status**
Active

**Symptoms**
Adding `org.springframework.boot.web.servlet.FilterRegistrationBean` to register a custom filter (CsrfFilter, etc.) causes compilation errors because the class does not exist in pure Spring MVC. The import fails and Maven build breaks.

**Root Cause**
`FilterRegistrationBean` is a Spring Boot class. In pure Spring MVC (no Spring Boot), filters must be registered by overriding `AbstractAnnotationConfigDispatcherServletInitializer.getServletFilters()` which returns a `Filter[]` array.

**Future mistake prevented**
When adding any custom Filter (CsrfFilter, RateLimitFilter, etc.), always override `AppInitializer.getServletFilters()` — NOT `FilterRegistrationBean`.

**Evidence**
CsrfFilter for Feature 003 was originally planned with `FilterRegistrationBean` (Spring Boot API). The user rejected it per constitution "No Spring Boot". Fixed by overriding `getServletFilters()` in AppInitializer.

**Prevention / Detection**
- Never import anything from `org.springframework.boot` package
- For filter registration: `AppInitializer.getServletFilters()` → `return new Filter[] { new CsrfFilter() }`

**Where to look next**
backend/src/main/java/com/resumainer/initializer/AppInitializer.java

---

### 2026-06-03 - Mockito-extensions config file with wrong content breaks all mock creation

**Status**
Active

**Symptoms**
All Mockito `mock()` calls fail with: &quot;Failed to load interface org.mockito.plugins.MockMaker implementation declared in java.lang.CompoundEnumeration&quot;. Tests error out immediately at mock creation, showing 0 failures but N errors.

**Root Cause**
Creating a file at `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` with content &quot;mock-maker-default&quot; (or any content that references a non-existent MockMaker). In Mockito 5.x, the inline mock maker is the only bundled implementation — &quot;mock-maker-default&quot; does NOT exist. Even after deleting the source file, the compiled copy remains in `target/test-classes/mockito-extensions/` and continues to break tests.

**Future mistake prevented**
Do NOT create `mockito-extensions/org.mockito.plugins.MockMaker` files manually. Mockito 5.x uses inline mock maker by default. If you need to configure it, use the official `-javaagent` approach in surefire argLine instead.

If the config file was already created:
1. Delete `src/test/resources/mockito-extensions/` (source)
2. Delete `target/test-classes/mockito-extensions/` (stale compile output)
3. Run `mvn clean test` to rebuild

**Evidence**
Phase 2 DAO tests failed after creating mockito-extensions file thinking we needed &quot;mock-maker-default&quot;. Even after removing the source file, `target/test-classes` still had the cached copy. Complete cleanup of both locations fixed all 16 lookup DAO tests.

**Prevention / Detection**
- After any change to test resources, run `mvn clean` to ensure no stale classpath files remain
- If Mockito mocks fail with &quot;MockMaker implementation declared in CompoundEnumeration&quot;, check `target/test-classes/mockito-extensions/` for leftover config files

**Where to look next**
`src/test/resources/mockito-extensions/` (should not exist), `target/test-classes/mockito-extensions/` (stale artifact)

---

### 2026-06-03 - MockMvc jsonPath() assertions require explicit jayway-jsonpath dependency

**Status**
Active

**Symptoms**
MockMvc standalone test fails with: `java.lang.NoClassDefFoundError: com/jayway/jsonpath/TypeRef` when using `MockMvcResultMatchers.jsonPath()`. The error occurs at test method invocation, not at setup.

**Root Cause**
Spring MVC's `MockMvcResultMatchers.jsonPath()` internally delegates to `com.jayway.jsonpath` library, but it is an optional/compile-only dependency — not transitively included by spring-webmvc or spring-test. Without an explicit test-scoped dependency, the class is not on the test classpath.

**Future mistake prevented**
Always add `com.jayway.jsonpath:json-path` as a test-scope dependency before writing MockMvc tests with `jsonPath()` assertions. Without it, every `jsonPath()` call throws NoClassDefFoundError.

**Evidence**
AuthControllerTest.register_validInput_returns200() failed with NoClassDefFoundError for com/jayway/jsonpath/TypeRef. Adding json-path 2.9.0 as test dependency fixed both controller tests.

**Prevention / Detection**
- Add json-path to pom.xml before writing MockMvc jsonPath assertions
- Version: 2.9.0+ (compatible with Spring MVC 6.2.x)
- Scope: test

**Where to look next**
backend/pom.xml (test dependencies), any new controller test class using jsonPath()

---

### 2026-06-03 - Long auto-unboxing NullPointerException when comparing with primitive

**Status**
Active

**Symptoms**
Comparison like `user.getRoleId() == 2L` throws `NullPointerException: Cannot invoke "java.lang.Long.longValue()" because the return value of "..." is null`. The getter returns null (object wasn't fully populated), and Java's auto-unboxing converts `Long` → `long` before comparing — which calls `.longValue()` on null.

**Root Cause**
In Java, `Long` (object) vs `long` (primitive) uses auto-unboxing. The expression `user.getRoleId() == 2L` is compiled as:
1. `user.getRoleId()` → returns `Long` (may be null)
2. `Long.longValue()` → called on the `Long` object (NPE if null)
3. `==` → compares primitives

This is silent at compile time and only fails at runtime when the value happens to be null. Common scenarios: a partially populated Model object from tests, or a ResultSet column that was NULL.

**Future mistake prevented**
Always check for null before comparing a `Long` (or any wrapper) with a primitive. Use one of:
- `LongUtils.equals(user.getRoleId(), 2L)` (Apache Commons)
- `Long.valueOf(2L).equals(user.getRoleId())`
- `user.getRoleId() != null && user.getRoleId() == 2L`
- `Long.valueOf(2L).equals(user.getRoleId())`
Or ensure the field is always populated before comparison.

In general: be paranoid about auto-unboxing when the source of the value could be null (database nullable column, partial object construction, mocked getters).

**Evidence**
AuthControllerTest.login_validInput_returns200() failed with NPE: `Cannot invoke "java.lang.Long.longValue()" because the return value of "com.resumainer.model.User.getRoleId()" is null`. The test created a User object without setting roleId, and the controller called `user.getRoleId() == 2L`. Fix: set roleId on the test User before passing it to the controller.

**Prevention / Detection**
- In tests: always fully populate Model objects that the controller/service will access
- In production code: use `Long.valueOf(x).equals(y)` pattern for comparison
- In DAO `mapRow()`: ensure all `getLong()` columns have a default or are non-null in the schema

**Where to look next**
Any Java class that compares a Long getter result with a primitive literal. Common in Controller and Service layers.

---

### 2026-06-03 - MockMvc standalone: each perform() creates a fresh session — use MockHttpSession for filter tests

**Status**
Active

**Symptoms**
A filter sets a session attribute (e.g., CSRF token) during request processing. A subsequent test request that depends on that session attribute fails because the attribute is missing. For example: first perform() sets CSRF token in session and cookie. Second perform() reads the cookie and sends it as header, but the filter rejects it because the session doesn't have the matching token.

**Root Cause**
In standalone MockMvc setup (MockMvcBuilders.standaloneSetup), each perform() call creates a NEW MockHttpSession. Session state does NOT persist between requests. This is different from WebApplicationContext-based MockMvc testing where session state persists across requests within the same test method.

**Future mistake prevented**
When testing filters that read/write session attributes (CsrfFilter, AuthInterceptor, etc.) with standalone MockMvc, pre-configure the session with required attributes using MockHttpSession:

```java
MockHttpSession session = new MockHttpSession();
session.setAttribute("CSRF_TOKEN", "known-token-value");

mockMvc.perform(post("/api/test")
        .session(session)
        .header("X-CSRF-Token", "known-token-value"))
    .andExpect(status().isOk());
```

Do NOT try to chain requests expecting session state to carry over between perform() calls.

**Evidence**
CsrfFilterTest.postWithValidCsrfToken_returns200 initially tried: perform GET → get cookie from response → perform POST with cookie value. The POST failed because the second perform() had a fresh session without the CSRF token. Fixed by using shared MockHttpSession with pre-set session attribute.

**Prevention / Detection**
- When testing filters with standalone MockMvc, always use `MockHttpSession` with pre-set attributes
- If you need session state across requests, use WebApplicationContext-based MockMvc (SpringBootTest or SpringJUnitWebConfig)
- For CsrfFilter specifically: pre-set "CSRF_TOKEN" session attribute + matching X-CSRF-Token header

**Where to look next**
Any test using standalone MockMvc + addFilters() that needs session state. CsrfFilterTest.java.

---

### 2026-06-03 - Flyway @Bean(initMethod="migrate") required in pure Spring MVC — no auto-migration

**Status**
Active

**Symptoms**
Application starts successfully, API endpoints return HTTP 500 with error: `ERROR: relation "users" does not exist` (or any other table). The SQL migration files exist in `db/migration/` but Flyway never created the tables.

**Root Cause**
In pure Spring MVC (without Spring Boot), Flyway is NOT auto-configured. The Flyway dependency and SQL migration files are in place, but nothing triggers `Flyway.migrate()`. Spring Boot has auto-configuration that runs Flyway automatically — pure Spring MVC does not.

**Future mistake prevented**
Every feature that adds or modifies database tables via Flyway must include a `@Bean(initMethod = "migrate")` for Flyway in the configuration:

```java
@Bean(initMethod = "migrate")
public Flyway flyway(DataSource dataSource) {
    return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load();
}
```

The `initMethod = "migrate"` ensures migrations run at bean creation time, before any DAO or Service beans that depend on the database schema.

**Evidence**
Docker Compose deployment: API returned 500 with "relation "users" does not exist" despite correct Flyway SQL files in `db/migration/` and correct DB connection. Adding the Flyway bean resolved the issue — registration and login both started working immediately after rebuild.

**Prevention / Detection**
- After deploying to Docker, always test an API call that reads/writes the database (e.g., POST /api/auth/register)
- Check the Tomcat startup logs for Flyway migration output
- If tables are missing, check for Flyway bean configuration in WebConfig or equivalent

**Where to look next**
backend/src/main/java/com/resumainer/config/WebConfig.java, any future @Configuration class

---

### 2026-06-03 - DataSource URL with ${...} placeholders stays as literal string — use System.getenv()

**Status**
Active

**Symptoms**
Database connection fails with: `Unable to parse URL jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:resumainer}`. The URL contains the literal `${DB_HOST:localhost}` string instead of the resolved value.

**Root Cause**
When creating a `DataSource` bean manually (e.g., `DriverManagerDataSource`) and the URL string contains `${...}` placeholders, Spring does NOT resolve them unless:
1. A `PropertySourcesPlaceholderConfigurer` bean is registered
2. AND `@Value` annotations are used

Without `@Value`, the string literal including `${...}` is passed directly to the JDBC driver, which can't parse it.

**Future mistake prevented**
When creating a DataSource programmatically in pure Spring MVC, use `System.getenv()` with defaults instead of `${...}` placeholders:

```java
String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
ds.setUrl("jdbc:postgresql://" + host + ":" + port + "/resumainer");
```

Alternatively, use `@Value("${db.url}")` with a `PropertySourcesPlaceholderConfigurer` bean.

**Evidence**
Docker deployment failed with PSQLException: `Unable to parse URL jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:resumainer}` because the URL was hardcoded with `${...}` placeholder syntax. Fixed by switching to `System.getenv()` with fallback defaults.

**Prevention / Detection**
- Never use `${...}` syntax in hardcoded Java string literals
- For env-var-based config, use `System.getenv(key, default)` pattern
- For properties-file-based config, use `@Value` + `PropertySourcesPlaceholderConfigurer`

**Where to look next**
backend/src/main/java/com/resumainer/config/WebConfig.java (dataSource() method)

---

### 2026-06-03 - PrimeVue 4 Zod resolver validation messages not reactive to locale changes

**Status**
Active

**Symptoms**
After switching the UI language (EN → RU), form validation messages remain in English. For example, after switching to Russian, empty email still shows "Email is required" instead of "Email обязателен". Labels and static text switch correctly, but Zod validation messages don't.

**Root Cause**
The `zodResolver` object is created ONCE when the component is set up. The `t()` function from `vue-i18n` is called at creation time with the initial locale's values. When the locale changes, the resolver still holds the old `t()` values because Zod schemas are immutable after creation.

PrimeVue 4's `<Form>` component accepts `:resolver` as a prop, but if a constant resolver is passed, it never updates. The resolver must be a reactive `ref` that is reassigned when the locale changes.

**Future mistake prevented**
Always make the Zod resolver reactive when using vue-i18n:

```typescript
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const resolver = ref(createResolver(t))

watch(locale, () => {
  resolver.value = createResolver(t)   // Re-create with new locale
})

function createResolver(t: (key: string) => string) {
  return zodResolver(z.object({
    email: z.string().min(1, { message: t('auth.error.emailRequired') })
  }))
}
```

The template must pass the ref: `<Form :resolver ...>` — Vue will pass `resolver.value` reactively.

**Evidence**
Manual testing after Phase 8: switching from EN to RU showed all labels and static text in Russian, but validation errors ("Email is required", "Password is required") stayed in English. Fixed by wrapping the resolver in a reactive `ref` and adding `watch(locale, ...)` to recreate it. After rebuild, validation messages correctly switched with locale.

**Prevention / Detection**
- Always test validation messages in ALL supported languages after any form-related changes
- If labels switch but validation messages don't — the resolver is not reactive
- Check that resolver is a `ref` (not a constant) and has a `watch(locale)` handler

**Where to look next**
frontend/src/components/LoginForm.vue, frontend/src/components/RegisterForm.vue, any future PrimeVue Form component

---

### 2026-06-03 - PR descriptions with backtick-escaped paths get mangled in gh CLI + PowerShell

**Status**
Active

**Symptoms**
GitHub PR description shows `\backend/\` and `\frontend/\` with literal backslash-plus-backtick instead of proper inline code formatting like `backend/`. The markdown is not rendering correctly.

**Root Cause**
When running `gh pr create --body "..."` from PowerShell, the body string contains markdown backticks for inline code (e.g., `` `backend/` ``). There are two compounding problems:

1. **PowerShell escape confusion**: The string `\`` in PowerShell is interpreted as literal backslash followed by the start of a PowerShell escape sequence (backtick is PowerShell's escape character). The markdown `\` before backtick is NOT standard GitHub markdown syntax.
2. **Incorrect markdown**: To escape a backtick inside a code span in GitHub markdown, use double backticks (`` ``foo`` ``), not backslash + backtick (`\`foo\``).

The `\` + `` ` `` combination is rendered as-is in GitHub markdown, producing the ugly `\` characters.

**Future mistake prevented**
When writing PR descriptions via `gh pr create` from PowerShell:

1. **Do not use backticks for inline code** in `gh` body strings unless absolutely necessary. Simple paths like `backend/` and `frontend/` are clear without formatting.
2. **If you need backticks**, use a heredoc or file-based approach instead of inline string:
   ```powershell
   # Read body from file
   gh pr create --title "..." --body-file pr-body.md
   ```
3. **Alternative**: Write backtick-free descriptions:
   ```
   - backend/ — 24 Java files
   - frontend/ — 16 Vue/TS files
   ```

**Evidence**
PR #5 showed `\backend/\`, `\frontend/\`, `\docker/\` instead of proper formatting. Fixed manually by removing backticks from path references.

**Prevention / Detection**
- Review PR body before submitting by echoing the string first
- Use `--body-file` with a markdown file for complex PR descriptions
- For simple path lists, omit backticks entirely — paths are readable without formatting

**Where to look next**
Any future `gh pr create` command with inline markdown containing backticks.

---

### 2026-06-06 — FK column type must match referenced entity PK — UUID for users(id), not BIGINT

**Status**: Active

**Symptoms**: Flyway migration fails at runtime with error:
```
ERROR: foreign key constraint "saved_resumes_user_id_fkey" cannot be implemented
DETAIL: Key columns "user_id" and "id" are of incompatible types: bigint and uuid.
```

**Root Cause**: The project uses hybrid PK strategy (D7): entity tables like `users` use `UUID` PK, while lookup tables use `BIGSERIAL`. When creating a new entity table that references `users(id)`, the FK column was incorrectly defined as `BIGINT` (matching lookup table convention) instead of `UUID`.

**Prevention**: Before writing any Flyway migration with a FK referencing `users.id` (or any other entity table), verify the FK column type matches `UUID`, not `BIGINT`. The `BIGINT` type is correct only for lookup/reference tables (e.g., `role_id`, `status_id`).

**Evidence**: Feature 005 Phase 2 implementation — V8 migration initially had `user_id BIGINT NOT NULL REFERENCES users(id)`, which would fail at runtime. Caught during code review.

---

### 2026-06-08 - vue-i18n @ character in message values causes runtime SyntaxError

**Status**
Active

**Symptoms**
A Vue component does not render (shows <!----> in slot). Browser console shows: SyntaxError: 10 with a parser/nextToken/parse stack trace originating from the vue-i18n message compiler (inside the JS bundle). The error occurs at runtime, not at build time. npm run build passes with 0 errors.

The component renders correctly when certain translation keys are NOT used in the template, and breaks when they ARE used.

**Root Cause**
vue-i18n interprets the @ character as a linked message reference syntax (@:message.key). When translation values contain literal @ (e.g., johndoe@example.com, @johndoe, @example), the message compiler fails with a SyntaxError because it tries to parse text after @ as a message key.

The error message '10' is the character position in the message where the parser fails (at the @ sign).

**Future mistake prevented**
When adding translation values to en.json or ru.json, any value containing literal @ (email addresses, Telegram handles, Twitter/X handles) must use vue-i18n literal interpolation syntax {'@'} instead of raw @.

Correct: johndoe{'@'}example.com
Wrong: johndoe@example.com

**Evidence**
ContactDetailsSection.vue in Feature 006 rendered as blank <!----> because profile.contact.emailPlaceholder and profile.contact.telegramPlaceholder contained @. All other profile sections rendered correctly because they did not reference those translation keys. Fixing all 4 occurrences of @ in en.json and ru.json restored rendering.

**Prevention / Detection**
1. Before adding any translation value, check for @ in the string
2. Search i18n JSON files for raw @: grep -rn '@' frontend/src/i18n/*.json
3. Use {'@'} instead of @ in all vue-i18n translation values
4. After adding new translations, test the component in both EN and RU modes

**Where to look next**
frontend/src/i18n/en.json, frontend/src/i18n/ru.json

---

### 2026-06-08 - New frontend API services must include CSRF token handling for unsafe methods

**Status**
Active

**Symptoms**
POST, PUT, PATCH, or DELETE requests to any /api/* endpoint return 403 with: {"error":"Invalid or missing CSRF token"}. The XSRF-TOKEN cookie is present in document.cookie, but the X-CSRF-Token header is missing from the request.

**Root Cause**
The backend CsrfFilter implements OWASP cookie-to-header pattern: it sets a non-HTTP-only cookie XSRF-TOKEN and validates the X-CSRF-Token header for unsafe methods (POST, PUT, PATCH, DELETE). The authService.ts already handles this correctly with getCsrfToken() + buildOptions(), but newly created API services (like profileService.ts) were built without CSRF handling.

**Future mistake prevented**
Every new frontend API service that sends state-changing requests must include CSRF token handling. The recommended approach is to use a shared HTTP client (httpClient.ts) that:
1. Reads XSRF-TOKEN from document.cookie via regex
2. Adds X-CSRF-Token header for unsafe methods only (not GET/HEAD/OPTIONS)
3. Uses new Headers() to safely merge with existing headers
4. Sets Content-Type: application/json for requests with body
5. Handles 204 No Content safely

**Evidence**
profileService.ts was created with a local request() function that only handled credentials: 'include' without CSRF token. All 6 profile section save/update/delete operations returned 403. After refactoring to use shared httpClient.ts with CSRF handling, all operations returned 200.

**Prevention / Detection**
1. All new API service files must use the shared httpClient.ts instead of raw fetch()
2. The shared client automatically adds X-CSRF-Token for POST/PUT/PATCH/DELETE
3. Test state-changing operations during development — if they return 403, CSRF token is missing
4. Code review: check that unsafe HTTP methods include X-CSRF-Token header

**Where to look next**
frontend/src/services/httpClient.ts, frontend/src/services/a*.ts (all API services)

---

### 2026-06-08 - Java Set.contains() with toLowerCase() — Set values must be lowercase too

**Status**
Active

**Symptoms**
A validateSortField() method converts the input to lowercase (field.trim().toLowerCase()) and then checks Set.contains(f). Even though the input value is in the ALLOWED list, the check fails and throws IllegalArgumentException. Error message shows the allowed values including the expected value, but contains() still returns false.

Example error:
Invalid sort field: courseName. Allowed: [courseName, provider, ...]

The value courseName IS in the allowed list but validation fails.

**Root Cause**
Java's Set.contains() is case-sensitive. If the method does .toLowerCase() on the input before checking, the ALLOWED_SORT_FIELDS Set must also contain lowercase strings. If the Set contains "courseName" (camelCase) but the check is against "coursename" (lowercased), contains() returns false.

Similarly, SORT_FIELDS_NEEDING_MAP must use lowercase keys when the input has been lowercased before the map check.

**Future mistake prevented**
When implementing field validation with the pattern:
1. Convert input to lowercase
2. Check Set.contains()

Make sure ALL entries in the Set are also lowercase. Otherwise validation fails for valid inputs.

Wrong:
ALLOWED = Set.of("courseName", "startDate")
input = "courseName".toLowerCase() // "coursename"
ALLOWED.contains(input) // false!

Correct:
ALLOWED = Set.of("coursename", "startdate")

**Evidence**
CourseCertificateDao.validateSortField() in Feature 006 had ALLOWED_SORT_FIELDS with camelCase values ("courseName", "startDate") but the method did field.toLowerCase() before contains(). Sorting by any column failed with 500 Internal Server Error. Fixing the Set values to all lowercase resolved the issue.

**Prevention / Detection**
1. When using toLowerCase() before Set.contains(), ALWAYS use lowercase values in the Set
2. Code review: check that Set values match the case of the comparison
3. Test sorting by every column during development
4. Check that IllegalArgumentException mentions the field is in the allowed list but still fails

**Where to look next**
Backend DAO classes with validateSortField() methods: CourseCertificateDao, ResumeDao

---

### 2026-06-08 - PrimeVue Form onSubmit try without catch silently swallows API errors

**Status**
Active

**Symptoms**
After submitting a form (login/register), the API returns a non-2xx response (409, 401, etc.) with a descriptive error message. The request fails on the network level but the user sees NO error message on the page. The form just resets or stays unchanged with no feedback.

The browser console shows the error response body being thrown as an unhandled exception: {success: false, message: "..."}

**Root Cause**
The PrimeVue Form onSubmit handler uses try { ... } finally { ... } or try { ... } without a catch block. When the async API call throws (via throw data as T in handleResponse), the catch block is missing, so the error propagates up to Vue's global error handler and never reaches the user-visible UI.

The authService.ts handleResponse() throws the parsed error object on non-ok responses, which is correct behavior. But the consuming component (LoginForm.vue or RegisterForm.vue) must catch it and display the message.

**Future mistake prevented**
Every Vue form component with async API calls in onSubmit MUST have:
1. A catch block that extracts the error message
2. A generalError ref to store the message
3. A Message component in the template to display it
4. Clear the error on each new submission

Pattern:
try {
  submitting.value = true
  generalError.value = ''
  const response = await apiCall(...)
  if (response.success) emit('success')
} catch (err) {
  const data = err as { message?: string }
  generalError.value = data.message || t('error.serverError')
} finally {
  submitting.value = false
}

**Evidence**
Both LoginForm.vue and RegisterForm.vue had try {} finally {} without catch. Registration with existing email returned 409 with message "Email already registered" but the user saw nothing. Login with wrong password also showed no error. Adding catch blocks with generalError display fixed both.

**Prevention / Detection**
1. Code review: every async form onSubmit must have try/catch/finally or try/catch
2. Test: submit forms with intentionally invalid data and verify error messages appear
3. Search for try { without catch { in form components
4. Test failed API responses show user-friendly messages

**Where to look next**
frontend/src/components/LoginForm.vue, frontend/src/components/RegisterForm.vue

---

### 2026-06-12 - Ambiguous Date reference from java.sql.* + java.util.* wildcard imports

**Status**
Active

**Symptoms**
Compilation error: "reference to Date is ambiguous — both class java.util.Date in java.util and class java.sql.Date in java.sql match". Occurs when a Java file imports both java.sql.* and java.util.* and uses Date (for example calling Date.valueOf()).

**Root Cause**
Both java.sql and java.util packages contain a class named Date. When both are imported via wildcard (import java.sql.*; import java.util.*;), the compiler cannot resolve which Date is intended for calls like Date.valueOf().

**Future mistake prevented**
Avoid using both import java.sql.* and import java.util.* in the same file. Replace wildcard imports with explicit imports. For java.sql.Date, either use fully qualified names (java.sql.Date.valueOf()) or add an explicit import java.sql.Date.

**Evidence**
GenerationResponseDao.java compiled with 3 errors on Date.valueOf() calls when using import java.sql.* and import java.util.*. Fixing to explicit imports resolved the issue.

**Prevention / Detection**
- Replace java.sql.* and java.util.* wildcard imports with explicit imports
- When both java.sql and java.util types are needed, use fully qualified names for the less common Date variant

**Where to look next**
All DAO files that use both SQL and utility types.

---

### 2026-06-12 - OpenRouter JSON response parsing must use Jackson ObjectMapper, not manual string operations

**Status**
Active

**Symptoms**
AI generation returns "Generation failed" despite a valid 200 response from OpenRouter. Backend log shows: "Failed to parse AI response: Unexpected character ('\' (code 92)): was expecting double-quote to start field name". The response contains escaped characters, nested quotes, or multi-line content that breaks manual parsing.

**Root Cause**
OpenRouterClient.extractContent() used indexOf/substring to extract choices[0].message.content from the JSON response. This approach fails when the AI response content contains escaped quotes, backslashes, or line breaks, because indexOf finds the wrong quote position. Additionally, the method did not detect provider-error JSON objects returned within 200 responses.

**Future mistake prevented**
When parsing external API JSON responses (especially AI providers where content contains escape characters), always use a proper JSON parser (Jackson ObjectMapper.readTree / JsonNode.path) instead of indexOf/substring. Manual JSON parsing of AI responses will inevitably break on escaped characters, nested JSON, or multi-line content.

**Evidence**
OpenRouterClient.extractContent() failed on a real OpenRouter response containing a backslash character. Replacing manual indexOf/substring parsing with Jackson ObjectMapper.readTree + JsonNode.path() resolved the issue. 8 unit tests verify the fix across normal responses, escaped quotes, missing choices, empty choices, provider errors, and invalid JSON.

**Prevention / Detection**
- Always use Jackson for JSON response parsing in API clients.
- Never use indexOf, substring, regex, or manual string parsing for JSON.
- Test with responses containing escaped quotes, backslashes, line breaks, and provider error objects.
- Check for error objects even in 200 responses.

**Where to look next**
backend/src/main/java/com/resumainer/service/ai/OpenRouterClient.java

---

### 2026-06-13 - Standalone MockMvc without setControllerAdvice causes ServiceException to become 500

**Status**
Active

**Symptoms**
A standalone MockMvc controller test (setup via `MockMvcBuilders.standaloneSetup(controller)`) sends a request without authentication. The controller's `getUserId()` method throws `ServiceException("auth.unauthorized", "Not authenticated")`. The test expects HTTP 401 (UNAUTHORIZED) but receives HTTP 500 (INTERNAL_SERVER_ERROR).

**Root cause**
Standalone MockMvc does not auto-configure `@ControllerAdvice` beans. The `ServiceException` thrown by the controller falls through to the generic `Exception` handler in `GlobalExceptionHandler` — but since the handler is not registered, the exception propagates as an unhandled error (500).

The `AuthInterceptor` normally prevents unauthenticated requests from reaching the controller, but standalone MockMvc does not include interceptors either.

**Fix**
Add `GlobalExceptionHandler` as controller advice in the MockMvc builder:

```java
mockMvc = MockMvcBuilders.standaloneSetup(controller)
    .setControllerAdvice(new GlobalExceptionHandler())
    .build();
```

Also ensure `GlobalExceptionHandler` has an explicit handler for `ServiceException` (not just the generic `Exception` catch-all):

```java
@ExceptionHandler(ServiceException.class)
public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex, HttpServletRequest request) {
    boolean isAuth = "auth.unauthorized".equals(ex.getErrorCode());
    HttpStatus status = isAuth ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(Map.of(
        "errorCode", ex.getErrorCode(),
        "message", ex.getMessage()
    ));
}
```

**Prevention**
Any standalone MockMvc test for a controller that can throw `ServiceException` MUST:
1. Add `.setControllerAdvice(new GlobalExceptionHandler())` to the builder.
2. Test the expected HTTP status code for unauthenticated access, not a redirect.

Related to B10 (MockMvc standalone session handling) but distinct — this is about exception handling, not session persistence.

---

### 2026-06-17 - DeepSeek V4 Flash returns reasoning-only responses with null content intermittently

**Status**
Active

**Symptoms**
OpenRouter HTTP 200 response has `choices[0].message.content: null` while `choices[0].message.reasoning` / `reasoning_details` exists. Backend log: "OpenRouter response missing choices[0].message.content". Generation fails with "Unexpected AI response format." The OPENROUTER_RESPONSE_SHAPE diagnostic shows `hasContent=false, messageKeys=[role, content, refusal, reasoning, reasoning_details]`.

**Root Cause**
DeepSeek V4 Flash (`@preset/deepseekflashonly`) by default may produce reasoning tokens without final content. The model "thinks" but doesn't produce the generated text.

**Fix**
1. Add `"reasoning": {"effort": "none", "exclude": true}` to OpenRouter request body. Verified against official OpenRouter docs.
2. Retry loop (max 3 attempts) when content is missing but reasoning is present (`MISSING_CONTENT_WITH_REASONING`). If any attempt returns valid content, use it. If all 3 fail, throw `AiClientException("MISSING_CONTENT_WITH_REASONING")`.
3. Never parse reasoning as content. `extractContentSafely()` only reads `choices[0].message.content`.

**Prevention**
- Always log response shape for AI provider diagnostics.
- Never trust AI model output to always include content — add graceful retry.
- Verify provider-specific API parameters against official docs before implementing.
- Do not use model reasoning/diagnostics as business content.

---

### 2026-06-17 - Cover letter column exists in migration but INSERT SQL omits it

**Status**
Active

**Symptoms**
Cover letter is visible on Review page, `generate.cover_letter` is included in the prompt, `ResumeGenerationResponse.getCoverLetter()` returns the generated text, but the Export page never shows the cover letter block. DB query shows `cover_letter IS NULL` for all saved_resumes rows.

**Root Cause**
The `saved_resumes.cover_letter` column was created in V8 migration and read correctly in SELECT queries (`SavedResumeDao.findByGenerationRequestId`, `findById`), but the INSERT statement in `SavedResumeDao.insert()` did not include the `cover_letter` column. `ResumeFinalizeService` also did not pass `response.getCoverLetter()` to the insert method or set it in the export DTO.

**Fix**
1. Added `cover_letter` to INSERT SQL: `+ "public_code, public_url_link, html_file_path, pdf_file_path, cover_letter, "`.
2. Added `String coverLetter` parameter to both `insert()` method overloads.
3. Updated `ResumeFinalizeService` to pass `response.getCoverLetter()` to insert and `item.setCoverLetter(coverLetter)` in export DTO.
4. Updated `ResumeFinalizeServiceTest` mock signatures to include new parameter.

**Prevention**
When adding a database column via migration, always verify all INSERT, UPDATE, and SELECT paths. Add a DAO integration test that inserts a full row and reads back every column.

---

### 2026-06-21 - Docker BuildKit: COPY . . cache survives --no-cache — must docker rmi --force

**Status**
Active

**Why this is durable**
Docker BuildKit caches the `COPY . .` layer based on file checksums. The `--no-cache` flag only affects `RUN` layers, not `COPY` layers. When source files change but the built output does not reflect the changes, the root cause is almost always a stale `COPY` cache layer. This is a cross-project pattern affecting any Docker multi-stage build with `COPY . .` followed by a build step.

**Symptoms**
- `npm run build` / `mvn package` succeeds locally with correct output
- `docker-compose build --no-cache` succeeds but container still has old code
- Content-hash-based output filenames (e.g., `index-XYZ.js`) are identical between old and "new" builds, confirming no actual rebuild occurred

**Mitigation**
1. First line of defense: `docker rmi <image> --force` before `docker-compose build`
2. Alternative: use BuildKit `--no-cache-filter` to target specific stages: `docker build --no-cache-filter=build ...`
3. Verification always: `docker exec <container> sh -c "grep -c '<unique-string>' /path/to/built/assets/*.js"` to confirm expected code is in the container

**Evidence**
Feature 008 Phase 4 Frontend: `ReviewStepForm.vue` had bullet editing template code, local `npm run build` included it, but `docker-compose build --no-cache frontend` produced a container without it. `docker rmi docker-frontend --force` followed by rebuild resolved the issue. Built filenames changed from stale to fresh only after image deletion.

---

### 2026-06-21 - Dropped spike method causes universal validation failure in ported algorithm

**Status**
Active

**Why this is durable**
When porting algorithmic code from a proven spike to production, the class-by-class audit must verify that every method is present AND correctly called. Omitting a single method (buildForPlannedPage) caused the PDF fit engine to fail every single validation attempt because the isolated page validator expected content from all pages, not just the page being fitted. This pattern applies to any algorithm port, not just PDF rendering.

**Root cause**
The spike had two ContentExpectationBuilder methods: build() for combined multi-page validation, and buildForPlannedPage() for isolated single-page validation during fitting. The port dropped buildForPlannedPage and used build() for both, causing the page 1 validator to demand page 2 content. Result: every fitting attempt returned MISSING_TEXTS.

**Evidence**
Feature 008 Phase Group 3 debugging: E2E tests showed PDF_FITTING_FAILED for every generation attempt with MISSING_TEXTS containing page 2 items while fitting page 1. The spike-vs-production audit matrix (T154-T156) identified this as a PARTIAL PORT gap.

**Mitigation**
1. Always compare production port method-by-method against the spike/spike source
2. Create an audit matrix mapping every spike method/behavior to production equivalent before claiming port complete
3. Test isolated page fitting separately from combined validation
4. Package-private helper methods enable targeted unit tests without full integration setup

---

### 2026-06-22 - Paths.get(first, more...) concatenates segments regardless of absoluteness — always check isAbsolute() before path resolution

**Status**
Active

**Why this is durable**
Unlike `Path.resolve()`, Java's `Paths.get(String first, String... more)` concatenates all segments indiscriminately. It does NOT detect that a segment is absolute. This caused the public PDF route `GET /{username}/{publicCode}` to return 404 on all Docker deployments for weeks. The stored DB path `/usr/local/tomcat/generated_results/93d41e45.../resume.pdf` was resolved by `Paths.get("generated_results", absolutePath)` into the nonsense path `generated_results/usr/local/tomcat/generated_results/.../resume.pdf`. File not found → 404 despite DB row and file both existing.

**Root cause**
`GeneratedFileStorageService.resolveSafePath()` used `Paths.get(BASE_DIR, storedPath)` without checking whether `storedPath` was absolute. On Linux, `Paths.get("a", "/b/c")` produces `a/b/c` — the `/` is just a character, not an absolute-path indicator. Meanwhile `Path.resolve("/b/c")` correctly returns `/b/c`.

**Fix**
Check `Path parsed = Paths.get(storedPath).normalize(); if (parsed.isAbsolute())` before concatenation. If absolute, use `Path.resolve()` semantics or direct the parsed path directly.

**Prevention**
Any code that stores file paths in a database and later resolves them MUST handle both absolute and relative stored paths. The storage format can change over time (old rows have absolute paths, new rows have relative paths). Always check `isAbsolute()` before concatenating with a base directory.

**Tags**
java, path, nio, security, path-traversal, filesystem, bug, resolveSafePath, docker

---

### 2026-06-23 — Uniform artificial delay on public 404 responses prevents timing-based enumeration

**Status**
Active

**Symptoms**
A public unauthenticated route (like `GET /{username}/{publicCode}`) has multiple 404 conditions: invalid username, invalid code, deleted resume, disabled resume. Without a uniform delay, an attacker can measure response times to distinguish "valid username but wrong code" (~10ms DB query) from "invalid username" (~1ms immediate return). This leaks information about which usernames exist in the system.

**Root Cause**
The route handler has multiple `return publicNotFound()` branches, but without a delay, each branch returns at a different speed depending on how far the code reached (validation → rate limit check → DB query → path resolution → file existence check). Even though the HTTP status is the same, the timing difference reveals information.

**Mitigation**
Add a uniform artificial delay (`Thread.sleep(200)`) in the shared `publicNotFound()` helper method. Apply it to ALL error branches on the public route. This ensures an attacker cannot distinguish between "valid username + wrong code" and "invalid username" by timing alone.

```java
private ResponseEntity<Resource> publicNotFound() {
    try { Thread.sleep(200); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
}
```

**Prevention**
Every new public unauthenticated endpoint that returns 404 for multiple indistinguishable conditions MUST use a uniform delay helper. Do not inline `ResponseEntity.notFound()` directly — always go through a shared method that applies the delay. The delay duration should be longer than the slowest error path (~200ms covers slow DB queries).

**Detection**
Code review: look for `ResponseEntity.notFound()` or `ResponseEntity.status(404)` on public routes. If any error path returns 404 faster than another, the route is vulnerable to timing enumeration.

**Evidence**
`PublicResumeController.publicNotFound()` — called from 4 branches: null/blank check, DB lookup return null, file-not-found, path-safety SecurityException. `PublicResumeControllerTest` verifies 404 responses and uniform timing behavior.

**Where to look next**
backend/src/main/java/com/resumainer/controller/PublicResumeController.java — `publicNotFound()` method
