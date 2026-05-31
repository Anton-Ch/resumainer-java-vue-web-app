# ResumAIner Design DNA

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Document Type:** Design DNA / Cross-Interface Visual Consistency Guide
**Recommended File Path:** `docs/05_ui-ux/design_dna.md`
**Status:** Approved for UI implementation guidance
**Applies To:** Thymeleaf Landing Page + Vue SPA authenticated application

---

## 1. Purpose

This document defines the shared visual and interaction DNA of **ResumAIner**.

It should be used to keep visual consistency between:

* public **Thymeleaf Landing Page**;
* authenticated **Vue SPA**;
* user-facing screens;
* admin screens;
* resume generation and review flow;
* profile management flow;
* public recruiter resume view where applicable.

This document is not a detailed screen-by-screen UI specification.
It defines the reusable design language, visual principles, design tokens, component behavior, and consistency rules.

---

## 2. Brand Essence

### Product Identity

**ResumAIner — Resume AI Aligner**

ResumAIner is a structured career-tech tool that helps users prepare tailored resumes and cover letters from one reusable professional profile.

### Core Product Promise

**One profile. Tailored resumes for any role.**

### Main Emotional Signal

The product should make the user feel:

* organized;
* confident;
* in control;
* professionally represented;
* supported by AI, but not replaced by it.

### Design Personality

ResumAIner should feel:

* warm, but not soft;
* professional, but not corporate-blue;
* structured, but not cold;
* modern, but not trendy for the sake of trend;
* AI-assisted, but not AI-hype.

---

## 3. Core Design Principle

## Warm background, sharp structure.

This principle controls the full product UI.

### Warm background

Use warmth to create trust, calmness, and approachability.

Warmth appears in:

* page backgrounds;
* soft surfaces;
* calm empty states;
* supportive guidance;
* friendly but concise text.

### Sharp structure

Use structure to create professionalism, clarity, and confidence.

Sharpness appears in:

* clear grid;
* strong hierarchy;
* consistent cards;
* visible states;
* precise form layout;
* structured tables;
* disciplined spacing;
* clean navigation.

---

## 4. What the Product Should Not Feel Like

Avoid these visual associations:

* corporate HR portal;
* generic blue SaaS dashboard;
* dark AI startup;
* psychology/coaching website;
* interior design or lifestyle site;
* magazine/editorial publication;
* casual blog;
* gamified productivity app;
* generic Bootstrap template.

The UI should always look like a **serious career-tech product**.

---

## 5. Visual Direction

### Approved Direction

**Warm Professional / Ink + Sand + Emerald**

### Visual Meaning

| Design Element            | Meaning                             |
| ------------------------- | ----------------------------------- |
| Warm off-white background | Calm, trust, clarity                |
| Ink text                  | Control, seriousness, readability   |
| Emerald accent            | Growth, action, progress, alignment |
| Paper-like cards          | Resume/document context             |
| Sharp grid                | Professional structure              |
| Soft shadows              | Depth without decoration            |
| Alignment lines/chips     | Profile-to-vacancy matching         |

---

## 6. Core Color Tokens

Use the same color DNA across Landing Page and Vue SPA.

```css
:root {
  --color-bg: #FAF7F0;
  --color-surface: #FFFDF8;
  --color-surface-soft: #F4EFE6;

  --color-ink: #17211D;
  --color-ink-soft: #53615B;

  --color-border: #E6DED2;

  --color-emerald: #0F8A6A;
  --color-emerald-dark: #076653;
  --color-sage: #DDE9E1;

  --color-brass: #C49A4A;

  --color-danger: #B42318;
  --color-danger-soft: #FDECEC;

  --color-warning: #B7791F;
  --color-warning-soft: #FFF4D6;

  --color-info: #356A75;
  --color-info-soft: #E2F0F2;

  --color-success: #0F8A6A;
  --color-success-soft: #DDE9E1;
}
```

### Color Usage Rules

Use:

* `--color-bg` for main application background;
* `--color-surface` for cards, panels, forms, and document-like areas;
* `--color-emerald` for main actions, active states, progress, and alignment highlights;
* `--color-sage` for soft badges, chips, and low-intensity highlights;
* `--color-ink` for strong readable text.

Avoid:

* blue as the main brand color;
* black pure `#000000` for primary text;
* neon colors;
* large brass/gold areas;
* full dark-mode AI visual style.

---

## 7. Typography DNA

### Font System

Use:

* **Manrope** for headings;
* **Inter** for body text and UI text.

Fallback stack:

```css
font-family: Manrope, Inter, "Helvetica Neue", Arial, sans-serif;
```

### Typography Roles

| Role               | Font    | Purpose                    |
| ------------------ | ------- | -------------------------- |
| Hero / page titles | Manrope | Strong product personality |
| Section titles     | Manrope | Clear hierarchy            |
| Form labels        | Inter   | Readability and precision  |
| Body text          | Inter   | Comfortable reading        |
| Table text         | Inter   | Dense but readable UI      |
| Chips / badges     | Inter   | Compact UI elements        |

### Typography Rules

Do:

* keep headings confident and concise;
* use clear form labels;
* use readable body text;
* keep table text compact but not cramped.

Do not:

* use decorative fonts;
* use handwritten fonts;
* use magazine-like serif display fonts;
* make headings overly playful.

---

## 8. Spacing DNA

Use a 4px-based spacing system.

```css
:root {
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;
  --space-20: 80px;
  --space-24: 96px;
}
```

### Spacing Rules

* Use generous spacing on Landing Page.
* Use denser but still readable spacing in Vue SPA.
* Forms should feel structured, not cramped.
* Admin tables may be more compact than user-facing forms.
* Resume Review should use more whitespace because content reading and editing require focus.

---

## 9. Shape and Depth DNA

### Radius

```css
:root {
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 18px;
  --radius-xl: 28px;
}
```

Usage:

| Radius        | Use                                        |
| ------------- | ------------------------------------------ |
| `--radius-sm` | chips, small controls                      |
| `--radius-md` | buttons, inputs, small panels              |
| `--radius-lg` | cards, form sections                       |
| `--radius-xl` | hero mockup, major CTA cards, large panels |

### Shadows

Use soft, warm shadows.

```css
:root {
  --shadow-soft: 0 10px 30px rgba(23, 33, 29, 0.06);
  --shadow-medium: 0 18px 50px rgba(23, 33, 29, 0.10);
  --shadow-focus: 0 0 0 3px rgba(15, 138, 106, 0.22);
}
```

Do not use heavy black shadows.

---

## 10. Interaction DNA

All interactions should feel:

* clear;
* calm;
* predictable;
* professional;
* low-friction.

### Primary Action Behavior

Primary actions use emerald.

Examples:

* Get started;
* Create your profile;
* Save;
* Save & Create;
* Generate / Prepare draft;
* Download PDF;
* Copy public link.

### Secondary Action Behavior

Secondary actions use neutral or outline styling.

Examples:

* Cancel;
* Back;
* Edit;
* Reset filters;
* View details.

### Destructive Action Behavior

Destructive actions use danger styling.

Examples:

* Delete;
* Soft-delete resume;
* Deactivate user;
* Delete API key.

Destructive actions should require confirmation when the action affects stored data, access, or visibility.

---

## 11. Core Component DNA

The same component principles should apply across Landing Page and Vue SPA.

## 11.1 Buttons

### Primary Button

Use for one main action per area.

Visual:

* emerald / deep teal gradient or solid emerald;
* white text;
* medium radius;
* strong but calm presence;
* visible focus state.

### Secondary Button

Use for supporting actions.

Visual:

* surface background;
* ink text;
* neutral border;
* subtle hover.

### Text Button

Use for low-emphasis actions.

Visual:

* no heavy background;
* emerald or ink text;
* underline only when helpful.

---

## 11.2 Cards and Panels

Cards represent grouped information.

Use cards for:

* profile sections;
* resume records;
* generation settings blocks;
* admin overview summaries;
* AI model details;
* empty states;
* trust messages;
* feature explanations.

Card style:

* paper-like surface;
* subtle border;
* rounded corners;
* soft shadow only where elevation helps.

---

## 11.3 Forms

Forms are central to Vue SPA.

Form design should follow these rules:

* one clear section at a time;
* labels always visible;
* placeholders support but do not replace labels;
* required fields clearly marked;
* help text placed near complex inputs;
* errors shown inline;
* backend errors mapped to fields where possible.

Preferred form layout:

* user-facing forms: spacious;
* admin forms: compact but readable;
* repeatable profile sections: card-based Add/Edit/Delete pattern.

---

## 11.4 Tables

Tables are used for:

* User Home resume list;
* Admin Users;
* Admin Resumes;
* Admin AI Models.

Table DNA:

* clean surface;
* readable row height;
* clear pagination;
* search/filter controls above table;
* action buttons grouped consistently;
* current page clearly visible;
* empty state shown when no records exist.

PrimeVue tables should be themed to match the design tokens.

Avoid default PrimeVue theme feeling disconnected from the Landing Page.

---

## 11.5 Chips and Badges

Use chips for compact status/context.

Examples:

* EN;
* RU;
* EN/RU;
* PDF;
* Public link;
* Draft;
* Saved;
* Active;
* Inactive;
* Hidden;
* Privileged;
* Admin;
* User;
* Minimal;
* Balanced;
* Maximum.

Chip style:

* small;
* rounded;
* soft background;
* clear text;
* not too colorful.

---

## 11.6 Alerts and Messages

Use clear message types:

| Type    | Use                                               |
| ------- | ------------------------------------------------- |
| Success | Saved, created, copied, completed                 |
| Info    | Helpful guidance                                  |
| Warning | Risky but allowed actions                         |
| Error   | Failed validation, failed API call, denied action |

Tone:

* direct;
* short;
* helpful;
* no blame.

Example:

Good:

**Resume saved. You can now download the PDF or copy the public link.**

Bad:

**Oops! Something went wrong.**

---

## 11.7 Empty States

Empty states should guide the next useful action.

Examples:

### No resumes yet

**No resumes yet.**
Create your first tailored resume from your profile and a vacancy description.

CTA:

**Create resume**

### No profile data yet

**Your profile is still empty.**
Add your experience, education, projects, and skills before preparing a resume.

CTA:

**Complete profile**

### No AI models configured

**No AI models configured.**
Add an AI model before allowing resume preparation.

CTA:

**Add AI model**

---

## 12. Layout DNA for Vue SPA

The authenticated Vue SPA should not copy the Landing Page layout directly.
It should inherit the same visual DNA but use application-focused patterns.

### Recommended App Shell

Main app layout:

* left sidebar or top navigation depending on implementation simplicity;
* main content area;
* top-right language switcher;
* logout visible only inside authenticated app;
* role-aware navigation for User/Admin.

### User App Navigation

Suggested user navigation:

* Home
* My Profile
* Generate Resume
* Saved Resumes / Resume table area

If resume list stays on User Home, avoid duplicating navigation.

### Admin App Navigation

Suggested admin navigation:

* Admin Home
* Users
* Resumes
* AI Models

### Layout Feeling

User pages:

* calmer;
* more guided;
* more whitespace.

Admin pages:

* more compact;
* table-focused;
* operational.

Resume Review:

* strongest document-like feeling;
* should visually connect to PDF/resume output;
* use paper surfaces, section blocks, and editing controls.

---

## 13. Landing Page to Vue SPA Consistency Map

| Landing Page Element  | Vue SPA Equivalent       | Consistency Rule                    |
| --------------------- | ------------------------ | ----------------------------------- |
| Hero CTA              | Main action buttons      | Same emerald primary style          |
| Hero mockup cards     | App cards/panels         | Same radius, borders, paper surface |
| Problem cards         | Empty states/help panels | Same calm tone and card style       |
| How It Works timeline | Stepper/generation flow  | Same numbered step style            |
| Feature cards         | Dashboard cards          | Same card hierarchy                 |
| Trust cards           | Security/help messages   | Same reassurance tone               |
| FAQ accordion         | Help sections/tooltips   | Same concise explanation style      |
| Language switcher     | App language switcher    | Same EN/RU visual pattern           |
| Chips in mockup       | Real status chips        | Same chip style and color logic     |

---

## 14. PrimeVue Styling Guidance

The Vue SPA uses PrimeVue components, but they should be visually aligned with ResumAIner DNA.

### PrimeVue Components Likely Used

| UI Need              | PrimeVue Component Direction     |
| -------------------- | -------------------------------- |
| Buttons              | Button                           |
| Inputs               | InputText, Textarea, Select      |
| Checkbox/multiselect | Checkbox, MultiSelect            |
| Tables               | DataTable                        |
| Pagination           | Paginator / DataTable pagination |
| Dialogs              | Dialog                           |
| Toasts               | Toast                            |
| Tabs/sections        | Tabs or custom section nav       |
| Cards                | Card or custom card component    |
| Confirmations        | ConfirmDialog                    |
| Menus                | Menu / PanelMenu / custom nav    |

### PrimeVue Customization Rule

PrimeVue should provide behavior and accessibility.
ResumAIner tokens should provide identity.

Do not let the default PrimeVue theme dominate the visual identity.

### PrimeVue Token Mapping

Use app-level CSS variables or theme override to map:

* primary color → emerald;
* surface → paper white;
* text → ink;
* border → warm neutral border;
* success → emerald;
* warning → warm amber;
* danger → deep red;
* info → muted teal.

---

## 15. Page Pattern DNA

## 15.1 User Home

Purpose:

* give access to saved resumes;
* show resume table;
* support PDF download/public link actions.

Design pattern:

* page header with title and main action;
* resume table in large card;
* filters/search above table;
* empty state if no resumes;
* primary action: create/generate resume.

DNA:

* functional;
* clean;
* not overloaded.

---

## 15.2 My Profile

Purpose:

* collect structured career data once.

Design pattern:

* section navigation;
* cards for profile sections;
* repeatable sections use Add/Edit/Delete;
* progress or completeness hint if useful.

DNA:

* structured;
* guided;
* not intimidating.

---

## 15.3 Generate Resume

Purpose:

* collect vacancy context and generation settings.

Design pattern:

* form divided into clear blocks;
* vacancy/company description fields are visually important;
* language and adaptation level are easy to see;
* primary action placed clearly at bottom or sticky footer if useful.

DNA:

* focused;
* action-oriented;
* no unnecessary distractions.

---

## 15.4 Resume Review

Purpose:

* review, edit, save final resume and cover letter.

Design pattern:

* document-like preview;
* editable fields grouped by resume section;
* clear Save & Create action;
* PDF/public link result after saving;
* cover letter accessible in same flow.

DNA:

* high trust;
* controlled editing;
* strong document feeling.

---

## 15.5 Admin Pages

Purpose:

* manage users, resumes, AI models, and usage.

Design pattern:

* compact tables;
* clear filters;
* role/status badges;
* dangerous actions confirmed;
* API keys always masked.

DNA:

* operational;
* precise;
* secure;
* less warm than user pages but still visually consistent.

---

## 16. Copywriting DNA

### Voice

ResumAIner voice is:

* clear;
* practical;
* supportive;
* professional;
* calm;
* not hype-driven.

### Preferred Words

Use:

* tailor;
* adapt;
* prepare;
* create;
* review;
* save;
* share;
* structured profile;
* vacancy-specific;
* public link;
* polished PDF;
* bilingual resume.

Use carefully:

* AI;
* draft;
* generated;
* automation.

Avoid overusing:

* generate;
* magic;
* revolutionize;
* unlock;
* dream job;
* instant;
* perfect;
* effortless.

### Microcopy Rules

Good microcopy:

* tells the user what happens next;
* avoids vague errors;
* avoids blame;
* uses practical wording;
* stays short.

Example:

**Your resume draft is ready. Review it before saving the final version.**

---

## 17. i18n Consistency Rules

The product supports English and Russian UI.

Rules:

* use shared message key naming where possible;
* keep the same meaning across Thymeleaf and Vue;
* avoid hardcoded UI strings;
* keep tone consistent in both languages;
* do not translate product name;
* keep key product terms consistent.

Recommended key style:

```text
landing.hero.title
landing.hero.subtitle
common.action.getStarted
common.action.save
common.action.cancel
profile.section.workExperience
resume.status.saved
admin.user.status.active
```

English/Russian translations should preserve:

* clarity;
* professional tone;
* calm confidence;
* no exaggerated marketing.

---

## 18. Accessibility DNA

Accessibility is part of the product identity.

Minimum requirements:

* visible focus states;
* keyboard-accessible navigation;
* keyboard-accessible forms;
* accessible language switcher;
* accessible dialogs;
* sufficient contrast;
* field-level error messages;
* no color-only meaning;
* semantic headings;
* table headers correctly defined;
* buttons and links visually distinct.

The design should be WCAG AA-friendly.

---

## 19. Responsive DNA

Landing Page and Vue SPA must both support responsive behavior.

### Landing Page

* rich desktop presentation;
* simplified mobile stack;
* hero text before mockup;
* cards stack on mobile.

### Vue SPA

* tables may require responsive strategies;
* form sections stack vertically;
* side navigation may collapse;
* primary actions remain easy to reach;
* modal dialogs fit mobile screens.

Do not create desktop-only workflows.

---

## 20. Motion and Animation DNA

Motion should be minimal and purposeful.

Allowed:

* subtle hover;
* soft button transition;
* accordion open/close;
* small loading spinner;
* progress indication;
* skeleton loading for tables/cards.

Avoid:

* decorative animations;
* bouncing elements;
* heavy scroll animations;
* animated AI sparkles;
* motion that distracts from forms.

---

## 21. Loading States

Use calm loading states.

Examples:

* button loading state during save;
* skeleton rows for tables;
* loading panel during AI-assisted draft preparation;
* progress message if operation may take time.

Good loading copy:

**Preparing your tailored draft...**

Better than:

**Generating magic...**

---

## 22. Error State DNA

Errors should be readable and actionable.

### Form Errors

Show near the field.

Example:

**Company name is required.**

### Page Errors

Show in a clear alert.

Example:

**Resume draft could not be prepared. Check the vacancy description and try again.**

### System Errors

Do not expose stack traces.

Example:

**Something went wrong while saving. Please try again.**

---

## 23. Security and Trust DNA

Security-related UI should feel precise and transparent.

Use clear labels for:

* public/private status;
* public link availability;
* API key masking;
* account status;
* generation permission;
* hidden AI models;
* privileged users.

Never make public sharing feel automatic.

Public link copy should always imply intentional user action.

---

## 24. Consistency Checklist

Before implementing or reviewing any screen, check:

* Does it use the same color tokens?
* Does it use Manrope/Inter or fallback?
* Does it use paper-like surfaces?
* Are primary actions emerald?
* Are destructive actions clearly separated?
* Are cards and panels consistent?
* Are chips visually consistent?
* Are errors clear and actionable?
* Is the page responsive?
* Is i18n externalized?
* Does the page feel like ResumAIner, not generic PrimeVue?

---

## 25. Anti-Patterns

Avoid:

* default unmodified PrimeVue theme dominating the app;
* random blue primary buttons;
* inconsistent card radius;
* multiple competing CTA styles;
* too many icon styles;
* generic stock illustrations;
* decorative AI visuals;
* hidden labels in forms;
* overusing modals for complex editing;
* dense admin styling leaking into user pages;
* Landing Page and SPA looking like separate products.

---

## 26. Implementation Recommendation

Create a shared visual foundation for Vue SPA:

Recommended files:

```text
src/assets/styles/tokens.css
src/assets/styles/base.css
src/assets/styles/components.css
src/assets/styles/primevue-overrides.css
```

Landing Page equivalent:

```text
static/css/landing.css
static/vendor/pico/pico.min.css
static/fonts/manrope/
static/fonts/inter/
```

The same token values should be reused in both:

* Thymeleaf Landing Page;
* Vue SPA;
* PrimeVue override layer.

---

## 27. Summary

ResumAIner Design DNA is based on this principle:

**Warm background, sharp structure.**

The product should look like a calm, professional career-tech tool that helps users structure their profile, adapt resumes, prepare bilingual versions, review drafts, save versions, and share polished outputs.

Landing Page and Vue SPA may have different layouts, but they must share:

* same color DNA;
* same typography;
* same button logic;
* same card language;
* same chip/status style;
* same calm professional voice;
* same trust and control mindset.

Main message:

**Apply smarter, not harder.**

Supporting message:

**One profile. Tailored resumes for any role.**
