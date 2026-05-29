# UI/UX Elicitation Plan

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

This document defines the plan for eliciting UI/UX requirements for the ResumAIner Capstone project.

The purpose of this elicitation activity is to identify which screens, page blocks, actions, error states, empty states, and user guidance elements are necessary for the MVP and which items should be moved to stretch goals or future scope.

The results of this elicitation activity will be used to prepare:
- UI/UX requirements;
- sitemap;
- page-level requirements;
- user flows;
- wireframes;
- mockups;
- acceptance criteria for key screens;
- development handoff notes.

## 2. Elicitation Objectives

The elicitation process has the following objectives:

1. Identify the screens required for the MVP.
2. Determine which page blocks are mandatory, optional, or unnecessary.
3. Clarify the key user actions available on each page.
4. Identify expected error states and empty states.
5. Define what user guidance, hints, and explanations are needed.
6. Validate the resume generation and review workflow.
7. Clarify public resume viewing expectations for recruiters.
8. Clarify admin panel needs for user management and abuse control.
9. Check UI feasibility from a developer perspective.
10. Convert survey results into actionable UI/UX requirements.

## 3. Respondent Segments

The questionnaire is divided into **role-based sections**. Each respondent should answer only the section relevant to their role.

| Respondent Segment | Purpose of Input | Key Focus |
|---|---|---|
| Registered User / Job Seeker | Understand how users create profiles, generate resumes, review drafts, save versions, and download/share resumes | Dashboard, profile, generation, review, history, settings |
| Recruiter / External Viewer | Understand how public resume links should work for people who do not have an account | Public resume page, PDF download, readability, ATS-friendly access |
| Administrator | Understand what admin functions are required for user control and system monitoring | User management, resume review, generation permissions, usage statistics |
| Developer / Technical Reviewer | Validate feasibility, complexity, integration risks, and implementation priorities | Architecture, UI complexity, data impact, validation, errors, deployment |

## 4. Elicitation Scope

### 4.1 Pages in Scope

The elicitation activity covers the following pages and UI areas:

| Page / UI Area | Primary Role | Purpose |
|---|---|---|
| Landing Page | Visitor / Registered User | Explain product value and provide entry to login/register |
| Login / Register | Registered User | Create account and access the system |
| Dashboard | Registered User | Show progress, quick actions, and recent activity |
| My Profile | Registered User | Manage structured professional profile data |
| Generate Resume | Registered User | Provide vacancy data and generation settings |
| Resume Review | Registered User | Review, compare, edit, and save generated resume drafts |
| Resume History | Registered User | Manage saved resume versions |
| Settings | Registered User | Manage default language, profile preferences, and account-related options |
| Public Resume Page | Recruiter / External Viewer | View and download shared resume without registration |
| Admin Dashboard | Administrator | Monitor users, resumes, and system usage |
| User Management | Administrator | View, search, and manage registered users |
| Resume Review by Admin | Administrator | Inspect generated resumes if needed |
| Technical Feasibility Review | Developer / Technical Reviewer | Confirm implementation feasibility and constraints |

### 4.2 Out of Scope for This Elicitation

The following topics are not the main focus of this questionnaire:
- detailed visual design style;
- color palette;
- final typography;
- final resume PDF template design;
- monetization flows;
- payment pages;
- advanced analytics;
- detailed API design.

These may be handled in later design or implementation activities.

## 5. Elicitation Technique

The selected elicitation technique is a structured role-based questionnaire.

### 5.1 Justification

A questionnaire is suitable because:
- it provides consistent input across different respondent types;
- it allows page-level comparison of priorities;
- it helps separate MVP needs from future ideas;
- it creates evidence for UI/UX decisions;
- it is lightweight and appropriate for a Capstone project;
- it can be reused for future iterations.

### 5.2 Complementary Techniques

If needed, the questionnaire may be supported by:
- short follow-up interviews;
- mockup review sessions;
- usability walkthroughs;
- mentor review;
- self-review against implementation constraints.

## 6. Questionnaire Structure

The questionnaire is stored in a separate file:

`docs/02_requirements/elicitation/ui_ux_questionnaire.md`

It is divided into four role-based sections:

1. Questions for Registered User / Job Seeker
2. Questions for Recruiter / External Viewer
3. Questions for Administrator
4. Questions for Developer / Technical Reviewer

Within each role section, questions are grouped by page or workflow area.

Each page-level group may include questions about:
- page necessity;
- required blocks;
- optional blocks;
- key actions;
- error states;
- empty states;
- user guidance and hints;
- prioritization;
- open comments.

## 7. Controlled Answer Values

To make answers easier to compare, the questionnaire uses controlled values.

### 7.1 Page Priority Values

| Value | Meaning |
|---|---|
| MVP | Required for the first working version |
| MVP Stretch | Useful if time allows but not required |
| Post-MVP | Should be implemented after MVP |
| Future Scope | Long-term idea |
| Not Needed | Not useful for this product |

### 7.2 Block Importance Values

| Value | Meaning |
|---|---|
| Must Have | Required for the page to work properly |
| Should Have | Important but not critical |
| Could Have | Nice to have if implementation is easy |
| Not Needed | Should not be included |
| Not Sure | Needs more discussion |

### 7.3 Action Importance Values

| Value | Meaning |
|---|---|
| Critical | The workflow cannot work without this action |
| Important | Strongly improves user experience |
| Optional | Useful but not necessary |
| Not Needed | Should not be included |
| Not Sure | Needs clarification |

### 7.4 Complexity Values for Developer Review

| Value | Meaning |
|---|---|
| Low | Simple to implement |
| Medium | Requires moderate effort |
| High | Requires significant effort or coordination |
| Critical | May threaten MVP timeline or architecture |

## 8. Analysis Method

Survey answers will be analyzed using the following approach:

### 8.1 Quantitative Analysis

For scale-based answers:
- calculate average importance score;
- identify high-priority pages and blocks;
- identify low-priority or unnecessary items;
- compare priorities across respondent segments.

### 8.2 Qualitative Analysis

For open answers:
- group answers into recurring themes;
- identify repeated pain points;
- identify missing page elements;
- convert useful suggestions into candidate requirements;
- record unclear items in the Open Questions Log.

### 8.3 Screen-Level Decision Rules

| Result Pattern | Suggested Decision |
|---|---|
| Strong need from primary role + feasible implementation | Include in MVP |
| Strong need but high implementation complexity | Consider MVP Stretch |
| Low user value + high complexity | Move to Post-MVP or Future Scope |
| Repeated confusion about a page or action | Add guidance, hint, or simplify flow |
| Repeated request for missing data/action | Add candidate requirement |
| Contradictory answers | Add to Open Questions Log |

## 9. Expected Outputs

The elicitation results should produce or update the following artifacts:

| Output Artifact                  | Location                                                        | Purpose                              |
| -------------------------------- | --------------------------------------------------------------- | ------------------------------------ |
| UI/UX Requirements               | `docs/05_ui-ux/ui_ux_requirements.md`                           | Define screen-level UI requirements  |
| Sitemap                          | `docs/05_ui-ux/information_architecture.md`                     | Define page structure and navigation |
| User Flows                       | `docs/03_processes-and-workflows/user_workflows.md`             | Define step-by-step flows            |
| Wireframe Notes                  | `docs/05_ui-ux/wireframes.md`                                   | Prepare screen wireframes            |
| Open Questions Log               | `docs/07_project-management/open_questions_log.md`              | Track unresolved questions           |
| Requirements Traceability Matrix | `docs/08_traceability/traceability_matrix.md`                   | Connect UI decisions to requirements |
| Requirement Readiness Checklist  | `docs/07_project-management/requirement_readiness_checklist.md` | Check implementation readiness       |

## 10. Elicitation Risks

Detailed elicitation-related risks are tracked in the project Risk Register:
`docs/07_project-management/risk_register.md`

The elicitation plan may introduce or update risks related to:
- unclear respondent expectations;
- overly broad questionnaire scope;
- insufficient screen-level feedback;
- UI preferences that conflict with MVP feasibility;
- missing developer feasibility review;
- privacy concerns around public resume access.

If a risk requires active monitoring or mitigation, it should be recorded in the Risk Register with a clear owner, severity, response strategy, and status.

## 11. Review and Approval

The elicitation plan should be reviewed before the questionnaire is used.

Review focus:
- Are the right respondent roles included?
- Are the questions specific enough to support wireframes?
- Are questions separated by role?
- Are screen-level blocks and actions covered?
- Are error states and empty states included?
- Are results easy to convert into requirements?

## 12. Summary

This elicitation plan defines a structured, role-based approach for collecting UI/UX requirements.

The plan is designed to support practical MVP decisions, not abstract interface preferences.

The expected result is a clear understanding of:
- which screens are necessary;
- what each screen must contain;
- which actions are required;
- what guidance users need;
- what errors and empty states must be handled;
- which UI features are MVP, stretch, or future scope.




