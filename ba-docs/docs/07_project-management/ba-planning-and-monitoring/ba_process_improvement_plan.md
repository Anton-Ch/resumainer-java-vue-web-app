# Business Analysis Process Improvement Plan for ResumAIner

**Project ID:** `resumainer`
**Date:** 2026-05-10
**Chapter:** 3.5 Evaluating BA Performance
**Author:** Anton
**Version:** 1.0
**Status:** Approved

***


## 1. Purpose  
  
This document defines how the business analysis process will be monitored and improved during the ResumAIner Capstone project.  
  
The goal is to make sure that the BA work remains useful, traceable, technically feasible, and aligned with the MVP without creating unnecessary bureaucracy.
  
The improvement approach is intentionally lightweight but professional enough to support a real project workflow.  
  
## 2. Improvement Focus  
  
The main improvement focus is alignment between:  
- business goals;  
- MVP scope;  
- user workflows;  
- functional and non-functional requirements;  
- mandatory capstone technical constraints;  
- database and UI design;  
- future implementation feasibility.  
  
The project has a higher risk of scope growth because it includes AI-assisted resume generation, PDF export, public links, admin features, and future monetization ideas. Therefore, the BA process must actively prevent uncontrolled expansion of the MVP.  
  
## 3. BA Performance Goals  
  
The BA process should achieve the following goals:  
  
| Goal                     | Description                                                                        |
| ------------------------ | ---------------------------------------------------------------------------------- |
| Scope clarity            | MVP, stretch goals, post-MVP, and future scope are clearly separated               |
| Requirement readiness    | Requirements are clear enough to support implementation                            |
| Technical feasibility    | Requirements respect the mandatory Java/Spring MVC/JDBC stack                      |
| Traceability             | Business objectives, requirements, workflows, data model, UI, and tests are linked |
| Decision transparency    | Key decisions are documented and explainable                                       |
| Documentation usefulness | BA artifacts support real development and are not created only for formality       |
  
## 4. Identified Process Risks  
  
| Risk ID     | Risk Area                   | Description                                                                              | Impact |
| ----------- | --------------------------- | ---------------------------------------------------------------------------------------- | ------ |
| BA-RISK-001 | Scope creep                 | AI, PDF, public links, admin panel, and future payment ideas can expand the MVP too much | High   |
| BA-RISK-002 | Technical feasibility gap   | Some ideas may not fit the mandatory Spring MVC + JDBC capstone stack or available time    | High   |
| BA-RISK-003 | Documentation-code mismatch | BA documents may describe features that are later not implemented                        | Medium |
| BA-RISK-004 | Requirement ambiguity       | Some requirements may be too broad or not testable                                       | Medium |
| BA-RISK-005 | Overdocumentation           | Too many documents may reduce focus and create non-value work                            | Medium |
| BA-RISK-006 | Underdocumentation          | Too little structure may weaken the portfolio and development handoff                    | Medium |
  
## 5. Improvement Actions  
  
### 5.1 Requirement Readiness Checklist  
  
Before a requirement is added to the MVP baseline, it should pass a short readiness check.  
  
A requirement is ready when:  
- it has a unique ID;  
- it has a clear business reason;  
- it belongs to MVP, MVP Stretch, Post-MVP, or Future Scope;  
- it is technically feasible within the mandatory capstone stack;  
- it has clear acceptance criteria;  
- it has an identified affected workflow or UI screen;  
- it has an identified affected data entity if applicable;  
- it does not conflict with approved technical constraints;  
- it can be tested or demonstrated.  
  
Recommended artifact:  
`docs/07_project-management/requirement_readiness_checklist.md`  
  
### 5.2 Technical Feasibility Filter  
  
Each significant requirement should be checked against technical constraints before it is approved for MVP.
  
The check should answer:  
- Does this requirement fit Spring MVC + JDBC architecture?  
- Does it require forbidden technologies such as Hibernate, JPA, or Spring Data JPA?  
- Does it affect the database schema?  
- Does it require external API integration?  
- Does it affect security, privacy, or public data exposure?  
- Can it be implemented within the Capstone timeline?  
- Can it be tested or demonstrated?  
  
Recommended implementation:   
Add a `Technical Feasibility` column to:  
- Requirements Backlog;  
- Traceability Matrix;  
- MVP Backlog.  
  
### 5.3 Lightweight De-scoping Rule 
  
If a feature is valuable but too complex for the MVP, it should not be deleted from the project vision. It should be moved to one of the following categories:  
- MVP Stretch Goal  
- Post-MVP  
- Future Scope  
  
Examples:

| Feature                 | Possible Decision                                              |
| ----------------------- | -------------------------------------------------------------- |
| User-owned API keys     | Future Scope                                                   |
| Full payment system     | Future Scope                                                   |
| Google OAuth2           | MVP Stretch Goal or Post-MVP                                   |
| Cover letter generation | MVP Stretch Goal                                               |
| ATS JSON endpoint       | MVP or MVP Stretch Goal depending on implementation complexity |
| Full resume analytics   | Future Scope                                                   |
  
Recommended artifact:  
`docs/07_project-management/change_request_log.md`  
  
### 5.4 Decision Log Discipline  
  
Major decisions should be recorded in the Decision Log.  
  
This helps explain why the project took a specific direction and prevents repeated debates.  
  
Examples of decisions to record:  
- Use plain JDBC instead of ORM.  
- Use a custom thread-safe Connection Pool.  
- Use Tomcat as the beginner-friendly deployment server.  
- Use Docker Compose for VPS deployment.  
- Use Flyway for minimal database migration management.  
- Use mock AI generation as fallback if external API access fails.  
- Decide whether Vue is allowed for the Capstone MVP.  
  
Recommended artifact:  
`docs/09_decisions/decision_log.md`  
  
### 5.5 Open Questions Management  
  
Unresolved questions should be tracked explicitly.  
  
This prevents silent assumptions from becoming hidden risks.  
  
Examples:  
- Is Vue allowed for the final implementation?  
- Is real OpenRouter API usage allowed during final review?  
- Is Google OAuth2 required or optional?  
- Should ATS JSON endpoint remain in MVP?  
- Should final resume content be stored as text, JSON, or structured sections?  
  
Recommended artifact:
`docs/07_project-management/open_questions_log.md`  
  
### 5.6 Traceability Improvement  
  
The project should maintain a lightweight Requirements Traceability Matrix.  
  
The matrix should connect:  
- business objectives;  
- functional requirements;  
- non-functional requirements;  
- use cases;  
- UI screens;  
- data entities;  
- test cases;  
- implementation references after development starts.  
  
Recommended artifact:  
`docs/08_traceability/traceability_matrix.md`  
  
The traceability matrix should be expanded gradually. It should not become a blocker for development.  
  
## 6. BA Process Metrics  
  
The project will use a small number of practical BA process metrics.  
  
| Metric                                                 | Purpose                         | Healthy Direction                       |
| ------------------------------------------------------ | ------------------------------- | --------------------------------------- |
| Number of open questions                               | Shows unresolved uncertainty    | Decreasing over time                    |
| Number of MVP requirements without acceptance criteria | Shows requirement readiness gap | Should approach zero before development |
| Number of unresolved technical feasibility concerns    | Shows implementation risk       | Should approach zero before development |
| Number of changed MVP requirements                     | Shows scope stability           | Should decrease after MVP baseline      |
| Number of documentation-code mismatches                | Shows handoff quality           | Should be zero before final submission  |
  
These metrics are not intended for heavy reporting. They are checkpoints for keeping the project under control.  
  
## 7. Review Cadence  
  
The BA process should be reviewed at practical checkpoints. 
  
| Checkpoint                             | Review Questions                                                                               |
| -------------------------------------- | ---------------------------------------------------------------------------------------------- |
| After initial planning documents       | Are approach, stakeholders, governance, information management, and improvement process clear? |
| Before ERD finalization                | Are entities driven by real requirements and user workflows?                                   |
| Before development repository creation | Is the MVP baseline clear enough for implementation?                                           |
| After first working vertical slice     | Do requirements match actual implementation complexity?                                        |
| Before final submission                | Do documentation, code, tests, and demo tell the same story?                                   |
  
## 8. Improvement Backlog  
  
The following improvement backlog will be used to keep BA work practical and focused.  
  
| ID      | Improvement Item                                              | Priority | Status  |
| ------- | ------------------------------------------------------------- | -------- | ------- |
| IMP-001 | Create Requirement Readiness Checklist                        | High     | Planned |
| IMP-002 | Create Decision Log                                           | High     | Planned |
| IMP-003 | Create Change Request Log                                     | Medium   | Planned |
| IMP-004 | Create Open Questions Log                                     | High     | Planned |
| IMP-005 | Create initial Traceability Matrix                            | High     | Planned |
| IMP-006 | Add technical feasibility field to requirements tracking      | High     | Planned |
| IMP-007 | Review MVP scope before development repository creation       | High     | Planned |
| IMP-008 | Review documentation-code consistency before final submission | High     | Planned |
  
## 9. Recommended Supporting Artifacts  
  
To keep the process professional without creating excessive documentation, only the following supporting artifacts are recommended:  
  
| Artifact                        | Location                                                        | Value                                              |
| ------------------------------- | --------------------------------------------------------------- | -------------------------------------------------- |
| Requirement Readiness Checklist | `docs/07_project-management/requirement_readiness_checklist.md` | Prevents vague requirements from entering MVP      |
| Decision Log                    | `docs/09_decisions/decision_log.md`                             | Makes key decisions transparent                    |
| Change Request Log              | `docs/07_project-management/change_request_log.md`              | Controls meaningful scope and requirement changes  |
| Open Questions Log              | `docs/07_project-management/open_questions_log.md`              | Makes uncertainty visible                          |
| Traceability Matrix             | `docs/08_traceability/traceability_matrix.md`                   | Connects BA work to design, development, and tests |
| Risk Register                   | `docs/07_project-management/risk_register.md`                   | Allows to manage risks and mitigate them           |
  
## 10. Definition of Improved BA Process  
  
The BA process is considered improved when:  
- MVP requirements are clear and feasible;  
- major decisions are documented;  
- open questions are visible and gradually resolved;  
- scope changes are controlled;  
- requirements have acceptance criteria;  
- technical constraints are respected;  
- documentation supports actual development;  
- final documentation matches the implemented product.  
  
## 11. Summary  
  
The BA process for ResumAIner should stay lightweight, practical, and implementation-oriented.  
  
The goal is not to create documents for their own sake. The goal is to create a clear analytical foundation that supports:  
- better design decisions;  
- easier implementation;  
- stronger Capstone review;  
- stronger portfolio presentation;  
- fewer late-stage surprises.