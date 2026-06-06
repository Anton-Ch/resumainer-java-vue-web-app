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
