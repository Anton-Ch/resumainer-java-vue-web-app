# Memory Index

This is a compact routing map for durable project memory (`docs/memory/`). Keep it short. 

> [!NOTE]
> High-level project governance, constitution, and standards are stored in the **Governance Layer** at `.specify/memory/` and should be reviewed before technical planning.

## Architecture
- A1 | Servlet Container Integration via Java SPI, Not XML Descriptor | servlet, tomcat, architecture, spi, jakarta-ee | [ARCHITECTURE.md](ARCHITECTURE.md) | active

## Bugs
- B1 | Controller Without Registration Is Invisible to Spring MVC | spring-mvc, controller, configuration, component-scan, bean-registration | [BUGS.md](BUGS.md) | active
- B2 | Spring MVC (non-Boot): Use @Value for Profile, Not Environment.getActiveProfiles() | spring-mvc, profile, environment, configuration, properties | [BUGS.md](BUGS.md) | active
- B3 | SpringResourceTemplateResolver: ServletContext prefix fails in MockMvc tests | thymeleaf, template, resolver, classpath, mockmvc, spring-mvc, testing | [BUGS.md](BUGS.md) | active
- B4 | Shell scripts with CRLF line endings fail in Linux Docker containers | docker, crlf, shell, bash, linux, line-endings, windows, devops | [BUGS.md](BUGS.md) | active
- B5 | All Spring stereotype annotations require explicit @Bean in pure Spring MVC | spring-mvc, bean, controller, controller-advice, component-scan, registration, configuration | [BUGS.md](BUGS.md) | active
- B6 | FilterRegistrationBean is Spring Boot API — use getServletFilters() in pure Spring MVC | spring-mvc, filter, servlet, appinitializer, csrf, boot-vs-core, configuration | [BUGS.md](BUGS.md) | active

## Decisions
- D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml) | servlet, spring-mvc, jakarta-ee, tomcat, initialization, web.xml | [DECISIONS.md](DECISIONS.md) | active
- D2 | Maven Wrapper Must Be at Same Directory Level as pom.xml | maven, wrapper, build, project-structure, best-practice | [DECISIONS.md](DECISIONS.md) | active
- D3 | Docker Tomcat: Use bash /dev/tcp Instead of nc for TCP Health Checks | docker, tomcat, wait-for-it, networking, shell, bash | [DECISIONS.md](DECISIONS.md) | active
- D4 | Docker Dev Workflow: Rebuild Image After Code Changes | docker, development, workflow, build, caching | [DECISIONS.md](DECISIONS.md) | active
- D5 | Error pages use Thymeleaf templates for i18n support, static fallback for low-level failures | thymeleaf, error, i18n, bilingual, global-exception-handler, architecture, error-pages | [DECISIONS.md](DECISIONS.md) | active
- D6 | Custom 404 requires DispatcherServlet config + bean registration | spring-mvc, error-handling, 404, dispatcher-servlet, exception-resolver, thymeleaf | [DECISIONS.md](DECISIONS.md) | active
- D7 | Hybrid PK strategy: gen_random_uuid() for entities, BIGSERIAL for lookups | postgresql, uuid, primary-key, database, schema, flyway, migration | [DECISIONS.md](DECISIONS.md) | active
- D8 | CSRF via OWASP cookie-to-header pattern in pure Spring MVC | csrf, security, spring-mvc, filter, owasp, cookie-to-header, vue, spa | [DECISIONS.md](DECISIONS.md) | active

## Workflow
- W1 | First Feature MVP Achieved: Hello World Tomcat | milestone, mvp, hello-world, docker, spring-mvc, tomcat | [WORKLOG.md](WORKLOG.md) | active
- W2 | Second Feature MVP Achieved: Thymeleaf Landing Page | milestone, mvp, landing-page, thymeleaf, i18n, feature-002, bilingual | [WORKLOG.md](WORKLOG.md) | active
- W3 | Feature 003 Planning and Security Review Completed | milestone, feature-003, vue-auth, planning, security-review, specification | [WORKLOG.md](WORKLOG.md) | active
