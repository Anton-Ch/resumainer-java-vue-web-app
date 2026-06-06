# OpenDesign Brief — ResumAIner User Home / Resume Workspace Prototype

**Document purpose:** Give OpenDesign a clear, standalone brief for creating a live visual prototype of the User Home page.

**Important:** This document is only for OpenDesign prototype creation. Do not use it as the production implementation plan. Production implementation instructions are in `OpenCode_Implementation_Brief.md`.

---

## 1. Product context

ResumAIner is an AI-assisted resume adaptation application.

The user flow is:

1. User creates a structured professional profile.
2. User enters a target vacancy and generation settings.
3. The system generates an adapted resume.
4. User reviews and saves the final resume.
5. Saved resumes are managed from the User Home page.
6. Recruiters can open a public resume URL that directly displays the PDF.

The User Home page must become the user's main **Resume workspace**, not a token/statistics dashboard.

---

## 2. Prototype goal

Create a standalone live frontend prototype for the User Home page using the existing ResumAIner visual direction.

The prototype must let the owner review:

- ready user state with saved resumes;
- incomplete profile state;
- ready profile with no saved resumes;
- loading state;
- table behavior;
- resume details modal;
- responsive layout;
- header/navigation behavior;
- i18n text in English and Russian.

---

## 3. Hard separation from production

### OpenDesign prototype may use

- Standalone HTML/CSS/JS.
- Vue 3 via CDN.
- PrimeVue via CDN.
- PrimeIcons via CDN.
- PrimeVue Aura theme or visually compatible styling.
- Local mock data.
- Separate demo pages for different states.

### OpenDesign prototype must not require

- Java backend.
- Tomcat.
- Maven.
- PostgreSQL.
- Docker.
- Real API calls.
- Vite build step, unless OpenDesign can still provide a directly reviewable artifact.

### Do not add

- Demo state switch inside the UI.
- Fake developer toolbar.
- Debug controls.
- Token dashboard cards.
- Dark mode toggle.
- User account menu.

The owner should be able to open the prototype and inspect it without starting the full Java/Vue project.

---

## 4. Role perspective to apply during design

OpenDesign should silently review the prototype through these lenses:

1. **UX Architect** — clear layout, scalable navigation, responsive behavior, developer-ready structure.
2. **Brand Guardian** — consistent ResumAIner identity, warm professional SaaS tone, no visual fragmentation.
3. **UI Designer** — polished component composition, readable hierarchy, accessible states.
4. **Frontend Prototype Developer** — working interactive prototype, stable mock data, no broken links.
5. **Accessibility QA** — keyboard-friendly actions, visible focus, tooltips/aria-labels for icon-only buttons.
6. **Business Analyst** — screen behavior matches MVP workflow and avoids unconfirmed features.

---

## 5. Visual style and design DNA

Use the existing ResumAIner Vue design direction.

Overall style:

- clean;
- warm;
- professional;
- structured;
- medium-density SaaS workspace;
- not a landing page hero;
- not a dense admin console.

Use the existing Vue SPA token direction from `vue_general.css` where possible:

- Canvas: `#F6F7FB`
- Surface: `#FFFFFF`
- Subtle surface: `#FBFCFE`
- Primary text: `#10233F`
- Secondary text: `#5D718B`
- Muted text: `#8091A7`
- Primary emerald accent: `#0F9D7A`
- Emerald hover: `#0C8467`
- Blue accent: `#2F6BFF`
- Warning: `#D97706`
- Warning background: `#FFF7ED`
- Success background: `#F2FFF9`
- Error: `#C2410C`
- Heading font direction: Manrope
- Body font direction: Inter

Use soft rounded cards, light borders, subtle shadows, and clear spacing. Keep the existing Auth/Landing visual language consistent.

---

## 6. Page naming

Navbar label:

- EN: `Home`
- RU: `Главная`

Page H1:

- EN: `Resume workspace`
- RU: `Рабочий центр`

Do not use `User Home` as the visible H1.

---

## 7. Prototype pages

Create four separate pages, not a demo state switch:

1. `/home-ready` — profile ready, saved resumes exist.
2. `/home-incomplete` — profile incomplete, no resumes.
3. `/home-empty` — profile ready, no resumes yet.
4. `/home-loading` — loading/skeleton state.

All four pages must include the same header and the same `Admin` navbar link so the owner can visually inspect its placement.

All non-home links should lead to simple placeholder pages.

---

## 8. Header / navbar

### Left

- ResumAIner logo.
- Logo click leads to Home.

### Primary navigation

- `Home` / `Главная`
- `My Profile` / `Профиль`
- `Generate Resume` / `Создать резюме`
- `Admin` / `Админ`

In prototype, show `Admin` in all four demo variants.

Production note to display visually nowhere in UI but keep in documentation comments if needed:

- In real app, `Admin` is shown only when `role === 'ADMIN'`.

### Right

- Language switcher: `EN / RU`.
- Logout icon-only button.

Logout icon:

- Use a recognizable sign-out icon, preferably PrimeIcons `pi pi-sign-out`.
- Tooltip:
  - EN: `Log out`
  - RU: `Выйти`
- `aria-label`:
  - EN: `Log out`
  - RU: `Выйти`

Do not add account dropdown in this prototype.

---

## 9. Navigation placeholders

All links outside User Home should open placeholder pages.

### My Profile placeholder pages

- `/profile/contact` — Contact
- `/profile/experience` — Experience
- `/profile/education` — Education
- `/profile/projects` — Projects
- `/profile/courses` — Courses
- `/profile/additional` — Additional

### Generate Resume placeholder pages

- `/generate/vacancy` — Vacancy
- `/generate/settings` — Settings
- `/generate/review` — Review
- `/generate/export` — Export

### Admin placeholder

- `/admin` — Admin

Placeholder text:

- EN: `This page is a placeholder for future work.`
- RU: `Это страница-заглушка для будущей работы.`

---

## 10. User Home page structure

Page order:

1. Header / navbar.
2. Main content container.
3. H1: `Resume workspace` / `Рабочий центр`.
4. Guided next-step block.
5. Three summary cards.
6. Saved Resumes section with table.
7. Resume Details modal.
8. Confirm delete dialog.
9. Toast notifications.

Use `max-width: 1280px` for main content on desktop.

---

## 11. Profile readiness rule

Use this logic in mock states:

`profileReady = contactComplete && hasWorkExperience && hasEducation`

Where:

- `contactComplete` = full name + email + phone + location.
- `hasWorkExperience` = at least one complete work experience record.
- `hasEducation` = at least one complete education record.

Course/certificate is not required for profile readiness.

Product reason: the MVP is meant for users who already have work experience and need help adapting it into strong resumes.

---

## 12. Guided next-step block

### If profile is incomplete

Show only profile completion guidance, not Generate Resume as the main CTA.

Title:

- EN: `Complete your profile first`
- RU: `Сначала заполни профиль`

Text:

- EN: `AI needs context about you. Add at least your contact details, work experience, and education. The more useful information you provide, the better resume you will get.`
- RU: `ИИ нужен контекст о тебе. Добавь хотя бы контакты, опыт работы и образование. Чем больше полезной информации ты укажешь, тем точнее получится резюме.`

CTA:

- EN: `Complete Profile`
- RU: `Заполнить профиль`

Mini-checklist with status:

1. Contact details
2. Work experience
3. Education

Statuses:

- EN: `Done` / `Missing`
- RU: `Готово` / `Не заполнено`

Each checklist item should be clickable and lead to the related placeholder profile section.

### If profile is ready

Title:

- EN: `Your next best step`
- RU: `Следующий лучший шаг`

Primary action card:

- Title EN: `Generate your next resume`
- Title RU: `Создай новое резюме`
- Hint EN: `Start with a vacancy and get an adapted resume.`
- Hint RU: `Начни с вакансии и получи адаптированное резюме.`
- CTA EN: `Generate Resume`
- CTA RU: `Создать резюме`
- Tooltip EN: `Make a new awesome resume for a specific vacancy.`
- Tooltip RU: `Создать сильное резюме под конкретную вакансию.`

Secondary action card:

- Title EN: `Update your profile`
- Title RU: `Обнови профиль`
- Hint EN: `Use this when your experience, education, skills, or contact details change.`
- Hint RU: `Используй, если изменились опыт, образование, навыки или контакты.`
- CTA EN: `Update Profile`
- CTA RU: `Обновить профиль`
- Tooltip EN: `Edit the profile data used for future resumes.`
- Tooltip RU: `Изменить данные, которые будут использоваться в будущих резюме.`

Primary action must be visually first and stronger.

---

## 13. Summary cards

Show exactly three cards.

### Card 1: Saved resumes

- EN title: `Saved resumes`
- RU title: `Сохранённые резюме`
- Value: total count.

### Card 2: Profile status

If ready:

- EN title: `Profile status`
- RU title: `Статус профиля`
- EN value: `Ready`
- RU value: `Готов`
- EN hint: `You can generate resumes now.`
- RU hint: `Теперь можно создавать резюме.`
- EN link: `Update profile`
- RU link: `Обновить профиль`

If incomplete:

- EN title: `Profile status`
- RU title: `Статус профиля`
- EN value: `Needs info`
- RU value: `Нужно заполнить`
- EN hint: `Add contact details, work experience, and education.`
- RU hint: `Добавь контакты, опыт работы и образование.`
- EN link: `Complete profile`
- RU link: `Заполнить профиль`

Use gentle warning/sand tone for incomplete state, not aggressive red.

### Card 3: Last resume

- EN title: `Last resume`
- RU title: `Последнее резюме`
- If exists: show title and created date.
- If no resumes: show empty hint.
- If exists: whole card or clear link must be clickable and open the same Resume Details modal as table row click.

---

## 14. Saved Resumes section

Section header:

- EN: `Saved resumes`
- RU: `Сохранённые резюме`

If `profileReady = true`, show a secondary `Generate Resume` button in the section header.

Button tooltip:

- EN: `Make a new awesome resume for a specific vacancy.`
- RU: `Создать сильное резюме под конкретную вакансию.`

If `profileReady = false`, do not show this section-level Generate button.

---

## 15. DataTable requirements

Use PrimeVue DataTable, not a custom table.

Required components/features:

- `DataTable`
- `Column`
- `paginator`
- `rowsPerPageOptions` with `10 / 20 / 50`
- default rows: `10`
- `sortable` columns
- `removableSort`
- default sort: `Created desc`
- `loading`
- Skeleton rows/cards for longer initial loading
- `filters` / `v-model:filters`
- `globalFilterFields`
- `IconField + InputIcon + InputText` for search
- `MultiSelect` for language filter
- `MultiSelect` for adaptation level filter
- `DatePicker` for Created date filter
- `Dialog` for Resume Details modal
- `ConfirmDialog` or custom confirmation modal for Delete
- `Toast` for feedback

### Table columns, in this exact order

1. `Resume title`
2. `Vacancy`
3. `Company`
4. `Language`
5. `Adaptation level`
6. `Created`

### Column behavior

`Vacancy` and `Company` must truncate long text neatly and show full value in tooltip.

`Created` format:

- `YYYY-MM-DD`
- Examples: `2025-12-31`, `2025-01-09`

### Sorting

Sortable columns:

- Resume title
- Vacancy
- Company
- Language
- Adaptation level
- Created

Default:

- Created descending / newest first.

Third click removes sorting via `removableSort`.

### Search

Global search fields:

- resume title
- company
- vacancy

For the visual prototype, live search after 3 characters with debounce is acceptable.

Also show an explicit `Search` button or Enter-key behavior so the MVP implementation can later choose simpler backend search.

### Filters

Language filter:

- MultiSelect with checkbox-like selection.
- Options:
  - English
  - Russian
- Default: both selected.

Adaptation level filter:

- MultiSelect with checkbox-like selection.
- Options:
  - Minimal
  - Balanced
  - Maximum
- Default: all selected.

Created date filter:

- DatePicker.
- Exact date filtering for MVP.

### Pagination

Use page size selector:

- `10`
- `20`
- `50`

Paginator should include:

- current page highlight;
- Previous;
- Next;
- first/last page access where appropriate;
- last page number or current page report.

Desktop report:

- EN: `Showing {first} to {last} of {totalRecords}`
- RU: `Показано {first}–{last} из {totalRecords}`

Mobile report:

- EN: `Page {current} of {totalPages}`
- RU: `Страница {current} из {totalPages}`

### Loading behavior

- Initial table load: DataTable loading overlay.
- Longer loading: Skeleton rows.
- Search/filter/sort/pagination: keep table visible and show loading overlay.
- Loading text:
  - EN: `Loading resumes. Please wait.`
  - RU: `Загружаем резюме. Подожди немного.`

---

## 16. Empty states

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

### No search results

Title:

- EN: `No resumes found`
- RU: `Ничего не найдено`

Text:

- EN: `Try another search or change filters.`
- RU: `Попробуй другой запрос или измени фильтры.`

---

## 17. Row click and Resume Details modal

Clicking a table row opens a responsive Resume Details modal.

The Last Resume summary card must open the same modal for the latest resume.

Modal content:

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
- technical metadata;
- PDF status.

### Modal actions

1. `View`
2. `Download PDF`
3. `Copy link`
4. `Delete`

Action behavior:

- `View`: opens PDF URL in a new browser tab.
- `Download PDF`: downloads the PDF file.
- `Copy link`: shows full public link and a nearby `Copy link` button.
- `Delete`: opens confirmation dialog.

### Public link display

Show full link text in the modal. Mock URL pattern:

`https://resumainer.example/{username}/{resumeCode}`

The production backend will later use a real public route based on username and resume code.

### Cover letter block

Use Accordion/collapsible section.

Title:

- EN: `Cover letter`
- RU: `Сопроводительное письмо`

If cover letter exists:

- show preview text;
- button:
  - EN: `Copy cover letter`
  - RU: `Скопировать письмо`

If missing:

- EN: `No cover letter for this resume.`
- RU: `Для этого резюме сопроводительное письмо не создано.`

---

## 18. Delete confirmation

When `Delete` is clicked, show confirmation dialog.

Title:

- EN: `Delete resume?`
- RU: `Удалить резюме?`

Text:

- EN: `This resume will no longer be available from your workspace or public link.`
- RU: `Это резюме исчезнет из рабочего центра, а публичная ссылка перестанет работать.`

Buttons:

- EN: `Cancel`
- RU: `Отмена`
- EN: `Delete`
- RU: `Удалить`

After delete:

- close confirmation dialog;
- close Resume Details modal;
- update table mock data;
- show toast:
  - EN: `Resume deleted.`
  - RU: `Резюме удалено.`

---

## 19. Generate Resume stepper placeholder behavior

Generate Resume pages are placeholders, but they should demonstrate the planned stepper logic.

Stepper stages:

1. Vacancy
2. Settings
3. Review
4. Export

Before Generate button is clicked:

- Vacancy: clickable.
- Settings: clickable after moving from Vacancy.
- Review: visible but disabled.
- Export: visible but disabled.

Disabled tooltip:

- EN: `Generate a resume first.`
- RU: `Сначала создай резюме.`

After Generate click:

- user is automatically moved to Review;
- Review shows loader:
  - EN: `Generating your resume...`
  - RU: `Создаём резюме...`
- subtext:
  - EN: `This may take a moment. Please keep this page open.`
  - RU: `Это может занять немного времени. Не закрывай страницу.`

Export becomes available only after Save Final Resume.

---

## 20. Responsive behavior

Desktop:

- main content max-width: `1280px`;
- guided block full width;
- three summary cards in one row;
- table full width.

Tablet:

- summary cards wrap naturally;
- filters wrap to multiple rows.

Mobile:

- guided block is one column;
- summary cards stack vertically;
- DataTable remains DataTable with horizontal scroll;
- do not build a separate card-list mode;
- Resume Details modal must be comfortable on mobile.

---

## 21. i18n requirement

100% visible UI text must exist in English and Russian.

Russian localization must use friendly `ты` tone without excessive informality.

Translation approach: semantic/adaptive translation, not word-for-word calque.

Every visible item must have both language variants:

- navbar;
- buttons;
- tooltips;
- table headers;
- filters;
- loading states;
- empty states;
- dialogs;
- toasts;
- aria labels;
- placeholder pages;
- stepper states.

---

## 22. Required i18n text matrix

### Navigation

| Key | EN | RU |
|---|---|---|
| nav.home | Home | Главная |
| nav.myProfile | My Profile | Профиль |
| nav.generateResume | Generate Resume | Создать резюме |
| nav.admin | Admin | Админ |
| nav.language | Language | Язык |
| nav.logout | Log out | Выйти |

### Page

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

### Values

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

### Generate stepper placeholders

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

## 23. Mock data requirements

Use realistic but fake mock data.

At least 8 saved resumes for ready state.

Include:

- long vacancy names to test truncation;
- long company names to test truncation;
- English and Russian resumes;
- all adaptation levels;
- several created dates;
- cover letter present for most resumes;
- one resume without cover letter to test empty cover letter state.

---

## 24. Acceptance checklist for OpenDesign

Prototype is accepted when:

- All 4 separate home pages exist.
- No demo switch is present.
- Header is consistent across pages.
- Admin link is visible in prototype navbar.
- Logo leads to Home.
- Logout icon has tooltip and aria-label.
- Guided block changes by profile state.
- Profile checklist appears for incomplete profile.
- Three summary cards appear.
- Last resume card opens Resume Details modal.
- Saved Resumes table uses PrimeVue DataTable-style behavior.
- Search, filters, sort, pagination are visually working with mock data.
- Loading page shows DataTable loading and Skeleton.
- Row click opens responsive modal.
- Modal has cover letter accordion.
- Delete confirmation appears before delete.
- Toast appears after copy/delete actions.
- Mobile layout uses horizontal table scroll, not separate card list.
- All visible text has EN/RU variants.
- Prototype can be reviewed without backend.

---

## 25. Final instruction to OpenDesign

Build a standalone live prototype that visually and interactively demonstrates the User Home / Resume workspace. Keep it practical, clear, and MVP-ready. Do not overdesign. Do not introduce new product features. Prioritize clarity, state coverage, PrimeVue-like component behavior, and consistency with the existing ResumAIner visual DNA.
