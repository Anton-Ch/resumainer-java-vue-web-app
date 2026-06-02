# ResumAIner Vue SPA Design DNA

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Document Type:** Vue SPA Design DNA / Shared Visual Foundation
**Status:** Approved for Vue SPA implementation
**Applies To:** All Vue SPA pages (Auth, User Home, Admin Home, My Profile, Generate Resume, Resume Review)

---

## 1. Purpose

This document defines the visual and interaction DNA for the **ResumAIner Vue SPA** authenticated application.

It should be used to keep visual consistency between:

- Auth Page (Login / Register);
- User Home (resume list, quick actions);
- My Profile (structured career data editing);
- Generate Resume (vacancy context + generation settings);
- Resume Review (document preview + editing);
- Admin Home / Users / Resumes / AI Models;
- any future Vue SPA page.

This document is not a screen-by-screen UI specification.
It defines the reusable design language, visual principles, design tokens, component rules, PrimeVue integration rules, and consistency rules for all Vue SPA pages.

---

## 2. Current Approved Direction

### Light Enterprise SaaS with premium animated auth entry

The Vue SPA follows a **Light Enterprise SaaS** visual direction:

- clean structured layouts;
- premium but restrained animated Auth Page;
- calm professional color palette;
- readable typography for English and Russian;
- controlled, purposeful motion;
- PrimeVue components themed to match ResumAIner identity.

### Visual Source of Truth

The **approved Auth Page** is the strongest current reference for the Vue SPA design language.

Key design decisions from the Auth Page that apply to all Vue SPA pages:

- 50/50 split layout pattern is auth-specific; other pages do not copy it, but they inherit its tokens, card style, input style, button style, language switcher style, error style, and motion principles.
- Diagonal animated gradient panel is auth-specific; other pages use static surface colors from the same palette.
- Link-based Login/Register toggle is auth-specific; other pages use standard navigation.
- Form slide + fade and staggered field entrance set the motion baseline for all SPA interactions.
- The stable card container pattern (white surface card on canvas) is reused across user-facing pages.
- Info panel cross-fade pattern may be reused for contextual guidance panels.

---

## 3. Design Principle

### Clean canvas, precise controls.

The Vue SPA should feel like a well-organized professional tool:

- **Clean canvas** — `#F6F7FB` background, generous whitespace, no decoration for its own sake;
- **Precise controls** — clear labels, visible states, predictable interactions, calm feedback.

This principle is not identical to the Landing Page's "Warm background, sharp structure."
The Vue SPA is cooler, more application-focused, and slightly denser than the marketing Landing Page.

---

## 4. Visual Keywords

What the Vue SPA should feel like:

- clean;
- structured;
- professional;
- trustworthy;
- modern;
- calm;
- enterprise-ready;
- portfolio-quality;
- polished but not flashy.

---

## 5. What the Vue SPA Should Feel Like

The authenticated user experience should feel:

- **calm** — no noisy backgrounds, no flashing elements, no excessive motion;
- **controlled** — the user always understands what happened and what to do next;
- **professional** — precise spacing, consistent styling, no rough edges;
- **trustworthy** — clear data states, explicit actions, no hidden side effects;
- **modern** — fresh palette, clean cards, subtle shadows, premium motion.

---

## 6. What the Vue SPA Must Avoid

Do not introduce:

- dark AI startup visual style;
- neon/cyberpunk gradients or glow;
- generic Bootstrap or default PrimeVue look;
- stock illustrations of any kind;
- robots, brains, sparkles, magic wand icons;
- childish gamification (streaks, badges, confetti);
- cluttered dashboards with too many competing modules;
- random decorative gradients on cards.

---

## 7. Color System

### Vue SPA Palette

```css
:root {
  --vue-bg-canvas: #F6F7FB;
  --vue-bg-surface: #FFFFFF;
  --vue-bg-subtle: #FBFCFE;
  --vue-bg-control: #F7FAFE;

  --vue-text-primary: #10233F;
  --vue-text-secondary: #5D718B;
  --vue-text-muted: #8091A7;
  --vue-text-inverse: #FFFFFF;

  --vue-border-default: #D8DEE9;
  --vue-border-soft: #E3E8F0;
  --vue-border-subtle: #E8EDF4;
  --vue-border-control: #E5EAF2;

  --vue-accent-primary: #0F9D7A;
  --vue-accent-primary-hover: #0C8467;
  --vue-accent-primary-active: #0A6F56;

  --vue-accent-blue: #2F6BFF;
  --vue-accent-blue-hover: #1A54D9;
  --vue-accent-bg-blue: #EEF4FF;

  --vue-accent-violet: #7C3AED;
  --vue-accent-bg-violet: #F5F3FF;

  --vue-accent-warning: #D97706;
  --vue-accent-bg-warning: #FFF7ED;

  --vue-accent-error: #C2410C;
  --vue-accent-bg-error: #FFF5F0;
  --vue-accent-border-error: #FDDCC8;

  --vue-accent-success: #0F9D7A;
  --vue-accent-bg-success: #F2FFF9;
}
```

### Semantic Color Usage

| Token | Purpose |
|---|---|
| `--vue-accent-primary` | Main action buttons, active states, success states |
| `--vue-accent-blue` | Links, focus indicators, language switch active state, info |
| `--vue-accent-violet` | AI / system metadata only — restrained, never dominant |
| `--vue-accent-warning` | Warning indicators, risky actions |
| `--vue-accent-error` | Error states, destructive actions, validation failures |
| `--vue-accent-success` | Success states (same as primary) |

### Color Rules

- Emerald (primary) = action, completion, positive.
- Blue = navigation, information, focus.
- Violet = AI-only metadata — use sparingly.
- Amber = warning only.
- Red/orange = errors and destructive.
- Never use pure black for text. Never use neon colors.

---

## 8. Typography System

### Fonts

```css
--vue-font-heading: 'Manrope', 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
--vue-font-body: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
--vue-font-mono: 'JetBrains Mono', 'SF Mono', ui-monospace, Menlo, monospace;
```

### Typography Rules

| Role | Font | Weight | Size |
|---|---|---|---|
| Page title | Manrope | 700 | 28–32px |
| Section title | Manrope | 600 | 18–22px |
| Card title | Manrope | 600 | 16px |
| Form label | Inter | 500 | 14px |
| Body text | Inter | 400 | 14–16px |
| Table text | Inter | 400 | 13–14px |
| Meta / captions | Inter | 400 | 12–13px |
| Chips / badges | Inter | 500 | 12px |

### Typography Rules

- labels visible and readable at all times;
- no hidden labels in forms;
- headings strong but calm — no playful styling;
- no decorative or handwritten fonts;
- line-height sufficient for both English and Russian text (1.5 minimum for body);
- Monospace for code, IDs, technical data only.

---

## 9. Spacing System

```css
:root {
  --vue-space-1: 4px;
  --vue-space-2: 8px;
  --vue-space-3: 12px;
  --vue-space-4: 16px;
  --vue-space-5: 20px;
  --vue-space-6: 24px;
  --vue-space-8: 32px;
  --vue-space-10: 40px;
  --vue-space-12: 48px;
  --vue-space-16: 64px;
}
```

### Spacing Rules

- Card padding: 20–24px (user-facing) / 16–20px (admin).
- Form section gap: 24px between field groups.
- Table cell padding: 12–16px vertical, 16–20px horizontal.
- Page content padding top: 24–32px below app header.
- Modal padding: 24px.

---

## 10. Radius and Shadows

### Radius

```css
:root {
  --vue-radius-sm: 6px;
  --vue-radius-md: 8px;
  --vue-radius-lg: 12px;
  --vue-radius-xl: 16px;
}
```

| Radius | Use |
|---|---|
| `--vue-radius-sm` | chips, small badges, tags |
| `--vue-radius-md` | buttons, inputs, selects |
| `--vue-radius-lg` | cards, panels, modals, form sections |
| `--vue-radius-xl` | large panels, CTA areas, auth card |

### Shadows

```css
:root {
  --vue-shadow-card: 0 1px 3px rgba(16, 35, 63, 0.06), 0 1px 2px rgba(16, 35, 63, 0.04);
  --vue-shadow-elevated: 0 4px 12px rgba(16, 35, 63, 0.08);
  --vue-shadow-modal: 0 20px 60px rgba(16, 35, 63, 0.14);
  --vue-shadow-focus: 0 0 0 3px rgba(47, 107, 255, 0.2);
}
```

Shadow rules:
- cards get subtle shadow + border;
- elevated elements (dropdowns, tooltips) get medium shadow;
- modals get the strongest shadow;
- focus ring is blue, not emerald.

---

## 11. Layout System

### Page Layout

All Vue SPA pages follow this structure:

```
┌─────────────────────────────────────┐
│ App Header (top bar)                │
├──────────────────┬──────────────────┤
│ Sidebar / Nav    │ Main Content     │
│ (optional)       │                  │
│                  │  ┌────────────┐  │
│                  │  │ Page Title  │  │
│                  │  │ + Action    │  │
│                  │  ├────────────┤  │
│                  │  │ Content    │  │
│                  │  │ Card(s)    │  │
│                  │  └────────────┘  │
└──────────────────┴──────────────────┘
```

- Top bar: app logo/brand, page context, language switcher, user menu / logout.
- Side navigation: role-aware (User vs Admin), collapsible on mobile.
- Main content: white card panels on canvas background.

### Width

- Content max-width: 1200px.
- Centered with horizontal padding on narrow screens.

---

## 12. App Shell Direction

### Top Bar

- height: 56–60px;
- background: white surface;
- bottom border: soft border;
- left: logo or app name;
- right: language switcher + logout/user menu.

### Sidebar (User)

- width: 220–240px desktop, full-width overlay mobile;
- navigation items: Home, My Profile, Generate Resume, Saved Resumes;
- active item highlighted with emerald or blue indicator;
- role badge (User / Admin) at bottom if applicable.

### Sidebar (Admin)

- same shell as user, different navigation items;
- navigation: Admin Home, Users, Resumes, AI Models;
- may be slightly more compact.

### Page Content Area

- padding: 24–32px (desktop), 16–20px (mobile);
- content in white cards with unified radius and shadow;
- page title + primary action(s) in a header row above content.

---

## 13. Auth Page Pattern

### Layout

The Auth Page is the only page in the Vue SPA that uses a full 50/50 split layout.

- Left panel: gradient animated panel (diagonal emerald-to-blue gradient) with product messaging.
- Right panel: white card container with Login/Register forms.
- No top bar navigation.
- No sidebar.

### Auth-Specific Elements

- Link-based toggle between Login and Register (no tab buttons).
- Form slide + fade on toggle.
- Staggered field entrance animation.
- Info panel cross-fade for product context switching.
- Language switcher in top-right corner inside the white card area.

### Inheritance to Other Pages

Other pages inherit from Auth Page:

- same input style, button style, error style, language switcher style;
- same motion easing and timing;
- same card container principles (white surface, radius, shadow);
- same form layout spacing rules.

Other pages do NOT inherit:

- 50/50 split layout;
- animated gradient panel;
- link-based form toggle.

---

## 14. User Pages Pattern

### User Pages

- Home (resume table / recent activity);
- My Profile (structured career data editing);
- Generate Resume (vacancy + settings form);
- Resume Review (document preview + editing).

### Design Pattern

- page header row with title and primary action;
- content in white card(s) on canvas background;
- consistent spacing between sections;
- enough whitespace for comfortable reading;
- primary action buttons visible and clear.

### User Home

- table or list of saved resumes;
- search and filter controls above table;
- empty state with CTA when no resumes exist;
- action buttons per row: Download PDF, Copy public link, Delete.

### My Profile

- section navigation (left or top tabs);
- card-based section editing;
- repeatable sections (work experience, projects) with Add/Edit/Delete;
- save action clear and always accessible.

### Generate Resume

- form divided into clear visual blocks;
- vacancy description area visually prominent;
- language + adaptation level clearly selectable;
- primary action at bottom (or sticky footer).

### Resume Review

- document-like preview area (paper surface);
- editable sections within the preview;
- cover letter accessible in same flow;
- Save & Create as primary action;
- post-save: PDF download + public link copy.

---

## 15. Admin Pages Pattern

### Admin Pages

- Admin Home (summary / overview);
- Users (table + filters);
- Resumes (table + filters);
- AI Models (table + CRUD).

### Design Pattern

- more compact than user pages;
- table-focused layouts;
- row action buttons (Edit, Deactivate, Delete);
- dangerous actions require confirmation dialog;
- API keys always masked.

### Table Pattern

- PrimeVue DataTable with consistent styling;
- clear pagination;
- search/filter row above table;
- role/status chips visible per row;
- empty state when no records.

---

## 16. Forms

### Form Design Rules

- one clear section at a time;
- labels always visible (not placeholder-only);
- required fields clearly marked with `*`;
- help text near complex inputs;
- errors shown inline near the field;
- backend errors mapped to fields where possible;
- form layout: spacious for user-facing, compact for admin.

### Form Group Pattern

```html
<div class="form-group">
  <label class="form-label" for="fieldId">Field name *</label>
  <input id="fieldId" class="form-input" ... />
  <span class="form-error" v-if="error">Error message</span>
  <span class="form-hint" v-if="hint">Helpful hint</span>
</div>
```

---

## 17. Buttons

### Primary Button

- background: `--vue-accent-primary`;
- hover: `--vue-accent-primary-hover`;
- active: `--vue-accent-primary-active`;
- text: white;
- radius: `--vue-radius-md`;
- font-weight: 600;
- height: 38–40px (default), 44–48px (large);
- padding: 0 20–24px.

### Secondary Button

- background: transparent or white surface;
- border: `--vue-border-default`;
- text: `--vue-text-primary`;
- hover: subtle background or border darkening;
- radius: `--vue-radius-md`;
- font-weight: 500.

### Text / Ghost Button

- no background or border;
- text: `--vue-text-secondary`;
- hover: `--vue-text-primary`;
- used for low-emphasis actions.

### Destructive Button

- background: `--vue-accent-error`;
- text: white;
- confirmation required for data-affecting actions.

### Button States

Every button must have: default, hover, active, focus-visible, disabled.

---

## 18. Inputs

### Input Style

```css
.vue-input {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-control);
  border-radius: var(--vue-radius-md);
  padding: 10px 12px;
  font-size: 14px;
  color: var(--vue-text-primary);
  transition: border-color var(--vue-motion-fast), box-shadow var(--vue-motion-fast);
}
.vue-input:focus {
  border-color: var(--vue-accent-blue);
  box-shadow: 0 0 0 3px rgba(47, 107, 255, 0.12);
  outline: none;
}
.vue-input.error {
  border-color: var(--vue-accent-error);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.10);
}
.vue-input:disabled {
  background: var(--vue-bg-subtle);
  color: var(--vue-text-muted);
  cursor: not-allowed;
}
```

### Input Types

- InputText — text, email, password;
- Textarea — multi-line text;
- Select — dropdown options;
- Checkbox — single toggle;
- MultiSelect — multiple selection.

All PrimeVue input components should be overridden to match the above style.

---

## 19. Cards and Panels

### Card Style

```css
.vue-card {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-lg);
  box-shadow: var(--vue-shadow-card);
  padding: 20px 24px;
}
```

### Card Variants

| Variant | Purpose |
|---|---|
| Default card | content sections, form blocks |
| Elevated card | hover states, selection, active editing |
| Flat card | inside other cards, no shadow needed |
| Panel card | grouped settings, admin sections |

---

## 20. Tables

### Table Style

- PrimeVue DataTable with ResumAIner tokens;
- header background: `--vue-bg-subtle`;
- header text: `--vue-text-secondary`, 13px, uppercase optional;
- row hover: subtle background highlight;
- alternating row stripes: optional, subtle;
- pagination: consistent with app style;
- empty state shown when no records.

### Table Token Mapping

| PrimeVue Token | ResumAIner Value |
|---|---|
| `--p-datatable-header-cell-background` | `--vue-bg-subtle` |
| `--p-datatable-row-hover-background` | `--vue-bg-subtle` |
| `--p-datatable-border-color` | `--vue-border-soft` |
| `--p-paginator-button-background` | transparent |

---

## 21. Chips and Badges

### Chip Style

```css
.vue-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  background: var(--vue-bg-subtle);
  color: var(--vue-text-secondary);
  border: 1px solid var(--vue-border-soft);
}
```

### Chip Variants

| Variant | Purpose |
|---|---|
| Default chip | neutral labels, metadata |
| Primary chip | active status, matched skills |
| Success chip | saved, completed, public |
| Warning chip | draft, pending |
| Error chip | failed, hidden |
| Role chip | User / Admin with --vue-accent-violet |

---

## 22. Alerts, Toasts, and Errors

### Alert Style

```css
.vue-alert {
  padding: 12px 16px;
  border-radius: var(--vue-radius-md);
  font-size: 14px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border: 1px solid transparent;
}
```

| Alert Type | Background | Border |
|---|---|---|
| Success | `--vue-accent-bg-success` | `--vue-accent-primary` |
| Error | `--vue-accent-bg-error` | `--vue-accent-border-error` |
| Warning | `--vue-accent-bg-warning` | `--vue-accent-warning` |
| Info | `--vue-accent-bg-blue` | `--vue-accent-blue` |

### Toast

PrimeVue Toast component, themed with ResumAIner tokens.
Default position: top-right.
Auto-dismiss: 4–5 seconds for success/info, manual dismiss for errors.

### Error States

- Inline errors: shown directly below the field, red text + red border.
- Form-level errors: alert at top of form.
- Page-level errors: alert banner at top of page content.
- API errors: readable messages, not technical stack traces.

---

## 23. Empty States

### Empty State Pattern

```html
<div class="vue-empty">
  <div class="vue-empty-icon"><!-- minimal SVG --></div>
  <h3 class="vue-empty-title">Title</h3>
  <p class="vue-empty-desc">Helpful description with next action guidance.</p>
  <button class="vue-btn vue-btn-primary">Action CTA</button>
</div>
```

### Empty State Examples

| Page | Empty Title | Description | CTA |
|---|---|---|---|
| User Home | No resumes yet | Create your first tailored resume from your profile and a vacancy description. | Create resume |
| My Profile | Your profile is still empty | Add your experience, education, projects, and skills before preparing a resume. | Complete profile |
| Admin AI Models | No AI models configured | Add an AI model before allowing resume preparation. | Add AI model |

---

## 24. Language Switcher / i18n UI

### Component Style

Small segmented control in the top bar:

```css
.vue-lang-switch {
  display: inline-flex;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--vue-border-soft);
  border-radius: 6px;
  overflow: hidden;
}
.vue-lang-switch button {
  padding: 4px 10px;
  background: transparent;
  color: var(--vue-text-muted);
  cursor: pointer;
  border: none;
  transition: background var(--vue-motion-fast), color var(--vue-motion-fast);
}
.vue-lang-switch button.active {
  background: var(--vue-accent-blue);
  color: var(--vue-text-inverse);
}
```

### i18n Behavior Rules

1. Detect browser locale on first visit.
2. If locale starts with `ru`, use Russian.
3. For any other locale, use English.
4. Manual language choice overrides browser locale.
5. Manual choice persists in localStorage.
6. Switching language must not clear form values.
7. All user-facing strings must be externalized.
8. Product name `ResumAIner` must not be translated.
9. Russian text must be natural, not literal machine translation.
10. Tone must remain professional, calm, and practical.

### Recommended File Structure

```
src/locales/en/common.json
src/locales/en/auth.json
src/locales/en/home.json
src/locales/en/profile.json
src/locales/en/generate.json
src/locales/en/resume.json
src/locales/en/admin.json

src/locales/ru/common.json
src/locales/ru/auth.json
src/locales/ru/home.json
src/locales/ru/profile.json
src/locales/ru/generate.json
src/locales/ru/resume.json
src/locales/ru/admin.json
```

Or a single file approach:

```
src/i18n/messages.js  ← exports { en: { ... }, ru: { ... } }
```

---

## 25. Animation and Motion

### Motion Philosophy

- subtle but premium;
- purposeful — never decorative;
- short and precise;
- respects `prefers-reduced-motion`.

### Motion Tokens

```css
:root {
  --vue-motion-fast: 150ms;
  --vue-motion-base: 240ms;
  --vue-motion-slow: 420ms;
  --vue-motion-panel: 900ms;
  --vue-ease-standard: cubic-bezier(0.2, 0, 0, 1);
  --vue-ease-premium: cubic-bezier(0.16, 1, 0.3, 1);
}
```

### Auth Page Animation

- link-based Login/Register toggle — text highlight transition;
- diagonal/curved gradient panel movement;
- form slide + fade on toggle;
- staggered field entrance (30–50ms delay per field);
- info panel cross-fade;
- stable card container (no card movement, only content changes).

### General Page Animation

- page transitions: subtle fade (200ms);
- card/appear: subtle fade + slide-up (300ms);
- button hover: color shift (150ms);
- dialog/modal: fade + scale (200ms);
- menu dropdown: fade (150ms);
- form error appearance: fade + slide-down (200ms).

### Reduced Motion

```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

---

## 26. Responsive Rules

### Breakpoints

| Breakpoint | Target |
|---|---|
| `< 640px` | Mobile |
| `640–1023px` | Tablet |
| `1024px+` | Desktop |
| `1280px+` | Wide desktop |

### Responsive Behavior

- Top bar: stack or collapse on mobile (hamburger menu).
- Sidebar: hidden on mobile, shown as overlay when toggled.
- Tables: horizontal scroll or responsive card layout on mobile.
- Forms: single column on mobile.
- Cards: stack vertically on mobile.
- Dialogs: full-screen or near-full-screen on mobile (max-width 95vw).
- Language switcher: stays compact, moves inside mobile menu if needed.
- Auth Page: single column on mobile (gradient panel collapses or becomes top section).

---

## 27. Accessibility Rules

Minimum requirements (WCAG AA-friendly):

- visible focus states on all interactive elements;
- keyboard-accessible navigation and forms;
- keyboard-accessible dialogs (trap focus, Escape to close);
- accessible language switcher (button roles, aria-label);
- sufficient contrast (4.5:1 for body text, 3:1 for large text);
- form errors associated with fields via `aria-describedby` or similar;
- no information conveyed by color alone;
- semantic heading hierarchy (h1 → h2 → h3);
- table headers correctly defined (`<th>` with scope);
- buttons and links visually distinct.

---

## 28. PrimeVue Styling Rules

### Principle

PrimeVue provides behavior and accessibility.
ResumAIner tokens provide identity.

### PrimeVue Components in Use

| UI Need | PrimeVue Component |
|---|---|
| Buttons | Button |
| Inputs | InputText, Textarea, Select |
| Checkbox / MultiSelect | Checkbox, MultiSelect |
| Tables | DataTable |
| Pagination | Paginator / DataTable |
| Dialogs | Dialog |
| Toasts | Toast |
| Confirmations | ConfirmDialog |
| Password | Password |
| Menus | Menu / PanelMenu |
| Tabs | TabView or custom |

### PrimeVue Token Mapping

| PrimeVue Token | ResumAIner Value |
|---|---|
| Primary color | `--vue-accent-primary` (emerald) |
| Surface | `--vue-bg-surface` (white) |
| Text color | `--vue-text-primary` |
| Border color | `--vue-border-default` |
| Success | `--vue-accent-success` |
| Warning | `--vue-accent-warning` |
| Danger | `--vue-accent-error` |
| Info | `--vue-accent-blue` |
| Focus ring | `--vue-shadow-focus` (blue) |

### Override Files

Recommended structure:

```
src/assets/styles/
├── vue_general.css         ← shared tokens + base styles
└── primevue-overrides.css   ← PrimeVue component overrides
```

Or combine both in `vue_general.css` if project simplicity is preferred.

### PrimeVue Override Approach

Use CSS specificity or `:deep()` in Vue SFC `<style>` blocks to apply ResumAIner tokens to PrimeVue components. Example:

```css
:deep(.p-inputtext) {
  background: var(--vue-bg-surface);
  border-color: var(--vue-border-control);
  color: var(--vue-text-primary);
  border-radius: var(--vue-radius-md);
}
:deep(.p-inputtext:focus) {
  border-color: var(--vue-accent-blue);
  box-shadow: 0 0 0 3px rgba(47, 107, 255, 0.12);
}
```

---

## 29. CSS Architecture Recommendation

### File Structure

```
src/assets/styles/
├── vue_general.css         ← tokens, base, utilities, shared components
└── primevue-overrides.css   ← PrimeVue component overrides (optional separate file)
```

### Import Order

```javascript
// main.js or main.ts
import 'primevue/resources/themes/lara-light-blue/theme.css'  // PrimeVue base (or custom build)
import '@/assets/styles/vue_general.css'                       // ResumAIner tokens + base
import '@/assets/styles/primevue-overrides.css'                // PrimeVue overrides (if separate)
```

### Page-Specific Styles

Keep page-specific styles in Vue SFC `<style scoped>` blocks.
Only put truly shared styles in `vue_general.css`.

---

## 30. Implementation Checklist

Before releasing any Vue SPA page, verify:

- [ ] Page uses the correct color tokens (no stray hex values).
- [ ] Typography uses Manrope (headings) + Inter (body/mono).
- [ ] Primary actions are emerald (`--vue-accent-primary`).
- [ ] Destructive actions use error styling and require confirmation.
- [ ] All cards use consistent radius and shadow.
- [ ] All inputs use the same focus style (blue focus ring).
- [ ] Language switcher works correctly (EN ↔ RU).
- [ ] All visible text is externalized in i18n keys.
- [ ] Russian text reads naturally (not machine-translated).
- [ ] Page is responsive (mobile, tablet, desktop).
- [ ] Empty states guide the next action.
- [ ] Errors are readable and actionable.
- [ ] Focus states are visible and keyboard-friendly.
- [ ] `prefers-reduced-motion` is respected.
- [ ] Page feels like ResumAIner, not generic PrimeVue.

---

## 31. Anti-patterns

Avoid:

- default unmodified PrimeVue theme dominating the app;
- random blue primary buttons (blue is for info/focus only);
- inconsistent card radius across pages;
- multiple competing CTA styles on the same page;
- too many icon styles mixed together;
- generic stock illustrations;
- decorative AI visuals (sparkles, brains, robots);
- hidden labels in forms;
- overusing modals for complex editing workflows;
- dense admin styling leaking into user-facing pages;
- the Landing Page and Vue SPA looking like separate products;
- pure black text (#000);
- neon gradients or dark AI backgrounds;
- animated decorative elements that do not serve a functional purpose.

---

## 32. Summary

ResumAIner Vue SPA Design DNA is based on this principle:

**Clean canvas, precise controls.**

The authenticated SPA should feel like a calm, professional career-tech tool that helps users structure their profile, adapt resumes, prepare bilingual versions, review drafts, save versions, and share polished outputs.

The Auth Page is the approved visual precedent. All other Vue SPA pages inherit its tokens, spacing, card style, input style, button style, error style, and motion principles — even though their layouts differ.

Main message:

**One profile. Tailored resumes for any role.**
