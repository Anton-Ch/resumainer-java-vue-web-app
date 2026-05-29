# Information Management Plan for ResumAIner

**Project ID:** `resumainer`
**Date:** 2026-05-10
**Chapter:** 3.4 Information Management
**Author:** Anton
**Version:** 1.0
**Status:** Approved

***
## 1. Purpose  
  
This document defines how business analysis and system analysis information will be organized, stored, reviewed, maintained, and reused during the ResumAIner Capstone project.  
  
The goal is to create a clear and auditable information structure that supports:  
- project planning;  
- requirements management;  
- decision tracking;  
- traceability;  
- future development handoff;  
- portfolio presentation.  
  
The information management approach remains lightweight enough for a Capstone project while still demonstrating professional business analysis discipline.  
  
## 2. Information Management Objectives  
  
The information management process has the following objectives:    
1. Maintain a single source of truth for approved project documentation.  
2. Separate source materials, working drafts, and approved artifacts.  
3. Keep requirements traceable from business need to implementation and testing.  
4. Ensure that key decisions and changes are recorded.  
5. Make the repository understandable for mentors, reviewers, and future portfolio readers.  
6. Prevent documentation from becoming outdated or inconsistent with the final application.

## 3. Repository Information Structure  
  
The BA repository is the main storage location for business analysis and system analysis documentation.  
  
Repository structure:  
```text  
ai-resume-tailor-business-analysis/  
│  
├── README.md  
├── CHANGELOG.md  
├── GLOSSARY.md  
│  
├── drafts/  
│ └── source project materials and initial context  
│  
├── docs/
│ ├── 01_project-overview/  
│ ├── 02_requirements/  
│ ├── 03_processes-and-workflows/  
│ ├── 04_domain-and-data-model/  
│ ├── 05_ui-ux/  
│ ├── 07_project-management/  
│ ├── 08_traceability/  
│ └── 09_decisions/  
│  
├── assets/  
│ ├── diagrams/  
│ ├── wireframes/  
│ └── screenshots/  
│  
└── templates/  
├── adr_template.md  
├── use_case_template.md  
├── user_story_template.md  
└── requirement_template.md
```


## 4. Information Categories and Storage Plan

| Information Category    | Purpose                                                                                 | Repository Location                | Format                        | Owner          |
| ----------------------- | --------------------------------------------------------------------------------------- | ---------------------------------- | ----------------------------- | -------------- |
| Source materials        | Initial project ideas, capstone constraints, early notes, and context                   | `drafts/`                          | Markdown, text, PDF if needed | BA             |
| Project overview        | Vision, business context, scope, MVP definition                                         | `docs/01_project-overview/`        | Markdown                      | BA             |
| Requirements            | BR, FR, NFR, user stories, acceptance criteria                                          | `docs/02_requirements/`            | Markdown, tables              | BA             |
| Workflows and use cases | User workflows, use cases, process diagrams                                             | `docs/03_processes-and-workflows/` | Markdown, Mermaid             | BA             |
| Domain and data model   | Domain model, ERD, data dictionary, normalization notes                                 | `docs/04_domain-and-data-model/`   | Markdown, Mermaid, images     | BA             |
| UI/UX documentation     | UI requirements, information architecture, wireframes, prototype notes                  | `docs/05_ui-ux/`                   | Markdown, images              | BA             |
| Project management      | Roadmap, MVP backlog, risk register, open questions, change requests                    | `docs/07_project-management/`      | Markdown, tables              | BA             |
| Traceability            | Links between goals, requirements, use cases, data entities, UI, tests                  | `docs/08_traceability/`            | Markdown tables               | BA             |
| Decisions               | Decision log, ADRs, key architectural and scope decisions                               | `docs/09_decisions/`               | Markdown                      | BA / Developer |
| Visual assets           | Diagrams, wireframes, screenshots                                                       | `assets/`                          | PNG, SVG, Mermaid             | BA / Developer |
| Templates               | Reusable templates for documentation consistency                                        | `templates/`                       | Markdown                      | BA             |


## 5. Artifact Status Model

Each important artifact should have a clear status.

| Status     | Meaning                                               |
| ---------- | ----------------------------------------------------- |
| Draft      | Initial working version; not yet reviewed             |
| Reviewed   | Checked for correctness, consistency, and project fit |
| Approved   | Stable enough to be used as a baseline                |
| Archived   | Kept for history but not used in active planning      |

Approved artifacts should be used as the source for development planning and implementation decisions.

## 6. Information Lifecycle

Business analysis information will follow this lifecycle:

1. **Capture**  
    Initial ideas, constraints, and source materials are stored in `drafts/`.
2. **Structure**  
    Raw information is transformed into structured BA artifacts.
3. **Review**  
    Artifacts are checked for consistency, technical feasibility, scope alignment, and usefulness.
4. **Approve**  
    Stable documents are marked as approved and stored in the appropriate `docs/` section.
5. **Trace**  
    Requirements and decisions are connected through the traceability matrix and decision log.
6. **Maintain**  
    Changes are handled through the lightweight governance process.
7. **Handoff**  
    Approved artifacts are used to prepare development tasks, implementation plans, and final project documentation.

## 7. Traceability Management

Traceability will be maintained through:
- unique item IDs;
- stable file naming;
- Requirements Traceability Matrix (RTM):
  `docs/08_traceability/traceability_matrix.md`;
- decision log:
  `docs/09_traceability/decision_log.md`;
- change request log:
  `docs/07_traceability/change_request_log.md`;
- links between business objectives, requirements, use cases, UI screens, data entities, and test cases.

Traceability will be maintained through documentation structure, ID conventions, and review discipline.

Recommended traceability chain:

```text
Business Objective
    ↓
Business Requirement
    ↓
Functional / Non-Functional Requirement
    ↓
Use Case / User Story
    ↓
UI Screen / Data Entity / Service
    ↓
Test Case
    ↓
Implementation Reference
```

### 7.1 ID Conventions
To ensure seamless traceability, the following ID prefixes are mandatory for all project artifacts:

| Prefix | Category | Example |
| :--- | :--- | :--- |
| **BG** | Business Goals / Objectives | `BG-001` |
| **BR** | Business Requirements | `BR-005` |
| **FR** | Functional Requirements | `FR-012` |
| **NFR** | Non-Functional Requirements | `NFR-002` |
| **UC** | Use Cases | `UC-04` (reserved for future use) |
| **US** | User Stories | `US-01` (reserved for future use) |
| **UI** | UI Screens / Wireframes | `UI-07` (reserved for future use) |
| **DEC** | Architectural/Design Decisions | `DEC-003` |
| **CR** | Change Requests | `CR-001` |
| **TC** | Test Cases | `TC-010` |

## 8. Naming Conventions

Repository file names should be clear, stable, and easy to navigate.

Recommended style:
- use English names;
- use lowercase file names where possible;
- separate words with underscores or hyphens consistently;
- avoid temporary names in approved documentation;
- avoid tool-specific names in public documentation.

Examples:
```bash
project_vision.md
business_context.md
stakeholder_engagement_plan.md
governance_plan.md
information_management_plan.md
ba_process_improvement_plan.md
functional_requirements.md
non_functional_requirements.md
traceability_matrix.md
decision_log.md
change_request_log.md
```


## 9. Versioning and Change Control

Documentation changes are managed through Git history and the lightweight governance process.

Minor changes may be committed directly.

Examples:
- typo fixes;
- formatting corrections;
- wording clarification;
- broken link fixes.

Significant changes should be recorded in the Change Request Log.

Examples:
- adding or removing MVP functionality;
- changing technical constraints;
- changing database structure;
- changing public URL behavior;
- changing deployment approach;
- changing security-related requirements.

Recommended artifact:
`docs/07_project-management/change_request_log.md`


## 10. Decision Management

Major decisions should be recorded in the Decision Log.

Examples:
- choosing plain JDBC instead of ORM;
- choosing Tomcat for deployment;
- deciding whether Vue is part of MVP;
- deciding whether public ATS JSON endpoint remains in MVP;
- deciding whether Google OAuth2 is MVP or post-MVP;
- deciding whether OpenRouter integration is real or mocked for demonstration.

Recommended artifact:
`docs/09_decisions/decision_log.md`

## 11. Information Quality Criteria

Approved BA documentation should meet the following quality criteria:

| Criterion    | Description                                                                  |
| ------------ | ---------------------------------------------------------------------------- |
| Clear        | The artifact is understandable without additional verbal explanation         |
| Consistent   | It does not contradict other approved artifacts                              |
| Traceable    | Key requirements can be traced to business needs and future implementation   |
| Feasible     | It respects capstone constraints and project timeline                        |
| Useful       | It supports real design, development, testing, or presentation decisions     |
| Proportional | It is detailed enough for a Capstone project without unnecessary bureaucracy |

## 12. Security and Confidentiality of Information

The repository must not contain sensitive secrets.

Exclude from commits:
- database passwords;
- OpenRouter API keys;
- private access tokens;
- `.env` files with real credentials;
- private personal data not required for demonstration;
- production server credentials.

Safe examples may be stored in:
```text
.env.example
application.example.properties
```

## 13. Documentation Review Points

Documentation should be reviewed at these checkpoints:

|Checkpoint|Review Focus|
|---|---|
|Before finalizing MVP scope|Scope boundaries, feasibility, must-have vs future scope|
|Before ERD finalization|Entity consistency, relationships, normalization|
|Before development repository creation|Development readiness and technical constraints|
|Before implementation demo|Documentation-code consistency|
|Before final portfolio publication|Clarity, completeness, and public presentation quality|


## 14. Minimal Supporting Artifacts

To keep the project professional but not overengineered, the following supporting artifacts planned to be applied:

| Artifact                        | Location                                                        | Purpose                                                             |
| ------------------------------- | --------------------------------------------------------------- | ------------------------------------------------------------------- |
| Decision Log                    | `docs/09_decisions/decision_log.md`                             | Record major project decisions                                      |
| Change Request Log              | `docs/07_project-management/change_request_log.md`              | Record non-trivial changes                                          |
| Open Questions Log              | `docs/07_project-management/open_questions_log.md`              | Track unresolved questions                                          |
| Traceability Matrix             | `docs/08_traceability/traceability_matrix.md`                   | Maintain requirement traceability                                   |
| Requirement Readiness Checklist | `docs/07_project-management/requirement_readiness_checklist.md` | Check whether a requirement is ready for implementation             |
| Risk Register                   | `docs/07_project-management/risk_register.md`                   | Identify, assess, and track project risks and mitigation strategies |

## 15. Summary

The `docs/` folder is the canonical location for approved, portfolio-ready business analysis and system analysis documentation.

The `drafts/` folder stores source materials.

The `assets/` folder stores diagrams, wireframes, screenshots, and visual evidence.

The `templates/` folder stores reusable documentation templates.

This structure keeps project information organized, auditable, and suitable for both Capstone delivery and portfolio presentation.






git commit -m "docs: add reusable BA log templates" -m "Add unified templates for unified log, decision log, change request log, open questions log, traceability matrix, requirement readiness checklist, and risk register.  These templates define consistent structure, controlled values, placeholder rows, and reusable details sections for future BA and project governance artifacts."




