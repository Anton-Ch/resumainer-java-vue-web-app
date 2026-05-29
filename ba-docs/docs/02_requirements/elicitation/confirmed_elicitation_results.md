# Confirmed Elicitation Results

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-12  
**Last Updated:** 2026-05-21  
**Author:** Anton  
**Version:** 6.0  
**Status:** Approved  
**Related BABOK Area:** 4.2 Conduct Elicitation / 4.3 Confirm Elicitation Results  

---

## 1. Description

This document confirms the results of UI/UX elicitation activities conducted for the ResumAIner Capstone project. It synthesizes role-based questionnaire responses, technical feasibility reviews, and stakeholder feedback into actionable UI/UX requirements, page-level decisions, and implementation guidance.

The confirmed results define MVP page scope, page-level structure, key user/admin flows, and implementation-relevant UI decisions. These results are used as input for wireframes, UI/UX requirements, sitemap, user flows, requirements traceability, and development handoff.

## 2. Elicitation Method Summary

The elicitation activity used a structured, role-based questionnaire covering four stakeholder perspectives:
- Registered User / Job Seeker (primary end-user)
- Recruiter / External Viewer (public resume consumers)
- Administrator (system oversight and control)
- Developer / Technical Reviewer (implementation feasibility)

The questionnaire focused on practical screen-level decisions including page necessity, required blocks, key actions, error/empty states, user guidance, and MVP prioritization. Responses were analyzed using both quantitative (priority scoring) and qualitative (thematic grouping) methods.

Confirmation Basis:
- project owner review;
- mentor feedback;
- questionnaire responses;
- technical feasibility review.

## 3. Respondent Segments / Review Sources

| Respondent Segment | Role | Input Method | Review Sources |
| :--- | :--- | :--- | :--- |
| **Registered User / Job Seeker** | End User | Role-based questionnaire section | Direct user feedback on profile, generation, review workflows |
| **Recruiter / External Viewer** | External Consumer | Role-based questionnaire section | Public resume access expectations, PDF usability |
| **Administrator** | System Manager | Role-based questionnaire section | User management, abuse prevention, usage monitoring needs |
| **Developer / Technical Reviewer** | Technical Validator | Role-based questionnaire section | Implementation complexity, feasibility, architecture constraints |

## 3. Confirmed MVP Page Map

### 3.1 Public / Visitor Pages

1. Landing Page
2. Login / Register
3. Public PDF Resume Link

### 3.2 Registered User Pages

1. User Home
2. My Profile
3. Generate Resume
4. Resume Review

Confirmed changes:

- Separate `Resume History` page is removed.
- Resume listing is integrated into `User Home`.
- Separate `User Settings` page is removed.
- User settings are integrated into `My Profile`.
- Separate `Resume Details` page is removed.
- PDF actions (download, public link copy) are available from User Home table and post-save flow.

### 3.3 Administrator Pages

1. Admin Home
2. Users
3. User Details
4. Resumes
5. AI Models
6. AI Model Details

Confirmed changes:

- Separate `Resume Details (admin view)` page is removed.
- Admin can view generated and saved resume PDFs from the Resumes table.

### 3.4 Recruiter / External Viewer Access

1. Public PDF Resume Link

Recruiters do not need an account or separate portal in MVP.

## 4. Confirmed Scope Classification

### 4.1 MVP

- Landing Page
- Login / Register
- User Home with integrated resume listing
- My Profile with core profile sections and settings
- Generate Resume
- Resume Review
- PDF download (from User Home and post-save flow)
- Public PDF resume link
- Admin Home
- Users
- User Details
- Resumes (with PDF preview and download actions)
- AI Models
- AI Model Details
- Token usage logging when provider data is available
- Mock AI provider fallback
- Generate all adaptation variants
- Cover letter generation

### 4.2 MVP Stretch

- ATS JSON endpoint
- Google OAuth2 login
- Language tabs for bilingual resume review
- Advanced filtering
- Admin access audit log
- Password change in My Profile
- Photo upload in My Profile
- Reorder skills, work experience, education, and projects

### 4.3 Post-MVP / Future Scope

- Payment system / subscription plans
- User-owned API keys
- Advanced analytics dashboard
- Resume scoring
- Prompt marketplace
- Recruiter accounts
- Recruiter comments/feedback
- Advanced PDF templates
- Full resume version comparison
- Values and Hobbies profile sections
- Advanced public resume defaults

## 5. Confirmed Page-Level Results

## 5.1 Landing Page

**Purpose:** Explain product value, provide login/register entry, and briefly show how the product works.

**MVP blocks:**

- product name and short value proposition;
- Login and Register links;
- short “How it works” explanation;
- key feature list;
- interface language switcher if i18n is included in MVP.

**Stretch blocks:**

- example generated resume preview;
- FAQ/help block;
- privacy/security note.

## 5.2 Login / Register

**Purpose:** Create account and access the system.

**Confirmed decisions:**

- If an authenticated user opens Login/Register page, the system redirects to the appropriate Home page.
- User role redirects to `User Home`.
- Admin role redirects to `Admin Home`.
- Log out action is not shown on Login/Register page.

**MVP blocks:**

- email field;
- password field;
- confirm password field for registration;
- Login/Register switch link;
- password requirements hint.

**Stretch/future:**

- Google login;
- password reset.

## 5.3 User Home

**Purpose:** Main post-login page for regular users. Provides quick access to profile editing, resume generation, and saved resumes.

**MVP blocks:**

- primary CTA: Edit Profile;
- secondary CTA: Generate New Resume;
- searchable/sortable table of saved resumes;
- columns: number, vacancy, resume title, language, adaptation level, created date, PDF link/status;
- filtering and pagination;
- basic empty states.

**MVP actions:**

- open My Profile;
- open Generate Resume;
- search resumes;
- sort resume table columns;
- download PDF directly from table;
- copy public resume link if available.

**Empty states:**

- empty profile: “Complete your profile first to generate better resumes.”
- no resumes: “Generate your first adapted resume.”
- no search results: “No resumes match your search.”

## 5.4 My Profile

**Purpose:** Central place for user profile data and user settings.

**Confirmed structure:**

- Use tabs, sidebar sections, or accordion sections.
- Do not create a separate User Settings page.
- Keep profile data and settings in separate sections inside My Profile.

**Confirmed My Profile sections:**

1. Contact Details
2. Work Experience
3. Projects & Volunteering
4. Education
5. Courses & Certificates
6. Additional Info

**Contact Details fields:**

- Full name — required;
- Professional title — optional;
- Email — required;
- Phone — required;
- Location — required;
- LinkedIn URL — optional;
- Portfolio / Website URL — optional;
- Telegram — optional;
- WhatsApp — optional.

**Work Experience fields:**

- Job title — required;
- Company name — required;
- Location — optional;
- Start date — required;
- End date — optional if current role;
- Role and job description — required;
- Company URL — optional.

**Projects & Volunteering fields:**

- Project name — required;
- Role — optional;
- Start date — optional;
- End date — optional;
- Description — required;
- Project URL — optional.

**Education fields:**

- Institution name — required;
- Degree / Qualification — required;
- Field of study / Major — optional;
- Start year — required;
- End year — optional if currently studying;
- Location — optional;
- Comment / Description — optional;
- GPA / Grade — optional.

**Courses & Certificates fields:**

- Course / Certificate name — required;
- Provider / Issuer — required;
- Start date — required;
- End date — optional;
- Credential URL — optional;
- Skills / Topics — optional;
- Short description — optional.

**Additional Info fields:**

- Skills — optional;
- Languages — optional;
- Professional aspirations — optional;
- Achievements — optional;
- Default resume language — optional dropdown;
- Additional resume language — optional dropdown;
- General information for AI context — optional;
- Profile picture — optional;
- Username — required and URL-friendly.

**MVP actions:**

- add record;
- edit record;
- delete record;
- save section.

**Confirmed UI pattern:**

- Repeatable profile sections use card list + Add/Edit form.
- Repeatable records are automatically sorted where applicable.

## 5.5 Generate Resume

**Purpose:** Collect vacancy context and generation settings.

**MVP structure:**

- Single page for MVP.
- Multi-step wizard remains future improvement.

**MVP fields:**

- Vacancy description — required;
- Company description — optional;
- Professional aspirations — optional;
- Language — required dropdown;
- Adaptation level — required dropdown;
- AI model — required dropdown;
- Additional comments for AI — optional.

**MVP actions:**

- generate resume;
- clear form;
- return to My Profile;
- select language;
- select adaptation level;
- select AI model.

**Error states:**

- vacancy description is empty;
- profile data is incomplete;
- user is not allowed to generate resumes;
- AI provider is unavailable;
- generation timeout;
- selected model is invalid or inactive;
- generated result is empty.

**Guidance:**

- Balanced adaptation should be the recommended default.
- User must review generated content before saving.

## 5.6 Resume Review

**Purpose:** Let user review, edit, and save generated resume draft.

**MVP blocks:**

- generated resume preview;
- editable resume sections;
- save final version button;
- discard draft button;
- warning about factual review.

**MVP actions:**

- edit generated text;
- save final version;
- discard draft.

**Stretch actions:**

- return to generation settings;
- regenerate resume;
- compare variants;
- download PDF before saving;
- copy public link before final save.

## 5.7 Resume Details — Removed from MVP

**Status:** Removed per DEC-014 / CR-013.
**Reason:** Page did not provide standalone value. All functions — PDF viewing, download, and public link copying — are available from User Home (resume listing row actions) and the post-save success flow after Resume Review.

## 5.8 Public Resume Access

**Confirmed decision:** Public resume link opens PDF directly.

**Confirmed behavior:**

- Recruiter does not need an account.
- Public URL opens selected saved PDF resume version.
- PDF must be readable in browser where supported.
- PDF must support text selection and copying.

**Recruiter actions:**

- view PDF;
- copy text from PDF;
- print PDF;
- save/download PDF to local PC.

**Privacy rule:**

- Public resume shows only the final saved resume version.
- Public access must not expose internal profile data, drafts, generation settings, token usage, or admin data.

## 5.9 PDF Resume Experience

**Confirmed PDF expectations:**

- two-page layout is acceptable;
- selectable text is required;
- easy printing is required;
- ATS-friendly formatting is important;
- clear human-readable sections are required;
- contact details must be easy to find.

## 6. Confirmed Admin Results

## 6.1 Admin Home

**Purpose:** Main admin overview page.

**MVP blocks:**

- total users count;
- total generated resumes;
- token usage summary;
- quick links to Users, Resumes, AI Models.

## 6.2 Users

**Purpose:** Show all registered users in a searchable/sortable table.

**MVP columns:**

- username;
- email;
- role;
- account status;
- generation permission;
- created date;
- resume count;
- token usage summary.

**MVP actions:**

- open User Details;
- search users;
- filter by role, status, and generation permission.

## 6.3 User Details

**Purpose:** Show selected user details and manage access.

**MVP blocks/actions:**

- basic user information;
- role editor: User/Admin;
- account status editor;
- generation permission editor;
- resume count;
- token usage statistics;
- generation history;
- link to user resumes.

**Required confirmations:**

- change role;
- set inactive;
- reactivate;
- forbid generation;
- allow generation.

## 6.4 Resumes

**Purpose:** Show all generated/saved resumes in a searchable/sortable table.

**MVP columns:**

- resume title;
- owner username/email;
- language;
- adaptation level;
- AI model used;
- token usage;
- created date;
- public/private status.

**MVP actions:**
- open resume details — removed (per DEC-014 / CR-013).
- Admin can view generated resume PDF directly from the Resumes table.

## 6.5 Resume Details for Admin — Removed

**Status:** Removed per DEC-014 / CR-013.
**Reason:** Admin can view generated and saved resume PDFs directly from the Resumes table. A separate detail page did not provide standalone value.

## 6.6 AI Models

**Purpose:** Show all AI models configured in the system.

**MVP columns:**

- display name;
- provider name;
- model code;
- provider base URL;
- active/inactive status;
- free/paid flag;
- max context tokens;
- usage count.

## 6.7 AI Model Details

**Purpose:** Show and manage selected AI model configuration.

**MVP blocks/actions:**

- display name;
- provider name;
- model code;
- provider base URL;
- masked API key;
- active/inactive status;
- free/paid flag;
- max context tokens;
- notes/description;
- replace API key;
- delete API key;
- activate/deactivate model.

**Security decision:**

- API key is always masked after saving.
- API key is never logged.
- Admin can replace or delete the key, but cannot view the saved key in full.

## 7. Developer / Technical Reviewer Results

## 7.1 Frontend Architecture

**Confirmed decision:** Use hybrid frontend approach.

- Thymeleaf only for Landing Page.
- Vue 3 (Composition API) + Vite + PrimeVue for the main authenticated application (DEC-052).
- Backend exposes RESTful endpoints for Vue frontend interaction.
- PrimeVue provides responsive components, ready themes, and cross-browser compatibility (Chrome, Firefox, Edge).
- Frontend validation uses Vuelidate library integrated with Vue 3 Composition API (DEC-055).

## 7.2 Highest Complexity Areas

| Area | Complexity | Reason |
|---|---|---|
| My Profile | High | Multiple sections and repeatable forms |
| Resume Review | High | Editable generated content and save flow |
| PDF Generation | High | A4 layout, selectable text, formatting |
| AI Model Details | High | API key security |
| OpenRouter Integration | High | External dependency and provider errors |
| Vue + Spring MVC Integration | Medium/High | Frontend/backend coordination |

## 7.3 Recommended MVP Vertical Slice

1. Register/login.
2. Complete minimal profile.
3. Enter vacancy and generation settings.
4. Generate draft using mock AI provider.
5. Review and edit generated draft.
6. Save final resume.
7. Display resume in User Home table with PDF actions.
8. Download PDF from User Home table or post-save flow.
9. Open public PDF link.
10. Admin can view users, resumes, usage, and AI models.

## 8. Open Questions

The following questions remain open:

1. Is Vue fully allowed for final implementation?
2. Is real OpenRouter API usage allowed during final demo?
3. Should ATS JSON endpoint remain MVP Stretch or move to MVP?
4. How much admin access logging is required for MVP?

The following questions are closed:

- Resume History is integrated into User Home.
- User Settings is integrated into My Profile.
- Public recruiter link opens PDF directly.
- API key is always masked after saving and never logged.
- Work Experience description is required.
- Education start year is required.
- Profile picture is optional.

## 9. Affected Project Control Artifacts

The confirmed results require updates to:

- `decision_log.md`
- `change_request_log.md`
- `open_questions_log.md`
- `risk_register.md`
- `requirement_readiness_checklist.md`
- `traceability_matrix.md`
- `wireframe_field_requirements.md`

## 10. Summary

The elicitation results confirm a focused but portfolio-worthy MVP.

The main product flow is:

1. Visitor opens Landing Page.
2. User registers or logs in.
3. User lands on User Home.
4. User completes profile and settings in My Profile.
5. User generates resume.
6. User reviews and saves final resume.
7. Saved resume appears in User Home table with PDF actions.
8. User downloads PDF or copies public recruiter link directly from User Home table or post-save flow.
9. Recruiter opens public PDF link directly.
10. Admin monitors users, resumes, AI models, and usage.