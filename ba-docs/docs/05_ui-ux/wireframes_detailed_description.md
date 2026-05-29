# Wireframes Detailed Description

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Source File:** `Wireframes scheme.drawio`  
**Source Tool:** diagrams.net / draw.io  
**Date Prepared:** 2026-05-16  
**Status:** Draft for downstream BA analysis  
**Related BABOK Area:** 7.1 Specify and Model Requirements  

---

## 1. Purpose

This document describes the full content of the ResumAIner wireframes exported from diagrams.net.

The goal is to provide a clear, structured, and unambiguous description of all visible screens, flows, UI blocks, fields, tables, buttons, and role-specific areas for downstream business analysis and requirements refinement.

This document describes what is shown in the wireframes and incorporates confirmed interpretation decisions provided by the project owner.

---

## 2. Confirmed Interpretation Rules

The following decisions must be used when interpreting the wireframes:

| Topic | Confirmed Interpretation |
|---|---|
| Removed duplicate Generate Resume screen | The accidental duplicate was removed from the updated XML. Only the current `Generate Resume 6` screen should be described. |
| Resume Details | Resume Details is not a separate full page. It is represented as a modal popup opened from the `Open details` button in the User Home resume table. |
| Public resume access | Public recruiter link opens PDF directly. |
| AI model API key | API key is entered in full during Add AI Model, but after saving it must be displayed as masked value in AI Model Details. |
| User Details examples | In Admin User Details, `eg. John Doe` means full name and `eg. johndoe` means username. |
| User Home resume table | The table visibly shows `Open details`; download/copy link actions are available in the modal popup. |
| Resume Review `Save & Create` | Saves the final resume version and creates PDF/public link. |
| Cover letter | Cover letter is included in MVP. |
| Tables search/filter/sort | Describe only visible search/filter fields. Sorting is intended for columns, but do not invent filters that are not visible. |
| Profile picture | Although the wireframe visually marks Photo with an asterisk, confirmed requirement is that profile picture is optional. |

---

## 3. Overall Role-Based Flow

The wireframe contains a general role-based scheme with four swimlanes:

1. Visitor
2. Registered User
3. Admin
4. Recruiter

### 3.1 Visitor Flow

Visible nodes:

- Landing page
- Register
- Login

Flow:

1. Visitor opens Landing Page.
2. Visitor can proceed to Register.
3. Visitor can proceed to Login.
4. After authentication, the user is routed according to role.

### 3.2 Registered User Flow

Visible nodes:

- User Home
- Profile
- Generate Resume
- Resume Review

Flow:

1. Registered user lands on User Home.
2. From User Home, user can open Profile.
3. From User Home, user can open Generate Resume.
4. Generate Resume leads to Resume Review.
5. Resume Review `Save & Create` creates final resume, PDF, and public link.
6. Saved resumes are listed on User Home.
7. User opens Resume Details modal from User Home table.

### 3.3 Admin Flow

Visible nodes:

- Admin Home
- Users
- User Details
- Resumes
- AI Models
- Add AI Model
- AI Model Details

Flow:

1. Admin lands on Admin Home.
2. Admin Home links to Users.
3. Admin Home links to Resumes.
4. Admin Home links to AI Models.
5. Users leads to User Details.
6. AI Models leads to Add AI Model.
7. Add AI Model leads to AI Model Details.

### 3.4 Recruiter Flow

Visible node:

- Resume PDF page

Flow:

1. Recruiter opens public resume URL.
2. Public URL opens PDF directly.
3. Recruiter can view, print, copy selectable text, and save/download PDF.

---

## 4. Shared Layout Elements

Most application pages share these elements:

| Element | Description |
|---|---|
| Logo | Located near the top-left area. |
| URL bar / route indicator | Shows example application URL, usually `https://www.project.com/app`. |
| Language Switcher | Located in the top-right area. |
| Logout button | Visible on authenticated user/admin pages. Not shown on Landing/Register/Login pages. |
| Main content area | Central area where page-specific forms, tables, or cards are displayed. |

---

# 5. Screen Descriptions

---

## 5.1 Landing Page 1

**Role:** Visitor  
**Route shown:** `https://www.project.com/`  
**Purpose:** Introduce the product and provide entry points to registration and login.

### Visible Blocks

| Block | Description |
|---|---|
| Logo | Brand or application logo. |
| Language Switcher | Allows user to switch interface language. |
| Hero Value Proposition | Main product value statement. Visible text: `1. HERO VALUE PROPOSITION`. |
| Register button | Primary entry to account creation. |
| Login button | Entry for existing users. |
| Short “How it works” explanation | Brief explanation of product workflow. |

### Main Actions

- Open Register page.
- Open Login page.
- Switch interface language.

### Notes

Landing Page is mandatory for MVP.

---

## 5.2 Register 2

**Role:** Visitor  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Create a new account.

### Visible Blocks and Fields

| Element | Description |
|---|---|
| Logo | Application logo. |
| Language Switcher | Interface language selector. |
| Page title | `Register`. |
| Email field | User email input. |
| Password field | User password input. |
| Password confirmation field | Repeated password input. |
| Login link | `Already Registered? Login now!` |

### Main Actions

- Enter email.
- Enter password.
- Confirm password.
- Switch to Login page.
- Switch interface language.

### Expected Validation

- Email is required.
- Password is required.
- Password confirmation is required.
- Password and confirmation must match.

---

## 5.3 Login 2.1

**Role:** Visitor  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Authenticate existing user.

### Visible Blocks and Fields

| Element | Description |
|---|---|
| Logo | Application logo. |
| Language Switcher | Interface language selector. |
| Page title | `Login`. |
| Email field | User email input. |
| Password field | User password input. |
| Register link | `Don't have account: Register now!` |

### Main Actions

- Enter email.
- Enter password.
- Log in.
- Switch to Register page.
- Switch interface language.

### Confirmed Behavior

If an already authenticated user opens Login/Register, the system redirects them to the appropriate Home page:

- regular user → User Home;
- admin → Admin Home.

---

## 5.4 User Home 3

**Role:** Registered User  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Main page after login for regular users. Provides resume generation entry, profile entry, user statistics, hints, and saved resume listing.

### Visible Blocks

| Block | Description |
|---|---|
| Logo | Application logo. |
| Language Switcher | Interface language selector. |
| Logout | Logout button. |
| Stats Summary | Shows `Total tokens sent`, `Total tokens generated`, `Total resumes created`. |
| Edit my profile | Button to open Profile. |
| Generate new resume | Button to open Generate Resume. |
| Profile Hints | Help/hint block for profile completion or next steps. |
| Resume listing table | Main table of generated/saved resumes. |
| Pagination | Previous, page numbers, Next. |

### Resume Listing Table Columns

| Column | Description |
|---|---|
| № | Row number. |
| Vacancy | Vacancy title or reference. |
| Resume Title | Saved resume title. |
| Language | Resume language. |
| Adaptation level | Minimal / Balanced / Maximum. |
| Created | Creation date. |
| Details | Action column with `Open details`. |

### Visible Filter/Search Elements

Visible filter/search-like elements are shown for:

- Vacancy title;
- Resume title;
- Language;
- Adaptation level;
- Created date.

### Main Actions

- Open Profile.
- Open Generate Resume.
- View saved resumes in table.
- Use visible table filters/search fields.
- Navigate pages through pagination.
- Click `Open details` for a selected resume.

### Confirmed Behavior

`Open details` opens a Resume Details modal popup on User Home.

---

## 5.5 Resume Details Modal Popup

**Role:** Registered User  
**Opened from:** User Home → resume table → `Open details`  
**Purpose:** Show selected resume output details and provide PDF/link actions.

### Visible Blocks

| Block | Description |
|---|---|
| Modal pop-up window | Popup container. |
| Link to your pdf resume | Public link block. |
| Public URL | Example: `https://example/username/YRFJ`. |
| Download PDF | Button to download generated PDF. |
| Cover letter | Cover letter block. |
| Cover letter text | Example text: `Interested in your vacancy would be glad if you consider my CV`. |

### Main Actions

- Copy public PDF resume link.
- Download PDF.
- View cover letter text.

### Confirmed Behavior

This modal is the replacement for a separate Resume Details page in the current MVP wireframe.

---

## 5.6 Admin Home 4

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Main page after login for admin. Provides overview and entry points to admin areas.

### Visible Blocks

| Block | Description |
|---|---|
| Logo | Application logo. |
| Language Switcher | Interface language selector. |
| Logout | Logout button. |
| Stats Summary | Shows `Total tokens sent`, `Total tokens generated`, `Total resumes created`, `Total users`. |
| Users card/link | Entry to Users page. |
| Users description/help text | Short explanation of Users area. |
| Resumes card/link | Entry to Resumes page. |
| Resumes description/help text | Short explanation of Resumes area. |
| AI Models card/link | Entry to AI Models page. |
| AI Models description/help text | Short explanation of AI Models area. |

### Main Actions

- Open Users page.
- Open Resumes page.
- Open AI Models page.
- Logout.
- Switch language.

---

## 5.7 Profile 5 — Contact Details

**Role:** Registered User  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Manage contact information that may be included in generated resumes.

### Shared Profile Navigation

Profile screens share a left-side section navigation:

- Contact Details
- Work Experience
- Projects & Volunteering
- Education
- Courses and Certificates
- Additional info

### Visible Instruction

`Add the contact information that can be safely included in your resume. Only selected contact fields will be shown in generated resume versions.`

### Fields

| Field | Required Marker Visible | Example / Notes |
|---|---:|---|
| Full name | Yes | `eg. John Doe` |
| Location (Country + City) | Yes | `eg. Kazakhstan, Astana` |
| Professional title | No | `eg. Business Analyst, Junior Java Developer` |
| Phone | Yes | `eg. +7-777-777-77-77` |
| Email | Yes | `eg. johndoe@example.com` |
| LinkedIn URL | No | `eg. https://www.linkedin.com/in/john/` |
| Portfolio / Personal Website URL | No | `eg. https://johndoe.portfolio.com` |
| Telegram | No | `eg. @johndoe` |
| WhatsApp | No | `eg. +7-777-777-77-77` |

### Main Actions

- Edit contact fields.
- Save changes.

---

## 5.8 Profile 5.1 — Work Experience

**Role:** Registered User  
**Purpose:** Add and edit recent and relevant work experience.

### Visible Instruction

`Add your recent and relevant work experience. Focus on responsibilities, achievements, tools, and measurable results.`

### Visible Controls

| Control | Description |
|---|---|
| `+ Add Work Experience` | Adds a new work experience record. |
| Delete | Deletes selected work experience record. |
| Save | Saves work experience record. |

### Fields

| Field | Required Marker Visible | Example / Notes |
|---|---:|---|
| Job Title | No explicit asterisk | `eg. Business Analyst` |
| Company name | No explicit asterisk | `eg. Mickey Mouse LLP` |
| Start date | No explicit asterisk | `01.05.2026` |
| End date | No explicit asterisk | `DD.MM.YYYY`; note says leave empty if still working here. |
| Role and job description | No explicit asterisk | Example describes completed requirements gathering and stakeholder meetings. Confirmed requirement: description is required. |
| Location | Yes | `eg. Kazakhstan, Astana (remote work)` |
| Company URL | No | `eg. company.com` |

### Hint Text

`Can include here your responsibilities and achievements.`

### Main Actions

- Add work experience.
- Edit fields.
- Delete record.
- Save record.

---

## 5.9 Profile 5.2 — Projects & Volunteering

**Role:** Registered User  
**Purpose:** Add projects, volunteering, and practical experience records.

### Visible Instruction

`Add personal, academic, professional, or volunteering projects that demonstrate your skills, initiative, and practical experience.`

### Visible Controls

| Control | Description |
|---|---|
| `+ Add Project` | Adds a new project/volunteering record. |
| Delete | Deletes selected project record. |
| Save | Saves project record. |

### Fields

| Field | Example / Notes |
|---|---|
| Project Name | `eg. AI Telegram chat bot` |
| Role | `eg. Developer` |
| Start date | `01.05.2026` |
| End date | `DD.MM.YYYY`; note says leave empty if still part of it. |
| Role and project description | Example describes requirements gathering and stakeholder meetings. |
| Location | `eg. Kazakhstan, Astana (remote work)` |
| Project URL | `eg. project.com` |

### Hint Text

`Describe the project in 2–5 sentences: what it is, who it helps, and what problem it solves. Can include here your responsibilities and achievements.`

### Main Actions

- Add project.
- Edit fields.
- Delete record.
- Save record.

---

## 5.10 Profile 5.3 — Education

**Role:** Registered User  
**Purpose:** Add formal education records.

### Visible Instruction

`Add your formal education, including universities, colleges, degrees, and relevant programs.`

### Visible Controls

| Control | Description |
|---|---|
| `+ Add Study record` | Adds new education record. |
| Delete | Deletes selected education record. |
| Save | Saves education record. |

### Fields

| Field | Example / Notes |
|---|---|
| Institution name | `eg. University ABC` |
| Degree / Qualification | `eg. Bachelor` |
| Start date | `01.05.2026`; confirmed requirement: start year/date is required. |
| End date | `DD.MM.YYYY`; note says leave empty if still studying. |
| Field of study / Major | `eg. Information Systems` |
| Location | `eg. Kazakhstan, Astana (remote work)` |
| Comment | `eg. International University (online study)` |
| GPA / Grade | `eg. 3.75` |

### Main Actions

- Add study record.
- Edit fields.
- Delete record.
- Save record.

---

## 5.11 Profile 5.4 — Courses and Certificates

**Role:** Registered User  
**Purpose:** Add completed or in-progress courses, certificates, and professional training.

### Visible Instruction

`Add completed or in-progress courses, certificates, and professional training that support your target role.`

### Visible Controls

| Control | Description |
|---|---|
| `+ Add Course or certificate record` | Adds course/certificate record. |
| Delete | Deletes selected record. |
| Save | Saves record. |

### Fields

| Field | Example / Notes |
|---|---|
| Course / Certificate name | `eg. Java for everyone` |
| Provider / Issuer | `eg. Coursera` |
| Start date | `01.05.2026` |
| End date | `DD.MM.YYYY`; note says leave empty if still studying. |
| Skills / Topics | `eg. Information Systems` |
| Location | `eg. Kazakhstan, Astana (remote online study)` |
| Description | `eg. International University (online study)` |
| Credential URL | `eg. https://sf.com/4as` |

### Main Actions

- Add course/certificate.
- Edit fields.
- Delete record.
- Save record.

---

## 5.12 Profile 5.5 — Additional Info

**Role:** Registered User  
**Purpose:** Store additional user context, preferences, and settings for resume generation.

### Fields

| Field | Example / Notes |
|---|---|
| Languages | `eg. English C1, Russian native` |
| General Information | `eg. any job related info which can help AI to align data on you to specific vacancy` |
| Skills | `eg. Business Analysis, Java, Python, SQL` |
| Professional aspirations | `eg. Business Analysis, Java, Python, SQL` |
| Achievements | `eg. Business Analysis, Java, Python, SQL` |
| Default language for resume | Example value: `English` |
| Additional language for resume | Example value: `Russian` |
| Ready for relocation | Example value: `Yes` |
| Ready for business trips and rotational schedule | Visible as separate readiness field. |
| Date of birth | Example value: `31.12.1992` |
| Photo | File upload control. Confirmed requirement: optional despite visible asterisk. |
| Username | Example text shown; used in public resume links. |

### Visible Hint

For Username:

`Will be used in your public resume links.`

### Main Actions

- Edit additional information.
- Upload optional photo.
- Save changes.

### Important Notes

- Profile picture is optional.
- Username is important for public resume URL structure.
- Additional info combines user settings and extra AI context; there is no separate Settings page.

---

## 5.13 Generate Resume 6

**Role:** Registered User  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Provide vacancy context and generation settings for AI resume adaptation.

### Visible Instruction

`Paste a target vacancy and choose how the system should adapt your resume.`

### Sections

| Section | Description |
|---|---|
| Vacancy context for AI | Input area for target vacancy and company context. |
| Generation settings for AI | Dropdown settings for resume language, adaptation level, and AI model. |

### Fields

| Field | Required Marker Visible | Example / Options |
|---|---:|---|
| Vacancy description | Yes | `eg. Business Analyst... Main Responsibilities; Key skills...` |
| Company description | No | `eg. ABC Company working in Fintech` |
| Language | Dropdown | Default (English), Additional (Russian), Both |
| Adaptation level | Dropdown | Min, Balanced, Max |
| AI model | Dropdown | GPT, Llama, Qwen, Nematron |
| Additional comments for AI to consider while generating resume | No | `eg. Focus in resume on my courses and certificates` |

### Main Actions

- Select language.
- Select adaptation level.
- Select AI model.
- Enter vacancy context.
- Enter optional company description.
- Enter optional additional comments for AI.
- Click Generate.

### Output Flow

Generate Resume leads to Resume Review.

---

## 5.14 Resume Review 7

**Role:** Registered User  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Review generated resume fields before saving the final version and creating PDF/public link.

### Visible Instruction

`Your adopted resume fields are ready. Please review them below before saving your final version.`

### Top-Level Generated Fields

| Field | Example |
|---|---|
| Professional summary | `eg. Business Analyst with 5 years of experience in ...` |
| Professional role | `eg. Project Management | Agile Delivery` |
| Professional aspiration | `eg. Become the best ever manager` |

### Work Experience Section

Visible repeated rows include:

| Field | Example |
|---|---|
| Job Title | `eg. Business Analyst` |
| Company | `eg. MickeyMouse LLP` |
| Location | `eg. Kazakhstan, Astana` |
| Start date | `01.05.2024` |
| End date | `01.05.2026` |

### Skills Section

Visible repeated rows include:

| Field | Example |
|---|---|
| Skill group | `eg. Leadership`, `eg. Reporting` |
| Skill details | `eg. Team Leadership, Process Improvement, Strategic Planning`; `eg. Performance Reporting, Agile Frameworks, Process Mapping` |

### Courses & Certifications Section

Visible repeated rows include:

| Field | Example |
|---|---|
| Course / Certificate name | `eg. Java for everyone` |
| Provider / Issuer | `eg. Coursera` |
| Focus | `eg. Java Backend Development` |

### Projects & Volunteering Section

Visible repeated rows include:

| Field | Example |
|---|---|
| Project Name | `eg. Java for everyone` |
| Your Role | `eg. Lead Project Manager` |
| Description | `eg. 1 sentence overview of the project's goal and your core responsibility` |
| Impact | `eg. 1 sentence outlining the scale, tech stack, or measurable results of your work` |

### Cover Letter Section

| Field | Example |
|---|---|
| Cover letter text | `eg. Interested in your vacancy would be glad if you consider my CV` |

### Main Action

| Button | Confirmed Meaning |
|---|---|
| Save & Create | Saves final resume version and creates PDF/public link. |

### Notes

- Cover letter is included in MVP.
- The review screen shows generated fields as editable/reviewable content before final saving.
- After `Save & Create`, the resulting resume can be accessed from User Home resume table and details modal.

---

## 5.15 Users 8

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** View registered users in a table.

### Visible Blocks

| Block | Description |
|---|---|
| Page title | `Users`. |
| Description/help text | Explains Users area. |
| Users table | Main table with user data and pagination. |
| Pagination | Previous, page numbers, Next. |

### Table Columns

| Column | Description |
|---|---|
| № | Row number. |
| username | User username. |
| email | User email. |
| total resumes | Number of generated/saved resumes. |
| Total tokens sent | Token usage sent to AI. |
| Total tokens generated | Token usage generated by AI. |
| Created | User creation date. |

### Visible Search/Filter Fields

- username
- email
- Created date

### Main Actions

- View users table.
- Use visible search/filter fields.
- Use pagination.
- Open User Details from user table flow.

---

## 5.16 User Details 9

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** View and edit selected user account/contact data and access controls.

### Confirmed Interpretation

This is admin view of user account + contact data:

- `eg. John Doe` means full name;
- `eg. johndoe` means username.

### Visible Controls / Dropdown Values

| Control | Values |
|---|---|
| Role | User, Admin |
| Status | Active, Blocked |
| Generation Permission | Allowed, Forbidden |

### Fields

| Field | Example / Notes |
|---|---|
| Full name | `eg. John Doe` |
| Username | `eg. johndoe` |
| Phone | `eg. +7-777-777-77-77` |
| Resume Email | `eg. johndoe@example.com` |
| Created date | `01.05.2026` |

### Main Action

- Save changes.

### Admin Capabilities Represented

- Change role.
- Change account status.
- Change generation permission.
- Review basic account/contact information.

---

## 5.17 Resumes 10

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** View generated/saved resumes across users.

### Visible Blocks

| Block | Description |
|---|---|
| Page title | `Resumes`. |
| Description/help text | Explains Resumes area. |
| Resumes table | Main table with resume records. |
| Pagination | Previous, page numbers, Next. |

### Table Columns

| Column | Description |
|---|---|
| № | Row number. |
| username | Resume owner. |
| Resume title | Resume title. |
| Adaptation level | Min / Balanced / Max. |
| Language | Default (English), Additional (Russian), Both. |
| AI model | AI model used. |
| Created | Creation date. |

### Visible Search/Filter Fields

- username
- Resume title
- Created date
- Language dropdown

### Main Actions

- View resumes table.
- Use visible search/filter fields.
- Use pagination.

---

## 5.18 AI Models 11

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** View AI models configured in the system.

### Visible Blocks

| Block | Description |
|---|---|
| Page title | `AI Models`. |
| Description/help text | Explains AI Models area. |
| `+ Add AI Model` button | Opens Add AI Model screen. |
| AI models table | Main table with AI model metadata. |
| Pagination | Previous, page numbers, Next. |

### Table Columns

| Column | Description |
|---|---|
| № | Row number. |
| Provider | AI provider. |
| Model name | Human-readable model display name. |
| Model codes | Provider model code. |
| Total tokens sent | Total prompt/input tokens sent through model. |
| Total tokens generated | Total completion/output tokens generated by model. |
| Created | Creation date. |

### Visible Search/Filter Fields

- provider
- Model name
- Model codes
- Created date

### Main Actions

- Open Add AI Model.
- View AI models table.
- Use visible search/filter fields.
- Use pagination.

---

## 5.19 Add AI Model 12

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** Add a new AI model/provider configuration.

### Visible Controls / Dropdown Values

| Control | Values |
|---|---|
| Status | Active, Disabled |

### Fields

| Field | Example / Notes |
|---|---|
| Provider | `eg. OpenRouter` |
| Provider API URL | `eg. https://openrouter.ai` |
| API key | `eg. sk-or-v1-your_actual_key_here`; entered in full during creation. |
| Model Display Name | `eg. Deepseek v4 Pro` |
| Model code | `eg. deepseek/deepseek-v4-pro` |

### Main Action

- Save new AI model.

### Security Notes

- API key is entered in full only during creation/replacement.
- API key must not be logged.
- After saving, API key must be masked in details view.

---

## 5.20 AI Model Details 13

**Role:** Admin  
**Route shown:** `https://www.project.com/app`  
**Purpose:** View and edit AI model configuration.

### Visible Controls / Dropdown Values

| Control | Values |
|---|---|
| Status | Active, Disabled |

### Fields

| Field | Example / Notes |
|---|---|
| Provider | `eg. OpenRouter` |
| Provider API URL | `eg. https://openrouter.ai` |
| API key | Wireframe shows full example, but confirmed requirement is masked value after saving. |
| Model Display Name | `eg. Deepseek v4 Pro` |
| Model code | `eg. deepseek/deepseek-v4-pro` |
| Created date | `01.05.2026` |

### Main Action

- Save changes.

### Confirmed API Key Behavior

- Saved API key is displayed only as masked value.
- Admin can replace API key.
- Admin cannot view saved API key in full after saving.
- API key must not be logged.

---

## 5.21 Resume PDF Page 14

**Role:** Recruiter / External Viewer  
**Route shown:** `https://www.project.com/username/WRYJ`  
**Purpose:** Public resume access through direct PDF link.

### Visible Blocks

| Block | Description |
|---|---|
| Public URL | Example public resume URL. |
| PDF Resume | Main PDF content area. |

### Confirmed Behavior

- Public recruiter link opens PDF directly.
- Recruiter does not need an account.
- PDF must support direct viewing in browser where supported.
- PDF must support selectable text.
- Recruiter can print and save/download the PDF.

---

## 6. Page Inventory Summary

| No. | Screen / Component | Role | Type |
|---:|---|---|---|
| 1 | Landing Page | Visitor | Public page |
| 2 | Register | Visitor | Authentication page |
| 2.1 | Login | Visitor | Authentication page |
| 3 | User Home | Registered User | Main user page |
| 3a | Resume Details Modal | Registered User | Modal popup on User Home |
| 4 | Admin Home | Admin | Main admin page |
| 5 | Profile — Contact Details | Registered User | Profile section |
| 5.1 | Profile — Work Experience | Registered User | Profile section |
| 5.2 | Profile — Projects & Volunteering | Registered User | Profile section |
| 5.3 | Profile — Education | Registered User | Profile section |
| 5.4 | Profile — Courses and Certificates | Registered User | Profile section |
| 5.5 | Profile — Additional Info | Registered User | Profile/settings section |
| 6 | Generate Resume | Registered User | Resume generation page |
| 7 | Resume Review | Registered User | Review and save page |
| 8 | Users | Admin | Admin table page |
| 9 | User Details | Admin | Admin details/edit page |
| 10 | Resumes | Admin | Admin table page |
| 11 | AI Models | Admin | Admin table page |
| 12 | Add AI Model | Admin | Admin create form |
| 13 | AI Model Details | Admin | Admin details/edit form |
| 14 | Resume PDF Page | Recruiter | Public PDF page |

---

## 7. Confirmed MVP Navigation Summary

### Public / Visitor

- Landing Page → Register
- Landing Page → Login
- Register/Login → User Home for regular user
- Register/Login → Admin Home for admin

### Registered User

- User Home → Profile
- User Home → Generate Resume
- User Home → Resume Details modal through `Open details`
- Generate Resume → Resume Review
- Resume Review → creates saved resume + PDF/public link
- Resume Details modal → PDF link/download + cover letter

### Admin

- Admin Home → Users
- Admin Home → Resumes
- Admin Home → AI Models
- Users → User Details
- AI Models → Add AI Model
- Add AI Model → AI Model Details

### Recruiter

- Public link → Resume PDF page

---

## 8. Key MVP Decisions Reflected in Wireframes

| Decision | Reflected In |
|---|---|
| Landing Page is mandatory | Landing Page 1 |
| User and Admin have different Home pages | User Home 3, Admin Home 4 |
| Separate Resume History page is removed | Resume table is on User Home 3 |
| Resume Details is modal popup | Open details button + modal popup |
| Settings are inside My Profile | Profile 5.5 Additional Info |
| Profile uses section navigation | Profile 5–5.5 |
| Repeatable profile sections use Add/Edit/Delete pattern | Profile 5.1–5.4 |
| Generate Resume includes Additional comments for AI | Generate Resume 6 |
| Cover letter is MVP | Resume Review 7 and Resume Details modal |
| Public recruiter link opens PDF directly | Resume PDF page 14 |
| Admin manages users, resumes, AI models | Admin Home 4, Users 8, Resumes 10, AI Models 11 |
| API key is masked after saving | AI Model Details 13 confirmed interpretation |

---

## 9. Items That Need Care During Requirements Conversion

These items are visible in the wireframes but should be handled carefully when converting to final requirements:

1. **Profile picture marker**  
   Wireframe visually shows `Photo*`, but confirmed requirement is profile picture optional.

2. **AI Model Details API key example**  
   Wireframe shows a full example key, but final requirement is masked display after saving.

3. **Search/filter controls**  
   Describe only visible filters/search fields. Do not add hidden filters unless later confirmed.

4. **Resume Details modal**  
   This is not a separate page in the current MVP wireframe.

5. **Work Experience description**  
   Confirmed required for useful resume generation, even if no explicit asterisk is visible on the wireframe.

6. **Education start date/year**  
   Confirmed required.

7. **Location in Work Experience**  
   Wireframe visually marks Location with an asterisk. Treat as visible required marker unless later changed in requirements.

---

## 10. Recommended Downstream Artifacts to Update

The wireframe description should be used to update or refine:

- UI/UX requirements;
- sitemap / information architecture;
- user workflows;
- confirmed elicitation results;
- wireframe field requirements;
- requirements traceability matrix;
- requirement readiness checklist;
- data model draft;
- acceptance criteria for key screens;
- development handoff notes.

---

## 11. Summary

The wireframes define a compact but complete MVP structure:

1. Visitor can register or log in from Landing Page.
2. Regular user works from User Home, Profile, Generate Resume, Resume Review, and Resume Details modal.
3. Saved resumes are listed directly on User Home.
4. Resume Details is a modal popup with public PDF link, Download PDF, and cover letter.
5. Recruiter opens a public URL that directly displays the PDF resume.
6. Admin has Home, Users, User Details, Resumes, AI Models, Add AI Model, and AI Model Details.
7. AI model configuration includes API keys, but saved keys must be masked and not logged.
8. The MVP includes cover letter generation and PDF resume sharing.
