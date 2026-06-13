# Bug Report #2 — Generate Wizard Visual Issues

**Date**: 2026-06-12
**Test method**: Playwright visual inspection + DOM analysis + prototype comparison
**Prototype reference**: `specs/007-resume-generation/spec_input_files/frontend prototype/`

---

## Bug 6 — CRITICAL: Wrong CSS class names — no styling applied to Generate wizard

**Severity**: CRITICAL
**Location**: All generate pages and components
**Affected files**: 
- `GenerateVacancyPage.vue` — uses `generate-layout`, `generate-card`, `card-title`, `card-desc`
- `GenerateSettingsPage.vue` — same classes
- `GenerateReviewPage.vue` — same classes  
- `GenerateExportPage.vue` — same classes
- `GenerateErrorPage.vue` — uses `error-card`
- `VacancyStepForm.vue` — uses `vacancy-form`, `form-group`, `form-actions`
- `SettingsStepForm.vue` — uses `settings-form`, `form-group`
- `ReviewStepForm.vue` — uses `review-form`

**Symptom**: All generate wizard pages render with NO visual styling:
- Cards have no background, border, or padding
- Form fields have no proper layout, labels just stack as plain text
- Buttons are invisible (no label rendering or zero-size)
- The whole page looks like unstyled HTML

**Root cause**: The production implementation uses CSS class names that DON'T EXIST in `frontend/src/assets/styles/vue_general.css`:

| Production class | Should be | CSS has it? |
|-----------------|-----------|-------------|
| `generate-layout` | - | ❌ Not needed if using `vue-card` |
| `generate-card` | `vue-card` | ✅ `.vue-card` exists |
| `card-title` | - | ❌ Not needed, prototype uses `<h2>` |
| `card-desc` | - | ❌ Not needed |
| `vacancy-form` | - | ❌ Not needed |
| `form-group` | `vue-form-group` | ✅ `.vue-form-group` exists |
| `form-actions` | - | ❌ Not needed |
| `error-card` | `vue-card` | ✅ |

The production was written from scratch with new CSS class names instead of reusing the existing `.vue-card`, `.vue-form-group`, `.vue-form-label` classes from the prototype.

**Evidence**: DOM snapshot shows `box` dimensions are all full-width (1526px) with no card styling. Button has `box=0,408,16,6` — essentially invisible.

---

## Bug 7 — HIGH: Missing `common.continue` i18n key

**Severity**: HIGH
**Location**: `VacancyStepForm.vue` line 24
**Current**: `:label="$t('common.continue')"`
**Reality**: Key `common.continue` was added under `generate.vacancy.continue`, NOT under `common.continue`

**Symptom**: The Continue button on the Vacancy step has no visible label text. The button renders as a tiny 16x6px element (just the icon arrow with no text).

**Root cause**: The `common` section in en.json/ru.json has only `loading`. The continue key was added to `generate.vacancy.continue` but the template references `common.continue`.

**Fix needed**: Either add `"continue": "Continue"` to the `common` section, or change the template to use `generate.vacancy.continue`.

---

## Bug 8 — MEDIUM: Prototype CSS classes not reused

**Severity**: MEDIUM
**Location**: All generate pages and components

**Symptom**: The prototype uses a well-structured set of CSS classes that create consistent card-based layouts:
- `vue-card` — card container with white background, border, padding
- `vue-card-header` — optional header  
- `vue-form-group` — form field wrapper
- `vue-form-label` — label styling
- `vue-h4`, `vue-body-sm` — typography
- `vue-input-error` — error state styling

The production implementation ignored all these existing classes and created new ones, resulting in unstyled pages.

---

## Summary

| Bug | Severity | Root Cause |
|-----|----------|------------|
| **Bug 6** | 🔴 CRITICAL | All generate pages use CSS class names that don't exist |
| **Bug 7** | 🟠 HIGH | `common.continue` i18n key missing for button label |
| **Bug 8** | 🟡 MEDIUM | Existing prototype CSS classes not reused |

### Visual comparison

| Element | Prototype | Production |
|---------|-----------|------------|
| Page card | `.vue-card` with white bg, border, rounded corners | `.generate-card` — no styling at all |
| Form labels | `.vue-form-label` — bold, proper spacing | `.form-label` — no styling |
| Form groups | `.vue-form-group` — flex column, gap | `.form-group` — no styling |
| Submit button | PrimeVue Button with label | Invisible (missing i18n key + no styling) |
| Page title | `<h2>` with proper typography | `<h2>` with `.card-title` — no styling |
| Stepper | Custom stepper with active/completed states | Same stepper, visually ok |

### Fix recommendation

1. **Rewrite all generate pages/components** to use the existing `.vue-card`, `.vue-form-group`, `.vue-form-label`, `.vue-form-actions` CSS classes from `vue_general.css`
2. **Fix the button label** by adding `"continue"` to the `common` section or updating the template reference
3. **Remove custom unused CSS classes** like `generate-card`, `card-title`, `generate-layout`, `form-group`, `form-actions`
4. **Use prototype's `vue-form-actions` pattern** for form submit buttons
