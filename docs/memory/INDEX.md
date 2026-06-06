# Memory Index

This is a compact routing map for durable project memory (`docs/memory/`). Keep it short. 

> [!NOTE]
> High-level project governance, constitution, and standards are stored in the **Governance Layer** at `.specify/memory/` and should be reviewed before technical planning.

## Architecture
- A1 | Servlet Container Integration via Java SPI, Not XML Descriptor | servlet, tomcat, architecture, spi, jakarta-ee | [ARCHITECTURE.md](ARCHITECTURE.md) | active
- A2 | SPA under /app/ routing with landing page at / | architecture,routing,nginx,spa,vue,thymeleaf,landing-page | [ARCHITECTURE.md](ARCHITECTURE.md) | active

## Bugs
- B1 | Controller Without Registration Is Invisible to Spring MVC | spring-mvc, controller, configuration, component-scan, bean-registration | [BUGS.md](BUGS.md) | active
- B2 | Spring MVC (non-Boot): Use @Value for Profile, Not Environment.getActiveProfiles() | spring-mvc, profile, environment, configuration, properties | [BUGS.md](BUGS.md) | active
- B3 | SpringResourceTemplateResolver: ServletContext prefix fails in MockMvc tests | thymeleaf, template, resolver, classpath, mockmvc, spring-mvc, testing | [BUGS.md](BUGS.md) | active
- B4 | Shell scripts with CRLF line endings fail in Linux Docker containers | docker, crlf, shell, bash, linux, line-endings, windows, devops | [BUGS.md](BUGS.md) | active
- B5 | All Spring stereotype annotations require explicit @Bean in pure Spring MVC | spring-mvc, bean, controller, controller-advice, component-scan, registration, configuration | [BUGS.md](BUGS.md) | active
- B6 | FilterRegistrationBean is Spring Boot API — use getServletFilters() in pure Spring MVC | spring-mvc, filter, servlet, appinitializer, csrf, boot-vs-core, configuration | [BUGS.md](BUGS.md) | active
- B7 | Mockito-extensions config file with wrong content breaks all mock creation | mockito,testing,mock-maker,configuration,classpath | [BUGS.md](BUGS.md) | active
- B8 | MockMvc jsonPath() assertions require explicit jayway-jsonpath dependency | mockmvc,testing,jsonpath,spring-mvc,dependency | [BUGS.md](BUGS.md) | active
- B9 | Long auto-unboxing NullPointerException when comparing with primitive | java,nullpointer,unboxing,long,primitive,common-mistake | [BUGS.md](BUGS.md) | active
- B10 | MockMvc standalone: each perform() creates a fresh session — use MockHttpSession for filter tests | mockmvc,testing,standalone,session,filter,csrffilter,spring-mvc | [BUGS.md](BUGS.md) | active
- B11 | Flyway @Bean(initMethod="migrate") required in pure Spring MVC — no auto-migration | flyway,migration,spring-mvc,bean,database,pure-spring-mvc,boot-vs-core | [BUGS.md](BUGS.md) | active
- B12 | DataSource URL with ${...} placeholders stays as literal string — use System.getenv() | datasource,spring-mvc,properties,placeholder,env-var,boot-vs-core | [BUGS.md](BUGS.md) | active
- B13 | PrimeVue 4 Zod resolver validation messages not reactive to locale changes | primevue,vue3,i18n,locale,zod,resolver,form,validation,reactive | [BUGS.md](BUGS.md) | active
- B14 | PR descriptions with backtick-escaped paths get mangled in gh CLI + PowerShell | pr,github,markdown,backtick,formatting,powershell,cli | [BUGS.md](BUGS.md) | active

## Decisions
- D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml) | servlet, spring-mvc, jakarta-ee, tomcat, initialization, web.xml | [DECISIONS.md](DECISIONS.md) | active
- D2 | Maven Wrapper Must Be at Same Directory Level as pom.xml | maven, wrapper, build, project-structure, best-practice | [DECISIONS.md](DECISIONS.md) | active
- D3 | Docker Tomcat: Use bash /dev/tcp Instead of nc for TCP Health Checks | docker, tomcat, wait-for-it, networking, shell, bash | [DECISIONS.md](DECISIONS.md) | active
- D4 | Docker Dev Workflow: Rebuild Image After Code Changes | docker, development, workflow, build, caching | [DECISIONS.md](DECISIONS.md) | active
- D5 | Error pages use Thymeleaf templates for i18n support, static fallback for low-level failures | thymeleaf, error, i18n, bilingual, global-exception-handler, architecture, error-pages | [DECISIONS.md](DECISIONS.md) | active
- D6 | Custom 404 requires DispatcherServlet config + bean registration | spring-mvc, error-handling, 404, dispatcher-servlet, exception-resolver, thymeleaf | [DECISIONS.md](DECISIONS.md) | active
- D7 | Hybrid PK strategy: gen_random_uuid() for entities, BIGSERIAL for lookups | postgresql, uuid, primary-key, database, schema, flyway, migration | [DECISIONS.md](DECISIONS.md) | active
- D8 | CSRF via OWASP cookie-to-header pattern in pure Spring MVC | csrf, security, spring-mvc, filter, owasp, cookie-to-header, vue, spa | [DECISIONS.md](DECISIONS.md) | active
- D9 | JDK version must match project target to avoid build and test failures | jdk,java-version,mockito,testing,dev-env,setup | [DECISIONS.md](DECISIONS.md) | active
- D10 | DAO connection-accepting overloads for JDBC transaction support | dao,jdbc,transaction,connection,service-layer,architecture | [DECISIONS.md](DECISIONS.md) | active
- D12 | PrimeVue 4 Form with Zod resolver: standard validation pattern | primevue,vue3,form,validation,zod,resolver,frontend | [DECISIONS.md](DECISIONS.md) | active
- D13 | All user-facing strings must use i18n $t() — no hardcoded text in templates | i18n,internationalization,vue3,vue-i18n,frontend,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D14 | Mandatory manual integration testing phase after all implementation phases | testing,integration,manual-testing,playwright,docker,i18n,quality | [DECISIONS.md](DECISIONS.md) | active
- D15 | Separate @Configuration for infrastructure beans via @ComponentScan | spring-mvc,configuration,component-scan,datasource,infrastructure,architecture | [DECISIONS.md](DECISIONS.md) | active
- D16 | Controller tests: standalone MockMvc over full Spring context when no DB needed | testing,mockmvc,spring-mvc,controller,standalone,integration-testing | [DECISIONS.md](DECISIONS.md) | active
- D17 | PrimeVue DataTable lazy mode for server-paginated APIs | primevue,vue3,datatable,lazy,pagination,frontend,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D18 | Independent block loading for resilient page architecture | architecture,dashboard,resilience,failure-isolation,frontend,ux | [DECISIONS.md](DECISIONS.md) | active

## Workflow
- W1 | First Feature MVP Achieved: Hello World Tomcat | milestone, mvp, hello-world, docker, spring-mvc, tomcat | [WORKLOG.md](WORKLOG.md) | active
- W2 | Second Feature MVP Achieved: Thymeleaf Landing Page | milestone, mvp, landing-page, thymeleaf, i18n, feature-002, bilingual | [WORKLOG.md](WORKLOG.md) | active
- W3 | Feature 003 Planning and Security Review Completed | milestone, feature-003, vue-auth, planning, security-review, specification | [WORKLOG.md](WORKLOG.md) | active
- W4 | Feature 003 Phase 2 Foundational Database and DAO Layer Completed | milestone,feature-003,phase-2,flyway,dao,tdd | [WORKLOG.md](WORKLOG.md) | active
- W5 | Feature 003 Phase 3 Registration Service and Controller Completed | milestone,feature-003,phase-3,registration,tdd | [WORKLOG.md](WORKLOG.md) | active
- W6 | Feature 003 Phase 4 Login with Rate Limiting Completed | milestone,feature-003,phase-4,login,rate-limiting,security | [WORKLOG.md](WORKLOG.md) | active
- W7 | Feature 003 Phase 5 Interceptor, CSRF Filter, and Configuration Completed | milestone,feature-003,phase-5,interceptor,csrf,webconfig | [WORKLOG.md](WORKLOG.md) | active
- W8 | Feature 003 Phase 8 Bilingual Auth Forms with PrimeVue + Zod Completed | milestone,feature-003,phase-8,auth,primevue,zod,forms | [WORKLOG.md](WORKLOG.md) | active
- W9 | Feature 003 Bug-Fix and Integration Testing Completed | milestone,feature-003,bug-fix,integration-testing,i18n,docker | [WORKLOG.md](WORKLOG.md) | active
- W10 | Feature 004 Custom JDBC Connection Pool Implementation Completed | milestone,feature-004,jdbc-pool,tdd,component-scan,datasource | [WORKLOG.md](WORKLOG.md) | active
- W11 | Feature 005 Planning and All Artifacts Completed | milestone,feature-005,planning,user-home-page,spec,plan,tasks,security-review | [WORKLOG.md](WORKLOG.md) | active