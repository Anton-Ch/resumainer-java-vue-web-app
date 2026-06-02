# Auth Animation Refinement Notes

## What Changed in the Animation

### 1. Diagonal Curved Shapes
Two new CSS-only curved/diagonal shapes were added inside `.auth-card`:

- **`auth-diagonal--primary`**: A large rounded shape positioned at the top-right, with a subtle gradient from emerald (`rgba(15,157,122,0.10)`) to blue (`rgba(47,107,255,0.06)`). In login mode it's rotated (`rotate(6deg) skewY(38deg)`) to create a diagonal visual divide between the form and info columns. In register mode it rotates to flat (`rotate(0deg) skewY(0deg)`), creating a different visual emphasis.

- **`auth-diagonal--secondary`**: A counter-rotating shape positioned at the bottom-left, with a complementary gradient. Starts flat (below card, out of view). In register mode it rotates up (`rotate(-6deg) skewY(-38deg)`) with a staggered delay (500ms) to create depth perception.

Both shapes use `pointer-events: none` and `aria-hidden="true"` — they don't affect form interaction or accessibility.

### 2. Horizontal Slide (replacing Vertical Slide)
Login/Register switching changed from vertical slide+fade to horizontal slide:

- **Login → Register**: Login form slides left (`translateX(-64px)`) and out. Register form enters sliding in from right (`translateX(64px) → 0`).
- **Register → Login**: Register form slides right (`translateX(64px)`) and out. Login form enters sliding in from left (`translateX(-64px) → 0`).
- **Info panel**: Slides horizontally in the opposite direction of the form. When the form slides left, the info content slides right (and vice versa), creating a cohesive visual exchange.

### 3. Timing
- Shape movement: 1000ms with `cubic-bezier(0.16, 1, 0.3, 1)` easing
- Secondary shape: 1000ms with 500ms stagger delay
- Form panel slide: 350ms
- Info panel slide: 350ms
- Field stagger: 80ms per field (Email → Password → Confirm → Checkbox → Submit → Link)
- Exit → Enter gap: 280ms

### 4. Reduced Motion
Added `prefers-reduced-motion` support that disables all animations/transitions.

## How the Reference Was Adapted

The reference (`auth_page_reference.html/css/js`) uses:
- Two CSS curved shapes with `skewY` rotation to create a diagonal card split
- Neon cyan (`#27f4ff`) on dark background (`#25252b`)
- Font Awesome icons
- Lato font with white text

**Adaptation for ResumAIner:**
- Shape mechanic preserved (two shapes, rotate/skew transform, transform-origin)
- Colors changed to Light Enterprise SaaS palette: emerald → blue gradient at low opacity (10% → 6%)
- No Font Awesome — inline SVG icons preserved
- No dark background — existing `#F6F7FB` canvas preserved
- No neon — calm, professional gradient
- Stagger pattern preserved but simplified (CSS custom properties in reference → JS stagger in current)
- Info panel slides independently (same horizontal concept, adapted timing)

## Files Changed

Only `auth.html` was modified.

### Sections modified in auth.html:

| Section | What Changed |
|---------|-------------|
| HTML (lines 894-896) | Added two curved shape divs inside `.auth-card` |
| CSS (lines 615-760) | Replaced old stagger/slide CSS with diagonal shapes + horizontal slide + reduced-motion |
| CSS (line 837) | Added `.auth-diagonal { display: none }` to mobile responsive |
| CSS (lines 867-870) | Updated info panel exit width class selector for mobile |
| JS (lines 1412-1513) | Rewrote `switchMode()` to use horizontal slide + diagonal shape animation + stagger |
| Handoff notes (line ~1200) | Updated animation documentation |

## What Was Preserved

- ResumAIner brand direction
- Light Enterprise SaaS visual style
- All form fields, labels, validation states
- i18n system (EN/RU), locale detection, language switcher
- EN/RU language switch
- Password visibility toggle
- Remember me checkbox
- Submit handler, API simulation, error states
- Redirect overlay
- User Home / Admin Home routing
- Responsive mobile/tablet/desktop layouts
- All existing CSS tokens, typography, spacing

## QA Checklist

- [x] Login mode renders correctly
- [x] Register mode renders correctly
- [x] Login → Register animation plays (horizontal slide + diagonal shape)
- [x] Register → Login animation plays (reverse horizontal slide + diagonal shape)
- [x] Diagonal/curved gradient shape moves correctly
- [x] Form fields remain clickable (shapes have `pointer-events: none`)
- [x] Password visibility toggle works
- [x] Remember me checkbox works
- [x] Language switch EN/RU works
- [x] i18n strings resolve correctly in both modes
- [x] Typed values are not cleared on mode switch
- [x] Validation errors display correctly
- [x] Loading state displays correctly
- [x] Submit simulation works (bad@test.com, blocked@test.com, etc.)
- [x] Mobile layout works (diagonal shapes hidden on mobile)
- [x] No horizontal overflow (card has `overflow: hidden`)
- [x] `prefers-reduced-motion` respected
- [x] No Font Awesome added
- [x] No dark neon/cyberpunk style copied
- [x] No unrelated files changed

## Context7 MCP Availability

Context7 MCP was not required for this task. The changes were purely CSS/JSS animation refinements adapting existing patterns from the reference files. No new Vue, PrimeVue, or i18n library behavior needed verification.
