# Risk Register

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-10
**Last Updated:** 2026-05-23
**Author:** Anton
**Version:** 8.0
**Status:** Active
**Related BABOK Area:** 3.1 Plan Business Analysis Approach / 3.3 Plan Business Analysis Governance

---
## 1. Description

This document identifies, assesses, and tracks project **risks** that may affect scope, requirements, architecture, implementation, testing, deployment, or final presentation.

The purpose is to make risks visible early and define practical mitigation strategies.

## 2. Usage Rules and Controlled Values

### 2.1 Usage Rules

- Add risks that may meaningfully affect project success.
- Do not add every minor inconvenience.
- Keep mitigation actions practical.
- Update risk status as the project evolves.
- If a risk becomes an issue, create a change request or decision if needed.

### 2.2 Risk Category Values

| Value       | Meaning                                                     |
| ----------- | ----------------------------------------------------------- |
| Scope       | Risk related to uncontrolled feature growth or unclear MVP  |
| Technical   | Risk related to implementation, framework, or architecture  |
| Data        | Risk related to database model, migration, or data quality  |
| Security    | Risk related to auth, permissions, secrets, or public data  |
| UX          | Risk related to usability, navigation, or user confusion    |
| Integration | Risk related to external APIs or third-party services       |
| Deployment  | Risk related to Docker, VPS, domain, or runtime environment |
| Schedule    | Risk related to time, workload, or project deadlines        |
| Quality     | Risk related to testing, maintainability, or defects        |
| Compliance  | Risk related to mandatory capstone requirements             |

### 2.3 Probability Values

| Value | Meaning |
|---|---|
| Low | Unlikely to happen |
| Medium | Possible |
| High | Likely |

### 2.4 Impact Values

| Value | Meaning                                             |
| -------- | --------------------------------------------------- |
| Low | Minor inconvenience                                 |
| Medium | Noticeable rework or delay                          |
| High | Major rework, demo risk, or quality issue           |
| Critical | May block project completion or capstone compliance |

### 2.5 Severity Values

| Value | Meaning |
|---|---|
| Low | Low priority risk |
| Medium | Should be monitored |
| High | Requires mitigation |
| Critical | Requires immediate attention |

### 2.6 Response Strategy Values

| Value | Meaning |
|---|---|
| Avoid | Change plan to remove the risk |
| Mitigate | Reduce probability or impact |
| Transfer | Move responsibility or dependency elsewhere |
| Accept | Acknowledge and proceed without active mitigation |
| Monitor | Watch the risk and act if it grows |

### 2.7 Risk Status Values

| Value | Meaning |
|---|---|
| Open | Risk is active |
| Monitoring | Risk is being watched |
| Mitigated | Mitigation was applied |
| Accepted | Risk is accepted |
| Closed | Risk is no longer relevant |

## 3. Summary Table

| Risk ID | Date | Risk | Category | Probability | Impact | Severity | Response Strategy | Owner | Status |
|---|---|---|---|---|---|---|---|---|---|
| RISK-001 | 2026-05-10 | MVP may become too large due to AI, PDF, public links, admin pages, and future monetization ideas | Scope | High | High | Critical | Mitigate | BA | Open |
| RISK-002 | 2026-05-11 | UI/UX questionnaire may become too broad and difficult to analyze | UX | Medium | Medium | Medium | Mitigate | BA | Mitigated |
| RISK-003 | 2026-05-11 | UI feedback may focus on visual preferences instead of functional decisions | UX | Medium | High | High | Mitigate | BA | Mitigated |
| RISK-004 | 2026-05-11 | UI ideas may be accepted into MVP without feasibility check | Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-005 | 2026-05-13 | UI scope may become too large due to many user/admin pages | Scope / UX | Medium | Medium | Medium | Mitigate | BA | Open |
| RISK-006 | 2026-05-13 | PDF generation may become technically difficult | Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-007 | 2026-05-13 | API key handling in AI Model Details may expose secrets | Security | Medium | High | High | Mitigate | BA / Developer | Closed |
| RISK-008 | 2026-05-13 | Vue + Spring MVC integration may increase implementation workload | Technical / Schedule | Medium | Medium | Medium | Monitor | BA / Developer | Open |
| RISK-009 | 2026-05-13 | Resume Review page may become complex due to editing, variants, and languages | UX / Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-010 | 2026-05-13 | Wireframe field inconsistencies may create requirement ambiguity | Quality | Medium | Medium | Medium | Mitigate | BA | Mitigated |
| RISK-011 | 2026-05-18 | Accidental resume deletion may cause user frustration and data loss | UX / Data | Low | High | Medium | Mitigate | BA | Open |
| RISK-012 | 2026-05-20 | AI-generated HTML may break template layout or introduce XSS | Security / Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-013 | 2026-05-21 | Inconsistent error handling may expose stack traces or miss critical failures | Quality / Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-014 | 2026-05-21 | Custom Connection Pool may have thread-safety bugs or performance issues | Technical | Medium | High | High | Mitigate | BA / Developer | Open |
| RISK-015 | 2026-05-23 | Misconfigured budget settings may cause bad PDF layout or generation errors | Data | Low | High | Medium | Monitor | BA / Developer | Monitoring |
| RISK-999 | YYYY-MM-DD | [Risk description] | [Category] | [Low/Medium/High] | [Low/Medium/High/Critical] | [Low/Medium/High/Critical] | [Avoid/Mitigate/Transfer/Accept/Monitor] | [Owner] | Open |

## 4. Details
 
### RISK-001 MVP Scope Creep
 
**Date Identified:** 2026-05-10  
**Category:** Scope  
**Probability:** High  
**Impact:** High  
**Severity:** Critical  
**Response Strategy:** Mitigate  
**Owner:** BA  
**Status:** Open  
**Risk Description:** MVP may become too large because the project includes AI generation, PDF export, public links, admin pages, token usage, AI model management, and future monetization ideas.  
**Cause:** The product idea has strong expansion potential and combines several technically different areas.  
**Impact if Occurs:** Development may become delayed, core functionality may remain unfinished, and the final demo may become weaker.  
**Mitigation Plan:** Freeze MVP around the core flow and move advanced features to MVP Stretch or Future Scope.  
**Trigger / Early Warning:** New MVP features are added without removing or postponing other features.  
**Contingency Plan:** Implement core vertical slice first: profile → generate → review → save → PDF → public link.
 
### RISK-002 UI/UX Questionnaire Scope Becomes Too Broad
 
**Date Identified:** 2026-05-11  
**Category:** UX  
**Probability:** Medium  
**Impact:** Medium  
**Severity:** Medium  
**Response Strategy:** Mitigate  
**Owner:** BA  
**Status:** Mitigated  
**Risk Description:** Questionnaire could produce noisy results that are hard to analyze.  
**Cause:** The product includes many possible pages, user roles, actions, states, and future features.  
**Impact if Occurs:** Elicitation results may become noisy, making it harder to identify MVP screens and page-level requirements.  
**Mitigation Plan:** Use role-based and screen-by-screen structure with controlled answer values.  
**Trigger / Early Warning:** Respondents provide vague feedback or many answers cannot be converted into concrete UI decisions.  
**Contingency Plan:** Reduce the questionnaire to the core MVP screens and move secondary questions to a follow-up review.
 
### RISK-003 UI Feedback Focuses on Visual Preferences
 
**Date Identified:** 2026-05-11  
**Category:** UX  
**Probability:** Medium  
**Impact:** High  
**Severity:** High  
**Response Strategy:** Mitigate  
**Owner:** BA  
**Status:** Mitigated  
**Risk Description:** Feedback may focus on colors/style instead of useful page behavior.  
**Cause:** UI/UX discussions often drift toward visual design instead of functional usability.  
**Impact if Occurs:** Questionnaire results may not provide enough useful input for sitemap, wireframes, UI requirements, and acceptance criteria.  
**Mitigation Plan:** Ask about pages, blocks, actions, errors, empty states, and guidance.  
**Trigger / Early Warning:** Most feedback mentions visual style but does not clarify required functionality.  
**Contingency Plan:** Conduct a follow-up review focused only on screen structure, workflow, and MVP actions.
 
### RISK-004 UI Requirements Are Not Checked for Implementation Feasibility
 
**Date Identified:** 2026-05-11  
**Category:** Technical  
**Probability:** Medium  
**Impact:** High  
**Severity:** High  
**Response Strategy:** Mitigate  
**Owner:** BA / Developer  
**Status:** Mitigated  
**Risk Description:** UI ideas may be accepted into MVP without checking architecture, timeline, and implementation complexity.  
**Cause:** User-facing features such as tabs, comparison views, PDF download, public pages, admin statistics, and ATS JSON may increase implementation complexity.  
**Impact if Occurs:** MVP scope may become too difficult to implement, causing delays or unfinished features.  
**Mitigation Plan:** Use Developer / Technical Reviewer checks, readiness checks in Requirements Log, and MVP/Stretch/Future classification.  
**Trigger / Early Warning:** Several UI requirements are marked as MVP but have High or Critical implementation complexity.  
**Contingency Plan:** Move complex UI items to MVP Stretch or Future Scope.

### RISK-005 UI Scope May Become Too Large Due to Many User/Admin Pages

**Date Identified:** 2026-05-13  
**Category:** Scope / UX  
**Probability:** Low  
**Impact:** Low  
**Severity:** Low  
**Response Strategy:** Monitor  
**Owner:** BA  
**Status:** Monitoring  
**Risk Description:** Confirmed page map includes several user and admin pages.  
**Cause:** The confirmed elicitation results show numerous pages and sections for both user and admin roles that could expand scope.  
**Impact if Occurs:** Development timeline may extend, core features may receive less attention, and integration complexity may increase.  
**Mitigation Plan:** Keep pages simple, prioritize tables/forms over advanced UI, and implement vertical slices.  
**Trigger / Early Warning:** More than the agreed number of UI elements are marked as MVP during detailed design.  
**Contingency Plan:** Reduce admin analytics and advanced filters first if schedule pressure appears.
**Update (2026-05-15):** Risk reduced. Resume Details page removed from MVP per DEC-014 / CR-013. User page count decreased, lowering overall scope risk.
 
### RISK-006 PDF Generation May Become Technically Difficult
 
**Date Identified:** 2026-05-13  
**Category:** Technical  
**Probability:** Medium  
**Impact:** High  
**Severity:** High  
**Response Strategy:** Mitigate  
**Owner:** BA / Developer  
**Status:** Open  
**Risk Description:** Generating selectable, printable, ATS-friendly PDFs may be harder than expected.  
**Cause:** Generating PDFs with proper layout, selectable text, and ATS-friendly formatting can be technically challenging.  
**Impact if Occurs:** PDF download feature may fail or produce low-quality output, affecting user experience and recruiter usability.  
**Mitigation Plan:** Start with a simple A4 PDF layout with selectable text and minimal styling.  
**Trigger / Early Warning:** Initial PDF generation attempts produce poor formatting or missing text selection.  
**Contingency Plan:** Use a proven PDF library and postpone advanced templates.
**Update (2026-05-16):** Risk scope expanded. Two-page HTML template (DEC-019) and HTML-to-PDF conversion approach (DEC-017) add template design and content distribution complexity. Even content spreading across two pages (courses count limits per page) needs careful implementation. Cover letter content also appears in the PDF, affecting page layout calculations.
 
### RISK-007 API Key Handling May Expose Secrets
 
**Date Identified:** 2026-05-13  
**Category:** Security  
**Probability:** Medium  
**Impact:** High  
**Severity:** High  
**Response Strategy:** Mitigate  
**Owner:** BA / Developer  
**Status:** Closed  
**Risk Description:** Showing or logging full API keys could expose secrets.  
**Cause:** Displaying and managing full API keys in the admin interface creates potential security vulnerabilities.  
**Impact if Occurs:** API keys could be exposed, leading to unauthorized AI usage and potential costs.  
**Mitigation Plan:** API keys are masked after saving, never logged, and can only be replaced or deleted.  
**Trigger / Early Warning:** Security review identifies potential key exposure in logs or UI.  
**Contingency Plan:** Keep masked key display, allow key replacement/deletion only, and log only key management actions without storing full key values.
 
### RISK-008 Vue + Spring MVC Integration May Increase Implementation Workload
 
**Date Identified:** 2026-05-13  
**Category:** Technical / Schedule  
**Probability:** Medium  
**Impact:** Medium  
**Severity:** Medium  
**Response Strategy:** Monitor  
**Owner:** BA / Developer  
**Status:** Open  
**Risk Description:** Vue + Spring MVC requires additional integration work, API design, and deployment setup.  
**Cause:** Integrating Vue frontend with Spring backend requires additional setup for API communication, state management, and build configuration.  
**Impact if Occurs:** Development velocity may decrease, and integration issues may delay MVP completion.  
**Mitigation Plan:** Validate integration early with one vertical slice.  
**Trigger / Early Warning:** Initial integration attempts show significant configuration complexity or data mapping issues.  
**Contingency Plan:** Keep Landing Page simple and reduce frontend complexity if needed.
 
### RISK-009 Resume Review Page Complexity
 
**Date Identified:** 2026-05-13  
**Category:** UX / Technical  
**Probability:** Medium  
**Impact:** High  
**Severity:** High  
**Response Strategy:** Mitigate  
**Owner:** BA / Developer  
**Status:** Open  
**Risk Description:** Resume Review can become complex due to editable sections, languages, variants, and regeneration.  
**Cause:** Supporting multiple adaptation variants, language versions, and editable sections increases UI and implementation complexity.  
**Impact if Occurs:** The resume review process may become confusing or difficult to implement within timeline.  
**Mitigation Plan:** MVP supports one generated draft first; variants and language tabs are stretch.  
**Trigger / Early Warning:** UI mockups show excessive complexity or user testing reveals confusion with multiple options.  
**Contingency Plan:** Simplify to section-based editing and single save flow.
**Update (2026-05-16):** Risk scope expanded. Cover letter generation (DEC-016) adds a new editable section in Resume Review. Cover letter text needs editing, saving, and display in the Resume Details modal. This increases Resume Review complexity moderately but is managed within the existing generation flow.

### RISK-010 Wireframe Field Inconsistencies Create Requirement Ambiguity

**Date Identified:** 2026-05-13  
**Category:** Quality  
**Probability:** Medium  
**Impact:** Medium  
**Severity:** Medium  
**Response Strategy:** Mitigate  
**Owner:** BA  
**Status:** Mitigated  
**Risk Description:** Early wireframe notes had inconsistencies in required fields and validation rules.  
**Cause:** Wireframe fields, validation rules, and error messages were drafted iteratively and some fields had conflicting required/optional status.  
**Impact if Occurs:** Requirements, wireframes, data model drafts, and tests may become inconsistent.  
**Mitigation Plan:** Field rules were cleaned and consolidated in `wireframe_field_requirements.md`.  
**Trigger / Early Warning:** Same field has different required/optional status in different artifacts.
**Contingency Plan:** Use `wireframe_field_requirements.md` as the field-level source of truth and update related requirements/logs if contradictions appear.

### RISK-011 Accidental Resume Deletion May Cause User Frustration and Data Loss

**Date Identified:** 2026-05-18
**Category:** UX / Data
**Probability:** Low
**Impact:** High
**Severity:** Medium
**Response Strategy:** Mitigate
**Owner:** BA / Developer
**Status:** Open
**Risk Description:** A user may accidentally delete a saved resume, causing frustration and potential data loss.
**Cause:** The delete action is available from the Resume Details modal with a confirmation step, but a user might still confirm accidentally or regret the decision.
**Impact if Occurs:** User loses access to the generated resume and its public sharing link. Data is soft-deleted but cannot be restored without admin intervention.
**Mitigation Plan:** Implement confirmation dialog with two-step process: first click shows confirmation prompt, second click confirms. Consider undo option within a short time window.
**Trigger / Early Warning:** User reports accidentally deleting a resume or requests data restoration.
**Contingency Plan:** Provide admin ability to restore soft-deleted resumes by setting `is_deleted` back to `false`.

### RISK-012 AI-Generated HTML May Break Template Layout or Introduce XSS

**Date Identified:** 2026-05-20
**Category:** Security / Technical
**Probability:** Medium
**Impact:** High
**Severity:** High
**Response Strategy:** Mitigate
**Owner:** BA / Developer
**Status:** Open
**Risk Description:** AI may generate HTML with unsafe tags, malformed markup, or unintended content that breaks the resume layout or introduces XSS vulnerabilities.
**Cause:** AI text fields may contain limited HTML for formatting (DEC-037). Without strict sanitization, unsafe tags or broken markup could be rendered in the final HTML/PDF.
**Impact if Occurs:** Resume PDF may have broken layout, missing content, or — in worst case — XSS vulnerability if HTML is rendered in a web context.
**Mitigation Plan:** Backend sanitizes all AI-provided HTML using an allowlist (DEC-038). Allowlist: `<strong>`, `<b>`, `<i>`, `<em>`, `<ul>`, `<ol>`, `<li>`, `<p>`, `<br>`. All other tags stripped.
**Trigger / Early Warning:** AI output contains unexpected HTML tags or malformed markup during testing.
**Contingency Plan:** If sanitization fails, fall back to plain text rendering and strip all HTML.

### RISK-013 Inconsistent Error Handling May Expose Stack Traces or Miss Critical Failures

**Date Identified:** 2026-05-21
**Category:** Quality / Technical
**Probability:** Medium
**Impact:** High
**Severity:** High
**Response Strategy:** Mitigate
**Owner:** BA / Developer
**Status:** Open
**Risk Description:** Inconsistent error handling across controller, service, and DAO layers may result in Java stack traces exposed to the frontend, unhandled exceptions causing blank pages, or critical errors being silently swallowed.
**Cause:** Without a global exception handler, per-layer custom exceptions, and structured logging, each developer may handle errors differently, creating gaps in coverage.
**Impact if Occurs:** Stack traces in API responses create security and professionalism issues. Silent failures may hide data corruption or generation bugs. Debugging becomes harder without consistent logging.
**Mitigation Plan:** Implement custom exception hierarchy (ControllerException, ServiceException, DaoException), global `@ControllerAdvice` handler, user-friendly error responses, and structured logging per NFR-002 through NFR-005.
**Trigger / Early Warning:** Code review reveals try-catch blocks swallowing exceptions without logging, or generic error responses without context.
**Contingency Plan:** Add error handling audit to code review checklist. Fix gaps identified during review before each demo iteration.

### RISK-014 Custom Connection Pool May Have Thread-Safety Bugs or Performance Issues

**Date Identified:** 2026-05-21
**Category:** Technical
**Probability:** Medium
**Impact:** High
**Severity:** High
**Response Strategy:** Mitigate
**Owner:** BA / Developer
**Status:** Open
**Risk Description:** The custom Connection Pool may contain thread-safety bugs, connection leaks, deadlocks, or performance bottlenecks that affect the entire application's database access layer.
**Cause:** Manual Connection Pool implementation is a Capstone requirement. Thread-safety, timeout handling, connection validation, and graceful shutdown are complex to implement correctly.
**Impact if Occurs:** Connection leaks may exhaust database connections. Thread-safety bugs may cause data corruption or application crashes. Performance issues may slow all database operations.
**Mitigation Plan:** Thorough internal documentation of the pool's thread-safety mechanism, connection lifecycle, timeout handling, and edge cases. Unit tests for concurrent connection acquisition/release. Stress testing with multiple concurrent requests.
**Trigger / Early Warning:** Application hangs during concurrent database operations. Database connection limit is reached unexpectedly.
**Contingency Plan:** Add connection leak detection (e.g., tracking unreleased connections with stack traces). Increase pool timeout and maximum size temporarily while investigating root cause.

### RISK-015 Misconfigured Budget Settings May Cause Bad PDF Layout or Generation Errors

**Date Identified:** 2026-05-23
**Category:** Data
**Probability:** Low
**Impact:** High
**Severity:** Medium
**Response Strategy:** Monitor
**Owner:** BA / Developer
**Status:** Monitoring
**Risk Description:** Incorrect or inconsistent budget configuration values in PostgreSQL may cause AI to generate content that does not fit the selected template, resulting in PDF overflow, overcrowded layout, or poor visual balance.
**Cause:** Budget configuration values (sentence counts, bullet limits, job distribution rules) are stored in PostgreSQL and manually editable. Data entry errors, conflicting rules, or missing rule rows may produce unexpected content budgets.
**Impact if Occurs:** Generated resume PDF may have overflow, missing sections, or visually unbalanced layout requiring regeneration.
**Mitigation Plan:** Active config fallback logic (NFR-034) prevents generation failure when config is misconfigured. Partial unique index prevents multiple active configs. Config version is stored with generation request for traceability. Backend validation should check basic budget consistency before generation.
**Trigger / Early Warning:** Generation produces content that overflows the template or produces unexpected section lengths.
**Contingency Plan:** Fix budget values in PostgreSQL and regenerate. Maintain a known-good default seed config.

### RISK-999 [Risk Short Title Template]

**Date Identified:** YYYY-MM-DD  
**Category:** [Scope / Technical / Data / Security / UX / Integration / Deployment / Schedule / Quality / Compliance]  
**Probability:** [Low / Medium / High]  
**Impact:** [Low / Medium / High / Critical]  
**Severity:** [Low / Medium / High / Critical]  
**Response Strategy:** [Avoid / Mitigate / Transfer / Accept / Monitor]  
**Owner:** [Owner]  
**Status:** Open  
**Risk Description:** [What may happen]  
**Cause:** [Why it may happen]  
**Impact if Occurs:** [What will be affected]  
**Mitigation Plan:** [How to reduce probability/impact]  
**Trigger / Early Warning:** [How to know the risk is becoming real]  
**Contingency Plan:** [What to do if the risk happens]

***
*This risk register follows the Information Management Plan structure and conventions for the ResumAIner project. Risks are reviewed regularly and mitigation actions are updated as the project progresses.*
