# Auth Page (Login / Register) — Complete Design Specification

**Project:** ResumAIner  
**Date:** 2026-06-02  
**Status:** Approved by stakeholder  
**Version:** 1.0  
**Design DNA:** Light Enterprise SaaS  

---

## Table of Contents

1. [Scope and Overview](#1-scope-and-overview)
2. [User Journey and States](#2-user-journey-and-states)
3. [Component Architecture](#3-component-architecture)
4. [API Contracts](#4-api-contracts)
5. [Visual Design Summary](#5-visual-design-summary)
   - [Full visual spec → separate document](#51-visual-design-summary)
6. [Edge Cases Matrix](#6-edge-cases-matrix)
7. [Decisions and Rationale](#7-decisions-and-rationale)
8. [Open Questions](#8-open-questions)
9. [QA Observable States](#9-qa-observable-states)
10. [Designer Handoff Notes](#10-designer-handoff-notes)

---

## 1. Scope and Overview

### 1.1 What We Are Building

A single-page Vue 3 SPA that handles all user authentication — **Login** and **Register** — with a link-based toggle between modes. The page is **standalone/minimal** (no landing page header, no app shell, no footer). It uses the **Light Enterprise SaaS** design DNA.

### 1.2 What Is In Scope

| Item | Details |
|---|---|
| Auth page (`/app`) | Login ↔ Register toggle, all states |
| User Home placeholder | Empty page at `/user-home` — shell only |
| Admin Home placeholder | Empty page at `/admin-home` — shell only |
| Language switcher | EN/RU bilingual support |
| "Remember me" checkbox | Token storage persistence choice |
| Password visibility toggle | Eye icon show/hide |
| Form validation | Dual: PrimeVue Form + Zod (frontend), Jakarta Validation (backend) |
| PRG pattern | Button disabled + spinner on submit |
| Router guards | Redirect already-authenticated users; protect routes |
| Auth interceptor | Axios interceptor for token attachment + 401 handling |

### 1.3 What Is NOT In Scope

| Item | Reason |
|---|---|
| Password recovery / reset | Not in MVP wireframes |
| Landing Page header/footer | Auth page is standalone |
| User Home content | Separate design batch |
| Admin Home content | Separate design batch |
| Email verification flow | Deferred to post-MVP |
| Session vs JWT decision | Deferred — not blocking design |
| 2FA / MFA | Not in MVP |

### 1.4 Technology Stack

| Layer | Technology |
|---|---|
| Frontend framework | Vue 3 (Composition API + `<script setup>`) |
| UI library | PrimeVue 4 |
| Form validation | PrimeVue Form + Zod resolver |
| Router | Vue Router 4 |
| HTTP client | Axios with interceptor |
| Backend framework | Spring MVC (no Spring Boot) |
| Auth mechanism | JWT or session (TBD — not blocking design) |
| Password hashing | BCrypt |
| i18n | Vue I18n or PrimeVue locale |
| Styling | CSS custom properties (shared design tokens with Landing) |

---

## 2. User Journey and States

### 2.1 Pre-flight: Router Guard

```
ROUTER GUARD: beforeEach(to /app)
  CHECK: Is user authenticated? (token in localStorage/sessionStorage)
    ├── YES:
    │   FETCH: User role (from token payload or /api/auth/me)
    │   ├── Role = USER → router.replace('/user-home')
    │   └── Role = ADMIN → router.replace('/admin-home')
    │   RETURN: next(false) + replace
    └── NO:
        ALLOW: next() → render AuthPage (LOGIN mode by default)
```

### 2.2 Auth Page State Machine

```
                    ┌──────────────┐
                    │  AUTH_CHECK  │  (router guard — pre-render)
                    │  (loading)   │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
           ┌────────│  AUTHENTICATED│─────────┐
           │        │  (redirect)  │         │
           │        └──────────────┘         │
           │                                  │
    ┌──────▼──────┐                  ┌───────▼──────┐
    │  REDIRECT   │                  │   IDLE       │
    │  /user-home │                  │  (form view) │
    │  or /admin  │                  └───────┬──────┘
    └─────────────┘                          │
                                    ┌───────▼──────┐
                                    │   TYPING     │
                                    │  (validating) │
                                    └───────┬──────┘
                                            │
                                    ┌───────▼──────┐
                                    │  SUBMITTING  │
                                    │  (spinner)   │
                                    └───────┬──────┘
                                            │
                               ┌────────────┼────────────┐
                               │            │            │
                        ┌──────▼─────┐ ┌───▼───┐ ┌──────▼──────┐
                        │  SUCCESS   │ │ERROR  │ │ TIMEOUT     │
                        │ (redirect) │ │(toast)│ │ (toast+retry)│
                        └────────────┘ └───────┘ └─────────────┘
                                            │
                                    ┌───────▼──────┐
                                    │    IDLE      │
                                    │ (back to form)│
                                    └──────────────┘
```

### 2.3 Login Mode Flow (L1–L8)

| Step | User Action | System Response | Validation |
|---|---|---|---|
| L1 | Focus email field | Focus styles, no validation yet | — |
| L2 | Type email | Real-time validation after first change | Required, valid email format |
| L3 | Focus password field | Focus styles | — |
| L4 | Click eye icon (optional) | Toggle password visibility | — |
| L5 | Toggle Remember me | Boolean state | — |
| L6 | Click Login / press Enter | Run full validation → if valid: disable button, show spinner, POST `/api/auth/login` | All required + valid format |
| L7 | (Backend processes) | 200 → success; 401 → "Invalid email or password"; 403 → blocked/inactive; 500 → server error | Backend @Valid |
| L8 | (Success) | Store token, emit auth-success, router.replace to home | — |

### 2.4 Register Mode Flow (R1–R7)

| Step | User Action | System Response | Validation |
|---|---|---|---|
| R1 | Click "Register now!" link | Animation: form slides out, Register form slides in (staggered), URL updates to `?mode=register` | — |
| R2 | Type email | Real-time validation | Required, valid email format |
| R3 | Type password | Real-time validation + strength indicator (PrimeVue Password) | Required, min 8 chars |
| R4 | Type confirm password | Real-time match validation | Must match password |
| R5 | Click Register / press Enter | Full validation → if valid: disable button, show spinner, POST `/api/auth/register` | All required |
| R6 | (Backend processes) | 201 → success; 409 → email exists; 500 → error | Backend @Valid + unique check |
| R7 | (Success) | Store token, auth-success, router.replace('/user-home') | — |

### 2.5 State — Visual Indicators Matrix

| State | Form | Submit Button | Error Display | Toast |
|---|---|---|---|---|
| **IDLE** | Empty, pristine | Enabled, "Login"/"Register" | None | None |
| **TYPING** | Content present | Enabled | Inline per-field errors | None |
| **VALIDATION_ERROR** | Invalid fields highlighted | Enabled | Red border + inline message | None |
| **SUBMITTING** | Fields dimmed/disabled | Disabled, spinner | Previous errors hidden | None |
| **AUTH_ERROR** | Password cleared | Enabled | Inline above form "Invalid..." | None |
| **NETWORK_ERROR** | Fields preserved | Enabled | None | Toast: timeout |
| **SERVER_ERROR** | Fields preserved | Enabled | None | Toast: generic |
| **EMAIL_EXISTS** | Fields preserved | Enabled | Inline "Already registered" | None |
| **SUCCESS** | Form disappears | — | — | Redirect spinner |
| **ALREADY_AUTH** | Not rendered | — | — | Router guard |

---

## 3. Component Architecture

### 3.1 Component Tree

```
AuthPage.vue (orchestrator)
├── AuthHeader (logo + language switcher)
├── <Transition> wrapper
│   ├── LoginForm.vue (mode='login')
│   │   ├── InputText (email) + pi pi-envelope
│   │   ├── Password (password) + eye toggle
│   │   ├── Checkbox (remember me)
│   │   ├── Button (submit — emerald primary)
│   │   └── ModeSwitchLink ("Register now!")
│   └── RegisterForm.vue (mode='register')
│       ├── InputText (email)
│       ├── Password (password) + strength + eye toggle
│       ├── Password (confirm) no strength
│       ├── Checkbox (remember me)
│       ├── Button (submit)
│       └── ModeSwitchLink ("Log In")
├── AuthSidebar.vue (info panel, right side)
│   ├── Heading (login: "Welcome Back" / register: "Join ResumAIner")
│   ├── Body text
│   └── Feature bullets (register only)
└── Toast (PrimeVue Toast component)
```

### 3.2 Component Responsibilities

| Component | Props | Emits | Responsibilities |
|---|---|---|---|
| **AuthPage** | — | — | Mode state; URL sync; transition orchestration; auth-success → router.replace; toast management |
| **LoginForm** | `mode` | `@auth-success`, `@switch-mode`, `@submitting` | Email/password/remember-me inputs; validation; submit → API; inline errors; mode switch link |
| **RegisterForm** | `mode` | `@auth-success`, `@switch-mode`, `@submitting` | Email/password/confirm inputs; strength indicator; match validation; 409 handling; mode switch link |
| **AuthSidebar** | `mode` | — | Content varies per mode; cross-fade animation on switch |
| **AuthHeader** | — | — | Logo (router-link to `/`), language switcher (SelectButton EN/RU) |
| **AuthInterceptor** | — | — | Axios: attach token on request; catch 401 → clear token → redirect `/app` |

---

## 4. API Contracts

### 4.1 POST /api/auth/login

**Payload:**
```json
{
  "email": "string — validated email format",
  "password": "string — min 8 chars"
}
```

**Success (200):**
```json
{
  "ok": true,
  "token": "jwt-or-session-string",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

**Failures:**
```json
// 400 — validation error (generic)
{ "ok": false, "error": "Validation failed", "code": "VALIDATION_ERROR", "retryable": false }

// 401 — invalid credentials (SAME message for wrong email OR wrong password)
{ "ok": false, "error": "Invalid email or password", "code": "AUTH_FAILED", "retryable": true }

// 403 — user blocked
{ "ok": false, "error": "Your account has been blocked. Contact support.", "code": "USER_BLOCKED", "retryable": false }

// 403 — user inactive
{ "ok": false, "error": "Your account is inactive. Contact support.", "code": "USER_INACTIVE", "retryable": false }

// 500 — server error
{ "ok": false, "error": "Internal server error", "code": "SERVER_ERROR", "retryable": true }
```

Timeout: 15s

### 4.2 POST /api/auth/register

**Payload:**
```json
{
  "email": "string — validated email format",
  "password": "string — min 8 chars",
  "confirmPassword": "string — must match password"
}
```

**Success (201):**
```json
{
  "ok": true,
  "token": "jwt-or-session-string",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

**Failures:**
```json
// 400 — validation error
{ "ok": false, "error": "Validation failed", "code": "VALIDATION_ERROR", "retryable": false }

// 409 — email already registered
{ "ok": false, "error": "This email is already registered", "code": "EMAIL_EXISTS", "retryable": false }

// 500 — server error
{ "ok": false, "error": "Internal server error", "code": "SERVER_ERROR", "retryable": true }
```

Timeout: 15s

### 4.3 Token Storage Rule

| Remember Me | Storage | Behavior |
|---|---|---|
| Checked | `localStorage` | Survives browser tab close |
| Unchecked | `sessionStorage` | Cleared when tab closes |

---

## 5. Visual Design Summary

### 5.1 Visual Design Summary

For the **complete visual specification** with exact hex colors, spacing grid, typography per element, animation timing, and mobile breakpoints, see:

> **`docs/superpowers/specs/2026-06-02-auth-page-visual-design.md`**

### 5.2 Key Visual Decisions

| Aspect | Decision |
|---|---|
| **Page layout** | Standalone — no Landing header, no app shell, no footer |
| **Card layout** | Centered 1060px card, 50/50 two-column split |
| **Left column** | Logo + lang switcher + form fields + submit + mode link |
| **Right column** | Info panel `#EEF4FF` bg — content varies by mode |
| **Animation** | Staggered slide-vertical + fade (0–400ms delays per field) |
| **Easing** | `cubic-bezier(0.16, 1, 0.3, 1)` — emphasized ease-out |
| **Mobile** | Right panel → compact header strip (80-100px), form full width |
| **Links** | Blue `#2F6BFF`, hover `#1A54D9` |
| **Primary button** | Emerald `#0F9D7A` → hover `#0C8467` |
| **Errors** | Red `#C2410C`, error bg `#FFF5F0`, border `#FDDCC8` |

### 5.3 PrimeVue Component Mapping

| Element | Component | Notes |
|---|---|---|
| Email | `<InputText>` | With `pi pi-envelope` slot |
| Password | `<Password>` | `:feedback="false"` on login, strength on register |
| Confirm | `<Password>` | `:feedback="false"`, register only |
| Remember me | `<Checkbox>` | `v-model` boolean |
| Submit | `<Button :loading>` | Emerald primary, `iconPos="left"` for spinner |
| Lang switch | `<SelectButton>` | EN / RU options |
| Toast | `<Toast>` | PrimeVue Toast plugin |

---

## 6. Edge Cases Matrix

| # | Edge Case | Expected Behavior |
|---|---|---|
| EC-01 | Double submit (rapid clicks) | Button disabled after first click (PRG). Server-side idempotency if needed. |
| EC-02 | Network timeout (15s) | Toast: "Connection timed out. Please try again." Button re-enabled, fields preserved. |
| EC-03 | Backend 500 | Toast: "Something went wrong. Please try again later." No stack trace. |
| EC-04 | Already authenticated → `/app` | Router guard redirects to /user-home or /admin-home. AuthPage never renders. |
| EC-05 | Token expired during session | AuthInterceptor catches 401, clears token, redirects `/app`, toast "Session expired." |
| EC-06 | Back button after success | `router.replace` prevents back-navigation to auth form. Back → Landing page. |
| EC-07 | Refresh on `/app` while authenticated | Router guard detects token, redirects immediately. |
| EC-08 | Concurrent registration same email | Database unique constraint → first succeeds, second gets 409. |
| EC-09 | Invalid token manipulation | Backend verifies signature → 401 → AuthInterceptor handles redirect. |
| EC-10 | Cyrillic email | Spring MVC UTF-8 config + frontend regex allows Unicode. |
| EC-11 | Special chars in password | BCrypt handles all chars. Frontend allows printable (min 8). |
| EC-12 | Empty form submit | All required field errors show inline simultaneously. No API call. |
| EC-13 | Paste into password field | Allowed. Normal validation runs after paste. |
| EC-14 | Change password after confirm typed | Re-validate confirm field in real-time on password change. |
| EC-15 | Slow network (8s response) | Button disabled + spinner for full duration. User cannot resubmit. |
| EC-16 | Browser tab close (no remember-me) | sessionStorage cleared. Next visit requires login. |

---

## 7. Decisions and Rationale

| ID | Decision | Rationale | Source |
|---|---|---|---|
| D-01 | Single page with Login/Register toggle (links) | UX best practice for simple auth; avoids route switch overhead | CodePen reference, user approval |
| D-02 | Vue SPA (not Thymeleaf) | Auth is part of app flow, not Landing | Architecture, user approval |
| D-03 | Minimal/standalone auth page (no header, no footer) | Top SaaS pattern; user mental mode switches to transaction; avoids Thymeleaf+Vue header duplication | Brand Guardian, Context7 research, user approval |
| D-04 | Light Enterprise SaaS design DNA | Consistent redesign direction across product | REDESIGN_NOTES, light_design_dna.md |
| D-05 | Email field (not username) | Wireframe 5.3 shows email | wireframes_detailed_description.md |
| D-06 | "Remember me" checkbox | UX best practice | Context7, user approval |
| D-07 | Password visibility toggle | UX best practice | User approval |
| D-08 | Dual validation: Zod + Jakarta | Constitution NFR-019 | Constitution |
| D-09 | PRG pattern: button disabled + spinner | Constitution NFR-017 | Constitution |
| D-10 | Generic auth error "Invalid email or password" | Security: no disclosure of whether email exists | Constitution V, wireframes |
| D-11 | Already authenticated → redirect by role | Wireframe confirmed behavior | wireframes_detailed_description.md |
| D-12 | Staggered slide animation (CodePen approach) | Polished UX, visual continuity | CodePen reference, user approval |
| D-13 | PrimeVue Form + Zod resolver | PrimeVue 4 recommended approach | Context7 / PrimeVue docs |
| D-14 | Right panel cross-fades independently | Better UX than sliding both sides simultaneously | UI Designer |
| D-15 | Logo links to `/` (Landing Page) | Standard navigation pattern; implicit "Back to Home" | Wireframes, common practice |
| D-16 | User Home / Admin Home as empty placeholders | Out of scope for this batch | User approval |
| D-17 | 50/50 card split with right info panel | Balanced layout: form + brand value side by side | UI Designer, CodePen adapted |

---

## 8. Open Questions

| # | Question | Why It Matters | Owner | Status |
|---|---|---|---|---|
| Q-01 | JWT vs session-based auth? | Affects token format, storage, and backend architecture | Architect | Deferred |
| Q-02 | Exact `/user-home` and `/admin-home` route paths | Need to define Vue Router configuration | Architect | Deferred |
| Q-03 | Password strength requirements beyond min 8 chars | Wireframes say "password required" but not complexity | User | Needs clarification |
| Q-04 | "Remember me" default state | Checked or unchecked? | User | Assumed: unchecked (security) |
| Q-05 | Exact i18n key names for all auth strings | Need to design i18n resource file structure | Architect | Implementation detail |
| Q-06 | Right panel decorative shapes | Abstract SaaS SVG shapes — exact design TBD | Designer | Creative freedom |

---

## 9. QA Observable States

| ID | Test Scenario | Expected Result |
|---|---|---|
| QA-01 | Unauthenticated → `/app` | Login form renders, no redirect |
| QA-02 | Authenticated USER → `/app` | Redirect to `/user-home` |
| QA-03 | Authenticated ADMIN → `/app` | Redirect to `/admin-home` |
| QA-04 | Click "Register now!" | Animation plays, Register form appears, URL: `?mode=register` |
| QA-05 | Click "Log In" (on Register) | Reverse animation, Login appears, URL: `?mode=login` |
| QA-06 | Valid login | Spinner → redirect to home |
| QA-07 | Invalid email format | Inline error before submit |
| QA-08 | Wrong password | "Invalid email or password", password cleared |
| QA-09 | Non-existent email | Same generic error as wrong password |
| QA-10 | Blocked user login | Toast: blocked message |
| QA-11 | Successful register | Redirect to `/user-home`, user in DB |
| QA-12 | Duplicate email register | "Email already registered", link to Login |
| QA-13 | Password mismatch | Inline error on confirm field |
| QA-14 | Network timeout | Toast after 15s, button re-enabled |
| QA-15 | Server error (500) | Generic toast, no stack trace |
| QA-16 | Double click submit | One API call, button disabled |
| QA-17 | Password eye toggle | Toggle show/hide, icon changes |
| QA-18 | Remember me checked | Token in localStorage |
| QA-19 | Remember me unchecked | Token in sessionStorage |
| QA-20 | EN/RU switch while typing | Text changes, field values preserved |
| QA-21 | Logo click | Navigates to `/` |
| QA-22 | Submit with empty fields | All required errors simultaneously |
| QA-23 | Expired token + API call | AuthInterceptor → clear token → redirect `/app` + toast |
| QA-24 | Loading state (slow network) | Button spinner, fields dimmed, no resubmit |
| QA-25 | Mobile view (< 768px) | Single column, right panel → header strip |

---

## 10. Designer Handoff Notes

### 10.1 Where to Start

1. Read this document for **complete context** (workflow, states, behavior, constraints)
2. Read `2026-06-02-auth-page-visual-design.md` for **exact visual spec** (colors, typography, spacing, animations)
3. Read `tempfiles/light_design_dna.md` for **design system reference** (broader token set, rationale)
4. Read `tempfiles/auth_page_reference.{html,css,js}` for **animation approach** (adapt not copy)

### 10.2 Key Creative Freedoms

| Element | Constraint | Freedom |
|---|---|---|
| Right panel decorative shapes | Must be SaaS-appropriate, no AI-hype | Abstract SVG curves/blobs, position TBD |
| Right panel feature bullets (Register) | Content listed in spec, icons checked | Number of bullets, exact wording |
| Logo placement | Top-left of form column | Exact sizing, margin alignment |
| Mobile right panel collapse behavior | Must become compact header | Exact height, content priority |
| Focus ring style | Color and radius specified | Slight artistic variation allowed |

### 10.3 What NOT to Include

- ❌ Landing page navigation (Home, Features, How it Works, FAQ)
- ❌ Landing page footer
- ❌ App shell sidebar or top navigation
- ❌ AI-hype imagery (robots, brains, sparkles)
- ❌ Dark gradients or neon effects
- ❌ Stock photos

### 10.4 Deliverables Expected from Designer

1. High-fidelity prototype (Figma/XD/Sketch) at 2560×1440
2. Mobile mockup at 390×844 (iPhone 14)
3. Tablet mockup at 1024×1366
4. Animation prototype or keyframe documentation
5. Component states: default, hover, focus, error, loading, disabled
6. Token/style guide export for developer handoff

### 10.5 Files for Reference

| File | Content |
|---|---|
| `tempfiles/light_design_dna.md` | Full design system with all tokens |
| `tempfiles/REDESIGN_NOTES.md` | Migration from Warm Sand to Light SaaS |
| `tempfiles/auth_page_reference.html` | CodePen reference — animation approach |
| `tempfiles/auth_page_reference.css` | CodePen reference CSS (do NOT copy — use design DNA) |
| `tempfiles/auth_page_reference.js` | CodePen reference JS — toggle logic |
| `ba-docs/docs/05_ui-ux/wireframes_detailed_description.md` | Wireframes section 5.3 (Login) and 5.2 (Register) |
| `ba-docs/docs/05_ui-ux/wireframe_field_requirements.md` | Field validation rules |

---

*This specification was prepared by Web Pages Design Orchestrator in collaboration with Workflow Architect, UI Designer, and Brand Guardian subagents. Visual details refined via Context7 documentation search and CodePen reference analysis.*
