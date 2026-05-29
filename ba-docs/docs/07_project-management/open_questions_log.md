# Open Questions Log
 
**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-10  
**Last Updated:** 2026-05-21  
**Author:** Anton  
**Version:** 5.0  
**Status:** Active  
**Related BABOK Area:** 3.2 Plan Stakeholder Engagement / 3.3 Plan Business Analysis Governance  
 
---
 
## 1. Description
 
This document tracks unresolved **questions** that may affect scope, requirements, architecture, data model, UI/UX, deployment, security, or project delivery.
 
The goal is to prevent hidden assumptions and make uncertainty visible until a decision is made.
 
## 2. Usage Rules and Controlled Values
 
### 2.1 Usage Rules
 
- Use this log for questions that may affect project decisions.
- Do not use this log for minor personal reminders.
- Each question must have a unique ID: `OQ-001`, `OQ-002`, `OQ-003`.
- If a question results in a decision, link it to a Decision Log entry.
- If a question results in a change, link it to a Change Request.
- Close questions only after an answer is documented.
 
### 2.2 Question Category Values
 
| Value               | Meaning                                                                 | When to Use                              |
| ------------------- | ----------------------------------------------------------------------- | ---------------------------------------- |
| Scope               | Question affects MVP, stretch goals, or future scope                    | Is feature X in MVP?                     |
| Requirement         | Question affects FR/NFR or acceptance criteria                          | What should happen in edge case?         |
| Architecture        | Question affects system structure or technology                         | Is Vue allowed?                          |
| Data Model          | Question affects entities, fields, relationships, or storage            | Should resume be stored as JSON?         |
| UI/UX               | Question affects screens, workflows, or user interaction                | Should public link open PDF or web page? |
| Deployment          | Question affects server, Docker, domain, or environment                 | How to deploy on VPS?                    |
| Security            | Question affects authentication, authorization, secrets, or public data | Can public link expose contact info?     |
| Process             | Question affects documentation, governance, traceability, or workflow   | How to review requirements?              |
| Capstone Constraint | Question affects mandatory capstone rules                               | Is external API allowed?                 |
 
### 2.3 Impact Values
 
| Value | Meaning                                                              |
|---|---|
| Low | Answer has minor documentation impact                                |
| Medium | Answer affects one requirement, screen, or artifact                  |
| High | Answer affects MVP, architecture, data model, or implementation plan |
| Critical | Answer may block implementation or capstone compliance               |
 
### 2.4 Question Status Values
 
| Value | Meaning |
|---|---|
| Open | Question is unresolved |
| In Review | Question is being clarified |
| Answered | Answer is known and recorded |
| Converted | Question became a decision or change request |
| Closed | No further action is required |
 
## 3. Summary Table
 
| OQ ID | Date | Question | Category | Owner | Impact | Target Resolution | Status | Answer / Decision Link |
|---|---|---|---|---|---|---|---|---|
| OQ-001 | 2026-05-10 | Is Vue allowed for the final Capstone implementation? | Architecture | BA / Mentor | High | 2026-05-21 | Closed | DEC-052 |
| OQ-002 | 2026-05-13 | Should Resume History be a separate page? | Scope | BA | Medium | Resolved during wireframe review | Closed | DEC-005, CR-007 |
| OQ-003 | 2026-05-13 | Should saved API keys ever be visible in full? | Security | BA | High | Resolved during security review | Closed | DEC-008, CR-011 |
| OQ-004 | 2026-05-13 | Should Work Experience description be required? | Requirement | BA | Medium | Resolved during wireframe review | Closed | CR-012 |
| OQ-005 | 2026-05-13 | Should Education start year be required? | Requirement | BA | Medium | Resolved during wireframe review | Closed | CR-012 |
| OQ-006 | 2026-05-13 | Should profile picture be required? | Requirement | BA | Low | Resolved during wireframe review | Closed | CR-012 |
| OQ-007 | 2026-05-13 | Is real OpenRouter API usage allowed during final demo? | Capstone Constraint | BA / Mentor | High | 2026-05-21 | Closed | DEC-009, DEC-059 |
| OQ-008 | 2026-05-13 | Should ATS JSON endpoint remain MVP Stretch or move to MVP? | Scope | BA | Medium | 2026-05-21 | Closed | Confirmed as MVP Stretch |
| OQ-009 | 2026-05-13 | How much admin access logging is required for MVP? | Security | BA / Developer | Medium | 2026-05-21 | Closed | DEC-059 |
| OQ-010 | 2026-05-16 | What information does LLM return as structured JSON vs taken directly from profile? | Architecture | BA / Developer | Medium | Before AI prompt design | Open | N/A |
| OQ-999 | YYYY-MM-DD | [Question text] | [Category] | [Owner] | [Low/Medium/High/Critical] | YYYY-MM-DD | Open | [DEC/CR link or N/A] |
 
## 4. Details
 
### OQ-001 Is Vue Allowed for the Final Capstone Implementation?
 
**Date:** 2026-05-10
**Category:** Architecture
**Owner:** BA / Mentor
**Status:** Open
**Question:** Is Vue allowed for the final Java Capstone implementation, or should the project use Thymeleaf/JSP for all UI pages?
**Why It Matters:** This affects frontend architecture, routing, deployment, Docker Compose structure, and the amount of work required for integration.
**Options Considered:**
    -   Option A: Spring MVC backend + Vue frontend.
    -   Option B: Spring MVC + Thymeleaf/JSP for all pages.
    -   Option C: Hybrid approach with Thymeleaf landing page and Vue app for authenticated UI.
**Answer / Decision:** Vue 3 + Vite + PrimeVue for authenticated SPA; Thymeleaf for Landing Page (DEC-052). Hybrid approach confirmed viable.
**Related Artifacts:**
    -   `decision_log.md` — DEC-052
    -   `strategic_context_and_gap_analysis.md`
    -   `confirmed_elicitation_results.md`
**Follow-up Actions:** Implement SPA with Vue 3 Composition API. Use PrimeVue components for responsive design and cross-browser compatibility.

### OQ-002 Should Resume History Be a Separate Page?

**Date:** 2026-05-13
**Category:** Scope
**Owner:** BA
**Status:** Closed
**Question:** Should Resume History be a separate page, or should resume listing be integrated into User Home?
**Why It Matters:** This affects navigation structure, User Home scope, sitemap, wireframes, and implementation effort.
**Options Considered:**
    -   Option A: Keep Resume History as a separate page.
    -   Option B: Integrate resume listing into User Home.
**Answer / Decision:** Resume listing is integrated into User Home. A separate Resume History page is not included in MVP.
**Related Artifacts:**
    -   `decision_log.md` — DEC-005
    -   `change_request_log.md` — CR-007
    -   `confirmed_elicitation_results.md`
    -   `wireframe_field_requirements.md`
**Follow-up Actions:** Keep all future navigation, sitemap, UI/UX, and requirements documents aligned with User Home as the resume listing location.

### OQ-003 Should Saved API Keys Ever Be Visible in Full?

**Date:** 2026-05-13
**Category:** Security
**Owner:** BA
**Status:** Closed
**Question:** Should saved API keys ever be visible in full in AI Model Details?
**Why It Matters:** This affects admin security, secret handling, logging rules, and trustworthiness of the project architecture.
**Options Considered:**
    -   Option A: Always mask saved API keys and allow only replace/delete actions.
    -   Option B: Allow full API key reveal after admin confirmation.
    -   Option C: Always show full API keys in admin UI.
**Answer / Decision:** Saved API keys must always be masked, must never be logged, and can only be replaced or deleted.
**Related Artifacts:**
    *   `decision_log.md` — DEC-008
    -   `change_request_log.md` — CR-011
    -   `risk_register.md` — RISK-007
    -   `wireframe_field_requirements.md`
**Follow-up Actions:** Ensure AI Model Details, security requirements, and implementation notes consistently use masked API key behavior.

### OQ-004 Should Work Experience Description Be Required?

**Date:** 2026-05-13
**Category:** Requirement
**Owner:** BA
**Status:** Closed
**Question:** Should Work Experience description be required or optional?
**Why It Matters:** Work experience descriptions provide critical context for resume generation, especially for role-specific adaptation.
**Options Considered:**
    -   Option A: Require Work Experience description.
    -   Option B: Keep Work Experience description optional.
**Answer / Decision:** Work Experience description is required.
**Related Artifacts:**
    -   `change_request_log.md` — CR-012
    -   `wireframe_field_requirements.md`
    -   `requirement_readiness_checklist.md`
**Follow-up Actions:** Update validation rules, error messages, and field-level requirements for Work Experience.

### OQ-005 Should Education Start Year Be Required?

**Date:** 2026-05-13
**Category:** Requirement
**Owner:** BA
**Status:** Closed
**Question:** Should Education start year be required or optional?
**Why It Matters:** Start year supports consistent education record structure, sorting, validation, and resume timeline clarity.
**Options Considered:**
    -   Option A: Require start year for Education records.
    -   Option B: Keep start year optional.
**Answer / Decision:** Education start year is required.
**Related Artifacts:**
    -   `change_request_log.md` — CR-012
    -   `wireframe_field_requirements.md`
    -   `requirement_readiness_checklist.md`
**Follow-up Actions:** Update Education validation rules and field-level requirements.

### OQ-006 Should Profile Picture Be Required?

**Date:** 2026-05-13
**Category:** Requirement
**Owner:** BA
**Status:** Closed
**Question:** Should profile picture be required or optional?
**Why It Matters:** Requiring a photo may increase friction and may not be necessary for all resume formats or regions.
**Options Considered:**
    -   Option A: Make profile picture required.
    -   Option B: Make profile picture optional.
    -   Option C: Move profile picture to MVP Stretch.
**Answer / Decision:** Profile picture is optional.
**Related Artifacts:**
    -   `change_request_log.md` — CR-012
    -   `wireframe_field_requirements.md`
    -   `confirmed_elicitation_results.md`
**Follow-up Actions:** Update My Profile and PDF/resume generation rules so photo is optional.

### OQ-007 Is Real OpenRouter API Usage Allowed During Final Demo?

**Date:** 2026-05-13
**Category:** Capstone Constraint
**Owner:** BA / Mentor
**Status:** Closed
**Question:** Is real OpenRouter API usage allowed during the final demo?
**Why It Matters:** If real external API calls are not allowed or unreliable, mock AI generation must be used for demo stability.
**Options Considered:**
    -   Option A: Use real OpenRouter integration during demo.
    -   Option B: Use mock-only demo.
    -   Option C: Support both real and mock modes.
**Answer / Decision:** Mock AI for early dev tests and stable pipeline; real OpenRouter integration for MVP demo. Both implementations coexist behind the same interface (AiClientFactory — DEC-056).
**Related Artifacts:**
    -   `decision_log.md` — DEC-009, DEC-059
    -   `requirements_log.md` — FR-001
**Follow-up Actions:** Closed. Decision confirmed.

### OQ-008 Should ATS JSON Endpoint Remain MVP Stretch or Move to MVP?

**Date:** 2026-05-13
**Category:** Scope
**Owner:** BA
**Status:** Closed
**Question:** Should the ATS JSON endpoint remain MVP Stretch or move into MVP?
**Why It Matters:** ATS JSON is portfolio-friendly but adds an additional public output format, validation rules, and traceability requirements.
**Options Considered:**
    -   Option A: Include ATS JSON in MVP.
    -   Option B: Keep ATS JSON as MVP Stretch.
    -   Option C: Move ATS JSON to Post-MVP.
**Answer / Decision:** Keep ATS JSON as MVP Stretch. Not moved to MVP — no dedicated FR/NFR created. Documented in Target Vision as MVP Stretch and listed in Confirmed Elicitation Results section 4.2.
**Related Artifacts:**
    -   `confirmed_elicitation_results.md`
    -   `strategic_context_and_gap_analysis.md`
**Follow-up Actions:** Close. No further action — confirmed decision.

### OQ-009 How Much Admin Access Logging Is Required for MVP?

**Date:** 2026-05-13
**Category:** Security
**Owner:** BA / Developer
**Status:** Closed
**Question:** How much admin access logging is required for MVP?
**Why It Matters:** Admin can view user and resume data. Access logging improves accountability but adds implementation effort.
**Options Considered:**
    *   Option A: No admin access logging in MVP.
    *   Option B: Minimal — log only critical admin actions.
    *   Option C: Full admin audit log.
**Answer / Decision:** Minimal admin action logging (Option B). Log only critical admin actions: change user role, block/unblock user, forbid/allow generation. Non-critical operations (viewing, searching) are not logged. Standard request logging via Interceptors (NFR-021) covers basic access tracking.
**Related Artifacts:**
    *   `decision_log.md` — DEC-059
    *   `requirements_log.md` — NFR-021
**Follow-up Actions:** Closed. Decision confirmed.

### OQ-010 LLM Output Format — JSON vs Direct Profile Data

**Date:** 2026-05-16
**Category:** Architecture
**Owner:** BA / Developer
**Status:** Closed
**Question:** What information does the LLM return as structured JSON (generated fields) vs what is taken directly from user profile data and inserted into the resume without AI processing?
**Why It Matters:** This affects prompt design, generation request format, resume review screen behavior, and how much of the profile data is editable in the generated output vs hardcoded.
**Options Considered:**
- Option A: LLM returns only generated fields (summary, adaptation of experience, cover letter); profile data (name, contact, education, courses) is inserted directly from user profile.
- Option B: LLM returns all resume content including profile data; user reviews everything.
- Option C: Hybrid — LLM returns generated content and a transformed version of profile sections with adaptation applied.
**Answer / Decision:** AI returns structured JSON matching the generation response contract. Backend parses JSON and populates `resume_generation_response` and `generation_response_*` tables directly (DEC-036). Profile data (name, contact, education, courses) is taken from user profile; AI generates adapted content (summary, experience descriptions, skills, aspirations) in JSON format.
**Related Artifacts:**
- `decision_log.md` (DEC-016, DEC-017, DEC-036)
- `requirements_log.md` (FR-001, FR-011)
**Follow-up Actions:** Monitor complexity; revisit for structured JSON contract after MVP baseline.
**Status:** Closed

### OQ-999 [Question Short Title Template]

**Date:** YYYY-MM-DD
**Category:** [Scope / Requirement / Architecture / Data Model / UI/UX / Deployment / Security / Process / Capstone Constraint]
**Owner:** [Owner]
**Status:** Open
**Question:** [Full question]
**Why It Matters:** [Impact if unresolved]
**Options Considered:** [Option A / Option B / Option C]
**Answer / Decision:** [Answer or N/A]
**Related Artifacts:** [Files, requirements, decisions]
**Follow-up Actions:** [What should happen next]

***

*This open questions log follows the Information Management Plan structure and conventions for the ResumAIner project.*