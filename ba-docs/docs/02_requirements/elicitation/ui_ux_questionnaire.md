# UI/UX Questionnaire

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-11  
**Last Updated:** 2026-05-11  
**Author:** Anton  
**Version:** 1.0  
**Status:** Approved  
**Related BABOK Area:** 4.1 Prepare for Elicitation  

---

## 1. Description

This questionnaire is used to collect role-specific UI/UX requirements for the ResumAIner Capstone project.

The questionnaire focuses on practical screen-level decisions:

- which page blocks are necessary;
- which actions must be available;
- which errors and empty states should be handled;
- what user guidance is needed;
- what can be postponed to later versions;
- which UI decisions may create implementation risk.

The results will be used to create UI/UX requirements, sitemap, user flows, wireframes, acceptance criteria, and development handoff notes.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Respondents should answer only the section relevant to their role.
- If a question is not relevant, answer `N/A`.
- When possible, use the controlled values provided below.
- Open comments should be specific and practical.
- Feature ideas should be marked as MVP, MVP Stretch, Post-MVP, Future Scope, or Not Needed.
- This questionnaire is not intended to define the final visual design. It is intended to identify useful UI structure, behavior, and priorities.

### 2.2 Page Priority Values

| Value | Meaning |
|---|---|
| MVP | Required for the first working version |
| MVP Stretch | Useful if time allows but not required |
| Post-MVP | Should be implemented after MVP |
| Future Scope | Long-term idea |
| Not Needed | Not useful for this product |
| Not Sure | Requires further discussion |

### 2.3 Block Importance Values

| Value | Meaning |
|---|---|
| Must Have | Required for the page to work properly |
| Should Have | Important but not critical |
| Could Have | Nice to have if implementation is easy |
| Not Needed | Should not be included |
| Not Sure | Requires further discussion |

### 2.4 Action Importance Values

| Value | Meaning |
|---|---|
| Critical | The workflow cannot work without this action |
| Important | Strongly improves user experience |
| Optional | Useful but not necessary |
| Not Needed | Should not be included |
| Not Sure | Requires clarification |

### 2.5 Complexity Values for Developer Review

| Value | Meaning |
|---|---|
| Low | Simple to implement |
| Medium | Requires moderate effort |
| High | Requires significant effort or coordination |
| Critical | May threaten MVP timeline or architecture |

---

## 3. Respondent Profile

1. What role are you answering as?
   - Registered User / Job Seeker
   - Recruiter / External Viewer
   - Administrator
   - Developer / Technical Reviewer

2. How familiar are you with resume builders or job application tools?
   - Not familiar
   - Slightly familiar
   - Moderately familiar
   - Very familiar

3. What device would you most likely use for this system?
   - Laptop / Desktop
   - Tablet
   - Mobile phone
   - Multiple devices

4. What matters most for this product?
   - Fast resume generation workflow
   - Detailed control over resume content
   - Easy editing before saving
   - Clean public resume presentation
   - PDF download
   - Public sharing link
   - Other: [please specify]

---

## 4. Questions for Registered User / Job Seeker

This section is for users who create profiles, generate resumes, review drafts, save final versions, download PDFs, and share public links.

---

### 4.1 Landing Page

The Landing Page is mandatory. The purpose of these questions is not to decide whether the page exists, but to clarify what it should contain.

#### Candidate Blocks

Rate each block:

| Block | Importance |
|---|---|
| Product name and short value proposition | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Short “How it works” explanation | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Login and Register buttons | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Example generated resume preview | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Key feature list | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Privacy/security note | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Interface language switcher | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| FAQ / help block | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available on the Landing Page?
   - Log in
   - Register
   - View example resume
   - Change interface language
   - Read short product explanation
   - Other: [please specify]

#### Guidance

2. What should a new visitor understand within the first 10 seconds?

3. What information should not be shown on the Landing Page because it would distract from login or registration?

---

### 4.2 Login / Register Page

#### Candidate Blocks

| Block | Importance |
|---|---|
| Email field | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Username field | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Password field | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Confirm password field | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Login/Register switch link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Google login button | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Password requirements hint | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Terms/privacy notice | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions are required?
   - Register account
   - Log in
   - Switch between login and registration
   - Show/hide password
   - Reset password
   - Other: [please specify]

2. If an already authenticated user opens Login/Register page, should the system redirect them to Home page?
   - Yes
   - No
   - Not sure

#### Errors and Empty States

3. Which error messages should be clear and user-friendly?
   - Empty email
   - Invalid email format
   - Empty password
   - Weak password
   - Passwords do not match
   - Email already exists
   - Wrong login or password
   - User account is inactive
   - Other: [please specify]

#### Guidance

4. What hints should be shown near password and email fields?

---

### 4.3 User Home Page

The User Home Page is the first page after login for a regular user.

#### Page Role

1. What should be the main purpose of the User Home Page?
   - Guide the user to complete their profile
   - Provide quick access to resume generation
   - Show recent resumes
   - Show generation progress/status
   - Show onboarding tips
   - Other: [please specify]

#### Candidate Blocks

| Block | Importance |
|---|---|
| Profile completion progress | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Main action: Generate New Resume | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Main action: Edit Profile | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Recent saved resumes | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Recent generation requests | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Quick onboarding hints | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Account status or generation permission warning | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Basic usage statistics | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which actions should be available from the User Home Page?
   - Edit profile
   - Generate resume
   - Open latest resume
   - Download latest PDF
   - Copy public link
   - Open resume history
   - Open settings
   - Other: [please specify]

#### Empty States

3. What should the User Home Page show if the user has not created any profile data yet?

4. What should the User Home Page show if the user has no saved resumes yet?

#### Guidance

5. What should the system recommend as the first action after registration?

---

### 4.4 My Profile Page

#### Page Structure

1. How should profile editing be organized?
   - One long page
   - Sidebar with sections
   - Tabs by section
   - Accordion sections
   - Separate page for each section
   - Not sure

#### Candidate Profile Sections

Rate each profile section:

| Profile Section | Importance |
|---|---|
| Photo | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Contact details | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Positioning / target title | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Professional summary | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Skills | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Work experience | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Education | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Courses and certificates | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Projects | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Achievements | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Languages | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Professional aspirations | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Personal information | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Hobbies | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Values | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Candidate Blocks

| Block                               | Importance                                                     |
| ----------------------------------- | -------------------------------------------------------------- |
| Section navigation                  | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Profile completion progress         | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Required/optional section labels    | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Save button per section             | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Add/Edit/Delete controls per record | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Reorder items control               | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Example text / field hints          | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Warning about incomplete profile    | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which profile actions are required?
   - Add record
   - Edit record
   - Delete record
   - Save section
   - Upload photo
   - Reorder skills/experience/projects
   - Preview how profile data may appear in resume
   - Other: [please specify]

#### Errors and Empty States

3. Which validation errors are important?
   - Required field is empty
   - Invalid date range
   - Invalid link format
   - Invalid phone/email
   - Invalid photo file type
   - File too large
   - Duplicate language
   - Other: [please specify]

4. What should be shown when a profile section is empty?

#### Guidance

5. Which fields need examples or hints the most?

6. Should optional sections be hidden by default to reduce visual overload?

---

### 4.5 Generate Resume Page

#### Page Structure

1. Should resume generation be a single page or a multi-step wizard?
   - Single page
   - Multi-step wizard
   - Wizard for first-time users, single page for experienced users
   - Not sure

#### Candidate Blocks

| Block | Importance |
|---|---|
| Vacancy description textarea | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Company information textarea | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| AI model selector | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Adaptation level selector | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Resume language selector | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Generate all variants option | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Cover letter checkbox | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| ATS-friendly JSON option | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Profile completeness warning | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Short explanation of generation settings | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which actions are required?
   - Generate resume
   - Clear form
   - Return to profile
   - Preview selected profile data before generation
   - Select language
   - Select adaptation level
   - Select AI model
   - Other: [please specify]

#### Errors and Empty States

3. Which generation errors should be handled?
   - Empty vacancy description
   - Profile data is incomplete
   - User is not allowed to generate resumes
   - AI provider is unavailable
   - Generation timeout
   - Invalid selected model
   - Generated result is empty
   - Other: [please specify]

#### Guidance

4. What explanation is needed for adaptation levels?
   - Minimal
   - Balanced
   - Maximum
   - Generate all variants

5. What should the user understand before clicking Generate?

---

### 4.6 Resume Review Page

#### Page Structure

1. What is the best way to review generated results?
   - One editable document
   - Editable sections
   - Side-by-side comparison
   - Tabs for variants
   - Tabs for languages
   - Not sure

#### Candidate Blocks

| Block | Importance |
|---|---|
| Generated resume preview | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Editable resume sections | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Adaptation variant tabs | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Language tabs | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Vacancy context panel | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Save final version button | [Must Have / Should Have / context panel | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Save final version button | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Regenerate button | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Discard draft button | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Warning about factual review | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which actions are required?
   - Edit generated text
   - Select preferred variant
   - Save final version
   - Discard draft
   - Regenerate resume
   - Download PDF
   - Copy public link
   - Return to generation settings
   - Other: [please specify]

#### Errors and Empty States

3. What should happen if generation returns incomplete content?

4. What should happen if the user tries to leave the page with unsaved edits?

5. What should happen if saving the final version fails?

#### Guidance

6. Should the system warn users to verify factual accuracy before saving?

7. Should the system explain the difference between draft and final saved resume?

8. Should the system show which sections were adapted most strongly?

---

### 4.7 Resume History Page

#### Page Priority

1. How important is Resume History for MVP?
   - MVP
   - MVP Stretch
   - Post-MVP
   - Not Needed
   - Not Sure

#### Candidate Blocks

| Block | Importance |
|---|---|
| List/table of saved resumes | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Resume title | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Language | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Adaptation level | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Created date | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Vacancy/company reference | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Public link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| PDF download action | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Soft delete action | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Filters/search | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which actions should be available from Resume History?
   - Open resume details
   - Download PDF
   - Copy public link
   - Edit saved resume
   - Delete / soft delete resume
   - Restore deleted resume
   - Filter by language
   - Filter by adaptation level
   - Search by title/company
   - Other: [please specify]

#### Empty States

3. What should the page show if the user has no saved resumes?

4. What should the page show after a resume is deleted?

#### Guidance

5. Should the system explain the difference between deleting a resume and disabling public access?

---

### 4.8 Settings Page

#### Candidate Blocks

| Block | Importance |
|---|---|
| Interface language setting | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Default resume language setting | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Account information | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Public resume defaults | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Default AI model | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Password change | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Delete account option | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions and Guidance

1. Which settings are important for MVP?

2. Which settings require confirmation before saving?

3. Which settings can be safely postponed to later versions?

---

## 5. Questions for Recruiter / External Viewer

This section is for people who open a public resume link without registration.

---

### 5.1 Public Resume Page

#### Page Structure

1. What should a public resume link open?
   - Web resume page
   - PDF directly
   - Web page with embedded PDF
   - Web page with Download PDF button
   - Not sure

#### Candidate Blocks

| Block | Importance |
|---|---|
| Candidate name | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Target position/title | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Professional summary | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Contact details | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Skills | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Work experience | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Education | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Projects | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Courses/certificates | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Languages | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Download PDF button | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| ATS-friendly JSON link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Last updated date | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

2. Which actions should be available to external viewers?
   - View resume
   - Download PDF
   - Print resume
   - Copy candidate email
   - Open ATS-friendly JSON
   - Switch language version
   - Copy public link
   - Other: [please specify]

#### Errors and Empty States

3. What should be shown if the public resume link is invalid?

4. What should be shown if the resume was deleted or made private?

5. What should be shown if PDF download fails?

#### Guidance and Privacy

6. Should the public page explain that the PDF text is selectable?

7. Should the public page include a note about ATS-friendly JSON access?

8. What information should not be visible to recruiters by default?

---

### 5.2 PDF Resume Experience

#### Content and Layout

1. What matters most in a downloaded PDF resume?
   - One-page layout
   - Two-page layout is acceptable
   - Clear sections
   - Selectable text
   - Easy printing
   - ATS-friendly formatting
   - Contact details are easy to find
   - Other: [please specify]

#### Actions and Links

2. Should the PDF include a public link back to the online resume page?

3. Should the PDF include a link to the ATS-friendly JSON version?

#### Pain Points

4. What makes a resume PDF difficult to use from a recruiter perspective?

---

## 6. Questions for Administrator

This section is for users who manage accounts, review users/resumes, manage AI models, and monitor usage.

---

### 6.1 Admin Home Page

The Admin Home Page is the first page after login for an administrator. It acts as the main admin overview.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Total users count | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Active/inactive users count | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Users with generation forbidden | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Total generated resumes | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Token usage summary | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Recent generation failures | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Recent users | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Quick links to Users, Resumes, and AI Models | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available from Admin Home?
   - Open Users page
   - Open Resumes page
   - Open AI Models page
   - Search user
   - View generation statistics
   - View failed generations
   - Other: [please specify]

#### Empty States

2. What should Admin Home show if there are no users, resumes, or usage statistics yet?

---

### 6.2 Users Page

The Users page shows all registered users in a table.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Users table | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Username | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Email | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Role | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Account status | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Generation permission | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Created date | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Resume count | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Token usage summary | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Search/filter controls | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Pagination | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available from the Users table?
   - Open user details
   - Search users
   - Filter by role
   - Filter by status
   - Filter by generation permission
   - Other: [please specify]

#### Empty States and Errors

2. What should the page show if no users match the search/filter?

3. What should the page show if loading users fails?

---

### 6.3 User Details Page

The User Details page shows details for a selected user.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Basic user information | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Role editor: User/Admin | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Account status editor | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Generation permission editor | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| User resume count | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| User token usage statistics | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| User generation history | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Link to user's resumes | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which admin actions are required?
   - Change user role
   - Set user inactive
   - Reactivate user
   - Forbid AI generation
   - Allow AI generation
   - Open user's resumes
   - View user's token usage
   - Other: [please specify]

#### Confirmation and Errors

2. Which actions require confirmation?
   - Change role
   - Set user inactive
   - Reactivate user
   - Forbid generation
   - Allow generation
   - Other: [please specify]

3. What should be shown if an admin action fails?

---

### 6.4 Resumes Page

The Resumes page shows all generated/saved resumes in a table.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Resumes table | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Resume title | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Owner username/email | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Language | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Adaptation level | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| AI model used | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Token usage | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Created date | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Public/private status | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Search/filter controls | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Pagination | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available from the Resumes table?
   - Open resume details
   - Search resumes
   - Filter by user
   - Filter by language
   - Filter by adaptation level
   - Filter by model
   - Filter by public/private status
   - Other: [please specify]

#### Empty States and Errors

2. What should the page show if no resumes exist?

3. What should the page show if loading resumes fails?

---

### 6.5 Resume Details Page

The Resume Details page shows details for a selected resume.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Resume metadata | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Resume owner information | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Generated resume content preview | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Public link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| PDF download link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| ATS JSON link | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| AI model used | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Token usage details | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Generation status/error details | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available?
   - View resume content
   - Open public link
   - Download PDF
   - Open ATS JSON
   - Return to Resumes table
   - Other: [please specify]

#### Security and Privacy

2. Should admin access to resume content be limited or logged?

3. Which resume/user data should admin not see unless necessary?

---

### 6.6 AI Models Page

The AI Models page shows all AI models configured in the system.

#### Candidate Blocks

| Block | Importance |
|---|---|
| AI models table | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Display name | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Provider name | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Model code | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Provider base URL | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Active/inactive status | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Free/paid flag | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Max context tokens | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Usage count | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Search/filter controls | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available from the AI Models table?
   - Open model details
   - Search models
   - Filter by provider
   - Filter by active/inactive status
   - Filter by free/paid flag
   - Other: [please specify]

#### Empty States and Errors

2. What should be shown if no AI models are configured?

3. What should be shown if AI model loading fails?

---

### 6.7 AI Model Details Page

The AI Model Details page shows configuration details for a selected AI model.

#### Candidate Blocks

| Block | Importance |
|---|---|
| Display name | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Provider name | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Model code | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Provider base URL | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Masked API key | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Active/inactive status | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Free/paid flag | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Max context tokens | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Notes/description | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |
| Created/updated date | [Must Have / Should Have / Could Have / Not Needed / Not Sure] |

#### Actions

1. Which actions should be available?
   - View model details
   - Edit display name
   - Edit provider base URL
   - Replace API key
   - Activate/deactivate model
   - Return to AI Models table
   - Other: [please specify]

#### Security and Errors

2. Should the API key be visible in full?
   - No, always masked
   - Visible only during creation
   - Visible only to admin after confirmation
   - Not sure

3. What should happen if an invalid API key is saved?

4. What should happen if a model is inactive but selected in a generation request?

---

## 7. Questions for Developer / Technical Reviewer

This section validates implementation feasibility and helps avoid UI decisions that are too complex for MVP.

---

### 7.1 Overall UI Architecture

1. Which frontend approach is most realistic for MVP?
   - Spring MVC + Thymeleaf/JSP
   - Spring MVC backend + Vue frontend
   - Hybrid approach
   - Not sure

2. What is the expected implementation complexity of each approach?
   - Thymeleaf/JSP: [Low / Medium / High / Critical]
   - Vue frontend: [Low / Medium / High / Critical]
   - Hybrid: [Low / Medium / High / Critical]

3. Which approach gives the best balance between implementation effort and user experience?

4. Which approach creates the lowest deployment risk?

---

### 7.2 Page Complexity Review

Rate implementation complexity:

| Page | Complexity |
|---|---|
| Landing Page | [Low / Medium / High / Critical] |
| Login/Register | [Low / Medium / High / Critical] |
| User Home | [Low / Medium / High / Critical] |
| My Profile | [Low / Medium / High / Critical] |
| Generate Resume | [Low / Medium / High / Critical] |
| Resume Review | [Low / Medium / High / Critical] |
| Resume History | [Low / Medium / High / Critical] |
| Settings | [Low / Medium / High / Critical] |
| Public Resume Page | [Low / Medium / High / Critical] |
| Admin Home | [Low / Medium / High / Critical] |
| Users | [Low / Medium / High / Critical] |
| User Details | [Low / Medium / High / Critical] |
| Resumes | [Low / Medium / High / Critical] |
| Resume Details | [Low / Medium / High / Critical] |
| AI Models | [Low / Medium / High / Critical] |
| AI Model Details | [Low / Medium / High / Critical] |
| PDF Download | [Low / Medium / High / Critical] |
| ATS JSON Endpoint | [Low / Medium / High / Critical] |

#### Follow-up

1. Which pages should be simplified for MVP?

2. Which pages can be postponed without damaging the main value flow?

3. Which page is the highest implementation risk?

---

### 7.3 Data, Validation, and Access Review

1. Which pages require the most server-side validation?
2. Which pages require pagination?
3. Which pages require file upload validation?
4. Which pages require ownership checks?
5. Which pages require admin-only access?
6. Which pages require public access without login?
7. Which pages require special handling for sensitive data?
8. Which pages require audit or logging?

---

### 7.4 Error and Empty State Feasibility

1. Which error states must be implemented for MVP?
   - Login failure
   - Validation error
   - Empty profile
   - Empty resume history
   - AI generation failure
   - PDF generation failure
   - Public resume not found
   - Access denied
   - Database error
   - Other: [please specify]

2. Which errors can use a generic error page in MVP?

3. Which errors require page-specific messages?

---

### 7.5 Development Prioritization

1. Which vertical slice should be implemented first?
   - Authentication
   - Profile CRUD
   - Resume generation request
   - Mock AI generation
   - Draft review and saving
   - Public resume link
   - PDF download
   - Admin pages
   - AI model management

2. Which UI feature should be MVP Stretch instead of MVP?

3. Which UI feature should be moved to Future Scope?

4. What is the minimum UI needed to demonstrate the full product value chain?

---

## 8. Final Open Questions

These questions may be answered by any respondent.

1. What is the single most important screen for making this product useful?

2. Which screen or feature is most likely to confuse users?

3. Which feature sounds useful but should probably not be in MVP?

4. What should be visible immediately after login for a regular user?

5. What should be visible immediately after login for an admin?

6. What should be the main success moment for the user?

7. What would make the generated resume review process feel trustworthy?

8. What would make the public resume link useful for recruiters?

9. What should be simplified to make the MVP more realistic?

10. What is one thing that should definitely not be added to the MVP?

11. Any additional comments or suggestions?