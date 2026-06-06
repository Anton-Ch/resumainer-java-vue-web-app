# OpenCode Implementation Brief — ResumAIner User Home / Resume Workspace

**Document purpose:** Give OpenCode a clear implementation brief for building the real User Home feature inside the existing ResumAIner project.

**Important:** This document is for production implementation. Do not copy OpenDesign demo architecture, demo routes, CDN setup, or mock-only decisions into production.

---

## 1. Implementation goal

Replace the current UserHomePage placeholder/token dashboard with a production-ready MVP **Resume workspace**.

The real User Home must:

- guide the user based on profile readiness;
- show profile status and resume summary;
- list saved resumes with search/filter/sort/pagination;
- open Resume Details modal from table row and Last Resume card;
- support PDF/public link/cover letter actions;
- use existing Vue 3 + Vite + PrimeVue project structure;
- use full EN/RU i18n;
- preserve the existing landing page at site root.

---

## 2. Current project context

The current frontend stack already uses:

- Vue 3;
- Vite;
- TypeScript;
- vue-router;
- vue-i18n;
- PrimeVue;
- PrimeVue Aura theme;
- shared CSS file `frontend/src/assets/styles/vue_general.css`.

Current UserHomePage has placeholder cards for token counts and basic action buttons. This must be replaced.

Current auth routes are `/login`, `/register`, `/home`, `/admin`. This feature must move Vue app routes under `/app/...` while keeping root `/` for the existing landing page.

---

## 3. Hard production rules

### Must do

- Use existing Vite/Vue/PrimeVue production setup.
- Use existing `vue_general.css` tokens and style direction.
- Keep the landing page at `/`.
- Move SPA routes under `/app/...`.
- Use real vue-router route guards.
- Use real auth state from backend/session check.
- Use i18n for every visible string.
- Use PrimeVue components where suitable.
- Implement real loading/empty/error states.

### Must not do

- Do not use CDN setup in production.
- Do not create four separate production User Home routes.
- Do not add demo state switch.
- Do not keep token cards on User Home.
- Do not expose Admin nav item to regular users.
- Do not rely on hidden UI links as security.
- Do not show token usage, model name, or technical metadata to regular users on User Home.
- Do not make a separate mobile card-list implementation for saved resumes in MVP.

---

## 4. Routing requirements

### Root site behavior

`/` must serve the existing landing page.

The Vue SPA must live under:

`/app/...`

### Production SPA routes

- `/app/auth` — existing Auth page.
- `/app/home` — User Home / Resume workspace.
- `/app/profile/contact`
- `/app/profile/experience`
- `/app/profile/education`
- `/app/profile/projects`
- `/app/profile/courses`
- `/app/profile/additional`
- `/app/generate/vacancy`
- `/app/generate/settings`
- `/app/generate/review`
- `/app/generate/export`
- `/app/admin`

### Redirect behavior

After successful login/register:

- USER → `/app/home`
- ADMIN → `/app/home`

Admin users see an additional `Admin` navbar item.

Logout:

1. call backend logout endpoint;
2. clear frontend auth state;
3. redirect to `/app/auth`.

Unknown SPA route:

- unauthenticated → `/app/auth`;
- authenticated → `/app/home` or 404 page if available.

---

## 5. Header / navbar implementation

Create/extend shared `AppHeader.vue`.

### Left

- ResumAIner logo.
- Logo click navigates to `/app/home`.

### Main navigation

- `Home` → `/app/home`
- `My Profile` → `/app/profile/contact`
- `Generate Resume` → `/app/generate/vacancy`
- `Admin` → `/app/admin`, shown only when `role === 'ADMIN'`

### Right

- Language switcher `EN / RU`.
- Logout icon button.

Logout button:

- PrimeIcon: `pi pi-sign-out`.
- Tooltip from i18n.
- `aria-label` from i18n.
- Use a recognizable icon-only button.

Do not add Account menu in this feature.

---

## 6. Security requirements for Admin

UI behavior:

- Render `Admin` nav item only for `role === 'ADMIN'`.

Route guard:

- `/app/admin` requires authentication and admin role.
- Non-admin direct access redirects to `/app/home` or shows 403.

Backend:

- All future admin endpoints must verify admin role server-side.
- Hiding the link in the UI is not security by itself.

---

## 7. Page identity and layout

Navbar label:

- EN: `Home`
- RU: `Главная`

H1:

- EN: `Resume workspace`
- RU: `Рабочий центр`

Layout:

- main container max-width: `1280px`;
- medium density;
- practical SaaS dashboard feel;
- guided block first;
- summary cards second;
- saved resumes table third.

Do not create a huge landing-style hero.

---

## 8. Profile readiness rule

Implement product readiness separately from database `NOT NULL` checks.

Formula:

`profileReady = contactComplete && hasWorkExperience && hasEducation`

Where:

`contactComplete = fullName && email && phone && location`

`hasWorkExperience = at least one non-deleted complete work experience record`

`hasEducation = at least one non-deleted complete education record`

Course/certificate is not required for MVP readiness.

### Work experience completeness

A work experience record counts as complete when it has at least:

- job title;
- company name;
- start date;
- role/job description;
- end date OR current role flag.

### Education completeness

An education record counts as complete when it has at least:

- institution name;
- degree / qualification;
- start date/year or required date fields defined by the data model.

If exact backend field names differ, preserve the product rule and map to actual model fields.

---

## 9. User Home data architecture

Use two backend data sources.

### 1. Home summary endpoint

`GET /api/user/home`

Purpose:

- profile readiness;
- checklist status;
- summary cards;
- last resume card data.

Suggested response shape:

~~~json
{
  "profileReady": true,
  "profileChecklist": {
    "contactDetails": true,
    "workExperience": true,
    "education": true
  },
  "summary": {
    "savedResumesCount": 12,
    "profileStatus": "READY",
    "lastResumeId": 101
  },
  "lastResume": {
    "id": 101,
    "resumeTitle": "Business Analyst Resume",
    "vacancy": "Middle Business Analyst",
    "company": "Example Company",
    "language": "EN",
    "adaptationLevel": "BALANCED",
    "createdAt": "2025-01-09",
    "publicUrl": "/anton/business-analyst-example",
    "pdfUrl": "/api/resumes/101/pdf",
    "coverLetter": "..."
  }
}
~~~

### 2. Saved resumes paginated endpoint

`GET /api/resumes`

Query parameters:

- `search`
- `language`
- `adaptationLevel`
- `createdDate`
- `sort`
- `page`
- `size`

Example:

`GET /api/resumes?search=analyst&language=EN,RU&adaptationLevel=MINIMAL,BALANCED,MAXIMUM&createdDate=2025-01-09&sort=createdAt,desc&page=0&size=10`

Suggested response shape:

~~~json
{
  "items": [
    {
      "id": 101,
      "resumeTitle": "Business Analyst Resume",
      "vacancy": "Middle Business Analyst",
      "company": "Example Company",
      "language": "EN",
      "adaptationLevel": "BALANCED",
      "createdAt": "2025-01-09",
      "publicUrl": "/anton/business-analyst-example",
      "pdfUrl": "/api/resumes/101/pdf",
      "coverLetter": "..."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 12,
  "totalPages": 2
}
~~~

If backend is not ready yet, implement frontend with service functions and mock fallback only behind clearly named mock service files. Do not hardcode mock data inside components.

---

## 10. User Home states

Production has one `UserHomePage.vue` route.

It must render these states based on API data:

1. Ready user with resumes.
2. Incomplete profile, no resumes.
3. Ready profile, no resumes.
4. Loading state.
5. Error state.
6. No search results.

Do not create separate production routes for these states.

---

## 11. Guided next-step block

### If profile is incomplete

Show only completion guidance.

Title:

- EN: `Complete your profile first`
- RU: `Сначала заполни профиль`

Text:

- EN: `AI needs context about you. Add at least your contact details, work experience, and education. The more useful information you provide, the better resume you will get.`
- RU: `ИИ нужен контекст о тебе. Добавь хотя бы контакты, опыт работы и образование. Чем больше полезной информации ты укажешь, тем точнее получится резюме.`

CTA:

- EN: `Complete Profile`
- RU: `Заполнить профиль`

Show checklist:

1. Contact details
2. Work experience
3. Education

Each item shows:

- Done / Missing.

Each item links to the corresponding profile route.

### If profile is ready

Title:

- EN: `Your next best step`
- RU: `Следующий лучший шаг`

Primary action:

- EN title: `Generate your next resume`
- RU title: `Создай новое резюме`
- EN hint: `Start with a vacancy and get an adapted resume.`
- RU hint: `Начни с вакансии и получи адаптированное резюме.`
- EN CTA: `Generate Resume`
- RU CTA: `Создать резюме`
- EN tooltip: `Make a new awesome resume for a specific vacancy.`
- RU tooltip: `Создать сильное резюме под конкретную вакансию.`

Secondary action:

- EN title: `Update your profile`
- RU title: `Обнови профиль`
- EN hint: `Use this when your experience, education, skills, or contact details change.`
- RU hint: `Используй, если изменились опыт, образование, навыки или контакты.`
- EN CTA: `Update Profile`
- RU CTA: `Обновить профиль`
- EN tooltip: `Edit the profile data used for future resumes.`
- RU tooltip: `Изменить данные, которые будут использоваться в будущих резюме.`

Generate action must appear first.

---

## 12. Summary cards

Show exactly three cards.

1. Saved resumes
2. Profile status
3. Last resume

Last Resume card:

- if a last resume exists, make it clickable;
- click opens the same Resume Details modal as the table row.

Profile status card:

Ready:

- Value: Ready / Готов.
- Hint: You can generate resumes now. / Теперь можно создавать резюме.
- Link: Update profile / Обновить профиль.

Incomplete:

- Value: Needs info / Нужно заполнить.
- Hint: Add contact details, work experience, and education. / Добавь контакты, опыт работы и образование.
- Link: Complete profile / Заполнить профиль.

Use soft warning tone for incomplete, not red error styling.

---

## 13. Saved Resumes DataTable

Use PrimeVue `DataTable`, not custom table markup.

Required PrimeVue components/features:

- `DataTable`
- `Column`
- `Button`
- `InputText`
- `IconField`
- `InputIcon`
- `MultiSelect`
- `DatePicker`
- `Dialog`
- `ConfirmDialog`
- `Toast`
- `Skeleton`
- `Tooltip`

### Columns, exact order

1. Resume title
2. Vacancy
3. Company
4. Language
5. Adaptation level
6. Created

### Data formatting

`Created` format: `YYYY-MM-DD`.

Examples:

- `2025-12-31`
- `2025-01-09`

`Vacancy` and `Company`:

- truncate long text;
- show full value via tooltip.

### Sorting

Sortable columns:

- resume title;
- vacancy;
- company;
- language;
- adaptation level;
- created.

Default:

- `sortField="createdAt"`
- `sortOrder="-1"`

Use `removableSort`.

### Search

Search fields:

- resume title;
- company;
- vacancy.

MVP implementation:

- Text input.
- Search runs on Enter or Search button.
- Optional debounce search after 3 characters is acceptable only if easy and clean.

Do not overcomplicate backend search for MVP.

### Filters

Language filter:

- MultiSelect.
- Options:
  - English
  - Russian
- Default: both selected.

Adaptation level filter:

- MultiSelect.
- Options:
  - Minimal
  - Balanced
  - Maximum
- Default: all selected.

Created date filter:

- DatePicker.
- Exact date filter.

### Pagination

Use PrimeVue paginator.

Rows per page:

- 10
- 20
- 50

Default: 10.

Paginator should show:

- current page;
- Previous;
- Next;
- last page number or current page report.

### Loading

Use:

- DataTable `loading` prop;
- Skeleton for long initial loading.

Behavior:

- initial load: show skeleton/loader;
- search/filter/sort/page change: keep table visible with loading overlay;
- failed request: show user-friendly error and retry option.

---

## 14. Saved Resumes section CTA

Show `Generate Resume` button in Saved Resumes section header only when `profileReady = true`.

If profile is incomplete, do not show disabled Generate button here.

Reason: avoid duplicate disabled CTAs; completion guidance already exists above.

---

## 15. Empty states

### Profile incomplete and no resumes

Title:

- EN: `No resumes yet`
- RU: `Резюме пока нет`

Text:

- EN: `Complete your profile first to create your first adapted resume.`
- RU: `Сначала заполни профиль, чтобы создать первое адаптированное резюме.`

CTA:

- EN: `Complete Profile`
- RU: `Заполнить профиль`

### Profile ready and no resumes

Title:

- EN: `No resumes yet`
- RU: `Резюме пока нет`

Text:

- EN: `Create your first resume for a specific vacancy.`
- RU: `Создай первое резюме под конкретную вакансию.`

CTA:

- EN: `Generate Resume`
- RU: `Создать резюме`

### Search returns no results

Title:

- EN: `No resumes found`
- RU: `Ничего не найдено`

Text:

- EN: `Try another search or change filters.`
- RU: `Попробуй другой запрос или измени фильтры.`

---

## 16. Resume Details modal

Open modal when:

- user clicks a DataTable row;
- user clicks Last Resume card.

Modal fields:

- Resume title
- Vacancy
- Company
- Language
- Adaptation level
- Created
- Public link
- Cover letter collapsible block

Do not show:

- token usage;
- AI model name;
- PDF status;
- raw technical metadata.

### Modal actions

- `View`
- `Download PDF`
- `Copy link`
- `Delete`

Behavior:

- View opens `pdfUrl` in a new tab.
- Download PDF triggers file download from PDF endpoint.
- Copy link copies public URL and shows toast.
- Delete opens confirmation dialog.

### Cover letter

Use collapsible section / accordion.

If cover letter exists:

- show preview text;
- button `Copy cover letter`.

If missing:

- show `No cover letter for this resume.` / `Для этого резюме сопроводительное письмо не создано.`

---

## 17. Delete flow

On Delete click:

1. Open confirmation dialog.
2. If Cancel: close dialog only.
3. If Delete confirmed:
   - call delete endpoint / soft-delete action when backend exists;
   - close confirmation;
   - close details modal;
   - refresh table data;
   - refresh summary data;
   - show toast.

Dialog text:

- EN title: `Delete resume?`
- RU title: `Удалить резюме?`
- EN text: `This resume will no longer be available from your workspace or public link.`
- RU text: `Это резюме исчезнет из рабочего центра, а публичная ссылка перестанет работать.`

Buttons:

- Cancel / Отмена
- Delete / Удалить

Toast:

- Resume deleted. / Резюме удалено.

---

## 18. Generate Resume navigation and blocking rules

`Generate Resume` remains visible in navbar even when profile is incomplete.

If `profileReady = false` and user opens Generate Resume:

- show blocking state;
- show same profile checklist;
- CTA to Complete Profile;
- do not show generation form.

Stepper stages:

1. Vacancy
2. Settings
3. Review
4. Export

Before Generate:

- Vacancy clickable.
- Settings clickable after Vacancy.
- Review disabled.
- Export disabled.

After Generate button:

- automatically move to Review.
- show loader while AI response is being received and processed.

Review loader:

- EN: `Generating your resume...`
- RU: `Создаём резюме...`
- EN subtext: `This may take a moment. Please keep this page open.`
- RU subtext: `Это может занять немного времени. Не закрывай страницу.`

If generation succeeds:

- Review active;
- draft visible.

Export remains disabled until `Save Final Resume`.

If generation fails:

- show error state;
- provide `Try again` and `Back to settings`;
- do not lose data from Vacancy and Settings.

### State preservation

Data entered in Vacancy and Settings must not be lost when moving between steps.

Suggested frontend structure:

- use a composable such as `useGenerationDraft()`;
- hold draft state across step routes;
- optional localStorage/sessionStorage persistence for unsaved input if suitable;
- production backend draft persistence can be added later if needed.

---

## 19. i18n implementation

Use `vue-i18n` existing structure:

- `frontend/src/i18n/en.json`
- `frontend/src/i18n/ru.json`

All visible strings must be externalized.

Russian tone:

- friendly `ты`;
- clear;
- no excessive formality;
- no slangy overfamiliarity;
- semantic/adaptive translation, not word-for-word.

No hardcoded UI text in Vue templates.

### Required i18n namespaces

Use clear nested keys, for example:

~~~json
{
  "nav": {},
  "home": {
    "guided": {},
    "checklist": {},
    "summary": {},
    "table": {},
    "modal": {},
    "deleteDialog": {}
  },
  "generate": {
    "steps": {},
    "states": {}
  },
  "placeholder": {},
  "common": {}
}
~~~

---

## 20. Required i18n text matrix

### Navigation

| Key | EN | RU |
|---|---|---|
| nav.home | Home | Главная |
| nav.myProfile | My Profile | Профиль |
| nav.generateResume | Generate Resume | Создать резюме |
| nav.admin | Admin | Админ |
| nav.language | Language | Язык |
| nav.logout | Log out | Выйти |

### Home page

| Key | EN | RU |
|---|---|---|
| home.title | Resume workspace | Рабочий центр |

### Guided incomplete

| Key | EN | RU |
|---|---|---|
| home.incomplete.title | Complete your profile first | Сначала заполни профиль |
| home.incomplete.text | AI needs context about you. Add at least your contact details, work experience, and education. The more useful information you provide, the better resume you will get. | ИИ нужен контекст о тебе. Добавь хотя бы контакты, опыт работы и образование. Чем больше полезной информации ты укажешь, тем точнее получится резюме. |
| home.incomplete.cta | Complete Profile | Заполнить профиль |
| home.checklist.contact | Contact details | Контакты |
| home.checklist.experience | Work experience | Опыт работы |
| home.checklist.education | Education | Образование |
| home.checklist.done | Done | Готово |
| home.checklist.missing | Missing | Не заполнено |

### Guided ready

| Key | EN | RU |
|---|---|---|
| home.ready.title | Your next best step | Следующий лучший шаг |
| home.ready.generate.title | Generate your next resume | Создай новое резюме |
| home.ready.generate.hint | Start with a vacancy and get an adapted resume. | Начни с вакансии и получи адаптированное резюме. |
| home.ready.generate.cta | Generate Resume | Создать резюме |
| home.ready.generate.tooltip | Make a new awesome resume for a specific vacancy. | Создать сильное резюме под конкретную вакансию. |
| home.ready.update.title | Update your profile | Обнови профиль |
| home.ready.update.hint | Use this when your experience, education, skills, or contact details change. | Используй, если изменились опыт, образование, навыки или контакты. |
| home.ready.update.cta | Update Profile | Обновить профиль |
| home.ready.update.tooltip | Edit the profile data used for future resumes. | Изменить данные, которые будут использоваться в будущих резюме. |

### Summary cards

| Key | EN | RU |
|---|---|---|
| home.summary.savedResumes | Saved resumes | Сохранённые резюме |
| home.summary.profileStatus | Profile status | Статус профиля |
| home.summary.ready | Ready | Готов |
| home.summary.needsInfo | Needs info | Нужно заполнить |
| home.summary.readyHint | You can generate resumes now. | Теперь можно создавать резюме. |
| home.summary.needsInfoHint | Add contact details, work experience, and education. | Добавь контакты, опыт работы и образование. |
| home.summary.updateProfile | Update profile | Обновить профиль |
| home.summary.completeProfile | Complete profile | Заполнить профиль |
| home.summary.lastResume | Last resume | Последнее резюме |
| home.summary.noLastResume | No resumes yet | Резюме пока нет |

### Table

| Key | EN | RU |
|---|---|---|
| home.table.title | Saved resumes | Сохранённые резюме |
| home.table.searchPlaceholder | Search by title, vacancy, or company | Найти по названию, вакансии или компании |
| home.table.search | Search | Найти |
| home.table.resumeTitle | Resume title | Название резюме |
| home.table.vacancy | Vacancy | Вакансия |
| home.table.company | Company | Компания |
| home.table.language | Language | Язык |
| home.table.adaptationLevel | Adaptation level | Уровень адаптации |
| home.table.created | Created | Создано |
| home.table.loading | Loading resumes. Please wait. | Загружаем резюме. Подожди немного. |
| home.table.emptyTitle | No resumes yet | Резюме пока нет |
| home.table.noResultsTitle | No resumes found | Ничего не найдено |
| home.table.noResultsText | Try another search or change filters. | Попробуй другой запрос или измени фильтры. |
| home.table.pageReport | Showing {first} to {last} of {totalRecords} | Показано {first}–{last} из {totalRecords} |
| home.table.mobilePageReport | Page {current} of {totalPages} | Страница {current} из {totalPages} |

### Common values

| Key | EN | RU |
|---|---|---|
| language.en | English | Английский |
| language.ru | Russian | Русский |
| adaptation.minimal | Minimal | Минимальная |
| adaptation.balanced | Balanced | Сбалансированная |
| adaptation.maximum | Maximum | Максимальная |

### Modal/actions

| Key | EN | RU |
|---|---|---|
| resumeDetails.title | Resume details | Детали резюме |
| resumeDetails.publicLink | Public link | Публичная ссылка |
| resumeDetails.view | View | Открыть |
| resumeDetails.downloadPdf | Download PDF | Скачать PDF |
| resumeDetails.copyLink | Copy link | Скопировать ссылку |
| resumeDetails.delete | Delete | Удалить |
| resumeDetails.coverLetter | Cover letter | Сопроводительное письмо |
| resumeDetails.copyCoverLetter | Copy cover letter | Скопировать письмо |
| resumeDetails.noCoverLetter | No cover letter for this resume. | Для этого резюме сопроводительное письмо не создано. |
| resumeDetails.copied | Copied. | Скопировано. |
| resumeDetails.linkCopied | Link copied. | Ссылка скопирована. |
| resumeDetails.coverLetterCopied | Cover letter copied. | Сопроводительное письмо скопировано. |

### Delete dialog

| Key | EN | RU |
|---|---|---|
| deleteResume.title | Delete resume? | Удалить резюме? |
| deleteResume.text | This resume will no longer be available from your workspace or public link. | Это резюме исчезнет из рабочего центра, а публичная ссылка перестанет работать. |
| deleteResume.cancel | Cancel | Отмена |
| deleteResume.confirm | Delete | Удалить |
| deleteResume.success | Resume deleted. | Резюме удалено. |

### Generate stepper

| Key | EN | RU |
|---|---|---|
| generate.steps.vacancy | Vacancy | Вакансия |
| generate.steps.settings | Settings | Настройки |
| generate.steps.review | Review | Проверка |
| generate.steps.export | Export | Экспорт |
| generate.disabledTooltip | Generate a resume first. | Сначала создай резюме. |
| generate.loadingTitle | Generating your resume... | Создаём резюме... |
| generate.loadingText | This may take a moment. Please keep this page open. | Это может занять немного времени. Не закрывай страницу. |

### Placeholder pages

| Key | EN | RU |
|---|---|---|
| placeholder.title | Placeholder page | Страница-заглушка |
| placeholder.text | This page is a placeholder for future work. | Это страница-заглушка для будущей работы. |

---

## 21. Placeholder pages

Create lightweight placeholder components/routes for:

- profile sections;
- generate resume sections;
- admin page if not already sufficient.

Each placeholder should use shared layout/header and show:

- page title;
- short placeholder text;
- optional subnav/stepper where relevant.

Do not implement full profile/generate/admin functionality in this feature unless already required elsewhere.

---

## 22. Responsive requirements

Desktop:

- content max-width `1280px`;
- 3 summary cards in one row;
- table full width.

Tablet:

- cards and filters wrap naturally.

Mobile:

- guided block single column;
- summary cards vertical;
- DataTable horizontal scroll;
- no separate resume card-list mode;
- modal fits small screens.

Use visible focus states and keyboard-accessible controls.

---

## 23. Suggested frontend files/components

Create or update:

- `frontend/src/views/UserHomePage.vue`
- `frontend/src/components/AppHeader.vue`
- `frontend/src/components/home/GuidedNextStep.vue`
- `frontend/src/components/home/ProfileChecklist.vue`
- `frontend/src/components/home/SummaryCards.vue`
- `frontend/src/components/home/SavedResumesTable.vue`
- `frontend/src/components/home/ResumeDetailsDialog.vue`
- `frontend/src/components/common/PlaceholderPage.vue`
- `frontend/src/composables/useUserHome.ts`
- `frontend/src/services/userHomeService.ts`
- `frontend/src/services/resumeService.ts`
- `frontend/src/i18n/en.json`
- `frontend/src/i18n/ru.json`
- `frontend/src/router/index.ts`

Keep components small and readable.

---

## 24. Backend implementation notes

If backend implementation is included in this feature, add endpoints:

- `GET /api/user/home`
- `GET /api/resumes`
- endpoint/action for resume soft delete when available;
- endpoint for PDF view/download if not already implemented.

If backend is not yet ready, frontend services may use typed mock data temporarily, but:

- mock data must be isolated;
- no mock data inside visual components;
- service API shape must match this brief.

---

## 25. Acceptance criteria

Feature is accepted when:

1. `/` still opens the landing page.
2. SPA routes are under `/app/...`.
3. Logout redirects to `/app/auth`.
4. Login/register redirect both USER and ADMIN to `/app/home`.
5. Admin nav item is visible only for admin role.
6. Non-admin cannot access `/app/admin` through direct URL.
7. User Home no longer shows token dashboard cards.
8. H1 is `Resume workspace` / `Рабочий центр`.
9. Profile readiness uses `contactComplete && hasWorkExperience && hasEducation`.
10. Incomplete profile state shows guidance + checklist + Complete Profile CTA.
11. Ready profile state shows Generate first, Update second.
12. Summary cards show Saved resumes, Profile status, Last resume.
13. Last resume card opens Resume Details modal.
14. Saved Resumes table uses PrimeVue DataTable.
15. Table columns are in the approved order.
16. Vacancy and Company truncate with tooltip.
17. Search works by title, vacancy, company.
18. Filters work for language, adaptation level, created date.
19. Sorting works for all approved columns and uses removableSort.
20. Pagination supports 10/20/50 rows.
21. Loading and Skeleton states are implemented.
22. Empty states match profile readiness.
23. Row click opens responsive Resume Details modal.
24. Modal has View, Download PDF, Copy link, Delete actions.
25. Modal includes Cover letter collapsible block.
26. Delete requires confirmation.
27. Copy/delete actions show Toast feedback.
28. Mobile uses horizontal DataTable scroll, not a separate card-list mode.
29. All visible text is i18n via `en.json` and `ru.json`.
30. Russian localization uses friendly `ты` tone.
31. No demo switch, demo routes, or CDN prototype code exists in production.

---

## 26. Final instruction to OpenCode

Implement the real User Home feature inside the existing Vue 3 + Vite + PrimeVue application. Use the OpenDesign prototype only as visual reference, not as architecture. Keep the solution MVP-ready, secure, i18n-complete, and consistent with the existing ResumAIner design system. Do not over-engineer beyond the confirmed decisions in this brief.
