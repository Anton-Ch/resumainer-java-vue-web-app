# E2E Testing Bug Report — Feature 007

**Date**: 2026-06-12
**Test method**: Playwright browser testing on Docker (localhost)
**Test account**: test@resumainer.com (registered via UI)

---

## Bug 1 — CRITICAL: i18n translation keys missing for Generate wizard pages

**Severity**: CRITICAL
**Location**: All 4 Generate wizard pages + Error page
**Affected URLs**: `/app/generate/vacancy`, `/app/generate/settings`, `/app/generate/review`, `/app/generate/export`, `/app/generate/error`

**Symptom**: All page titles, descriptions, form labels, and button texts show raw i18n keys instead of translated values:

```
generate.vacancy.title         (should be "Generate Resume — Vacancy")
generate.vacancy.description   (should be "Enter the target vacancy details")
generate.vacancy.vacancyTitle  (should be "Vacancy Title")
generate.vacancy.companyName   (should be "Company Name")
```

**Root cause**: The i18n keys (`generate.*`) were added to the Vue templates but the corresponding translations were never added to `frontend/src/i18n/en.json` and `frontend/src/i18n/ru.json`. When vue-i18n cannot find a key, it returns the key name itself.

**Missing keys** (partial list):
- `generate.vacancy.title`, `generate.vacancy.description`
- `generate.vacancy.vacancyTitle`, `generate.vacancy.vacancyDescription`
- `generate.vacancy.companyName`, `generate.vacancy.companyDescription`, `generate.vacancy.additionalComments`
- `generate.settings.title`, `generate.settings.description`, `generate.settings.languageMode`
- `generate.settings.adaptationSelection`, `generate.settings.aiModel`, `generate.settings.noModels`
- `generate.settings.includeCoverLetter`, `generate.settings.generate`
- `generate.review.title`, `generate.review.loading`, `generate.review.noData`
- `generate.review.selectLevel`, `generate.review.finalize`
- `generate.export.title`, `generate.export.description`, `generate.export.loading`
- `generate.export.downloadHtml`, `generate.export.copyPublicLink`, `generate.export.downloadPdf`
- `generate.export.openPdf`, `generate.export.copyCoverLetter`
- `generate.error.title`, `generate.error.description`, `generate.error.tryAgain`, `generate.error.changeSettings`

**Evidence**: Browser snapshot shows:
```
heading "generate.vacancy.title" [level=2]
paragraph: generate.vacancy.description
text: generate.vacancy.vacancyTitle *
text: generate.vacancy.companyName
button (no text — key not found)
```

---

## Bug 2 — CRITICAL: i18n translation keys missing for Education bilingual field labels

**Severity**: CRITICAL
**Location**: `/app/profile/education` — Education add/edit form
**Affected fields**: All 6 new bilingual RU/EN fields

**Symptom**: Form labels show raw i18n keys:

```
profile.education.institutionNameRu *   (should be "Institution name (RU)" or "Название заведения (RU)")
profile.education.institutionNameEn *   (should be "Institution name (EN)" or "Название заведения (EN)")
profile.education.degreeRu *            (should be "Degree (RU)" or "Степень (RU)")
profile.education.degreeEn *            (should be "Degree (EN)" or "Степень (EN)")
profile.education.fieldOfStudyRu *      (should be "Field of study (RU)" or "Специальность (RU)")
profile.education.fieldOfStudyEn *      (should be "Field of study (EN)" or "Специальность (EN)")
```

**Root cause**: Same as Bug 1 — i18n keys `profile.education.institutionNameRu` etc. were added to the EducationSection.vue template but never added to the translation files.

**Evidence**: Browser snapshot confirms all 6 labels show i18n keys.

---

## Bug 3 — MEDIUM: Education card does not switch language when toggling EN/RU

**Severity**: MEDIUM
**Location**: `/app/profile/education` — Education record card display
**Affected component**: `EducationSection.vue`

**Symptom**: When switching from RU to EN language, the education card still displays the Russian value for institution name, degree, and field of study.

**Root cause**: The RecordCard `:title` attribute uses `rec.institutionNameRu || rec.institutionNameEn` (line 102) and `formatDescription(rec)` uses locale-aware logic, but:
1. The card title always prefers `institutionNameRu` (Russian name shows first due to `||` operator)
2. The `formatDescription` uses locale to pick RU/EN fields — but this only affects the subtitle line, not the main card title
3. The side navigation panel "Education" counter shows 0 records even after adding (this may be a separate page refresh issue)

**Expected behavior**: When EN language is selected, card should show English institution name as the primary title and English degree/field in the description. The i18n issue (Bug 2) must be fixed first to properly test this.

---

## Bug 4 — LOW: Generate wizard stepper shows raw step labels

**Severity**: LOW
**Location**: `/app/generate/vacancy` — GenerateStepper component

**Symptom**: The stepper shows `1 Vacancy`, `2 Settings`, `3 Review`, `4 Export` — but these labels are hardcoded in the page templates (not using $t()), so they work in English but won't translate to Russian.

**Root cause**: The step labels are defined as hardcoded strings in each page's script section:
```typescript
const steps = [
  { label: 'Vacancy', route: '/generate/vacancy' },
  { label: 'Settings', route: '/generate/settings' },
  // ...
]
```
These should use `$t()` calls instead.

---

## Bug 5 — LOW: Review form `noData` text shows without styling

**Severity**: LOW
**Location**: `/app/generate/review`

**Symptom**: The `ReviewStepForm.vue` shows "generate.review.noData" as plain text when no review data is loaded. No styling or proper UX for empty state.

**Root cause**: Same i18n issue (Bug 1).

---

## Summary

| Bug | Severity | Area | Root Cause |
|-----|----------|------|------------|
| 1 | 🔴 CRITICAL | Generate pages (all 5) | i18n keys `generate.*` missing from en.json/ru.json |
| 2 | 🔴 CRITICAL | Education form labels | i18n keys `profile.education.institutionName*` missing |
| 3 | 🟡 MEDIUM | Education card language switch | Card title always prefers RU name |
| 4 | 🟢 LOW | Generate stepper | Hardcoded step labels, not using $t() |
| 5 | 🟢 LOW | Review empty state | Same i18n issue as Bug 1 |

### Console errors: None (0)
### All severity: i18n is the primary root cause for 4 out of 5 bugs

## Next Steps

1. **Fix Bug 1**: Add all `generate.*` keys to `frontend/src/i18n/en.json` and `frontend/src/i18n/ru.json`
2. **Fix Bug 2**: Add all `profile.education.institutionName*` keys to translation files
3. **Fix Bug 3**: Update card title logic to show locale-appropriate language field
4. **Fix Bug 4**: Replace hardcoded step labels with `$t()` calls
5. **Fix Bug 5**: Will be resolved by Bug 1 fix automatically

After fixes, rebuild Docker and re-test.
