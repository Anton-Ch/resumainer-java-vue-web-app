# Landing Page Design

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Page:** Landing Page
**Recommended File Path:** `docs/05_ui-ux/landing_page_design.md`
**Status:** Approved for implementation handoff
**Frontend Target:** Thymeleaf Landing Page + Vue SPA authentication flow

---

## 1. Purpose

The Landing Page introduces **ResumAIner — Resume AI Aligner** to first-time visitors.

Its purpose is to explain the product clearly, show the core value, and guide the visitor to start using the application through one primary action.

The page should communicate:

* what ResumAIner is;
* what problem it solves;
* how the workflow works;
* what key features are available;
* why the user stays in control;
* how to start.

The page must feel professional, mature, calm, structured, and trustworthy.

---

## 2. Core Positioning

ResumAIner is an **AI-assisted resume adaptation tool**, not just a classic resume builder.

Main idea:

**Create one structured career profile once, then prepare tailored resume and cover letter versions for specific vacancies and companies.**

Core message:

**One profile. Tailored resumes for any role.**

The Landing Page should avoid presenting AI as “magic.”
The product should be positioned as a structured workflow where AI assists, but the user reviews and controls the final result.

---

## 3. Target Visitor Understanding

After reading the Landing Page, the visitor should understand:

1. ResumAIner stores career data in one structured profile.
2. The user can paste a vacancy and company description.
3. The product helps create vacancy-specific resumes and cover letters.
4. English and Russian resume versions can be prepared depending on user needs.
5. The user reviews and edits drafts before saving.
6. Final resumes can be downloaded as polished PDFs or shared through public links.
7. The product helps reduce repeated manual rewriting, translation, and version tracking.

---

## 4. Page Structure

The Landing Page uses this section order:

1. **Header**
2. **Hero with one large HTML/CSS product mockup**
3. **Problem Section**
4. **How It Works**
5. **Features**
6. **Trust and Control**
7. **FAQ**
8. **Final CTA**

Key layout decision:

There is **only one major product mockup**, and it lives in the **Hero section**.

A separate Product Preview section is intentionally removed to avoid repetition, reduce visual noise, and simplify implementation.

---

## 5. Header

### Purpose

The header provides simple navigation, language access, and one clear primary CTA.

### Desktop Layout

Left:

* **ResumAIner** logo/product name

Center/right navigation anchors:

* **How it works**
* **Features**
* **FAQ**

Right:

* language switcher: **EN / RU**
* primary CTA button: **Get started**

### Mobile Layout

* left: **ResumAIner**
* right: **EN / RU**
* right: **Get started**

Navigation anchors may be hidden or collapsed on mobile if implementation complexity should stay low.

### Rules

* Do not show **Log in** anywhere.
* Do not show secondary authentication CTA.
* Header may be sticky or semi-sticky if implementation remains clean.
* Visual style: warm paper background, subtle blur, thin bottom border.

### Header Copy

Product name:

**ResumAIner**

Navigation anchors:

* **How it works**
* **Features**
* **FAQ**

Primary CTA:

**Get started**

CTA behavior:

`Get started` leads to the Vue SPA authentication flow.

---

## 6. Hero Section

### Purpose

The Hero section must explain the product quickly and create the strongest first impression.

It should answer:

* What is this?
* Why does it matter?
* What can I do here?
* What is the main action?

### Layout

Desktop:

* two-column layout;
* left column: copy, CTA, scroll hint;
* right column: large HTML/CSS product mockup.

Mobile:

* text first;
* CTA;
* mockup below;
* scroll hint optional if space is limited.

### Hero Copy

Eyebrow:

**ResumAIner — Resume AI Aligner**

H1:

**Apply smarter, not harder.**

Supporting slogan:

**One profile. Tailored resumes for any role.**

Subtitle:

**Create job-specific resumes and cover letters from one structured profile, tailored to each vacancy and company.**

Supporting line:

**Make personalized resumes and cover letters for any vacancy, review, save, and share polished PDFs or public links.**

CTA button:

**Get started**

Scroll hint:

**See how it works ↓**

### Hero Mockup

The Hero mockup is the main visual object of the page.

It must be built with **HTML/CSS**, not SVG-first.

The mockup should show:

* structured profile card;
* vacancy/company context card;
* AI alignment node/card;
* tailored resume preview card;
* cover letter badge;
* EN/RU chips;
* PDF chip;
* public link chip;
* highlighted relevant strengths;
* version saved indicator.

Core visual flow:

**Profile Data + Vacancy Context → AI Alignment → Tailored Resume**

### Mockup Style Rules

Do:

* make it product-like but simplified;
* use document/card UI;
* show the alignment workflow;
* use matched skill chips;
* use subtle emerald highlights.

Do not:

* use stock images;
* use robots;
* use brains;
* use magic wand visuals;
* use generic AI sparkles;
* make it look like a random SaaS dashboard.

---

## 7. Problem Section

### Purpose

The Problem Section helps the visitor recognize the pain and understand why the product exists.

Tone should be practical, not dramatic.

### Layout

* section heading;
* short intro text;
* responsive card grid.

Desktop:

* 5 cards in a balanced grid;
* preferred: 3 cards in the first row, 2 cards in the second row.

Tablet:

* 2 columns.

Mobile:

* 1 column.

### Section Copy

Section title:

**Resume adaptation should not feel like starting from zero every time.**

Section intro:

**Every serious application needs a focused resume. But rewriting, translating, and tracking versions manually quickly becomes slow, repetitive, and messy.**

### Problem Cards

#### Manual tailoring takes hours

Rewriting a resume for every vacancy is slow and mentally draining.

#### Career data is scattered

Experience, skills, projects, and achievements live across files, LinkedIn, old resumes, and memory.

#### Translation doubles the work

When you need English and Russian versions, you must translate, compare, and keep both versions consistent.

#### AI chats repeat the same setup

Without a structured profile, every AI session starts with the same context-building work.

#### Resume versions become messy

It becomes hard to remember which resume was sent to which company.

### Visual Style

* calm cards;
* mostly neutral colors;
* small minimal icons if used;
* emerald only as a small accent;
* no dramatic warning style.

---

## 8. How It Works Section

### Purpose

This section explains the product workflow clearly and makes the process feel simple, structured, and controlled.

### Layout

Desktop:

* horizontal 5-step timeline;
* step cards connected by thin line or arrows.

Mobile:

* vertical timeline;
* step number on top or left;
* card content below or right.

### Section Copy

Section title:

**How ResumAIner works**

Section intro:

**Create your profile once, then adapt your resume for each vacancy through a clear, controlled workflow.**

### Steps

#### 1. Create your profile

Add contact details, work experience, projects, education, courses, skills, and additional info.

#### 2. Paste a vacancy

Add vacancy description, company context, target language, and adaptation level.

#### 3. Prepare tailored drafts with AI assistance

Get a vacancy-specific resume and cover letter based on your structured profile.

#### 4. Review and edit

Keep control over the final content before saving.

#### 5. Export and share

Download a polished PDF or share a public recruiter link.

### Visual Style

* muted emerald step numbers;
* thin connection line;
* compact cards;
* no heavy animation;
* workflow should feel clear, not magical.

---

## 9. Features Section

### Purpose

The Features Section shows product capabilities as user value.

It should not become technical documentation.

### Layout

Desktop:

* 4 columns × 2 rows.

Tablet:

* 2 columns.

Mobile:

* 1 column.

### Section Copy

Section title:

**Everything you need to prepare focused job applications**

Section intro:

**ResumAIner combines structured profile storage, resume adaptation, bilingual preparation, version tracking, and sharing in one workflow.**

### Feature Cards

#### Structured career profile

Store your professional data once and reuse it for many applications.

#### AI-assisted resume adaptation

Tailor resumes for specific vacancies and company descriptions.

#### Cover letter drafting

Create vacancy-specific cover letters from the same profile context.

#### Bilingual interface and resume auto-translation

The product supports English and Russian UI, and can make resume versions in English, Russian, or both depending on your needs.

#### Resume version history

Save, search, sort, and manage resume versions.

#### PDF download

Export polished, print-friendly, selectable-text PDFs.

#### Public recruiter links

Share a public resume link without sending files manually.

#### User control before saving

AI helps prepare drafts; you review and edit the final result.

### Not Shown on Landing Page

Do not show admin AI model management on the Landing Page.

Reason:

This is an internal system feature and does not support the main visitor value proposition.

---

## 10. Trust and Control Section

### Purpose

This section reduces concerns about AI, privacy, public links, and final content control.

It reinforces that AI assists, but the user stays in control.

### Layout

Desktop:

* split section;
* left: main message;
* right: reassurance cards.

Mobile:

* message first;
* cards below.

### Section Copy

Section title:

**Stay in control of your career story.**

Core message:

**Stop rewriting and translating your resume from scratch manually. Highlight your most relevant strengths for each specific role without losing control over the final result.**

### Reassurance Cards

#### Editable before saving

Review and adjust resume and cover letter drafts before finalizing them.

#### Intentional public sharing

Public links are shared only when you decide to use them.

#### Bilingual consistency

Prepare English and Russian resume versions in one structured workflow.

### Visual Style

* calm;
* trust-focused;
* warm paper surface;
* no aggressive CTA inside this section;
* no fear-based copy.

---

## 11. FAQ Section

### Purpose

FAQ answers practical objections before the final CTA.

### Layout

* centered heading;
* max-width accordion container;
* native HTML `<details>` / `<summary>`;
* no JavaScript required.

### Section Copy

Section title:

**Questions before you start**

### FAQ Items

#### Is ResumAIner a resume builder?

Yes, but it is more focused than a classic resume builder. It helps you adapt resumes for specific vacancies using one structured profile.

#### Can I edit AI-assisted resume drafts?

Yes. You review and edit the resume before saving the final version.

#### Can it make cover letters?

Yes. ResumAIner can prepare cover letter drafts based on your profile and the target vacancy.

#### Does it support English and Russian resumes?

Yes. You can make resume versions in English, Russian, or both.

#### Does the interface support English and Russian?

Yes. The UI supports both English and Russian.

#### Can I download resumes as PDF?

Yes. You can download a polished, print-friendly PDF.

#### Can I share a resume with recruiters using a public link?

Yes. You can create a public link and send it to a recruiter.

#### Does AI publish or send anything automatically?

No. You stay in control. Nothing is final until you review and save it.

#### Why use this instead of a normal AI chat?

Because your career data is stored once in a structured profile. You do not need to rebuild the same context every time.

### Visual Style

* clean;
* readable;
* enough spacing;
* soft borders;
* not technical-documentation-like.

---

## 12. Final CTA Section

### Purpose

The final CTA gives one clear next action after the visitor understands the product.

Tone should be calm and non-pushy.

### Layout

* wide centered card;
* short title;
* one-line supporting text;
* one CTA button.

### Copy

Title:

**Ready to tailor your resume faster?**

Text:

**Create your structured profile once and adapt resumes for the roles you want.**

Button:

**Create your profile**

### Rules

* no secondary CTA;
* no **Log in**;
* button leads to Vue SPA authentication flow.

### Visual Style

* subtle warm/emerald gradient;
* professional;
* not salesy;
* enough whitespace.

---

## 13. Visual Direction

The Landing Page uses a **Warm Professional / Ink + Sand + Emerald** visual direction.

Core principle:

**Warm background, but sharp structure.**

The page should feel:

* professional;
* mature;
* calm;
* confident;
* growth-oriented;
* structured;
* modern, but not trendy for the sake of trend.

The page should not feel like:

* corporate blue HR portal;
* dark AI startup template;
* coaching / psychology website;
* interior design / lifestyle landing;
* magazine / publishing website;
* generic Bootstrap/SaaS template.

---

## 14. Color Palette

Use a warm, almost-white background with strong ink text and emerald accents.

### Color Tokens

| Token                  | Role                            | Suggested value |
| ---------------------- | ------------------------------- | --------------: |
| `--color-bg`           | Main warm page background       |       `#FAF7F0` |
| `--color-surface`      | Cards / paper surfaces          |       `#FFFDF8` |
| `--color-surface-soft` | Soft section background         |       `#F4EFE6` |
| `--color-ink`          | Main text / headings            |       `#17211D` |
| `--color-ink-soft`     | Secondary text                  |       `#53615B` |
| `--color-border`       | Borders / dividers              |       `#E6DED2` |
| `--color-emerald`      | Main accent / CTA               |       `#0F8A6A` |
| `--color-emerald-dark` | CTA hover / strong accent       |       `#076653` |
| `--color-sage`         | Soft accent backgrounds / chips |       `#DDE9E1` |
| `--color-brass`        | Optional tiny highlight only    |       `#C49A4A` |

### Usage Rules

* Background should stay almost white and lightly warm.
* Emerald is the main action/accent color.
* Brass is optional and used only for tiny details.
* Do not use corporate blue as the main palette.
* Do not use neon gradients or dark AI-style backgrounds.
* Avoid making the page look beige, yellow, creamy, or interior-like.

---

## 15. Gradient Direction

Use light, subtle gradients only.

Allowed:

* soft background gradient in hero: warm off-white → paper white;
* emerald / deep teal gradient for CTA;
* very subtle glow around the AI alignment node in the product mockup.

Avoid:

* neon gradients;
* purple/cyan AI hype gradients;
* heavy glassmorphism;
* glowing dark backgrounds;
* colorful abstract blobs.

The gradient should feel **quiet premium**, not “AI festival.”

---

## 16. Typography

### Fonts

Use professional web fonts:

* headings: **Manrope**
* body/UI: **Inter**

Production/portfolio approach:

* self-host fonts as `.woff2` when possible;
* Google Fonts CDN is acceptable during early implementation;
* always include fallback stack.

Fallback stack:

`Manrope, Inter, "Helvetica Neue", Arial, sans-serif`

### Type Scale

| Token              | Use                    |              Size |
| ------------------ | ---------------------- | ----------------: |
| `--font-size-xs`   | chips, labels          |  `0.75rem` / 12px |
| `--font-size-sm`   | small text             | `0.875rem` / 14px |
| `--font-size-base` | body                   |     `1rem` / 16px |
| `--font-size-md`   | large body             | `1.125rem` / 18px |
| `--font-size-lg`   | card title             |  `1.25rem` / 20px |
| `--font-size-xl`   | section subtitle/title |   `1.5rem` / 24px |
| `--font-size-2xl`  | section heading        |     `2rem` / 32px |
| `--font-size-3xl`  | large section heading  |  `2.75rem` / 44px |
| `--font-size-hero` | hero H1                |     `4rem` / 64px |

Responsive rule:

* hero H1 desktop: 56–64px;
* tablet: 42–48px;
* mobile: 34–38px.

Line-height:

* headings: `1.05–1.15`;
* body: `1.6–1.75`;
* cards: `1.5–1.6`.

---

## 17. Spacing

Base unit: **4px**.

| Token        | Value |
| ------------ | ----: |
| `--space-1`  |   4px |
| `--space-2`  |   8px |
| `--space-3`  |  12px |
| `--space-4`  |  16px |
| `--space-5`  |  20px |
| `--space-6`  |  24px |
| `--space-8`  |  32px |
| `--space-10` |  40px |
| `--space-12` |  48px |
| `--space-16` |  64px |
| `--space-20` |  80px |
| `--space-24` |  96px |

Section spacing:

* desktop: 80–96px vertical;
* tablet: 64px;
* mobile: 48px.

Container:

* max width: 1180–1200px;
* horizontal padding:

  * desktop: 32px;
  * tablet: 24px;
  * mobile: 16px.

---

## 18. Radius and Shadows

The design should be rounded, but not too soft.

### Radius Tokens

| Token         | Use                    |  Value |
| ------------- | ---------------------- | -----: |
| `--radius-sm` | chips, small controls  |  `8px` |
| `--radius-md` | buttons, small cards   | `12px` |
| `--radius-lg` | cards                  | `18px` |
| `--radius-xl` | hero mockup / CTA card | `28px` |

### Shadow Tokens

| Token             | Use          |
| ----------------- | ------------ |
| `--shadow-soft`   | cards        |
| `--shadow-medium` | hero mockup  |
| `--shadow-focus`  | focus states |

Shadow style should be subtle and warm, not heavy black drop-shadow.

---

## 19. Component System

Landing styling uses:

* **Pico CSS** as base reset / typography / form foundation;
* custom local file: `landing.css`;
* design tokens through CSS custom properties;
* semantic component classes;
* minimal JavaScript or no JavaScript.

Preferred class style:

* `.landing-header`
* `.hero`
* `.hero-copy`
* `.hero-mockup`
* `.section`
* `.section-header`
* `.problem-card`
* `.workflow-step`
* `.feature-card`
* `.trust-card`
* `.faq-item`
* `.cta-card`
* `.chip`
* `.btn-primary`

Avoid heavy utility-class soup.
Keep HTML readable.

---

## 20. Buttons

### Primary Button

Used for:

* Header: **Get started**
* Hero: **Get started**
* Final CTA: **Create your profile**

Style:

* background: emerald / deep teal gradient;
* text: white;
* radius: `12px`;
* comfortable padding;
* font-weight: 700;
* hover: slightly darker;
* focus: visible outline;
* no neon glow.

States:

* default;
* hover;
* active;
* focus-visible;
* disabled.

Rules:

* one primary CTA per major CTA area;
* no secondary auth CTA;
* no **Log in**.

---

## 21. Chips / Badges

Used in:

* hero mockup;
* feature cards;
* language indicators;
* PDF/public link indicators.

Examples:

* **ATS-friendly**
* **EN/RU**
* **PDF**
* **Public link**
* **Editable drafts**
* **Version saved**

Style:

* small;
* rounded;
* soft sage background;
* ink or emerald text;
* border optional;
* no loud colors.

---

## 22. Cards

Base card style:

* background: `--color-surface`;
* border: `1px solid --color-border`;
* radius: `--radius-lg`;
* subtle shadow;
* clear internal spacing.

### Card Variants

#### Problem Card

* calm, mostly neutral;
* small icon optional;
* emerald only as tiny accent.

#### Workflow Step Card

* includes step number;
* connected by timeline line;
* compact but readable.

#### Feature Card

* small icon or symbol;
* title;
* 1–2 sentence description;
* consistent height where possible.

#### Trust Card

* reassurance-focused;
* softer visual treatment;
* no CTA.

#### CTA Card

* wide card;
* subtle warm/emerald gradient;
* stronger visual presence;
* one button only.

---

## 23. Icons

Do not use Font Awesome.

Preferred:

* minimal inline SVG icons;
* simple CSS symbols;
* tiny custom icons only if needed.

Icon style:

* line-based;
* consistent stroke width;
* not decorative clutter;
* no AI robot / brain / magic wand icons.

---

## 24. Language Switcher

Options:

* **EN**
* **RU**

Style:

* compact segmented control or simple text toggle;
* active language clearly visible;
* should not compete with CTA.

Behavior:

* switches Landing Page language;
* consistent with project i18n direction.

---

## 25. Accessibility Requirements

Minimum requirements:

* sufficient text contrast;
* visible focus states;
* semantic HTML sections;
* buttons and links clearly distinguishable;
* FAQ accessible via keyboard;
* language switcher keyboard-accessible;
* CTA tap targets large enough on mobile;
* no essential information conveyed by color alone.

Target:

* WCAG AA-friendly design.

---

## 26. Responsive Behavior

Suggested breakpoints:

* mobile: `< 640px`;
* tablet: `640–1023px`;
* desktop: `1024px+`;
* wide desktop: `1280px+`.

Responsive rules:

* hero becomes single-column on mobile;
* hero text appears before mockup on mobile;
* mockup stacks or simplifies vertically;
* problem/features grids become one column on mobile;
* timeline becomes vertical;
* header simplifies;
* section spacing reduces;
* CTA buttons remain easy to tap;
* no horizontal overflow.

---

## 27. Technical Implementation Direction

Landing implementation should remain lightweight.

Use:

* Thymeleaf template;
* Pico CSS base;
* custom local `landing.css`;
* HTML/CSS mockup in Hero;
* native FAQ accordion;
* semantic sections with anchor IDs:

  * `#how-it-works`
  * `#features`
  * `#faq`

Avoid:

* heavy JavaScript;
* Font Awesome;
* stock images;
* generic Bootstrap-like styling;
* decorative animations before layout is stable.

Recommended static assets:

* `static/css/landing.css`
* `static/vendor/pico/pico.min.css`
* `static/fonts/manrope/`
* `static/fonts/inter/`

Production preference:

* local-first CSS and fonts;
* CDN only as temporary development helper or fallback if simple.

---

## 28. Design Guardrails

Do:

* use warm almost-white background;
* keep structure sharp and professional;
* use emerald as main action/accent color;
* use document/product UI visual language;
* make the Hero clean and light;
* use Manrope + Inter;
* make the product mockup custom and meaningful;
* keep copy simple and practical.

Do not:

* use corporate blue as the main palette;
* use dark AI visual style;
* use generic SaaS blobs;
* use psychology/coaching visual softness;
* overuse beige/sand;
* overuse brass/gold;
* make it look like a magazine or publishing site;
* use stock photos;
* use Font Awesome;
* overuse the word “generate” visually or textually;
* add **Log in** anywhere on the Landing Page.

---

## 29. Implementation Acceptance Criteria

The Landing Page implementation is acceptable when:

* all approved sections are present in the correct order;
* the page uses one large Hero mockup and no separate Product Preview section;
* no **Log in** link or button appears anywhere;
* all primary CTAs lead to the Vue SPA authentication flow;
* the page supports EN/RU language switching;
* layout is responsive on mobile, tablet, and desktop;
* FAQ works without JavaScript using native HTML;
* visual style follows Warm Professional / Ink + Sand + Emerald direction;
* fonts use Manrope + Inter with proper fallback;
* Pico CSS is used only as base and does not make the page look generic;
* custom `landing.css` defines the product identity;
* accessibility basics are covered;
* no Font Awesome, stock images, robots, brains, or magic AI visuals are used.

---

## 30. Summary

The ResumAIner Landing Page should present the product as a calm, professional, structured career-tech tool.

It should show that the user can create one structured profile, adapt resumes and cover letters for specific roles, prepare English and Russian versions, review the result, save versions, and share polished PDFs or public links.

The page should feel warm, mature, and trustworthy — but still sharp, modern, and product-focused.

Main message:

**Apply smarter, not harder.**

Supporting message:

**One profile. Tailored resumes for any role.**
