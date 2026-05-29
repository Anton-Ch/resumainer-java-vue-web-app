# Governance Plan for ResumAIner

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-10
**Last Updated:** 2026-05-21
**Chapter:** 3.3 Governance
**Author:** Anton
**Version:** 1.0
**Status:** Approved

***

### 1. Governance Objective

The primary goal of this Governance Plan is to establish formal, auditable, and transparent rules for decision-making, change management, and scope control. This is crucial to maintain stability while allowing the necessary flexibility of the Hybrid approach.

### 2. Roles and Authority Matrix (RACI)

The following roles define the authority structure for the project:

| Role                          | Decision Making Authority                          | Responsibility                                       | Accountability                                         |
| :---------------------------- | :------------------------------------------------- | :--------------------------------------------------- | :----------------------------------------------------- |
| **BA**                        | **Consulted** (Proposes solutions, flags risks).   | Eliciting, documenting, and proposing solutions.     | Ensuring documentation fidelity and process adherence. |
| **Mentor (Technical Lead)**   | **Approver** (Final technical sign-off).           | Reviewing all architecture and implementation plans. | Technical compliance and quality assurance.            |
| **Sponsor**                   | **Approver** (Final business sign-off).            | Funding, overall business mandate, risk acceptance.  | Project viability and ultimate business success.       |


*Note: All major changes must pass through the **Mentor** for technical approval and the **Sponsor** for business approval.*

### 3. Change Management Process (Change Request - CR)

Since the system is critical and uses a Hybrid model, all changes must be controlled.

1.  **Submission:** A stakeholder (BA, or Mentor) submits a Change Request (CR).
2.  **Impact Assessment:** BA documents the full scope of the change (inputs, required logic, affected modules).
3.  **Review & Analysis:** Mentor performs technical feasibility review; BA assesses impact on requirements and other modules.
4.  **Resolution:** The CR is presented to a **Change Approval Board (CAB)**.
5.  **Change Approval Board (CAB):** Consists of BA, Mentor, and Sponsor. The Sponsor must approve any change that impacts core business value, and the Mentor must approve any change that impacts the technical stack/architecture.
6.  **Implementation:** Only after approval the change can be introduced into the development cycle.

#### 3.1 Change Classification  
  
To keep the process lightweight, all changes are classified into three types:  
  
| Change Type                          | Description                                                                                                     | Approval Needed                                  |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------ |
| **Minor** documentation change       | Typos, formatting, wording clarification, repository path correction                                            | BA approval only                                 |
| **Requirement** change               | New, removed, or significantly modified FR/NFR, user flow, or data entity                                       | BA + Mentor review                               |
| **Scope** or **architecture** change | Any change affecting MVP scope, mandatory capstone stack, database structure, deployment model, or security model | BA + Mentor approval; Sponsor informed if needed |

#### 3.2 Decision Log  
  
All major decisions must be recorded in a Decision Log.  
  
Recommended location:    
`docs/09_decisions/decision_log.md`  
  
Each decision should include:  
- decision ID;  
- date;  
- decision title;  
- context;  
- selected option;  
- rejected alternatives;  
- rationale;  
- impact on scope, requirements, data model, or implementation.

##### Example of decision log table

| ID      | Date       | Title                         | Rationale                                             | Impact                                          | Status   |
| :------ | :--------- | :---------------------------- | :---------------------------------------------------- | :---------------------------------------------- | :------- |
| DEC-001 | 2026-05-10 | Use plain JDBC instead of ORM | Mandatory capstone requirement; no Hibernate allowed. | Affects DAO layer and Data Access architecture. | Approved |
| DEC-002 | 2026-05-10 | Decision Title                | Brief rationale here.                                 | Scope/Data Model/Impl impact.                   | Draft    |

#### 3.3 Change Request Log  
  
All non-trivial changes should be recorded in a Change Request Log.  
  
Recommended location:  
`docs/07_project-management/change_request_log.md`  
  
Each change request should include:  
- CR ID;  
- date;  
- requester;  
- description;  
- reason;  
- affected artifacts;  
- impact assessment;  
- decision;  
- status.

#### 3.4 De-scoping Rule  
  
If a feature is valuable but too complex for the MVP, it should not be deleted from the project vision. It should be moved to one of the following categories:  
- MVP Stretch Goal  
- Post-MVP  
- Future Scope  
  
This rule protects the MVP from *scope creep* while preserving useful ideas for future versions.

### 4. Conflict Resolution

When conflicts arise (e.g., business need vs. technical feasibility), the following escalation path must be followed:
1.  **BA:** Facilitates discussion, presents options with clear pros/cons, and documents the conflict.
2.  **Mentor:** Assesses the technical risk associated with each option.
3.  **Sponsor:** Makes the final, binding business decision, accepting the associated risk profile.

### 5. Governance Principles

*   **Traceability:** Every requirement (FR) must be linked back to a validated business need or a regulatory constraint.
*   **Single Source of Truth:** All requirements and decisions are logged in the project documentation repository.
*   **Non-Negotiables:** The core technical constraints (JDBC, Spring MVC, PostgreSQL, etc.) are non-negotiable and must guide all architectural decisions.


### 6. Requirements Change Control Workflow

This section defines the add/update/change sequence for project logs, matrices, and related BA artifacts when requirements are added, changed, or removed.

The goal is to keep documentation consistent without creating unnecessary bureaucracy.

#### 6.1 Artifacts Used in Requirement Change Control

| Artifact                         | Location                                           | Purpose                                                                                  |
| -------------------------------- | -------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Requirements Log                 | `docs/02_requirements/requirements_log.md`         | Main source of truth for requirements                                                    |
| Change Request Log               | `docs/07_project-management/change_request_log.md` | Tracks meaningful changes to scope, requirements, architecture, UI/UX, data, or security |
| Decision Log                     | `docs/09_decisions/decision_log.md`                | Records major approved decisions and rationale                                           |
| Open Questions Log               | `docs/07_project-management/open_questions_log.md` | Tracks unresolved questions before decisions are made                                    |
| Requirements Traceability Matrix | `docs/08_traceability/traceability_matrix.md`      | Links requirements to objectives, workflows, UI, data, services, and tests               |
| Risk Register                    | `docs/07_project-management/risk_register.md`      | Tracks risks created, changed, reduced, or closed by requirement changes                 |


#### 6.2 General Update Rule

**Requirements Log** is the primary *source of truth* for requirement content.

**Traceability Matrix** does not define requirements. It *links* approved or candidate requirements to other artifacts.

**Decision Log** *explains why* major choices were made.

**Change Request Log** explains *what changed* and why.

**Open Questions Log** *tracks uncertainty* until it becomes an answer, decision, or change request.

**Risk Register** tracks *project risks* created or affected by the change.

#### 6.3 Scenario A: Adding a New Requirement

Use this workflow when a **new requirement**, page, field, feature, validation rule, or constraint is added.

##### Step-by-step process
1. **Capture the need.**
   - If the need is unclear, add it to the *Open Questions Log*.
   - If the need is clear and affects scope or approved artifacts, add a *Change Request*.
2. **Assess impact.**
   - Check *affected scope*, UI, data model, services, tests, risks, and timeline.
3. **Record decision if needed.**
   - Add a *Decision Log* entry if the requirement affects MVP scope, architecture, security, data model, or public behavior.
4. **Update Requirements Log.**
   - Add a new *requirement ID*.
   - Define type, source, priority, scope, status, acceptance criteria, affected UI, affected data, and readiness check.
5. **Update supporting artifacts.**
   - Update Wireframe Field Requirements if fields, validation, or UI forms are affected.
   - *Update* relevant elicitation/design *artifacts* if the new requirement changes confirmed scope.
1. **Update Traceability Matrix.**
   - Add a *new trace* row linking business objective, requirement, workflow, UI screen, data entity, service/component, and test case.
1. **Update Risk Register.**
   - Add or update *risks* if the new requirement increases scope, technical, security, data, UX, or schedule risk.
1. **Close related OQ/CR.**
   - Mark the *Open Question* as Answered/Closed or Converted.
   - Mark the *Change Request* as Implemented once artifacts are updated.

##### Add a Requirement Workflow

~~~mermaid
flowchart TD
    A[New need identified] --> B{Is it clear?}
    B -- No --> C[Add / update Open Questions Log]
    C --> D[Clarify answer]
    B -- Yes --> E{Affects approved scope/artifacts?}
    D --> E
    E -- Yes --> F[Create Change Request]
    E -- No --> G[Add draft requirement]
    F --> H[Assess impact]
    G --> H
    H --> I{Major decision needed?}
    I -- Yes --> J[Update Decision Log]
    I -- No --> K[Update Requirements Log]
    J --> K
    K --> L[Update Wireframe / Design artifacts if affected]
    L --> M[Update Traceability Matrix]
    M --> N[Update Risk Register]
    N --> O[Close related OQ / CR]
~~~

#### 6.4 Scenario B: Removing a Requirement

Use this workflow when an existing requirement is **removed** from MVP, moved to later scope, or rejected.

##### Step-by-step process

1. **Create a Change Request.**
   - Removing an existing requirement is always a meaningful *change*.
2. **Define removal reason.**
   - Typical *reasons*: out of scope, too complex for MVP, duplicate, low value, security concern, technical constraint, or replaced by another requirement.
2. **Decide removal type.**
   - Remove from MVP and move to MVP Stretch.
   - Move to Post-MVP.
   - Move to Future Scope.
   - Reject as Not Needed.
   - Supersede by another requirement.
4. **Record decision if needed.**
   - Add *Decision Log entry* if removal affects MVP value, architecture, security, public behavior, or data model.
5. **Update Requirements Log.**
   - Do not delete the requirement row.
   - Change scope/status to the selected outcome.
   - Add removal reason in Details.
6. **Update Traceability Matrix.**
   - Mark related trace rows as Superseded, Removed, or Not Applicable.
   - Link to replacement requirement if one exists.
7. **Update supporting artifacts.**
   - Remove or mark affected UI fields, wireframe blocks, flows, or data fields as postponed/out of scope.
8. **Update Risk Register.**
   - Close or reduce risks if removal lowers scope or technical risk.
   - Add risk if removal creates product value or demo risk.
9. **Close CR/OQ.**
   - Mark the Change Request as Implemented.
   - Close related Open Questions if answered.

##### Remove a Requirement Workflow

~~~mermaid
flowchart TD
    A[Requirement removal proposed] --> B[Create Change Request]
    B --> C[Define removal reason]
    C --> D{Removal type}
    D --> D1[Move to MVP Stretch]
    D --> D2[Move to Post-MVP]
    D --> D3[Move to Future Scope]
    D --> D4[Reject / Not Needed]
    D --> D5[Supersede by another requirement]
    D1 --> E{Major decision needed?}
    D2 --> E
    D3 --> E
    D4 --> E
    D5 --> E
    E -- Yes --> F[Update Decision Log]
    E -- No --> G[Update Requirements Log]
    F --> G
    G --> H[Update Traceability Matrix]
    H --> I[Update UI / Data / Wireframe artifacts]
    I --> J[Update Risk Register]
    J --> K[Close related CR / OQ]
~~~

#### 6.5 Scenario C: Changing an Existing Requirement

Use this workflow when an existing requirement changes in behavior, scope, validation, UI, data, security, or implementation impact.

##### Step-by-step process

1. **Create a Change Request.**
   - Any non-trivial change to an existing requirement must be tracked.
2. **Clarify the change.**
   - If details are unclear, add or update an Open Question.
3. **Assess impact.**
   - Check affected FR/NFR, UI, data entity, service/component, test case, risk, timeline, and MVP scope.
4. **Record decision if needed.**
   - Add Decision Log entry if the change affects scope, architecture, data model, security, public behavior, or major UX flow.
5. **Update Requirements Log.**
   - Keep the same requirement ID if the core intent remains the same.
   - Create a new requirement ID if the change creates a different requirement.
   - Update description, acceptance criteria, affected UI/data, and readiness check.
6. **Update supporting artifacts.**
   - Update wireframe field requirements, UI requirements, user flows, or data model notes if affected.
7. **Update Traceability Matrix.**
   - Update affected trace row.
   - Add new trace row if the changed requirement creates a new workflow, UI screen, data entity, service, or test case.
8. **Update Risk Register.**
   - Add, update, mitigate, or close risks affected by the change.
9. **Close related OQ/CR.**
   - Close or convert related Open Questions.
   - Mark Change Request as Implemented after all affected artifacts are updated.

##### Change an Existing Requirement Workflow

~~~mermaid
flowchart TD
    A[Requirement change proposed] --> B[Create Change Request]
    B --> C{Is the change clear?}
    C -- No --> D[Add / update Open Questions Log]
    D --> E[Clarify answer]
    C -- Yes --> F[Assess impact]
    E --> F
    F --> G{Major decision needed?}
    G -- Yes --> H[Update Decision Log]
    G -- No --> I[Update Requirements Log]
    H --> I
    I --> J{Same requirement intent?}
    J -- Yes --> K[Keep same requirement ID]
    J -- No --> L[Create new requirement ID]
    K --> M[Update supporting artifacts]
    L --> M
    M --> N[Update Traceability Matrix]
    N --> O[Update Risk Register]
    O --> P[Close related OQ / CR]
~~~

#### 6.6 Minimal Documentation Update Checklist

Before closing a Change Request, verify the following:

| Check | Required? |
|---|---|
| Requirements Log updated | Always |
| Change Request Log updated | Always for non-trivial changes |
| Decision Log updated | If major decision was made |
| Open Questions Log updated | If uncertainty existed |
| Traceability Matrix updated | If requirement is added, removed, or changed |
| Risk Register updated | If risk level changed or new risk appeared |
| Wireframe Field Requirements updated | If UI fields, validation, or page blocks changed |
| Related UI/data/design artifacts updated | If affected |

#### 6.7 Practical Rule

Do not create extra documents for small changes.

Use the smallest update set that keeps the repository consistent:
- Requirements Log for requirement content;
- Change Request Log for meaningful changes;
- Decision Log for major decisions;
- Open Questions Log for unresolved questions;
- Traceability Matrix for requirement links;
- Risk Register for risks;
- Wireframe Field Requirements for UI fields and validation.
