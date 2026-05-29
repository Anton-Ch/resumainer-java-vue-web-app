# User Workflows

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-18
**Last Updated:** 2026-05-18
**Author:** Anton
**Version:** 1.0
**Status:** Approved
**Related BABOK Area:** 6.2 Define Future State / 7.1 Specify Requirements

***

## 1. Description

This document defines the key user workflows for the ResumAIner system. Each workflow describes a complete interaction sequence from trigger to outcome, covering the main success scenario and relevant extensions (alternate paths and error handling).

**Purpose:**
- For **developers**: a clear behavioral specification of what the system must support
- For **testing**: the basis for test case design

**Scope:** All workflows are within MVP scope unless explicitly marked otherwise.

## 2. Conventions

| Notation | Meaning |
|---|---|
| **Step N** | Main success scenario step |
| **N.a** | Alternate path (valid but different) |
| **N.e** | Error/exception path |
| Precondition | Must be true before the workflow can start |
| Postcondition | Guaranteed true after the workflow completes |

---

## 3. Workflow 1: First-Time User Generates a Resume

**Actors:** Registered User (primary)
**Trigger:** User registers and wants to produce their first adapted resume.
**MVP:** Yes

### Preconditions
- System is running and accessible.
- At least one active AI model is configured in the system.

### Main Success Scenario

| Step | Actor   | Action                                                            | System Response                                                  |
| ---- | ------- | ----------------------------------------------------------------- | ---------------------------------------------------------------- |
| 1    | Visitor | Opens Landing Page                                                | Displays product information, Register and Login buttons         |
| 2    | Visitor | Clicks Register                                                   | Opens registration form (email, password, password confirmation) |
| 3    | Visitor | Submits registration                                              | Validates input, creates account, redirects to User Home         |
| 4    | User    | Clicks "Edit my profile"                                          | Opens Profile page with section navigation                       |
| 5    | User    | Fills Contact Details (name, email, phone, location)              | Saves and displays saved data                                    |
| 6    | User    | Adds Work Experience (job title, company, dates, description)     | Adds record card to the list                                     |
| 7    | User    | Adds Education, Skills, Languages (as needed)                     | Each section saved independently                                 |
| 8    | User    | Clicks "Generate new resume"                                      | Opens Generate Resume page                                       |
| 9    | User    | Pastes vacancy description, enters company info                   | Fields populated in the form                                     |
| 10   | User    | Selects AI model, adaptation level (Balanced), language (English) | Form complete                                                    |
| 11   | User    | Clicks "Generate Resume"                                          | Validates fields, creates generation request, sends prompt to AI |
| 12   | System  | —                                                                 | Displays loading state during generation                         |
| 13   | System  | —                                                                 | Returns generated draft, opens Resume Review page                |
| 14   | User    | Reviews generated content, edits several sections                 | Changes visible in the preview                                   |
| 15   | User    | Clicks "Save & Create"                                            | Saves final version, generates PDF, creates public link          |
| 16   | User    | Downloads PDF, copies public link                                 | PDF downloaded, link copied to clipboard                         |

### Postconditions
- User has a complete structured editable and reusable profile info.
- First resume version is saved with a permanent public URL and downloadable PDF.
- Resume appears in the User Home listing table.

### Extensions

| Step     | Condition                                                      | Action                                                                                                                                               |
| -------- | -------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| **3.e**  | Email already registered                                       | System shows "Email already in use" error. User can switch to Login.                                                                                 |
| **3.e**  | Password too weak                                              | System shows validation error. User corrects and resubmits.                                                                                          |
| **5.a**  | User skips optional fields                                     | System saves required fields only, without optional fields. Non-required fields silently skipped.                                                    |
| **9.a**  | User pastes only vacancy description without company info      | System proceeds with vacancy data only — company info is optional.                                                                                   |
| **10.a** | User selects "Generate all three variants" instead of Balanced | System generates 3 versions (minimal, balanced, maximum). User selects preferred one on Resume Review.                                               |
| **10.b** | User selects "English and Russian"                             | System generates version in each language with separate save and public link.                                                                        |
| **11.e** | Profile incomplete for meaningful adaptation                   | System displays warning: "Your profile has fewer than 3 sections filled. AI adaptation quality may be reduced," but allows generation to proceed.    |
| **13.e** | AI API timeout or unavailable                                  | System shows error: "AI generation failed due to temporary service issue. Please try again." User can retry or cancel.                               |
| **13.e** | AI returns empty or malformed output                           | System shows error: "AI returned incomplete content. Please try a different AI model or adaptation level." Generation request is not charged/quoted. |

---

## 4. Workflow 2: Returning User Generates a Resume with Custom Settings

**Actors:** Registered User (primary)
**Trigger:** Returning user with existing profile applies to a new vacancy.
**MVP:** Yes

### Preconditions
- User is registered and logged in.
- User profile contains at least contact details, work experience, education, courses.

### Main Success Scenario

| Step | Actor | Action                                                                                                  | System Response                                                      |
| ---- | ----- | ------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| 1    | User  | Logs in                                                                                                 | Authenticates, redirects to User Home                                |
| 2    | User  | Reviews saved resume list on User Home                                                                  | Table shows existing resumes with search/sort/filter controls        |
| 3    | User  | Clicks "Generate new resume"                                                                            | Opens Generate Resume page                                           |
| 4    | User  | Pastes vacancy description                                                                              | Field populated                                                      |
| 5    | User  | Selects AI model (different from last used), adaptation level (Maximum), language (English and Russian) | All selections visible in the form                                   |
| 6    | User  | Enters optional comment for AI context                                                                  | Comment field populated                                              |
| 7    | User  | Clicks "Generate Resume"                                                                                | System validates, queues generation, returns both language versions  |
| 8    | User  | Compares English and Russian versions side by side                                                      | Both generated versions fields displayed on Resume Review page       |
| 9    | User  | Edits English version's professional summary                                                            | English draft updated                                                |
| 10   | User  | Reviews Russian version, accepts as-is                                                                  | Russian version ready                                                |
| 11   | User  | Clicks single "Save & Create" for both versions                                                         | English and Russian versions saved with its own public URLs and PDFs |


### Postconditions
- Two resume versions saved (English and Russian) with separate public links.
- Each version is independently editable and downloadable.
- Both versions appear in the User Home resume listing table.

### Extensions

| Step     | Condition                                 | Action                                                                                                                                                                                                                  |
| -------- | ----------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **5.a**  | Selected AI model is deactivated by admin | System filters out inactive or hidden models from the dropdown. If the last-used model becomes inactive, the system selects the first available active model by default. If user is priveleged - can see hidden models. |
| **7.e**  | User's generation permission is FORBIDDEN | System shows error: "Your account is not allowed to generate resumes. Contact support for more information." Generation form is disabled.                                                                               |
| **10.a** | User wants to discard and regenerate      | System shows confirmation: "Discard current draft and start over?" On confirm, returns to Generate Resume with last inputs preserved.                                                                                   |

---

## 5. Workflow 3: User Manages Resumes

**Actors:** Registered User (primary)
**Trigger:** User wants to review, share, or clean up their saved resumes.
**MVP:** Yes

### Preconditions
- User is logged in.
- At least one saved resume exists.

### Main Success Scenario

| Step | Actor | Action | System Response |
|------|-------|--------|----------------|
| 1 | User | Views resume listing table on User Home | Displays table with columns: vacancy, title, language, adaptation level, created date. Filters visible. |
| 2 | User | Searches by vacancy title | Table filters in real-time as user types |
| 3 | User | Filters by language (English) | Table displays only English resume entries |
| 4 | User | Clicks "Open details" on a selected resume | Opens modal popup with public link (copyable), download PDF button, cover letter text |
| 5 | User | Copies public resume link | Link copied to clipboard |
| 6 | User | Downloads PDF | PDF file download starts |
| 7 | User | Closes modal | Returns to User Home with table unchanged |

### Extensions

| Step    | Condition                     | Action                                                                                                                                                                                                                                                                                                                                        |
| ------- | ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **1.a** | No resumes exist yet          | System shows empty state guidance: "You haven't created any resumes yet. Click 'Generate new resume' to get started."                                                                                                                                                                                                                         |
| **2.a** | Search returns no results     | System shows: "No resumes match your search. Try adjusting your filters."                                                                                                                                                                                                                                                                     |
| **7.a** | User wants to delete a resume | No delete action in the table (not in table to prevent accidental deletion). User must go to Open Details modal and press "Delete this resume" button which will make soft-delete after a separate in-place confirmation question to user and his/her confirmation button pressed in Open Details modal window (not a separate model window). |

---

## 6. Workflow 4: Soft Delete a Resume

**Actors:** Registered User (primary)
**Trigger:** User wants to remove an outdated or incorrect resume from their listing.
**MVP:** Yes

### Preconditions
- User is logged in.
- At least one saved resume exists.

### Main Success Scenario

| Step | Actor | Action                                           | System Response                                                                                                                                                                        |
| ---- | ----- | ------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | User  | Opens Resume Details modal for the target resume | Modal displays resume info                                                                                                                                                             |
| 2    | User  | Clicks "Delete" (or equivalent action)           | System shows confirmation dialog instead of pressed deletion button: "Are you sure you want to delete this resume? Public link will stop working."                                     |
| 3    | User  | Confirms deletion                                | System performs soft delete: sets `deleted_at` timestamp and `is_deleted` to true. Resume no longer appears in the listing. Public link returns custom page with 410 http status code. |

### Postconditions
- Resume is soft-deleted (not removed from database).
- Resume is hidden from the user's listing.
- Public link returns a "Resume not found" message on page with 410 http status code (not deleted username/code disclosure).

### Extensions

| Step | Condition | Action |
|------|-----------|--------|
| **2.e** | User clicks Cancel on confirmation | Modal closes. No changes. Resume remains active. |

---

## 7. Workflow 5: Recruiter Views Public Resume

**Actors:** Recruiter / External Viewer (secondary)
**Trigger:** Recruiter opens a shared resume link received from a candidate.
**MVP:** Yes

### Preconditions
- A saved resume exists and is not soft-deleted.
- The public URL is valid: `/{username}/{resumeCode}`.

### Main Success Scenario

| Step | Actor     | Action                              | System Response                                                             |
| ---- | --------- | ----------------------------------- | --------------------------------------------------------------------------- |
| 1    | Recruiter | Opens public URL in browser         | System validates username and resume code                                   |
| 2    | System    | —                                   | Opens saved resume PDF directly in the browser                              |
| 3    | Recruiter | Reads the resume                    | PDF displays with selectable text, proper formatting, print-friendly layout |
| 4    | Recruiter | Selects and copies text from resume | Text selection works (not a scanned image)                                  |
| 5    | Recruiter | Saves/downloads the PDF             | File download starts                                                        |
| 6    | Recruiter | Prints the resume                   | PDF prints with correct A4 layout by outside the system means               |

### Postconditions
- Recruiter viewed the resume without registration or authentication.
- No private data (drafts, token usage, admin data) was exposed.
- PDF maintained formatting, selectable text, and print compatibility.

### Extensions

| Step    | Condition                                                         | Action                                                                                                             |
| ------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| **1.a** | Recruiter opens ATS JSON endpoint `/{username}/{resumeCode}/json` | System returns structured JSON resume data for machine parsing by ATS systems. Stretch MVP or POST-MVP             |
| **1.e** | Invalid username or resume code                                   | System shows: "Resume not found." No disclosure whether username is invalid, code is wrong, or resume was deleted. |
| **1.e** | Resume was soft-deleted                                           | System shows: "Resume was deleted by owner".                                                                       |
| **2.a** | Recruiter opens link on mobile                                    | PDF viewer should handle mobile display gracefully (scrollable, zoomable).                                         |

---

## 8. Workflow 6: Admin Monitors and Manages Users

**Actors:** Administrator (primary)
**Trigger:** Admin wants to review system usage and manage users.
**MVP:** Yes

### Preconditions
- Admin is logged in with ADMIN role.

### Main Success Scenario

| Step | Actor | Action                                       | System Response                                                                                        |
| ---- | ----- | -------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| 1    | Admin | Logs in                                      | System detects ADMIN role, redirects to Admin Home (not User Home)                                     |
| 2    | Admin | Views Admin Home dashboard                   | Shows statistics: total users, active/inactive counts, total resumes, token usage summary              |
| 3    | Admin | Clicks Users card                            | Opens Users page with searchable/sortable/paginated user table                                         |
| 4    | Admin | Searches for a specific user                 | Table filters by username or email                                                                     |
| 5    | Admin | Opens user details                           | Displays user profile, resume count, token usage breakdown (tokens in, out, total), generation history |
| 6    | Admin | Reviews unusual token usage                  | Data shows if user is generating excessive resumes                                                     |
| 7    | Admin | Sets user generation permission to FORBIDDEN | System shows confirmation dialog                                                                       |
| 8    | Admin | Confirms                                     | User status updated. User cannot generate new resumes. Existing resumes remain accessible.             |

### Postconditions
- Admin took action based on usage data.
- User is restricted from AI generation but retains access to existing data.

### Extensions

| Step | Condition | Action |
|------|-----------|--------|
| **7.a** | Admin sets user status to INACTIVE | User cannot log in. Attempted login shows: "Your account is inactive. Contact support for assistance." |
| **7.b** | Admin removes INACTIVE status | User can log in again, retains all profile data and saved resumes. |

---

## 9. Workflow 7: Admin Manages AI Models

**Actors:** Administrator (primary)
**Trigger:** Admin needs to configure or update available AI models for resume generation.
**MVP:** Yes

### Preconditions
- Admin is logged in.

### Main Success Scenario

| Step | Actor | Action                                                                   | System Response                                                                                                    |
| ---- | ----- | ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| 1    | Admin | Opens AI Models from Admin Home                                          | Shows table of configured AI models: display name, provider, model code, active status, free/paid flag, max tokens |
| 2    | Admin | Clicks "Add AI Model"                                                    | Opens Add AI Model form                                                                                            |
| 3    | Admin | Enters display name, provider, model code, base URL, API key, max tokens | Form fields populated and validated                                                                                |
| 4    | Admin | Sets model as active and free                                            | Model ready for user selection                                                                                     |
| 5    | Admin | Submits form                                                             | System validates, saves model configuration. API key stored masked in display, encrypted in database.              |
| 6    | Admin | Verifies model appears in AI Models table                                | New row visible with correct data                                                                                  |
| 7    | Admin | Opens newly created model details                                        | Details show all fields. API key displayed as masked: `sk-op-v1••••••••`                                           |

### Postconditions
- New AI model is configured and immediately available for user selection in Generate Resume.
- API key is stored masked; never logged or exposed in plain text after save.

### Extensions

| Step | Condition | Action |
|------|-----------|--------|
| **6.e** | API key is invalid and model fails during first user generation | System logs the failure. Admin can view usage logs, test the model, and update the API key. |
| **7.a** | Admin deactivates an existing model | Model is no longer shown in user dropdown. Users with saved resumes using this model still have access to their saved data. |

---

## 10. Workflow Summary

| # | Workflow | Actors | MVP |
|---|----------|--------|-----|
| 1 | First-time user generates a resume | User, Visitor | Yes |
| 2 | Returning user generates a resume with custom settings | User | Yes |
| 3 | User manages saved resumes | User | Yes |
| 4 | Soft delete a resume | User | Yes |
| 5 | Recruiter views public resume | Recruiter | Yes |
| 6 | Admin monitors and manages users | Admin | Yes |
| 7 | Admin manages AI models | Admin | Yes |

---

## 11. Related Artifacts

| Artifact | Location |
|----------|----------|
| Requirements Log | `docs/02_requirements/requirements_log.md` |
| Strategic Context and Gap Analysis | `docs/01_project-overview/strategic_context_and_gap_analysis.md` |
| Business Goals and KPIs | `docs/01_project-overview/business_goals_and_kpis.md` |
| Wireframe Descriptions | `docs/05_ui-ux/wireframes_detailed_description.md` |
| Wireframe Field Requirements | `docs/05_ui-ux/wireframe_field_requirements.md` |
| Data Dictionary | `docs/04_domain-and-data-model/data_dictionary.md` |

***

*This document is maintained as part of the ResumAIner business analysis portfolio. Workflows should be updated when new features are added or when existing behavior changes through the Change Request process.*