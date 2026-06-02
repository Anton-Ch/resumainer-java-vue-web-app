# Web Pages Open Questions

## Critical questions blocking prototype readiness
| ID | Question | Why it matters | Owner | Status | Answer/Resolution |
|---|---|---|---|---|---|
| Q1 | What exactly is in scope? | Determines how many screens to design | User | Resolved | Single auth page: Login + Register toggle |
| Q2 | Vue or Thymeleaf? | Stack for prototype | User/Architect | Resolved | Vue SPA with PrimeVue |
| Q3 | Does Login share layout with Landing? | Header/footer structure | User | Resolved | Auth page is standalone SPA page (no Landing header) |
| Q4 | Password visibility toggle? | Field component design | User | Resolved | Yes, add eye icon toggle |
| Q5 | Session or JWT? | Backend state design | Architect | Open | Deferred — not blocking design spec |

## Non-blocking questions
| ID | Question | Why it matters | Owner | Status | Answer/Resolution |
|---|---|---|---|---|---|
| Q6 | "Remember me" checkbox? | Form design | User | Resolved | Yes, include |
| Q7 | Logo URL on auth page? | Navigation | User | Resolved | Same logo as Landing (SVG), links to Landing |
| Q8 | "Forgot password?" link? | MVP scope | User | Resolved | Not in scope per MVP wireframes |
| Q9 | Exact route path for auth page? | Vue Router config | Architect | Open | Needs decision: /login or /auth |
| Q10 | PrimeVue Form or custom form? | Implementation approach | Architect | Resolved | PrimeVue Form + Zod resolver

## Assumptions currently being used
| ID | Assumption | Risk if wrong | Source | Validation needed |
|---|---|---|---|---|
| A1 | Login uses Vue 3 + PrimeVue (not Thymeleaf) | Could be Thymeleaf if Landing Page team handles it | Architecture (SPA vs Landing) | Confirm with architect |
| A2 | Login is a centered card on a light gray background | Layout might need to match Landing Page style | REDESIGN_NOTES | Confirm visual alignment |
| A3 | No "Forgot password" in MVP scope | User may expect it | Wireframe does not show it | Confirm with user |
