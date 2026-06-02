# Light Design DNA

**Project:** ResumAIner  
**Document type:** Visual design system / design DNA  
**Version:** 1.0  
**Purpose:** Provide a reusable visual style guide for AI-assisted UI design, landing page generation, and future mockups/prototypes.

---

## 1. Design Intent

The design style is a **light, professional, portfolio-ready SaaS interface** with a calm analytical feel.

It should communicate:

- clarity and trust;
- structured thinking;
- modern SaaS product quality;
- business-analysis professionalism;
- low visual noise;
- safe, readable enterprise UI.

The product should look like a practical career-tech / productivity tool, not like a flashy AI toy.

---

## 2. Visual Keywords

Use these words as the core design direction:

- clean
- structured
- calm
- professional
- trustworthy
- minimal
- readable
- analytical
- light enterprise SaaS
- modern productivity tool
- soft but precise
- portfolio-ready

Avoid:

- dark futuristic AI aesthetics;
- neon gradients;
- heavy shadows;
- excessive decorative illustrations;
- childish colors;
- aggressive sales landing style;
- cluttered dashboards;
- overly corporate blue-only styling.

---

## 3. Color Palette

### 3.1 Core Background Colors

| Token | Hex | Usage |
|---|---:|---|
| `background.canvas` | `#F6F7FB` | Main page background. Soft light gray-blue. |
| `background.surface` | `#FFFFFF` | Main cards, panels, forms, modals. |
| `background.subtle` | `#FBFCFE` | Header areas, app shell sections, light panels. |
| `background.control` | `#F7FAFE` | Inputs, table filters, light toolbar fields. |

### 3.2 Text Colors

| Token | Hex | Usage |
|---|---:|---|
| `text.primary` | `#10233F` | Main headings, strong labels, important content. |
| `text.secondary` | `#5D718B` | Descriptions, helper text, inactive navigation. |
| `text.muted` | `#8091A7` | Placeholders, timestamps, low-priority metadata. |
| `text.inverse` | `#FFFFFF` | Text on primary buttons or strong color surfaces. |

### 3.3 Border Colors

| Token | Hex | Usage |
|---|---:|---|
| `border.default` | `#D8DEE9` | General card and panel borders. |
| `border.soft` | `#E3E8F0` | App shell and large sections. |
| `border.subtle` | `#E8EDF4` | Secondary cards, rows, list items. |
| `border.control` | `#E5EAF2` | Input borders. |
| `border.focus-blue` | `#CFE0FF` | Active navigation / selected blue controls. |
| `border.success` | `#CDEDDD` | Positive / confirmed / active elements. |
| `border.warning` | `#FED7AA` | Warning boxes and caution notes. |
| `border.violet` | `#DDD6FE` | Optional / AI / metadata highlights. |

### 3.4 Accent Colors

| Token | Hex | Usage |
|---|---:|---|
| `accent.primary` | `#0F9D7A` | Main positive action, active section marker, success states. |
| `accent.blue` | `#2F6BFF` | Navigation, links, selected state, informational highlights. |
| `accent.violet` | `#7C3AED` | AI/model metadata, optional advanced features, admin/system notes. |
| `accent.warning` | `#D97706` | Warnings, validation notes, caution states. |
| `accent.error` | `#C2410C` | Destructive actions, blocked/inactive/error states. |
| `accent.teal` | `#0891B2` | Secondary operational/technical indicators. |

### 3.5 Accent Backgrounds

| Token | Hex | Usage |
|---|---:|---|
| `accent-bg.blue` | `#EEF4FF` | Active nav, blue badges, info icon backgrounds. |
| `accent-bg.success` | `#F2FFF9` | Active section, positive cards, success notes. |
| `accent-bg.violet` | `#F5F3FF` | AI/metadata stretch notes. |
| `accent-bg.warning` | `#FFF7ED` | Validation/warning blocks. |
| `accent-bg.danger` | `#FFF1F2` | Blocked/inactive/destructive summaries. |
| `accent-bg.table` | `#F8FBFF` | Table headers, selected background, PDF helper strips. |

---

## 4. Color Usage Rules

### 4.1 Primary Color Logic

Use **emerald / green (`#0F9D7A`)** for:

- primary action buttons;
- active profile section indicators;
- success states;
- `ready`, `active`, `completed` statuses;
- confirmed MVP decisions;
- selected positive options.

Use **blue (`#2F6BFF`)** for:

- links;
- selected navigation;
- informational badges;
- table action links;
- secondary positive actions;
- public link / PDF access labels.

Use **violet (`#7C3AED`)** for:

- AI model metadata;
- optional/stretch functionality;
- system/technical notes;
- advanced configuration areas.

Use **amber (`#D97706`)** for:

- validation notes;
- generation failures;
- factual review warnings;
- paid usage warnings;
- provider timeout states.

Use **red/orange (`#C2410C`)** only for:

- destructive actions;
- blocked users;
- inactive/error states;
- delete actions.

### 4.2 Accent Density

The base UI should be mostly white, gray, and soft blue-gray. Accent colors should be used as meaning markers, not decoration.

Recommended distribution:

- 70–80% neutral surfaces;
- 10–15% soft blue-gray controls;
- 5–10% emerald/blue accents;
- 1–3% warning/error colors.

---

## 5. Typography

### 5.1 Font Direction

Recommended font family:

~~~css
font-family: Inter, Arial, sans-serif;
~~~

If Inter is unavailable, use a clean system sans-serif.

### 5.2 Type Scale

| Element | Size | Weight | Usage |
|---|---:|---:|---|
| Page title | 28–36px | 800–900 | Main page or major screen heading. |
| Section title | 22–24px | 800 | Card titles, table titles. |
| Form label | 14–18px | 700–800 | Input labels, metadata labels. |
| Body text | 15–18px | 400–600 | Descriptions, row values. |
| Helper text | 13–16px | 400–500 | Notes, warnings, secondary info. |
| Button text | 15–18px | 700–800 | Clear readable actions. |
| Badge text | 14–15px | 700–800 | Status pills and small tags. |

### 5.3 Typography Rules

- Use strong headings, but keep body text calm and readable.
- Avoid long paragraphs inside UI cards.
- Break explanatory content into short lines.
- Prefer labels + compact helper text over dense prose.
- Keep text inside containers with enough padding.
- No text should overflow or touch container borders.

---

## 6. Layout Principles

### 6.1 Canvas

Portfolio wireframes use:

~~~text
Canvas: 2560 × 1440
Aspect ratio: 16:9
~~~

This creates enough room for:

- app shell;
- sidebar;
- main content;
- right-side notes;
- screen label;
- footer design standard note.

### 6.2 Spacing

Use generous spacing. The style prioritizes readability over density.

| Token | Size | Usage |
|---|---:|---|
| `space.xs` | 8px | Small internal gaps. |
| `space.sm` | 12–16px | Form field spacing, badge gaps. |
| `space.md` | 24–30px | Card internal padding. |
| `space.lg` | 34–40px | Main layout gaps. |
| `space.xl` | 60–80px | Major vertical/horizontal separation. |

### 6.3 Grid

Typical app layout:

~~~text
Outer canvas
└── App shell
    ├── Top navigation
    ├── Left sidebar
    └── Main content area
        ├── Page header
        ├── Summary cards / form cards / table cards
        └── Notes or supporting actions
~~~

For profile pages:

~~~text
Left sidebar: section navigation
Main area: 2–3 content columns
Right area: BA/SA notes, validation notes, or behavior rules
~~~

For admin pages:

~~~text
Top: summary cards
Middle: table or details layout
Bottom: concise BA/SA note
~~~

For public PDF page:

~~~text
Browser shell
Toolbar
Left context panel
Central PDF viewer
Right recruiter requirements panel
~~~

---

## 7. Shape Language

### 7.1 Corners

| Element | Radius |
|---|---:|
| App shell / browser shell | 28–30px |
| Major cards | 24–26px |
| Medium cards | 18–22px |
| Inputs | 14–16px |
| Buttons | 14–18px |
| Pills / badges | 18px or full pill |
| Small indicators | 4–8px |

### 7.2 Borders

Use thin borders:

~~~css
border: 1.1px to 1.5px solid;
~~~

Most cards use light gray-blue borders. Avoid black borders and thick outlines unless showing active state.

### 7.3 Shadows

Use soft, low-opacity shadows only for large containers or elevated modal elements.

Recommended card shadow:

~~~css
box-shadow: 0 10px 30px rgba(16, 35, 63, 0.08);
~~~

Recommended modal shadow:

~~~css
box-shadow: 0 22px 56px rgba(16, 35, 63, 0.18);
~~~

Avoid harsh shadows, dark shadows, and neumorphism.

---

## 8. Cards and Surfaces

### 8.1 Major Cards

~~~css
.card {
  background: #FFFFFF;
  border: 1.4px solid #E4E9F1;
  border-radius: 26px;
  box-shadow: 0 10px 30px rgba(16, 35, 63, 0.08);
  padding: 30px;
}
~~~

### 8.2 Subtle Panels

Use for page headers, toolbars, table headers, and browser-like areas.

~~~css
.panel-subtle {
  background: #FBFCFE;
  border: 1.4px solid #E4E9F1;
  border-radius: 24px;
}
~~~

### 8.3 Information Notes

Use colored note cards only when they carry meaning.

- blue note = informational behavior;
- green note = confirmed/positive state;
- amber note = validation/warning;
- violet note = AI/system/future feature.

---

## 9. Buttons

### 9.1 Primary Button

Use emerald for the main action on a screen.

~~~css
.button-primary {
  background: #0F9D7A;
  color: #FFFFFF;
  border: 1px solid #0F9D7A;
  border-radius: 18px;
  font-weight: 800;
}
~~~

Examples:

- `Generate`
- `Save`
- `Save Changes`
- `Save & Create`
- `Download`, when it is the main action.

### 9.2 Secondary Button

Use white background with light border and blue text.

~~~css
.button-secondary {
  background: #FFFFFF;
  color: #2F6BFF;
  border: 1.2px solid #D9E1EC;
  border-radius: 14px;
  font-weight: 700;
}
~~~

Examples:

- `Copy link`
- `Open details`
- `Back to Profile`
- `Download PDF`, when not primary.

### 9.3 Destructive Button

~~~css
.button-danger {
  background: #FFFFFF;
  color: #C2410C;
  border: 1.2px solid #F3CACA;
}
~~~

Examples:

- `Delete`
- `Discard`
- `Block user`

---

## 10. Forms

### 10.1 Input Style

~~~css
.input {
  background: #FFFFFF;
  border: 1.4px solid #E5EAF2;
  border-radius: 14px;
  height: 58px;
  padding: 0 18px;
  color: #10233F;
}
~~~

### 10.2 Labels

~~~css
.label {
  font-size: 14px;
  font-weight: 800;
  color: #8091A7;
}
~~~

Use `*` for required fields.

### 10.3 Textareas

Textareas should follow the same style as inputs but with more height and vertical padding.

### 10.4 Dropdowns

Dropdowns use the same input style plus a down-arrow indicator. Use dropdowns for controlled values such as:

- language;
- adaptation level;
- status;
- role;
- willingness to relocate;
- willingness for business travels.

### 10.5 Datepicker

Datepicker uses input style plus a calendar icon. Use for:

- date of birth;
- start/end dates where appropriate.

### 10.6 Multi-select Checkbox Group

Use a bordered white container with two columns if there are many options.

Checkbox style:

- 20px square;
- 6px radius;
- blue fill when selected;
- label to the right;
- generous row spacing.

Use for `Acceptable work formats`:

~~~text
full-time
part-time
rotational schedule
internship
offline
remote
hybrid
on project-site
~~~

Behavior:

- user may select one, several, or all options;
- selected values are used as resume context and filtering metadata;
- do not force a single option.

---

## 11. Tables

### 11.1 Table Container

Tables live inside large white cards with a title, helper text, controls, header, rows, and pagination.

### 11.2 Table Header

~~~css
.table-header {
  background: #F8FBFF;
  border: 1px solid #DDE8F7;
  border-radius: 14px;
}
~~~

### 11.3 Rows

Rows use white background and subtle border. Selected rows may use green-tinted background.

~~~css
.table-row-selected {
  background: #F2FFF9;
  border: 1px solid #BAE7D7;
}
~~~

### 11.4 Controls

Search/filter controls should look like inputs but remain compact.

Use only visible filters that are actually needed. Do not add filters to every column unless required.

### 11.5 Actions

Table actions should be compact secondary buttons:

- `Open details`
- `Edit`
- `View`

Sensitive actions should not be placed directly in dense tables unless confirmed.

---

## 12. Navigation

### 12.1 Top Navigation

For user area:

~~~text
Home
My Profile
Generate Resume
~~~

For admin area:

~~~text
Home
Users
Resumes
AI Models
~~~

Active navigation:

- soft blue background;
- blue text;
- rounded pill-like background.

### 12.2 Sidebar Navigation

Active sidebar item:

- light green background;
- green left indicator strip;
- dark text;
- subtle green border.

Inactive items:

- white background;
- muted text;
- subtle gray-blue border.

---

## 13. Wireframe-Specific Components

### 13.1 App Shell

~~~css
.app-shell {
  background: #FFFFFF;
  border: 1.5px solid #E3E8F0;
  border-radius: 28px;
  box-shadow: 0 10px 30px rgba(16, 35, 63, 0.08);
}
~~~

### 13.2 Browser Shell

Used for public PDF view. Should look like a browser window with:

- rounded white container;
- browser dots;
- URL bar;
- public link badges;
- viewer toolbar.

### 13.3 Modal

Modal uses:

- dimmed overlay;
- larger shadow;
- white card;
- clear title;
- top-right close button.

~~~css
.modal-overlay {
  background: rgba(16, 35, 63, 0.30);
}
~~~

### 13.4 PDF Preview

PDF mockup should look like an A4 white/very light sheet:

- thin border;
- slight shadow;
- text blocks represented as gray bars;
- highlighted text selection where useful;
- label: `Selectable-text PDF`.

---

## 14. Landing Page Direction

When applying this design DNA to a landing page, keep the landing page more polished but still restrained.

### Recommended Landing Sections

1. Hero section
   - product name;
   - one clear value proposition;
   - primary CTA;
   - secondary CTA;
   - small trust note.

2. How it works
   - fill profile;
   - paste vacancy;
   - generate adapted resume;
   - review and export PDF.

3. Feature blocks
   - AI adaptation;
   - editable resume review;
   - public PDF link;
   - selectable-text PDF;
   - admin/model control if presenting full product scope.

4. Example UI preview
   - show a clean dashboard or resume generation panel;
   - use the same card, badge, and form styling.

5. Security / trust section
   - profile data privacy;
   - public link exposes only saved PDF;
   - API keys are masked in admin area.

6. CTA footer
   - simple, calm, not aggressive.

### Landing Page Tone

Use direct product language:

- “Generate tailored resumes from structured profile data.”
- “Review generated content before saving.”
- “Share a public PDF link with recruiters.”
- “Keep profile data private while sharing only final resumes.”

Avoid hype:

- “Revolutionary”
- “Magical”
- “10x your career instantly”
- “AI that guarantees interviews”

---

## 15. Do / Don’t

### Do

- Use generous whitespace.
- Keep layouts grid-based.
- Use soft rounded cards.
- Use semantic accent colors.
- Make all text readable.
- Keep actions obvious.
- Use short UI explanations.
- Show BA/SA thinking through concise notes.
- Keep secrets and sensitive data visually protected.

### Don’t

- Do not use dense text blocks.
- Do not use neon gradients.
- Do not overuse shadows.
- Do not add too many colors.
- Do not make every card colorful.
- Do not show full API keys after saving.
- Do not create UI that looks like a crypto/AI hype product.
- Do not make the landing page feel like a manipulative sales funnel.

---

## 16. AI Designer Prompt Add-on

Use this instruction when giving the style to an AI design tool:

~~~text
Design a light, professional SaaS landing page using the ResumAIner Light Design DNA.
Use a clean enterprise-productivity style with soft gray-blue backgrounds, white rounded cards, subtle borders, Inter-like typography, emerald primary actions, blue informational links, and restrained semantic accents.
The page must feel trustworthy, structured, calm, and portfolio-ready.
Avoid neon AI gradients, dark futuristic themes, aggressive marketing, and clutter.
Prioritize readability, spacing, and clear product explanation.
~~~

---

## 17. CSS Token Starter

~~~css
:root {
  --background-canvas: #F6F7FB;
  --background-surface: #FFFFFF;
  --background-subtle: #FBFCFE;
  --background-control: #F7FAFE;

  --text-primary: #10233F;
  --text-secondary: #5D718B;
  --text-muted: #8091A7;
  --text-inverse: #FFFFFF;

  --border-default: #D8DEE9;
  --border-soft: #E3E8F0;
  --border-subtle: #E8EDF4;
  --border-control: #E5EAF2;

  --accent-primary: #0F9D7A;
  --accent-blue: #2F6BFF;
  --accent-violet: #7C3AED;
  --accent-warning: #D97706;
  --accent-error: #C2410C;
  --accent-teal: #0891B2;

  --accent-bg-blue: #EEF4FF;
  --accent-bg-success: #F2FFF9;
  --accent-bg-violet: #F5F3FF;
  --accent-bg-warning: #FFF7ED;
  --accent-bg-danger: #FFF1F2;
  --accent-bg-table: #F8FBFF;

  --radius-shell: 28px;
  --radius-card: 26px;
  --radius-panel: 24px;
  --radius-control: 14px;
  --radius-button: 18px;

  --shadow-card: 0 10px 30px rgba(16, 35, 63, 0.08);
  --shadow-modal: 0 22px 56px rgba(16, 35, 63, 0.18);
}
~~~
