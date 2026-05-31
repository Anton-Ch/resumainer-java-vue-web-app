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
