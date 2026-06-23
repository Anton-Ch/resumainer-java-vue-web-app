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
- B15 | FK column type must match referenced entity PK — UUID for users(id), not BIGINT | database,migration,postgresql,foreign-key,uuid,bigint,schema,flyway | [BUGS.md](BUGS.md) | active
- B16 | vue-i18n @ character in message values causes runtime SyntaxError | vue-i18n,i18n,frontend,syntax-error,special-characters,escaping | [BUGS.md](BUGS.md) | active
- B17 | New frontend API services must include CSRF token handling for unsafe methods | csrf,frontend,service,http-client,security,profile | [BUGS.md](BUGS.md) | active
- B18 | Java Set.contains() with toLowerCase() — Set values must be lowercase too | java,set,contains,case-sensitive,sort,validation,common-mistake | [BUGS.md](BUGS.md) | active
- B19 | PrimeVue Form onSubmit try without catch silently swallows API errors | primevue,vue3,form,error-handling,try,catch,async,common-mistake | [BUGS.md](BUGS.md) | active
- B20 | Ambiguous Date reference from java.sql.* + java.util.* wildcard imports | java, compiler, date, import, sql, util, ambiguous, compilation-error | [BUGS.md](BUGS.md) | active
- B21 | OpenRouter JSON response parsing must use Jackson ObjectMapper, not manual string operations | openrouter,ai,json,parsing,jackson,http-client,response-handling | [BUGS.md](BUGS.md) | active
- B22 | Standalone MockMvc without setControllerAdvice causes ServiceException to become 500 | mockmvc,testing,standalone,controller-advice,serviceexception,spring-mvc | [BUGS.md](BUGS.md) | active
- B23 | DeepSeek V4 Flash returns reasoning-only responses with null content intermittently | openrouter,deepseek,ai,reasoning,retry,missing-content,diagnostic,response-shape | [BUGS.md](BUGS.md) | active
- B24 | Cover letter column exists in migration but INSERT SQL omits it | database,migration,dao,cover-letter,insert,column-propagation,saved-resumes | [BUGS.md](BUGS.md) | active
- B25 | Docker BuildKit: COPY . . cache survives --no-cache — must docker rmi --force | docker, buildkit, cache, copy, no-cache, multi-stage, devops, frontend, rebuild | [BUGS.md](BUGS.md) | active
- B26 | Dropped spike method causes universal validation failure in ported algorithm | spike,porting,fit-engine,content-expectation,missing-texts,validation,algorithm-drift | [BUGS.md](BUGS.md) | active
- B27 | Paths.get(first, more...) concatenates absolute paths instead of resolving them — check isAbsolute() first | java, path, nio, security, path-traversal, filesystem, bug, resolveSafePath, docker | [BUGS.md](BUGS.md) | active
- B28 | Uniform artificial delay on public 404 responses prevents timing-based enumeration | security,timing,enumeration,404,public-route,rate-limiting,privacy | [BUGS.md](BUGS.md) | active

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
- D19 | Maven compiler -parameters flag required in Spring MVC 6 for controller parameter name resolution | spring-mvc,maven,compiler,parameters,controller,request-param,configuration,java-21 | [DECISIONS.md](DECISIONS.md) | active
- D20 | PrimeVue 4: ToastService and ConfirmationService require app.use() plugin installation | primevue,vue3,toast,confirmdialog,plugin,service,configuration,frontend | [DECISIONS.md](DECISIONS.md) | active
- D21 | PrimeVue 4: Tooltip is a global directive requiring explicit registration | primevue,vue3,tooltip,directive,configuration,frontend | [DECISIONS.md](DECISIONS.md) | active
- D22 | PrimeVue 4: PrimeIcons is a separate package requiring explicit CSS import | primevue,vue3,primeicons,icons,css,package,frontend | [DECISIONS.md](DECISIONS.md) | active
- D23 | Manual JDBC transaction must catch Exception, not just SQLException | transaction,jdbc,rollback,spring-mvc,service | [DECISIONS.md](DECISIONS.md) | active
- D24 | Sort field names must be mapped between frontend and backend in lazy DataTable | datatable,sort,pagination,frontend,backend,mapping,primevue | [DECISIONS.md](DECISIONS.md) | active
- D25 | HTML-first generation pipeline with deferred PDF conversion | html, pdf, generation, pipeline, architecture, feature-decomposition | [DECISIONS.md](DECISIONS.md) | active
- D26 | Vue composable state must be module-level singleton, not per-component-instance | vue3, composable, state-management, singleton, ref, multi-page-wizard | [DECISIONS.md](DECISIONS.md) | active
- D27 | Backend-generated opaque updateKey for review/save pattern | review,update-key,save,section-aware,security,spring-mvc,frontend,decoupling | [DECISIONS.md](DECISIONS.md) | active
- D28 | Frontend adapter pattern for hierarchical backend DTO to flat view model | frontend,adapter,transformer,review,dto,view-model,decoupling,architecture | [DECISIONS.md](DECISIONS.md) | active
- D29 | Verify external API parameters against official docs before implementation | openrouter,api,documentation,context7,reasoning,configuration,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D30 | GENERATION_ALREADY_IN_PROGRESS returns HTTP 409 Conflict, not 500 | http,409,conflict,generation,concurrency,lifecycle,spring-mvc,controller,error-handling | [DECISIONS.md](DECISIONS.md) | active
- D31 | Frontend download service must consume backend-provided DTO URLs, not reconstruct from IDs | frontend, service, dto, url, contract, download, refactor, decoupling | [DECISIONS.md](DECISIONS.md) | active
- D32 | Budget drift from proven spike capacity causes production PDF fitting failures — restore parity via migration | pdf, budget, spike, parity, migration, fitting, ec-016, v12.1, capacity, drift | [DECISIONS.md](DECISIONS.md) | active
- D34 | Spike audit matrix is mandatory before merging any ported code | spike,porting,audit,matrix,quality,regression,process,testing | [DECISIONS.md](DECISIONS.md) | active
- D35 | Footer safe zone detection as distinct PDF validation dimension | pdf,validation,footer,safe-zone,overlap,detection,position-stripper,fitting | [DECISIONS.md](DECISIONS.md) | active
- D33 | Content-Disposition header allowlist prevents CRLF injection | security,http,header,crlf,injection,download,controller,spring-mvc | [DECISIONS.md](DECISIONS.md) | active

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
- W12 | Feature 005 Implementation — Backend and Frontend Core Completed | milestone,feature-005,implementation,backend,frontend,user-home-page | [WORKLOG.md](WORKLOG.md) | active
- W13 | Feature 006 Profile Page DAO and Service/Controller Layers Completed | milestone,feature-006,dao,service,controller,profile | [WORKLOG.md](WORKLOG.md) | active
- W14 | Feature 006 bug fixes — CSRF, connection pool, favicon | milestone,feature-006,bugfix,csrf,rollback,favicon | [WORKLOG.md](WORKLOG.md) | active
- W15 | Feature 007 Review Page — backend DTO fix + frontend adapter + tabbed editing UI | milestone,feature-007,review-page,backend-fix,frontend-adapter,primevue-tabs | [WORKLOG.md](WORKLOG.md) | active
- W16 | Feature 007 Export View, Cover Letter, Generation Concurrency, and OpenRouter fixes completed | milestone,feature-007,export-view,cover-letter,generation-concurrency,openrouter,diagnostics,retry,fixes | [WORKLOG.md](WORKLOG.md) | active
- W17 | Feature 008 Phase Group 1 (Bullet Points + Review + Prompt/Parser) Completed | milestone,feature-008,phase-group-1,bullet-points,review,prompt,parser,tdd,cross-layer | [WORKLOG.md](WORKLOG.md) | active
- W18 | Feature 008 Phase Group 3 — Spike Parity Fixes Completed (Phases 19-21) | milestone,feature-008,phase-group-3,spike-parity,export-dto,public-route,path-safety,fit-engine,page-plan,tdd | [WORKLOG.md](WORKLOG.md) | active
- W19 | Feature 008 Phases 23-27: download security, PDF fitting, semantic render repair completed | milestone, feature-008, phase-23-27, pdf, download, security, fitting, semantic, budget, validation | [WORKLOG.md](WORKLOG.md) | active
- W20 | Feature 008 Phase 28: footer safe zone and overlap detection completed | milestone, feature-008, phase-28, footer, safe-zone, overlap, pdf, validation, fitting | [WORKLOG.md](WORKLOG.md) | active