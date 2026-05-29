# Stakeholder Engagement Plan for ResumAIner AI Resume Alignment System

**Project ID:** `resumainer`
**Date:** 2026-05-10
**Chapter:** 3.2 Stakeholder Planning
**Author:** Anton
**Version:** 1.0
**Status:** Approved

***

### 1. Stakeholder Identification and Analysis

*Based on project inputs and BABOK v3 methodology.*

| Stakeholder Group/Role             | Influence (Power) | Interest | Attitude | Primary Goal                                                                        | Risk/Concern                                                    |
| :--------------------------------- | :---------------- | :------- | :------- | :---------------------------------------------------------------------------------- | :-------------------------------------------------------------- |
| **BA**                             | High              | High     | Champion | Deliver a comprehensive, portfolio-ready solution.                                  | Scope creep, technical debt.                                    |
| **Mentor (Technical Lead)**        | High              | High     | Champion | Technical soundness, adherence to rigid stack (JDBC/Spring MVC), High code quality. | Architectural shortcuts, failure to meet technical constraints. |
| **Sponsor (Capstone Coordinator)** | High              | Medium   | Champion | Successful completion of the project; demonstrable business value.                  | Delay, failure to meet business objectives.                     |
| **Development Team**               | Medium            | Medium   | Neutral  | Implementable, maintainable, and testable code.                                     | Overly complex requirements, difficult to test areas.           |
| **End-Users (Candidates/HR)**      | Low               | High     | Champion | Ease of use; fast, accurate, and unintrusive experience.                            | Technical friction, confusing UI/UX.                            |

#### 2. Engagement Strategy (Communication Plan)

We use the Power/Interest Grid to manage expectations and maximize buy-in.

| Quadrant                            | Stakeholders         | Strategy                                                                                                     | Trigger Points                                                                    |
| :---------------------------------- | :------------------- | :----------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------- |
| **Manage Closely** (High P, High I) | BA, Mentor           | **Involve/Consult.** Keep them involved in all decision-making, but use structured decision gates.           | Any decision impacting scope, governance, or core architecture.                   |
| **Keep Satisfied** (High P, Med I)  | Capstone Coordinator | **Inform.** Keep them updated on high-level milestones, but avoid involving them in daily technical debates. | Project Milestone completion, or critical risk mitigation.                        |
| **Keep Informed** (Low P, High I)   | End-Users            | **Communicate/Demo.** Continuous feedback loops via usability testing and prototypes.                        | Prototype completion, User Acceptance Testing (UAT), or specific feature rollout. |
| **Monitor** (Low P, Low I)          | Development Team     | **Monitor.** Ensure they have clear requirements, but avoid over-communication.                              | When a design choice impacts their implementation path.                           |

#### 3. Communication Protocols

| Protocol | Action | Frequency | Responsible Party |
| :--- | :--- | :--- | :--- |
| **Decision Gate Review** | Formal review of proposed architectural changes or scope increases. | On Demand (only when required by the plan). | Mentor/Sponsor. |
| **Progress Checkpoint** | Demonstration of completed features (vertical slice). | Weekly/Bi-weekly. | BA. |
| **Feedback Loop** | Usability testing and requirement gathering. | Continuous (during adaptive cycles). | BA. |

***
*This document is the foundation for all future requirement elicitation and forms a key deliverable for the portfolio.*
