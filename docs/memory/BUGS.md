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
