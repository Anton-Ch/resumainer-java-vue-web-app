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
Active

**Symptoms**
`@ControllerAdvice` with `@ExceptionHandler` methods never gets invoked. Log shows: `ControllerAdvice beans: none` during DispatcherServlet init. 404 errors show Tomcat default page instead of custom Thymeleaf template.

**Root Cause**
In pure Spring MVC with `@Configuration` + `@EnableWebMvc` and no `@ComponentScan`, Spring does NOT scan the classpath for stereotype annotations. This applies to ALL annotation-driven Spring beans: `@Controller` (B1), `@ControllerAdvice`, `@RestController`, `@Service`, `@Repository`, `@Component`.

**Future mistake prevented**
Every annotated class must be registered as an explicit `@Bean` in a `@Configuration` class (typically `WebConfig.java`). This is a project-wide constraint — not a one-off workaround. See also B1 for `@Controller`.

**Evidence**
GlobalExceptionHandler (`@ControllerAdvice`) was never invoked despite correct `@ExceptionHandler` methods. Log showed `ControllerAdvice beans: none`. Adding `@Bean public GlobalExceptionHandler globalExceptionHandler()` to WebConfig.java fixed it immediately. 404 errors now serve our Thymeleaf template.

**Prevention / Detection**
After adding any new annotated class, check the startup log for:
- `ControllerAdvice beans: none` → @ControllerAdvice not registered
- HandlerMapping with zero mappings → @Controller not registered
Always add a corresponding `@Bean` method in WebConfig.java.

**Where to look next**
backend/src/main/java/com/resumainer/config/WebConfig.java, any new class with @Controller, @ControllerAdvice, @RestController, etc.

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
