

## Workflow
- W21 | Feature 009 Phase 1 Backend Foundation Completed | milestone,feature-009,phase-1,backend,dto,public-route,410,soft-delete | [WORKLOG.md](WORKLOG.md) | active
- W22 | Feature 009 Home Modal Fix Complete | milestone,feature-009,frontend,phase-2,modal,csrf-fix,e2e,410 | [WORKLOG.md](WORKLOG.md) | active
- W23 | Feature 010 Admin Console Users and Resumes Completed | milestone,feature-010,admin,backend,frontend,completed | [WORKLOG.md](WORKLOG.md) | active
- W23 | Feature 011 Auth Hardening Phases 1-6 (Spring Security migration) completed | milestone,feature-011,spring-security,auth-hardening,csrf,login,migrations | [WORKLOG.md](WORKLOG.md) | active

## Bugs
- B29 | Dual-flag soft delete must update both is_deleted and deleted_at | database,soft-delete,dao,consistency,is_deleted,deleted_at,security,feature-009 | [BUGS.md](BUGS.md) | active
- B30 | Public URLs must not be visually truncated | css,public-url,truncation,ui,copyable,readability | [BUGS.md](BUGS.md) | active

## Decisions
- D37 | Checkpoint evidence standard: changed files + assertions + sample + audit | evidence,checkpoint,quality,verification,testing,process,standard,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D38 | Do not mock the unit whose behavior is under test | testing,mock,unit-test,tdd,best-practice,anti-pattern | [DECISIONS.md](DECISIONS.md) | active
- D39 | Every bug fix must include a regression test that would fail on the previous implementation | bug-fix,regression,testing,tdd,quality,process,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D40 | Separate implementation complete from contract proven | quality,verification,contract,testing,evidence,process,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D44 | Report exact changed files and exact verification commands | reporting,handoff,checkpoint,evidence,verification,process,best-practice | [DECISIONS.md](DECISIONS.md) | activeS.md) | active
- D45 | Do not expand scope during cleanup/review | cleanup,review,scope,anti-pattern,process,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D46 | PrimeVue template placeholders must not pass through vue-i18n interpolation | primevue,i18n,pagination,antipattern,vue | [DECISIONS.md](DECISIONS.md) | active
- D47 | Data exposure audit must include SQL SELECT columns and log statements, not only DTOs | security,data-exposure,audit,sql,logging,pii,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D48 | Close frontend-backend API contracts end-to-end before marking feature complete | contract,backend,frontend,api,integration,quality,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D49 | Final review/audit phases must enforce no-silent-coding rules | review,audit,process,scope,anti-pattern,ai-assisted,best-practice | [DECISIONS.md](DECISIONS.md) | active
- D48 | SPA CSRF with Spring Security 6.5 requires CookieCsrfTokenRepository + SpaCsrfTokenRequestHandler + /api/csrf bootstrap endpoint | csrf,spring-security,spa,security,non-boot,6.5 | [DECISIONS.md](DECISIONS.md) | active
- D49 | Non-Boot Spring MVC: DAO and Security must share root ApplicationContext | spring-mvc,non-boot,architecture,root-context,dao,security,beans | [DECISIONS.md](DECISIONS.md) | active
- D50 | Failed login counter must only increment for password-eligible users (anti-enumeration) | security,authentication,failed-login,anti-enumeration,brute-force | [DECISIONS.md](DECISIONS.md) | active