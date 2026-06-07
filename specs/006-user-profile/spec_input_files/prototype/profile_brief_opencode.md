# ResumAIner User Profile — OpenCode Implementation Brief

**Target file:** `profile_brief_opencode.md`  
**Audience:** OpenCode agent using OpenRouter + DeepSeek v4 Flash  
**Purpose:** integrate the approved OpenDesign live User Profile prototype into the real ResumAIner Vue frontend without redesigning it from scratch.

---

## 0. Mission

Implement the **User Profile / My Profile** feature in the real ResumAIner frontend using the approved OpenDesign live prototype as the **strict implementation baseline**.

The OpenDesign prototype is already manually reviewed and refined. Treat it as the closest source of truth for UX, layout, component structure, behavior, validation style, i18n wording, and visual design.

This is **not** a brainstorming task and not a redesign task.

Your job:

1. Inspect the current real frontend codebase.
2. Inspect the OpenDesign prototype files.
3. Compare both carefully.
4. Integrate the User Profile implementation from the prototype into the real frontend.
5. Preserve the existing project architecture, auth/routing conventions, i18n setup, PrimeVue setup, and design DNA.
6. Make the result buildable, reviewable, and ready for backend integration.

---

## 1. Non-negotiable instruction

Use the OpenDesign live prototype as the **hard basis**.

Do **not**:

- redesign the profile page from scratch;
- replace the approved UX with your own layout;
- simplify the profile into one huge page;
- remove the sidebar/mobile tabs architecture;
- create a new design system;
- create a new i18n mechanism;
- hardcode user-facing strings;
- add profile picture upload;
- add extra fields not approved by BA / prototype;
- reintroduce long save button labels;
- remove the dirty-state warning;
- remove localStorage mock persistence until real API integration exists;
- overwrite unrelated existing frontend files blindly.

If the real project and the prototype conflict, first preserve the real project’s working infrastructure, then integrate the prototype behavior and UI into that infrastructure.

---

## 2. Important context

The OpenDesign prototype introduced the complete Profile UI that was not present in the original frontend.

The original frontend had Profile routes pointing to a placeholder page. The prototype replaced this placeholder approach with a real `ProfilePage.vue`, a `components/profile` component tree, `profileMockService.ts`, `types/profile.ts`, and expanded i18n keys.

The real implementation must now use the prototype as the concrete reference.

---

## 3. OpenDesign prototype files to treat as authoritative

Use these prototype files as the primary implementation reference.

### 3.1 Main Profile entry

~~~text
src/views/ProfilePage.vue
~~~

This is the route-level Profile view.

It is responsible for:

- rendering `AppHeader`;
- selecting the active profile section based on the current route;
- rendering `ProfileShell`;
- loading the correct section component;
- maintaining section status metadata;
- handling unsaved changes across section navigation;
- showing `UnsavedChangesDialog`;
- redirect-compatible route updates;
- browser refresh warning when dirty state exists.

### 3.2 Profile shell and navigation

~~~text
src/components/profile/ProfileShell.vue
src/components/profile/ProfileSidebar.vue
src/components/profile/ProfileMobileTabs.vue
src/components/profile/ProfileSectionHeader.vue
~~~

These define the approved profile layout:

- desktop left sidebar;
- mobile/tablet two-row tab navigation;
- main content area;
- section title/purpose header;
- Home/back button behavior;
- section status display.

### 3.3 Shared Profile components

~~~text
src/components/profile/RecordCard.vue
src/components/profile/EmptyRecordsState.vue
src/components/profile/UnsavedChangesDialog.vue
src/components/profile/InlineRecordForm.vue
~~~

Use these as visual/behavior references.

Important:

- `RecordCard.vue` defines the compact saved-record card style.
- `EmptyRecordsState.vue` defines the approved “No records yet” pattern.
- `UnsavedChangesDialog.vue` defines the approved leave-without-saving modal.
- `InlineRecordForm.vue` may exist as a reusable reference, but the current prototype also implements some forms directly inside section components. Do not force a refactor unless needed.

### 3.4 Profile section components

~~~text
src/components/profile/sections/ContactDetailsSection.vue
src/components/profile/sections/WorkExperienceSection.vue
src/components/profile/sections/ProjectsSection.vue
src/components/profile/sections/EducationSection.vue
src/components/profile/sections/CoursesSection.vue
src/components/profile/sections/AdditionalInfoSection.vue
~~~

These are the approved section implementations.

They contain:

- field layouts;
- validation;
- save behavior;
- dirty-state emits;
- toast notifications;
- card/table rendering;
- add/edit/delete flows.

### 3.5 Courses-specific components

~~~text
src/components/profile/courses/CoursesTable.vue
src/components/profile/courses/CourseDialog.vue
~~~

These define the approved high-volume Courses UX:

- PrimeVue DataTable;
- search;
- date filters;
- sorting;
- pagination;
- reset;
- row-click details dialog;
- add/edit mode;
- validation;
- delete confirmation.

### 3.6 Profile service and types

~~~text
src/services/profileMockService.ts
src/types/profile.ts
~~~

Use these as the current frontend data contract for the prototype.

`profileMockService.ts` uses `localStorage` with key:

~~~text
resumainer_profile_data
~~~

This is acceptable for the first frontend integration if backend endpoints are not ready yet.

`types/profile.ts` defines the approved TypeScript shape:

~~~ts
export interface ContactDetails {
  fullName: string
  professionalTitle: string
  email: string
  phone: string
  location: string
  linkedinUrl: string
  portfolioUrl: string
  telegram: string
  whatsapp: string
}

export interface WorkExperience {
  id: string
  jobTitle: string
  companyName: string
  location: string
  startDate: string
  endDate: string
  currentlyWorkHere: boolean
  description: string
  companyUrl: string
}

export interface Project {
  id: string
  projectName: string
  role: string
  startDate: string
  endDate: string
  isOngoing: boolean
  description: string
  projectUrl: string
}

export interface Education {
  id: string
  institutionName: string
  degree: string
  fieldOfStudy: string
  startDate: string
  endDate: string
  currentlyStudying: boolean
  location: string
  comment: string
  gpa: string
}

export interface Course {
  id: string
  courseName: string
  provider: string
  startDate: string
  endDate: string
  credentialUrl: string
  skills: string
  description: string
}

export interface AdditionalInfo {
  username: string
  defaultResumeLanguage: string
  additionalResumeLanguage: string
  acceptableWorkFormats: string[]
  willingnessToRelocate: string
  willingnessForBusinessTravel: string
  skills: string
  spokenLanguages: string
  professionalAspirations: string
  achievements: string
  additionalContextForAI: string
  dateOfBirth: string
  citizenship: string
}
~~~

### 3.7 Router

Prototype route behavior to preserve:

~~~text
/profile -> redirect to /profile/contact

/profile/contact
/profile/experience
/profile/education
/profile/projects
/profile/courses
/profile/additional
~~~

Each section route renders the same `ProfilePage.vue`, which selects the active section based on route path.

### 3.8 i18n files

Use the prototype’s Profile i18n keys as reference:

~~~text
src/i18n/en.json
src/i18n/ru.json
~~~

Important:

- Do not overwrite unrelated existing translations.
- Merge new `profile` keys carefully.
- Preserve manually reviewed Russian translations.
- Keep all user-facing Profile text translated.
- Do not hardcode labels, buttons, placeholders, toast text, validation messages, table labels, dialogs, or status text.

---

## 4. Prototype files that are not implementation source

Do not copy these into the real project:

~~~text
code2prompt.exe
mq3luiby-package.json
mq3luil5-package-lock.json
dist/
node_modules/
~~~

Only use actual source files and config files.

The `mq3...` package files are artifact-generated duplicates from the OpenDesign export. Ignore them.

Use the real project’s own:

~~~text
package.json
package-lock.json
vite.config.ts
tsconfig.json
tsconfig.node.json
index.html
~~~

unless the prototype has a necessary dependency that the real project lacks.

Current expected dependencies already include:

~~~text
Vue 3
Vite
TypeScript
PrimeVue
PrimeIcons
vue-router
vue-i18n
zod
@primeuix/themes
@primevue/forms
~~~

Do not add new dependencies unless absolutely required.

---

## 5. Implementation strategy

### Step 1 — Inspect before editing

Before changing files, inspect:

~~~text
frontend/src/main.ts
frontend/src/App.vue
frontend/src/router/index.ts
frontend/src/i18n/index.ts
frontend/src/i18n/en.json
frontend/src/i18n/ru.json
frontend/src/assets/styles/vue_general.css
frontend/src/components/AppHeader.vue
frontend/src/components/LanguageSwitcher.vue
frontend/src/views/UserHomePage.vue
frontend/src/components/home/SavedResumesTable.vue
frontend/src/components/home/ResumeDetailsDialog.vue
frontend/src/components/common/ProfilePlaceholderPage.vue
~~~

Then inspect the corresponding OpenDesign prototype files.

Do not guess.

### Step 2 — Add new profile files

Add the prototype’s profile feature files:

~~~text
frontend/src/views/ProfilePage.vue

frontend/src/components/profile/ProfileShell.vue
frontend/src/components/profile/ProfileSidebar.vue
frontend/src/components/profile/ProfileMobileTabs.vue
frontend/src/components/profile/ProfileSectionHeader.vue
frontend/src/components/profile/RecordCard.vue
frontend/src/components/profile/EmptyRecordsState.vue
frontend/src/components/profile/UnsavedChangesDialog.vue
frontend/src/components/profile/InlineRecordForm.vue

frontend/src/components/profile/sections/ContactDetailsSection.vue
frontend/src/components/profile/sections/WorkExperienceSection.vue
frontend/src/components/profile/sections/ProjectsSection.vue
frontend/src/components/profile/sections/EducationSection.vue
frontend/src/components/profile/sections/CoursesSection.vue
frontend/src/components/profile/sections/AdditionalInfoSection.vue

frontend/src/components/profile/courses/CoursesTable.vue
frontend/src/components/profile/courses/CourseDialog.vue

frontend/src/services/profileMockService.ts
frontend/src/types/profile.ts
~~~

If the real project already has files with these names, compare and merge carefully.

### Step 3 — Update router carefully

In `frontend/src/router/index.ts`:

- keep the existing auth guard;
- keep existing non-profile routes;
- keep Generate routes;
- keep Admin/User Home routes;
- replace Profile placeholder routing with `ProfilePage.vue`;
- add `/profile` redirect to `/profile/contact` if absent.

Expected profile route structure:

~~~ts
{
  path: '/profile',
  redirect: '/profile/contact'
},
{
  path: '/profile/contact',
  name: 'profile-contact',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile/experience',
  name: 'profile-experience',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile/education',
  name: 'profile-education',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile/projects',
  name: 'profile-projects',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile/courses',
  name: 'profile-courses',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile/additional',
  name: 'profile-additional',
  component: () => import('@/views/ProfilePage.vue'),
  meta: { requiresAuth: true }
}
~~~

Do not break auth behavior.

### Step 4 — Merge i18n

Merge prototype `profile` namespace into:

~~~text
frontend/src/i18n/en.json
frontend/src/i18n/ru.json
~~~

Rules:

- Do not overwrite unrelated existing keys.
- Preserve manually reviewed Russian wording from the prototype.
- Do not introduce English-only strings.
- Do not hardcode fallback text inside Vue components.
- Check JSON validity after merge.

### Step 5 — Merge CSS only if needed

The prototype uses existing `vue_general.css` design tokens and scoped component styles.

Do not replace the entire real `vue_general.css` unless the prototype version contains necessary approved tokens missing in the real project.

Preferred approach:

1. Compare real `vue_general.css` and prototype `vue_general.css`.
2. Keep the real project’s global design system.
3. Add only missing reusable tokens/utilities if required.
4. Keep most Profile-specific styling scoped inside Profile components.

### Step 6 — Preserve existing shared components

Do not overwrite these unless a real diff is required:

~~~text
AppHeader.vue
LanguageSwitcher.vue
UserHomePage.vue
SavedResumesTable.vue
ResumeDetailsDialog.vue
userHomeService.ts
resumeService.ts
authService.ts
~~~

Use them as references only.

The Profile implementation should integrate with them, not replace them.

---

## 6. Approved UX requirements

### 6.1 General Profile structure

The Profile area has exactly six sections:

1. Contact Details
2. Work Experience
3. Projects & Volunteering
4. Education
5. Courses & Certificates
6. Additional Info

Do not add new sections.

Do not remove any section.

Do not merge all sections into one long page.

### 6.2 Desktop layout

Desktop layout:

~~~text
AppHeader
ProfileShell
├── left ProfileSidebar
└── main section content
~~~

Rules:

- left sidebar is sticky;
- no right-side helper column;
- main content uses remaining horizontal space;
- active section is visually highlighted;
- sidebar shows section status under section name.

### 6.3 Mobile/tablet layout

At mobile/tablet breakpoint, sidebar becomes a two-row grid navigation.

Required order:

~~~text
Contacts / Experience / Education
Projects / Courses / Additional
~~~

Rules:

- no dropdown;
- exactly 3 columns;
- 2 rows;
- items centered;
- each tab shows name + small status text.

### 6.4 Status text rules

Sidebar/mobile status must follow these rules:

Contact Details:

~~~text
Completed ✓ / Incomplete !
~~~

Work Experience:

~~~text
0 records / 1 record / N records
~~~

Projects:

~~~text
No records / 1 record / N records
~~~

Education:

~~~text
0 records / 1 record / N records
~~~

Courses:

~~~text
No records / 1 record / N records
~~~

Additional Info:

~~~text
Completed ✓ / Incomplete !
~~~

Do not use large colorful badges.

Use subtle status styling:

- completed = emerald accent;
- incomplete = warning accent;
- counts/no records = muted gray.

### 6.5 Save behavior

No autosave.

All saves must be explicit.

Buttons should use short labels:

EN:

~~~text
Save
~~~

RU:

~~~text
Сохранить
~~~

No long labels like “Save contact details”.

### 6.6 Required fields note placement

The note:

EN:

~~~text
Fields marked with * are required.
~~~

RU:

~~~text
Поля, отмеченные *, обязательны.
~~~

must be shown near the save button area, directly above the save action block.

Do not place it at the top of the form.

### 6.7 Toast style

Toast messages should not have a period at the end.

Examples:

Correct:

~~~text
Saved successfully
Сохранено
Record deleted
Запись удалена
~~~

Wrong:

~~~text
Saved successfully.
Сохранено.
Record deleted.
Запись удалена.
~~~

---

## 7. Section specifications

## 7.1 Contact Details

Route:

~~~text
/profile/contact
~~~

Fields:

- Full name — required
- Professional title — required for completion
- Email — required, valid email
- Phone — required
- Location — required
- LinkedIn URL — optional, valid URL / website-like URL
- Portfolio / Website URL — optional, valid URL / website-like URL
- Telegram — optional
- WhatsApp — optional

Frontend validation:

- email must be valid;
- URL fields accept values with or without protocol, for example:
  - `https://linkedin.com/in/example`
  - `www.linkedin.com/in/example`
  - `linkedin.com/in/example`
- invalid contact form must not save;
- dirty state resets after successful save.

---

## 7.2 Work Experience

Route:

~~~text
/profile/experience
~~~

Use compact record cards + one shared inline Add/Edit form above the list.

Fields:

- Job title — required
- Company name — required
- Location — optional
- Company URL — optional
- Start date — required
- End date — optional if current
- I currently work here — checkbox
- Role and job description — required

Approved desktop form layout:

~~~text
Job title *              / Company name *
Location                 / Company URL
Start date *             / End date
I currently work here
Role and job description *
~~~

RU equivalent:

~~~text
Должность *              / Компания *
Локация                  / Сайт компании
Дата начала *            / Дата окончания
Я работаю здесь сейчас
Описание роли и работы *
~~~

Behavior:

- Add opens empty inline form.
- Edit loads selected card data into the same form.
- Page smoothly scrolls to section title/header area.
- If `I currently work here` is checked, hide End Date completely.
- Card displays `Present` / `по настоящее время`.
- Card shows subtle `Current` chip.
- Empty/invalid form must not save.
- Dirty state warning must work during Add/Edit.

Location placeholder:

EN:

~~~text
Kazakhstan, Astana
~~~

RU:

~~~text
Казахстан, Астана
~~~

Do not add separate Skills/Tools field.

---

## 7.3 Projects & Volunteering

Route:

~~~text
/profile/projects
~~~

Use compact record cards + one shared inline Add/Edit form above the list.

Fields:

- Project name — required
- Role — optional
- Start date — optional
- End date — optional if ongoing
- This project is ongoing — checkbox
- Description — required
- Project URL — optional

Behavior:

- Add opens empty inline form.
- Edit loads selected card into the same form.
- Smooth scroll to section title/header.
- If ongoing checkbox is checked, hide End Date completely.
- Card displays `Present` / `по настоящее время`.
- Card shows subtle `Ongoing` chip.
- Empty/invalid form must not save.
- Dirty state warning must work.

Do not add separate Skills/Tools field.

---

## 7.4 Education

Route:

~~~text
/profile/education
~~~

Use compact record cards + one shared inline Add/Edit form above the list.

Fields:

- Institution name — required
- Degree / Qualification — required
- Field of study / Major — optional
- GPA / Grade — optional
- Start date — required
- End date — optional if currently studying
- I am currently studying here — checkbox
- Location — optional
- Comment / Description — optional

Approved desktop form layout:

~~~text
Institution name *        / Degree / Qualification *
Field of study / Major    / GPA / Grade
Start date *              / End date
I am currently studying here
Location
Comment / Description
~~~

RU equivalent:

~~~text
Учебное заведение *        / Степень / квалификация *
Направление / специальность / GPA / оценка
Дата начала *              / Дата окончания
Я учусь здесь сейчас
Локация
Комментарий / описание
~~~

Education card must display:

~~~text
[Institution name]
Date range · Location
Degree / Qualification
Field of study
~~~

Behavior:

- Add opens empty inline form.
- Edit loads selected record into the same form.
- Smooth scroll to section title/header.
- If currently studying checkbox is checked, hide End Date completely.
- Empty/invalid form must not save.
- Dirty state warning must work.

Education empty state text:

EN:

~~~text
No education added yet. Add your education information to help improve generated resumes.
~~~

RU:

~~~text
Образование пока не добавлено. Добавь информацию об имеющемся образовании, это поможет улучшить резюме.
~~~

---

## 7.5 Courses & Certificates

Route:

~~~text
/profile/courses
~~~

This is a high-volume section. It must use DataTable, not cards.

Table columns:

- Course
- Provider
- Start Date
- End Date
- Skills

Required table behavior:

- PrimeVue DataTable;
- pagination with 10 / 20 / 50;
- default 10 rows;
- search across Course / Provider / Skills;
- search applies only after minimum 3 characters;
- if search is empty, show all records;
- if search length is 1–2 characters, do not filter yet;
- Start Date filter range with From / To;
- To date cannot be earlier than From date;
- reset button appears when filter or sorting differs from default;
- reset clears search, filters, sorting, and returns pagination to first page;
- column sorting has three-state behavior:
  - ascending;
  - descending;
  - reset / default;
- column headers have i18n tooltips describing next sort action;
- row click opens details dialog;
- no separate View action button;
- skills column is truncated with tooltip/full details in dialog.

Course dialog modes:

- add;
- view;
- edit.

Course dialog fields:

- Course / Certificate name — required
- Provider / Issuer — required
- Start date — required
- End date — optional
- Credential URL — optional
- Skills / Topics — optional
- Short description — optional

Validation:

- required fields must block save;
- dialog must remain open if validation fails;
- no success toast on invalid save;
- Start Date / End Date range must be valid;
- End Date cannot be before Start Date.

Delete:

- use PrimeVue ConfirmDialog;
- no browser confirm.

Dirty-state:

- if user is adding/editing a course and changes values, navigation/refresh must warn about unsaved changes if feasible.

---

## 7.6 Additional Info

Route:

~~~text
/profile/additional
~~~

Use four visual blocks.

### Block 1 — Resume & Public Profile Preferences

Fields:

- Username — required
- Default resume language — required
- Additional resume language — required

Username validation:

- valid as part of URL path;
- English letters only;
- digits allowed;
- `_` and `-` allowed if already implemented;
- no Cyrillic;
- no spaces;
- no special characters.

Recommended regex:

~~~text
^[a-zA-Z0-9_-]+$
~~~

Username helper:

EN:

~~~text
This username will be used as part of your public resume link, for example: resumainer.com/yourusername/YRFJ
~~~

RU:

~~~text
Это имя будет использоваться как часть ссылки на созданное резюме, например: resumainer.com/yourusername/YRFJ
~~~

Default and Additional resume language behavior:

Available MVP languages:

- English
- Russian

They must be mutually exclusive.

Example:

~~~text
Default: English
Additional: Russian
~~~

If user changes Default to Russian, Additional automatically changes to English.

The same language must never appear in both fields.

### Block 2 — Work Preferences

Fields:

- Acceptable work formats
- Willingness to relocate
- Willingness for business travel

Acceptable work formats:

- Office
- Remote
- Hybrid
- Rotational schedule

Important:

Do not include `Relocation` / `Переезд` as a checkbox because relocation has a separate dropdown.

Willingness dropdown values:

- Yes
- No
- Negotiable

### Block 3 — Professional Info

Fields:

- Skills
- Spoken languages
- Professional aspirations
- Achievements

These fields are textareas.

Do not create a structured language/proficiency table.

Examples/helper placeholders must remain helpful and generic.

### Block 4 — Personal Info

Fields:

- Additional context for AI
- Date of birth
- Citizenship

Date of birth must use full exact date, not year-only.

---

## 8. Date handling rules

All date pickers must select exact full dates.

Do not use month/year-only pickers.

Use the same PrimeVue DatePicker style as the existing Home/Saved Resumes table reference where possible.

Apply to:

- Work Experience Start Date / End Date
- Projects Start Date / End Date
- Education Start Date / End Date
- Courses Start Date / End Date
- Additional Info Date of Birth

Validation:

- where a Start Date and End Date exist, End Date cannot be earlier than Start Date;
- DatePicker should use `minDate`/`maxDate` where useful;
- submit validation must also catch invalid ranges;
- if current/ongoing checkbox hides End Date, skip End Date validation.

---

## 9. Dirty-state / unsaved-changes rules

Unsaved changes warning must work for:

- Contact Details form;
- Additional Info form;
- Work Experience inline Add/Edit form;
- Projects inline Add/Edit form;
- Education inline Add/Edit form;
- Courses dialog if feasible.

Warning text:

EN:

~~~text
Leave without saving?
You have unsaved changes.
Leave without saving
Stay on this page
~~~

RU:

~~~text
Выйти без сохранения?
Есть несохранённые изменения.
Выйти без сохранения
Остаться на странице
~~~

Rules:

- after successful save, dirty state resets;
- after cancel, dirty state resets;
- after confirmed leave, dirty state resets;
- navigation should not show warning after successful save;
- browser refresh should warn when dirty state exists if possible.

---

## 10. Backend integration guidance

The prototype currently uses `profileMockService.ts` with localStorage.

For this implementation pass:

### If backend profile APIs are not implemented yet

Keep `profileMockService.ts`.

Do not invent backend endpoints.

Make Profile UI functional with localStorage, but keep service boundaries clean so it can later be swapped with real API calls.

### If backend profile APIs already exist

Create or adapt a real service layer, for example:

~~~text
src/services/profileService.ts
~~~

Then map frontend methods to actual API endpoints.

Suggested frontend service methods:

~~~ts
getContactDetails()
saveContactDetails(data)

getWorkExperience()
saveWorkExperienceRecord(record)
deleteWorkExperienceRecord(id)

getProjects()
saveProjectRecord(record)
deleteProjectRecord(id)

getEducation()
saveEducationRecord(record)
deleteEducationRecord(id)

getCourses(params)
saveCourse(record)
deleteCourse(id)

getAdditionalInfo()
saveAdditionalInfo(data)

getProfileSectionStatuses()
~~~

If API endpoints are incomplete, do not block frontend integration. Keep localStorage mock temporarily and document TODO.

### Do not break future backend compatibility

Keep DTO shapes stable and clear.

Prefer explicit conversion functions if backend naming differs from frontend camelCase.

Example:

- frontend: `fullName`
- backend JSON might be: `fullName` or `full_name`

Do not silently guess backend naming. Inspect existing backend conventions first.

---

## 11. i18n requirements

All Profile UI strings must be i18n-based.

Do not hardcode:

- field labels;
- placeholders;
- help text;
- buttons;
- table columns;
- table tooltips;
- dialogs;
- toasts;
- validation messages;
- empty states;
- status text.

Use existing `vue-i18n`.

Preserve manually reviewed Russian translations.

When adding new keys:

- add both EN and RU;
- keep keys under the `profile` namespace;
- avoid duplicating existing keys;
- check JSON validity.

Russian tone:

- informal “ты”;
- professional, calm, not cute;
- no formal “Вы”.

---

## 12. Validation requirements

Use the existing validation pattern from the prototype.

Zod is available, but do not introduce heavy form architecture unless it already exists or is clearly beneficial.

Validation timing:

- after blur;
- on submit/save;
- not aggressively on every keystroke unless already implemented cleanly.

Forms must not save if required fields are missing.

Success toast must not appear for invalid forms.

Required fields:

### Contact Details

- Full name
- Professional title
- Email
- Phone
- Location

### Work Experience

- Job title
- Company name
- Start date
- Role and job description

### Projects

- Project name
- Description

### Education

- Institution name
- Degree / Qualification
- Start date

### Courses

- Course / Certificate name
- Provider / Issuer
- Start date

### Additional Info

- Username
- Default resume language
- Additional resume language

---

## 13. Visual design requirements

Use current ResumAIner Vue design DNA.

Use:

- `vue_general.css` tokens;
- PrimeVue Aura;
- PrimeIcons;
- existing header style;
- existing card/table/dialog visual language;
- scoped component styles from prototype.

Design tone:

- clean;
- structured;
- professional;
- warm;
- SaaS-like;
- not playful;
- not gamified;
- not overloaded.

Do not add a right helper column.

Do not make sidebar statuses large colorful badges.

---

## 14. Accessibility requirements

Keep implementation accessible enough for Capstone review:

- labels tied to inputs where possible;
- meaningful button text;
- no icon-only critical actions;
- visible focus states;
- dialogs usable by keyboard;
- route navigation works;
- no mobile horizontal overflow;
- proper contrast;
- table actions discoverable.

---

## 15. Regression test checklist

After implementation, test manually:

### Routing

- `/profile` redirects to `/profile/contact`
- `/profile/contact`
- `/profile/experience`
- `/profile/projects`
- `/profile/education`
- `/profile/courses`
- `/profile/additional`

### Layout

- desktop sidebar works;
- mobile/tiny width tabs show exactly 2 rows × 3 items;
- no right helper column;
- main content uses available width.

### Contact Details

- invalid email blocks save;
- invalid URLs block save if filled;
- save resets dirty state;
- status updates to Completed after required fields.

### Work Experience

- empty form cannot save;
- current checkbox hides End Date;
- Add/Edit works;
- dirty warning works;
- card preview is compact.

### Projects

- empty form cannot save;
- ongoing checkbox hides End Date;
- Add/Edit works;
- dirty warning works.

### Education

- empty form cannot save;
- currently studying checkbox hides End Date;
- empty state text is correct;
- card order is institution first.

### Courses

- required validation works;
- search applies only at 3+ characters;
- date filter range validation works;
- Reset appears when filters/sorting active;
- sorting has third reset state;
- tooltips exist on sortable headers;
- row click opens details;
- edit mode works;
- delete confirmation works;
- sidebar count updates immediately.

### Additional Info

- username validation rejects Cyrillic/special characters/spaces;
- username helper shown;
- default/additional languages are mutually exclusive;
- relocation checkbox absent from work formats;
- Date of Birth is full date.

### i18n

- EN/RU switch works;
- no hardcoded Profile UI text;
- toasts have no final period;
- manually reviewed RU text preserved.

### Build

Run:

~~~bash
npm install
npm run build
~~~

Fix all TypeScript/build errors.

---

## 16. Expected final output from OpenCode

After implementation, provide a concise summary:

1. files created;
2. files modified;
3. profile routes implemented;
4. whether localStorage mock or real API is used;
5. validation implemented;
6. known limitations / TODOs;
7. commands run and results.

Do not claim backend integration was completed unless real backend endpoints were actually connected and tested.

---

## 17. Final instruction

Implement User Profile by integrating the approved OpenDesign prototype into the real ResumAIner frontend.

Use the prototype as the strict baseline.

Do not design a new solution.

Do not remove approved UX decisions.

Focus on correct integration, clean code, working build, and future backend readiness.
