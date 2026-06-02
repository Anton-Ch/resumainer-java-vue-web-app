# Web Pages Prototype Brief

## 1. Prototype target
Single Vue SPA auth page with Login/Register toggle (link-switching), following reference CodePen approach. Scope: auth form card + sidebar info panel, all states, redirect to placeholder home pages. User Home and Admin Home are not in scope — empty placeholder pages only.

## 2. Business context
ResumAIner is an AI-assisted resume adaptation platform. The Login page is the entry point for returning users and admins. It is part of the public (Visitor) flow, alongside Landing Page and Register. The project is currently applying a "Light Enterprise SaaS" visual redesign (previously "Warm Sand").

## 3. User roles/personas
- **Visitor (unauthenticated)**: Primary user of Login. Needs to authenticate to access the app.
- **Registered User**: After login → redirected to User Home.
- **Admin**: After login → redirected to Admin Home.

## 4. Primary user goals
- Enter credentials (email + password) quickly.
- Know if login succeeded or failed.
- Navigate to Register if no account exists.
- Switch interface language (EN/RU).

## 5. Pages/screens in scope
**Single auth page** with two modes:
- Login mode (default)
- Register mode (toggled via link)

**Placeholder pages (empty shells):**
- User Home page (empty, just title "User Home" + logout)
- Admin Home page (empty, just title "Admin Home" + logout)

## 6. Pages/screens out of scope
- Landing Page (handled separately via Thymeleaf redesign)
- User Home content, Admin Home content
- Password recovery/reset (not in MVP wireframes)
- Profile, Generate Resume, Resume Review, Admin pages

## 7. End-to-end workflows
WF1: First-time user generates resume (steps 1-3: Landing → Register → login)
WF2: Returning user logs in → User Home
WF6: Admin logs in → Admin Home

## 8. Screen-by-screen requirements

### Screen: Login (5.3 Login 2.1)
- **Purpose:** Authenticate existing user by email + password.
- **Primary actions:** Enter email, enter password, click Log in.
- **Secondary actions:** Switch to Register page, switch language.
- **Required content:** Logo, Language Switcher, "Login" page title, Email field, Password field, Register link, Login button.
- **Required components:** Text input (email), password input (with visibility toggle?), submit button, link.
- **States:**
  - Default: Clean form with placeholder text.
  - Loading: Button disabled, spinner.
  - Empty: Not applicable — form is empty by default.
  - Validation error: Inline errors for empty/invalid fields.
  - Auth error: "Invalid email or password" message (no "user not found" disclosure).
  - Success: Redirect to appropriate Home page.
  - Already authenticated: Redirect to appropriate Home page (no form shown).
  - Mobile: Responsive stacking, full-width inputs.
  - Desktop: Centered card layout.
- **Validation rules:**
  - Email required, valid format.
  - Password required, non-empty.
  - Backend is authoritative (dual validation).
- **Trust/security cues:** Error messages must not disclose whether email exists. BCrypt protection. Button disabled after click (PRG).
- **Analytics/events:** Login attempt (success/fail).
- **Accessibility notes:** Labels, focus management, error announcements.

## 9. UX principles for this prototype
- Minimal friction: 2 fields only.
- Clear error feedback.
- Consistent with Light Enterprise SaaS visual style.

## 10. Visual/design direction
Light Enterprise SaaS (from REDESIGN_NOTES / light_design_dna.md):
- Background: `#F6F7FB`
- Card surface: `#FFFFFF`
- Primary text: `#10233F`
- Emerald primary: `#0F9D7A` for buttons
- Radius: 14px buttons, 22px cards
- Shadows: soft `rgba(16,35,63,0.06-0.10)`
- Fonts: Manrope (headings), Inter (body)

## 11. Brand requirements
- No AI-hype visuals (robots, brains, sparkles)
- Professional, enterprise-credible look
- Logo preserved (existing SVG)

## 12. Frontend implementation notes
- Vue 3 + Vite + PrimeVue for the SPA
- Login is a Vue page (not Thymeleaf — Landing Page is Thymeleaf)
- Dual validation: Vuelidate + Jakarta Validation
- PRG pattern: button disabled after click
- i18n: messages_en.properties / messages_ru.properties

## 13. Backend/API/state implications
- POST to auth endpoint with email + password
- Session-based or JWT-based auth (to be decided)
- AuthInterceptor checks session/token; redirects to login if not authenticated
- Already-authenticated users bypass login form (redirect to home)
- 3 states: unauthenticated, authenticated (user), authenticated (admin)

## 14. Prototype deliverables expected
_To be confirmed with user._

## 15. Acceptance checklist before handing to design prototyping
- [ ] Scope is clear
- [ ] Screens are listed
- [ ] User flows are mapped
- [ ] Edge states are documented
- [ ] Business constraints are included
- [ ] Brand direction is included
- [ ] Technical constraints are included
- [ ] Open questions are either resolved or explicitly documented
