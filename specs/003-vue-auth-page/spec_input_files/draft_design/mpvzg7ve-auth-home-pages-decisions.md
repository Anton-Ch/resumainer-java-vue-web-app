# Web Pages Design Decisions

## Decision log
| ID | Date | Decision | Rationale | Source/Evidence | Owner | Status |
|---|---|---|---|---|---|---|

## Accepted decisions
| ID | Decision | Rationale | Source/Evidence | Owner | Status |
|---|---|---|---|---|---|
| D-A1 | Single auth page with Login/Register toggle (link-switching) | UX best practice for simple auth; reference CodePen approach | User confirmation, CodePen reference | User | Accepted |
| D-A2 | Vue SPA (not Thymeleaf) for auth | Project uses Vue for app pages, Thymeleaf only for Landing | AGENTS.md, architecture | Architect | Accepted |
| D-A3 | Light Enterprise SaaS visual style (light_design_dna.md) | Consistent redesign direction applied across product | REDESIGN_NOTES, light_design_dna.md | Designer | Accepted |
| D-A4 | Email instead of username in Login form | Wireframe 5.3 shows email field | wireframes_detailed_description.md | User | Accepted |
| D-A5 | Add "Remember me" checkbox below password field | UX best practice for login forms | Context7, user confirmation | User | Accepted |
| D-A6 | Password visibility toggle (eye icon) on password fields | UX best practice, expected in modern forms | User confirmation | User | Accepted |
| D-A7 | Dual validation: Vuelidate frontend + Jakarta backend | Constitution NFR-019 | Constitution, requirements | Architect | Accepted |
| D-A8 | PRG pattern: button disabled + spinner after first click | Constitution NFR-017 | Constitution | Architect | Accepted |
| D-A9 | Auth error: generic "Invalid email or password" only | Security: don't disclose whether email exists | wireframes, Constitution V | Security | Accepted |
| D-A10 | Already authenticated → redirect to User Home or Admin Home | Based on role; skip login form entirely | wireframe confirmed behavior | User | Accepted |
| D-A11 | User Home / Admin Home = placeholder empty pages for now | Out of scope for this design batch | User confirmation | User | Accepted |
| D-A12 | Staggered slide animation for form switching | Reference CodePen approach: login/signup slides, curved shape transition | CodePen reference | User | Accepted |
| D-A13 | PrimeVue Form + Zod resolver for validation | PrimeVue 4 recommended approach with Zod | Context7 / PrimeVue docs | Architect | Accepted |
| D-A14 | Session-based auth (Spring MVC HandlerInterceptor) | Constitution NFR-021 | Constitution, requirements | Architect | Deferred (needs confirmation) |
| D-A15 | i18n for EN/RU auth strings | Constitution NFR-030 | Constitution | Architect | Accepted |

## Rejected ideas
- Dark futuristic AI aesthetics (rejected per light_design_dna.md)
- Separate pages for Login and Register (accepted: single page with toggle)
- Two separate routes /login and /register (accepted: single route with state toggle)
- Thymeleaf for auth (rejected: Vue SPA)
- Tabs-based switching (rejected: link-based switching per reference)

## Deferred decisions
| ID | Decision | Reason |
|---|---|---|
| D-D1 | Session vs JWT auth mechanism | Not yet decided in project — affects API design |
| D-D2 | Exact token refresh/expiry behavior | Depends on auth mechanism decision |
| D-D3 | User Home / Admin Home route paths | Will be decided when home pages are designed |

## Design constraints
- No Spring Boot, no ORM/JPA
- Vue 3 + PrimeVue for SPA pages
- i18n for EN/RU
- Must feel "enterprise SaaS" — not a side project aesthetic
- No stock images, no AI-hype visuals

## Non-negotiables
- Error messages must NOT disclose whether email exists (security)
- Submit button disabled after first click (PRG)
- Backend validation is authoritative
- BCrypt for password storage
