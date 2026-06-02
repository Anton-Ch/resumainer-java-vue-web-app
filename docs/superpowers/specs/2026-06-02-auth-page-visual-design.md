# Auth Page (Login / Register) — Visual Design Specification

**Design DNA:** Light Enterprise SaaS
**Project:** ResumAIner
**Date:** 2026-06-02
**Status:** Final (ready for prototype)

---

## 1. LAYOUT SPECIFICATION

### 1.1 Canvas & Page Shell

```
Canvas: 2560×1440 (portfolio wireframe standard)
Page bg: #F6F7FB (canvas)
Min-height: 100vh
```

The auth page is a **centered single-card layout** on the canvas. No app chrome (no sidebar, no topnav) — this is a standalone entry point, same visual treatment as the Landing Page.

### 1.2 Auth Card Container

```
Position:   Centered horizontally + vertically in viewport
Max-width:  1060px
Width:      90vw (with max)
Height:     Auto (min 600px, max 680px)
Bg:         #FFFFFF
Radius:     26px
Border:     1.4px solid #E4E9F1
Shadow:     0 10px 30px rgba(16, 35, 63, 0.08)
Padding:    0 (internal layout handled by child columns)
```

### 1.3 Internal Two-Column Split

Inside the card: a CSS Grid or Flex row with two equal columns.

```
┌──────────────────────────────────────────────────┐
│ ┌─────────────────────┬─────────────────────────┐ │
│ │   LEFT (50%)        │   RIGHT (50%)           │ │
│ │   Form Column        │   Info Panel            │ │
│ │                      │   bg: #EEF4FF           │ │
│ │   padding: 48px      │   border-left: 1.4px    │ │
│ │                      │   solid #E4E9F1         │ │
│ ├─────────────────────┼─────────────────────────┤ │
│ │   Logo (top-left)   │   "Welcome Back" /      │ │
│ │   Lang (top-right)  │   "Join ResumAIner"     │ │
│ │                      │                         │ │
│ │   "Login" /          │   Value proposition     │ │
│ │   "Registration"     │   text                  │ │
│ │                      │                         │ │
│ │   Form fields        │   Decorative shapes     │ │
│ │   Submit button      │   (SVG, SaaS style)     │ │
│ │   Mode switch link   │                         │ │
│ └─────────────────────┴─────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

**Left Column (Form Area):**
```
Width:      50% (1fr in grid)
Padding:    48px 44px (top/bottom, left/right)
Display:    flex, column
Justify:    space-between (logo+lang at top, form in middle, switch link at bottom)
```

**Right Column (Info Panel):**
```
Width:      50% (1fr in grid)
Bg:         #EEF4FF (accent-bg blue)
Border-left: 1.4px solid #E4E9F1
Border-radius: 0 26px 26px 0 (only right corners rounded)
                     (since card has radius, the right panel's right edge needs radius)
Padding:    52px 44px
Display:    flex, column
Justify:    center (vertically centered content)
```

### 1.4 Left Column — Vertical Spacing (top to bottom)

```
┌─ Top row (24px height) ──────────────────────────┐
│  Logo (left)          Language Switcher (right)   │
│  gap: auto (flex justify-between)                 │
├───────────────────────────────────────────────────┤
│  ↓ 28px gap                                       │
├───────────────────────────────────────────────────┤
│  Title: "Login" / "Registration"                  │
│  font-size: 30px, weight 800, color #10233F       │
│  line-height: 1.2                                 │
├───────────────────────────────────────────────────┤
│  ↓ 24px gap                                       │
├───────────────────────────────────────────────────┤
│  Subtitle (optional, variant text):               │
│  "Access your ResumAIner dashboard"               │
│  font-size: 15px, weight 400, color #5D718B       │
│  line-height: 1.5                                 │
├───────────────────────────────────────────────────┤
│  ↓ 32px gap                                       │
├───────────────────────────────────────────────────┤
│  Form fields stack:                               │
│  ┌─ Email field ──────────────────────────────┐  │
│  │  height: 58px, radius: 14px                │  │
│  └────────────────────────────────────────────┘  │
│  ↓ 16px gap                                      │
│  ┌─ Password field ───────────────────────────┐  │
│  │  height: 58px, radius: 14px                │  │
│  └────────────────────────────────────────────┘  │
│  ↓ 16px gap                                      │
│  ┌─ Confirm password [Register mode only] ────┐  │
│  │  height: 58px, radius: 14px                │  │
│  └────────────────────────────────────────────┘  │
│  ↓ 12px gap                                      │
│  ┌─ "Remember me" checkbox row ───────────────┐  │
│  │  [Checkbox]  Remember me                   │  │
│  └────────────────────────────────────────────┘  │
├───────────────────────────────────────────────────┤
│  ↓ 24px gap                                       │
├───────────────────────────────────────────────────┤
│  ┌─ Submit button (full width) ───────────────┐  │
│  │  height: 58px, radius: 18px                │  │
│  │  bg: #0F9D7A, text: #FFFFFF               │  │
│  │  font-size: 17px, weight 700               │  │
│  └────────────────────────────────────────────┘  │
├───────────────────────────────────────────────────┤
│  ↓ 20px gap                                       │
├───────────────────────────────────────────────────┤
│  Mode switch link (centered):                     │
│  "Don't have an account?" link:"Register now!"    │
│  font-size: 15px, weight 400, color #5D718B      │
│  link color: #2F6BFF, weight 600                  │
└───────────────────────────────────────────────────┘
```

### 1.5 Right Column — Internal Layout

```
┌─ Decorative top shape ───────────────────────────┐
│  Abstract SaaS-style SVG curve or blob           │
│  Position: top-right, partially overflow card    │
│  Color: #D6E3FF or transparent gradient          │
├──────────────────────────────────────────────────┤
│  ↓ auto gap (flex grow)                          │
├──────────────────────────────────────────────────┤
│  Heading (Login mode):                           │
│  "Welcome Back"                                  │
│  28px, weight 800, color #10233F                 │
│  line-height: 1.2                                │
│                                                   │
│  Heading (Register mode):                        │
│  "Join ResumAIner"                               │
│  28px, weight 800, color #10233F                 │
│  line-height: 1.2                                │
├──────────────────────────────────────────────────┤
│  ↓ 16px gap                                      │
├──────────────────────────────────────────────────┤
│  Body text (Login mode):                         │
│  "Sign in to manage your profiles, generate      │
│   tailored resumes, and track applications."     │
│  15px, weight 400, color #5D718B, line-height 1.6│
│                                                   │
│  Body text (Register mode):                      │
│  "Create your professional profile once.         │
│   Generate AI-adapted resumes for every job      │
│   with one click."                               │
│  15px, weight 400, color #5D718B, line-height 1.6│
├──────────────────────────────────────────────────┤
│  ↓ 24px gap                                      │
├──────────────────────────────────────────────────┤
│  Feature highlights (Register mode only):        │
│  ┌─ bullet ───────────────────────────────┐     │
│  │  ✓ AI-powered resume adaptation        │     │
│  │  ✓ Smart cover letter generation       │     │
│  └────────────────────────────────────────┘     │
│  (3-4 bullets, icon: checkmark in emerald)      │
│  font: 14px, weight 500, color #10233F          │
│  icon: #0F9D7A, size 18px                       │
├──────────────────────────────────────────────────┤
│  ↓ auto gap (flex grow)                          │
├──────────────────────────────────────────────────┤
│  Decorative bottom shape                         │
└──────────────────────────────────────────────────┘
```

---

## 2. COMPONENT TREE (PrimeVue)

| Element | PrimeVue Component | Notes |
|---|---|---|
| Card wrapper | `<Panel>` or custom `<div>` with card classes | Plain div with CSS class is preferred for full control |
| Email field | `<InputText>` with `icon` slot | Use `pi pi-envelope` |
| Password field | `<Password>` with toggle mask | Use `:feedback="false"` for login (no strength meter on login, only on register) |
| Confirm password | `<Password>` with `:feedback="false"` | Register mode only |
| Remember me | `<Checkbox>` with `<label>` | v-model Boolean |
| Submit button | `<Button>` with `icon` slot | `:loading` prop for spinner state |
| Language switch | `<SelectButton>` or two `<Button>` pills | EN / RU, toggle value |
| Mode switch link | Plain `<a>` or `<router-link>` | Styled as blue text link, triggers flip animation |
| Logo | `<img>` SVG | Embedded inline SVG or `<router-link>` to `/` |

**Non-PrimeVue / purely custom:**
- Right panel — plain `<div>` with CSS
- Decorative shapes — plain `<svg>` or `<div>` with CSS pseudo-elements
- Auth error message — plain `<div>` with red text
- Inline validation errors — `<small>` tag below each field

---

## 3. COLOR USAGE PER ELEMENT (All States)

### 3.1 Form Column Background
```
Default:  #FFFFFF  (inherited from card)
```

### 3.2 Info Panel Background
```
Default:  #EEF4FF  (accent-bg blue)
```

### 3.3 Logo
```
Default:  #10233F  (text primary) — or multi-color SVG
Hover:    opacity 0.85
```

### 3.4 Language Switcher Pills
```
┌───────────────────────┬────────────┬───────────┬───────────┐
│ State                 │ Bg         │ Text      │ Border    │
├───────────────────────┼────────────┼───────────┼───────────┤
│ Active (selected)     │ #EEF4FF    │ #2F6BFF   │ none      │
│ Inactive              │ transparent │ #8091A7   │ #D9E1EC   │
│ Hover (inactive)      │ #F0F2F5    │ #5D718B   │ #C8D0DC   │
└───────────────────────┴────────────┴───────────┴───────────┘
Border-radius: 8px
Padding: 6px 16px
Font: 13px, weight 700, Inter
```

### 3.5 Page Title
```
Default:  #10233F  (text primary)
```

### 3.6 Page Subtitle
```
Default:  #5D718B  (text secondary)
```

### 3.7 Form Labels (above each field, if used)
```
Default:  #5D718B  (text secondary)
Font: 14px, weight 800, Inter
Letter-spacing: 0.01em
```

### 3.8 Input Fields (InputText / Password)

| State | Border | Bg | Text | Placeholder | Icon |
|---|---|---|---|---|---|
| Default | `#E5EAF2` 1.4px | `#FFFFFF` | `#10233F` | `#8091A7` | `#8091A7` |
| Hover | `#D0D8E6` 1.4px | `#FFFFFF` | `#10233F` | `#8091A7` | `#5D718B` |
| Focus | `#2F6BFF` 1.8px | `#FFFFFF` | `#10233F` | — | `#2F6BFF` |
| Filled | `#D8DEE9` 1.4px | `#FFFFFF` | `#10233F` | — | `#5D718B` |
| Error | `#C2410C` 1.4px | `#FFF8F5` | `#10233F` | — | `#C2410C` |
| Disabled | `#E5EAF2` 1.4px | `#F6F7FB` | `#8091A7` | `#B0BCCB` | `#B0BCCB` |

```
Focus ring:  0 0 0 3px rgba(47, 107, 255, 0.12)  (blue glow)
Error ring:  0 0 0 3px rgba(194, 65, 12, 0.08)   (error glow)
Input height: 58px
Horizontal padding: 18px
Border-radius: 14px
```

### 3.9 Password Eye Toggle Icon
```
Default:  #8091A7
Hover:    #5D718B
Active:   #2F6BFF
```

### 3.10 Remember Me Checkbox

| State | Checkbox bg | Checkbox border | Check icon | Label text |
|---|---|---|---|---|
| Unchecked | `#FFFFFF` | `#D0D8E6` 2px | — | `#5D718B` |
| Checked | `#0F9D7A` | `#0F9D7A` 2px | `#FFFFFF` | `#10233F` |
| Hover (unchecked) | `#F6F7FB` | `#B8C3D4` 2px | — | `#5D718B` |
| Focus | `#FFFFFF` | `#2F6BFF` 2px | — | `#5D718B` |

```
Checkbox size: 20×20px
Border-radius: 6px (slightly rounded, not fully square)
Label font: 15px, weight 400, Inter
Gap between checkbox and label: 10px
```

### 3.11 Submit Button (Primary Emerald)

| State | Bg | Text | Border | Shadow |
|---|---|---|---|---|
| Default | `#0F9D7A` | `#FFFFFF` | none | none |
| Hover | `#0C8467` | `#FFFFFF` | none | `0 4px 12px rgba(15, 157, 122, 0.3)` |
| Active (pressed) | `#0A6F56` | `#FFFFFF` | none | `0 1px 4px rgba(15, 157, 122, 0.2)` |
| Focus | `#0F9D7A` | `#FFFFFF` | none | `0 0 0 3px rgba(15, 157, 122, 0.25)` |
| Loading | `#0C8467` | `#FFFFFF` | none | none (spinner visible) |
| Disabled | `#B8DED2` | `#FFFFFF` | none | none |

```
Height: 58px
Border-radius: 18px
Font: 17px, weight 700, Inter
Letter-spacing: 0.01em
Cursor: pointer (default), not-allowed (disabled)
Transition: all 200ms ease
```

### 3.12 Spinner (inside Loading button)
```
Color: #FFFFFF
Size: 22px
Stroke-width: 2.5px
```

### 3.13 Mode Switch Link
```
Text:     "Don't have an account?"  — #5D718B, weight 400
Link:     "Register now!" — #2F6BFF, weight 600
Hover:    "Register now!" — #1A54D9, text-decoration: underline
```

### 3.14 Right Panel — Heading
```
Default:  #10233F (text primary)
```

### 3.15 Right Panel — Body Text
```
Default:  #5D718B (text secondary)
```

### 3.16 Right Panel — Feature Bullets (Register mode)
```
Icon:     ✓ — #0F9D7A (emerald), 18px
Text:     #10233F, 14px, weight 500, Inter
```

### 3.17 Auth Error Message (above form, shown on error)

| Element | Color |
|---|---|
| Bg | `#FFF5F0` |
| Border | `#FDDCC8` 1.2px |
| Border-radius | 12px |
| Text | `#C2410C` |
| Icon (triangle-exclamation) | `#C2410C` |
| Padding | 14px 18px |
| Font | 14px, weight 500, Inter |

```
Position: between subtitle and first form field
Only visible when auth error is present
```

### 3.18 Inline Validation Error (under each field)
```
Text:     #C2410C
Font:     13px, weight 500, Inter
Margin-top: 6px
Icon:     none (text only, unless field icon already indicates error)
```

---

## 4. TYPOGRAPHY PER ELEMENT

| Element | Font | Size | Weight | Color | Line-Height | Letter-Spacing |
|---|---|---|---|---|---|---|
| Page title | Manrope | 30px | 800 | #10233F | 1.2 | -0.01em |
| Page subtitle | Inter | 15px | 400 | #5D718B | 1.5 | 0 |
| Form labels | Inter | 14px | 800 | #5D718B | 1.3 | 0.01em |
| Input text | Inter | 15px | 500 | #10233F | 1.4 | 0 |
| Input placeholder | Inter | 15px | 400 | #8091A7 | 1.4 | 0 |
| Submit button | Inter | 17px | 700 | #FFFFFF | 1.2 | 0.01em |
| Remember me label | Inter | 15px | 400 | #5D718B | 1.4 | 0 |
| Mode switch base | Inter | 15px | 400 | #5D718B | 1.5 | 0 |
| Mode switch link | Inter | 15px | 600 | #2F6BFF | 1.5 | 0 |
| Lang pill active | Inter | 13px | 700 | #2F6BFF | 1 | 0.02em |
| Lang pill inactive | Inter | 13px | 600 | #8091A7 | 1 | 0.02em |
| Right heading | Manrope | 28px | 800 | #10233F | 1.2 | -0.01em |
| Right body | Inter | 15px | 400 | #5D718B | 1.6 | 0 |
| Feature bullet | Inter | 14px | 500 | #10233F | 1.5 | 0 |
| Auth error | Inter | 14px | 500 | #C2410C | 1.4 | 0 |
| Inline error | Inter | 13px | 500 | #C2410C | 1.3 | 0 |

---

## 5. SPACING GRID

### 5.1 Base Spacing Units (8px grid)

```
2px, 4px, 8px, 12px, 16px, 20px, 24px, 28px, 32px, 36px, 40px, 44px, 48px, 52px
```

### 5.2 Vertical Spacing Map (Form Column)

```
From top edge of left column:  48px
Logo row to title:              28px
Title to subtitle:              8px (or omit subtitle gap)
Subtitle to first field:        32px
Between form fields:            16px
Last field to checkbox:         12px
Checkbox row to submit button:  24px
Submit button to switch link:   20px
Switch link to bottom edge:     48px
```

### 5.3 Horizontal Spacing Map

```
Left column padding:       48px top, 44px left+right
Right column padding:      52px top/bottom, 44px left+right
Logo from left edge:       44px (aligns with text)
Lang switcher from right:  44px (aligns with text)
Input icon from left edge: 18px (inside input padding)
Input text from left edge: 18px
Checkbox to label gap:     10px
```

### 5.4 Gap Between Columns

```
Grid/flex gap: 0 (columns touch, separated by border)
Divider: 1.4px solid #E4E9F1 (right column's left border)
```

---

## 6. ANIMATION SPECIFICATION

### 6.1 Login ↔ Register Toggle Mechanism

Based on CodePen reference (ZEmgjbB): **link-based switching** with **staggered slide transitions**.

**Trigger:** User clicks "Register now!" or "Log In" text link (not tabs, not buttons).

**Containers:** Both form and info panel content are wrapped in a shared container. The form portion animates as a **single group**; individual fields within stagger on entry.

### 6.2 Transition: Form Side (Left Column)

Direction depends on mode switch direction:

- **Login → Register**: Form elements slide **up and out**, new Register fields slide **up and in**
- **Register → Login**: Form elements slide **down and out**, Login fields slide **down and in**

Alternative (more elegant): **Cross-fade with vertical slide** — the old form fades + slides 20px away, new form fades + slides 20px in from opposite direction.

```
Transition type:  slide-vertical + fade
Old content:      opacity 1→0, translateY 0→-20px
New content:      opacity 0→1, translateY 20px→0
```

### 6.3 Staggered Delays (Per Field)

Each field within the appearing form animates with a **staggered delay**:

```
Field 1 (Email):       delay: 0ms
Field 2 (Password):    delay: 80ms
Field 3 (Confirm):     delay: 160ms  (Register only)
Checkbox:              delay: 240ms
Submit button:         delay: 320ms
Mode switch link:      delay: 400ms
```

When leaving, fields leave simultaneously (no stagger).

### 6.4 Transition: Info Panel (Right Column)

The right panel content **cross-fades** simultaneously with the form switch:

```
Old heading+text:   opacity 1→0, translateY 0→-8px
New heading+text:   opacity 0→1, translateY 8px→0
Delay:              60ms (slightly after form starts)
Duration:           350ms
```

The background (#EEF4FF) stays constant — only content changes.

### 6.5 Timing & Easing

```
Duration (form exit):   250ms
Duration (form enter):  400ms
Easing:                 cubic-bezier(0.25, 0.1, 0.25, 1.0)  — ease-in-out smooth
                        AKA "ease-in-out" with gentle curve

Alternative easing:     cubic-bezier(0.16, 1, 0.3, 1)  — "emphasized ease-out"
                        More natural, Apple-style deceleration
                        RECOMMENDED for this spec

Cross-fade duration:    300ms
```

### 6.6 Total Animation Timeline

```
0ms:    Exit animation starts (form slides out)
250ms:  Exit complete, content hidden
300ms:  Enter animation starts (new form slides in)
700ms:  Enter complete, all fields visible (400ms + 400ms stagger)
```

The right panel cross-fade runs from ~60ms to ~360ms (300ms duration), completing before the form fully enters.

### 6.7 Loading State (Submit button)

```
Button text fades out:      150ms
Spinner fades in:           150ms
Button bg shifts to loading: 200ms
```

### 6.8 Error State (Shake + Error Appearance)

```
Shake animation on card/form:  300ms, 3 oscillations (x: 0→-4→4→-2→2→0)
Error message slides in:       250ms, translateY -10px→0, opacity 0→1
Input borders turn red:        150ms
```

### 6.9 Implementation Notes (Vue)

```javascript
// Use <TransitionGroup> with staggered children
// Each field gets a dynamic :style with transition-delay based on index

// Vue Transition name: "auth-slide"
// CSS:
.auth-slide-enter-active {
  transition: all 400ms cubic-bezier(0.16, 1, 0.3, 1);
}
.auth-slide-leave-active {
  transition: all 250ms ease-in;
}
.auth-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.auth-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

// Per-child delay via inline style:
// :style="{ transitionDelay: `${index * 80}ms` }"
```

---

## 7. ICON CHOICES

| Element | Icon Name (PrimeIcons) | Position | Size | Notes |
|---|---|---|---|---|
| Email field | `pi pi-envelope` | Left inside input | 16px | Outline style |
| Password field | `pi pi-lock` | Left inside input | 16px | Outline style |
| Eye toggle (show) | `pi pi-eye` | Right inside password | 16px | Clickable |
| Eye toggle (hide) | `pi pi-eye-slash` | Right inside password | 16px | Clickable |
| Submit button (idle) | none | — | — | Text only |
| Submit button (loading) | `pi pi-spinner pi-spin` | Left of text | 18px | Animated spin |
| Checkbox check | `pi pi-check` | Inside checkbox | 12px | White on emerald |
| Auth error | `pi pi-exclamation-triangle` | Left of error text | 16px | Red (#C2410C) |
| Feature bullet | Custom ✓ (Unicode) or `pi pi-check-circle` | Left of text | 18px | Emerald (#0F9D7A) |
| Language (EN) | `pi pi-globe` or text "EN" | Within pill | 12px | Only text preferred |

**Icon style guide:**
- All input icons: `#8091A7` (default), `#2F6BFF` (focus), `#C2410C` (error)
- Icons should be placed inside a wrapper `<i>` with padding to separate from text
- Input icon padding: 18px from left edge of input
- Eye toggle: 14px from right edge of input

---

## 8. MOBILE ADAPTATION

### 8.1 Breakpoints

| Breakpoint | Name | Layout |
|---|---|---|
| ≥ 1060px | Desktop | Two columns side by side (50/50) |
| 768px – 1059px | Tablet | Two columns, narrower padding |
| < 768px | Mobile | Single column stacked |

### 8.2 Tablet Adaptation (768px – 1059px)

```
Card max-width: 90vw (no hard limit)
Left column padding:  36px
Right column padding: 36px
Title size:           26px (down from 30px)
Right heading:        24px (down from 28px)
Form field gap:       14px (down from 16px)
```

### 8.3 Mobile Adaptation (< 768px)

Layout transforms from side-by-side to stacked:

```
┌──────────────────────────────────────────┐
│  ┌─ Right Panel (compact header) ──────┐ │
│  │  bg: #EEF4FF                        │ │
│  │  border-radius: 26px 26px 0 0       │ │
│  │  padding: 28px 24px                 │ │
│  │  Heading: 22px                      │ │
│  │  Body: hidden (or 1 short line)     │ │
│  │  Feature bullets: hidden on mobile  │ │
│  └──────────────────────────────────────┘ │
│  ┌─ Left Panel (form) ─────────────────┐ │
│  │  padding: 28px 24px                 │ │
│  │  border-left: none                  │ │
│  └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

**Detailed mobile adjustments:**

```
Card width:         100vw (full width, remove margins)
Card max-width:     none
Card border-radius: 0 (edge to edge on mobile)
                     OR 26px with 16px margin (recommended)

Right panel:
  - Becomes a compact header strip (80-100px tall)
  - bg: #EEF4FF
  - border-radius: 26px 26px 0 0
  - Shows only heading, no body text, no feature bullets
  - decorative shapes: hidden on mobile
  - Heading: 22px, weight 800, #10233F

Left panel:
  - Takes full width
  - border-left: none
  - padding: 28px 20px
  - Logo + lang in one row, flex justify-between
  - Title: 26px
  - Subtitle: hidden on mobile (save space)
  - Form fields: full width
  - Submit button: full width

Spacing reduction on mobile:
  Top padding:          28px (from 48px)
  Title to first field: 24px (from 32px)
  Between fields:       14px (from 16px)
  Bottom padding:       28px (from 48px)
```

### 8.4 Small Mobile (< 400px)

```
Right panel: hidden entirely (just show form)
Form padding: 20px
Title: 22px
All fields remain full width
```

---

## 9. SUMMARY TABLE: All Hex Colors Used

```
Canvas bg:              #F6F7FB
Card bg:                #FFFFFF
Card border:            #E4E9F1
Card shadow:            rgba(16, 35, 63, 0.08)

Text primary:           #10233F
Text secondary:         #5D718B
Text muted:             #8091A7

Accent emerald (btn):   #0F9D7A
Emerald hover:          #0C8467
Emerald active:         #0A6F56
Emerald disabled:       #B8DED2
Emerald glow:           rgba(15, 157, 122, 0.25)

Accent blue (links):    #2F6BFF
Blue hover:             #1A54D9
Blue accent-bg:         #EEF4FF
Blue focus glow:        rgba(47, 107, 255, 0.12)

Accent violet:          #7C3AED  (not used on auth page)

Error:                  #C2410C
Error bg:               #FFF5F0
Error border:           #FDDCC8
Error glow:             rgba(194, 65, 12, 0.08)

Border default:         #D8DEE9
Border control:         #E5EAF2
Border hover:           #D0D8E6
Border dark:            #B8C3D4
Border card:            #E4E9F1
```

## 10. DESIGN BORDERS REFERENCE

```
Card border:              1.4px solid #E4E9F1
Right panel divider:      1.4px solid #E4E9F1 (border-left)
Input border:             1.4px solid #E5EAF2
Input focus border:       1.8px solid #2F6BFF
Input error border:       1.4px solid #C2410C
Checkbox border:          2px solid #D0D8E6
Checkbox checked border:  2px solid #0F9D7A
Error message border:     1.2px solid #FDDCC8
Language pill border:     1.2px solid #D9E1EC
```

---

## STATUS_UPDATE_FOR_ORCHESTRATOR

```
ID: auth-page-visual-spec
STATUS: COMPLETE
FILES:
  - docs/superpowers/specs/2026-06-02-auth-page-visual-design.md
CONTENT:
  Full visual design specification for Vue SPA auth page (Login/Register)
  with link-based staggered slide transitions in Light Enterprise SaaS DNA.
SECTIONS:
  1. Layout specification (desktop 2560×1440, responsive)
  2. PrimeVue component tree
  3. Color usage per element (default, hover, focus, error, disabled, active)
  4. Typography per element (font, size, weight, color, line-height)
  5. Spacing grid (8px base)
  6. Animation specification (timing, easing, staggered delays)
  7. Icon choices (PrimeIcons)
  8. Mobile adaptation (tablet + mobile breakpoints)
  9. Hex color summary table
  10. Design borders reference
NOTES:
  - reference CodePen (ZEmgjbB) adapted to SaaS style
  - staggered slide-vertical + fade with cubic-bezier(0.16, 1, 0.3, 1)
  - per-field delay: 0ms, 80ms, 160ms, 240ms, 320ms, 400ms
  - right panel cross-fades independently
  - mobile: right panel collapses to compact header strip
NEXT:
  Wait for designer review. After approval → transition to Vue implementation.
```
