# ResumAIner Landing Page Redesign Notes

## Visual Direction Applied

The redesign uses **light enterprise SaaS** style from `light_design_dna.md` as the primary visual target.

### Palette Shift

| Token | Before (Warm Sand) | After (Light SaaS) |
|---|---|---|
| Page background | `#FAF7F0` (warm off-white) | `#F6F7FB` (soft gray-blue) |
| Card surface | `#FFFDF8` (warm paper) | `#FFFFFF` (white) |
| Primary text | `#17211D` (ink) | `#10233F` (deep navy) |
| Secondary text | `#53615B` (soft ink) | `#5D718B` (blue-gray) |
| Borders | `#E6DED2` (warm sand) | `#D8DEE9` (cool gray-blue) |
| Emerald | `#0F8A6A` | `#0F9D7A` (brighter) |
| Blue accent | — (none) | `#2F6BFF` (added) |
| Violet accent | — (none) | `#7C3AED` (added, restrained) |

### Radius & Shadows

- Card radius increased: 12px → 22px
- Button/input radius: 12px → 14px
- Shadow colors shifted: `rgba(23,33,29,…)` → `rgba(16,35,63,…)` to match new text color
- Softer, lower-opacity shadows overall

## Files Changed

| File | Change Type | Description |
|---|---|---|
| `landing.css` | Full rewrite | Replaced warm palette with light enterprise SaaS tokens. Updated all radii, shadows, card styling, hover states, FAQ, timeline, buttons, error page styles, responsive breakpoints |
| `landing.html` | Minimal safe edits | Replaced 7 inline style blocks with CSS classes (`.hero-eyebrow-row`, `.hero-subtitle`, `.hero-body`, `.hero-secondary-link`, `.trust-card-title`, `.trust-card-desc`, `.mockup-side-col`, `.mockup-tag`). No Thymeleaf attributes changed |
| `404.html` | Rewritten | Removed inline `<style>` block (styles now in `landing.css`). Wrapped error content in `.error-card` for polished centered card layout. All `th:*` bindings preserved |
| `500.html` | Rewritten | Same treatment as 404.html. All `th:*` bindings preserved |

## What Was Intentionally Preserved

- All Thymeleaf `th:text`, `th:href`, `th:classappend`, `th:onclick`, `th:attr` bindings
- All i18n message keys (no renames, no hardcoding)
- Language switcher with `?lang=en` / `?lang=ru`
- Active language class logic
- All section IDs (`#how-it-works`, `#features`, `#faq`)
- Navigation anchors and logo links
- FAQ native `<details>` / `<summary>` behavior
- Footer year JavaScript
- Favicon link
- Stylesheet link (`landing.css`)
- Self-hosted font files (Manrope for headings, Inter for body)
- Logo SVGs (unchanged)
- All 9 existing sections in the same order
- No new sections added or removed
- No Bootstrap, Tailwind, Font Awesome, or other heavy dependencies
- No robots, brains, sparkles, or AI-hype visuals

## How the Design Follows `light_design_dna.md`

1. **Canvas background** → `#F6F7FB` (soft light gray-blue)
2. **Card surfaces** → `#FFFFFF` (white)
3. **Subtle panels** → `#FBFCFE`
4. **Control background** → `#F7FAFE` (available as `--color-control`)
5. **Emerald primary** → `#0F9D7A` for action buttons, active states, completion
6. **Blue accent** → `#2F6BFF` for informational highlights (available as `--color-blue`)
7. **Violet accent** → `#7C3AED` for AI/metadata (available as `--color-violet`, used in accent-bg classes)
8. **Radius progression** → 8px (chips) / 14px (buttons) / 22px (cards) / 28px (major containers)
9. **Shadows** → `rgba(16, 35, 63, 0.06-0.10)` soft, low-opacity
10. **Borders** → `#D8DEE9` default, `#E3E8F0` soft, `#E8EDF4` subtle
11. **Typography** → Manrope (headings) + Inter (body) — existing fonts preserved
12. **Layout** → generous spacing, grid-based, clear hierarchy

## Safe Implementation Notes for Java Spring MVC / Thymeleaf

- The redesign is **CSS-first**: 90%+ of changes are in `landing.css`
- HTML changes are limited to replacing inline `style` attributes with CSS class names on the same elements
- No Thymeleaf attributes were removed, renamed, or restructured
- No semantic HTML changes
- No i18n key changes
- The `landing.css` file still uses the same `th:href="@{/static/css/landing.css}"` path
- The `@font-face` declarations still point to the same font file paths
- Error pages (404, 500) now get their visual styling from `landing.css` (inline `<style>` blocks removed)

## Manual Checklist for Final Review

- [ ] Landing page still uses external `landing.css`
- [ ] Self-hosted fonts (Manrope, Inter) still load correctly
- [ ] Logo appears in header and footer
- [ ] Favicon appears in browser tab
- [ ] All `th:text` keys resolve correctly (English and Russian)
- [ ] `th:href="${ctaUrl}"` works in hero CTA and final CTA
- [ ] Language switcher changes locale correctly
- [ ] FAQ accordion opens/closes with native `<details>`
- [ ] Mobile hamburger menu works
- [ ] 404 page links back to home
- [ ] 500 page links back to home
- [ ] No Bootstrap/Tailwind/Font Awesome added
- [ ] No stock images, robots, sparkles, or brains
- [ ] No horizontal overflow at any breakpoint
- [ ] Mobile layout is usable (stacks correctly, tap targets ≥ 44px)
- [ ] Desktop layout looks polished
- [ ] Final style is visibly closer to `light_design_dna.md`
- [ ] Result feels like a quality Capstone portfolio landing page
