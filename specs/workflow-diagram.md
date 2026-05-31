# SDD Workflow Diagram

**Generated**: 2026-05-31
**Project**: ResumAIner — Java + Vue web app

```mermaid
flowchart TD
    %% Phases
    A["🏛️ Constitution"] -->|defines governance| B["📋 Specify"]
    B -->|generates| B1["spec.md<br/>✅ 001, ✅ 002"]
    B1 -->|review gate| B2["🎨 Wireframes<br/>(extension)"]
    B2 -->|approved| C["📐 Plan"]
    C -->|generates| C1["plan.md<br/>✅ 001, ✅ 002"]
    C1 --> C2["📊 Component Diagram<br/>(extension)"]
    C2 -->|next phase| D["📝 Tasks"]
    D -->|generates| D1["tasks.md<br/>✅ 001 (21/21), ✅ 002 (28/28)"]
    D1 --> D2["📈 Task DAG<br/>(extension)"]
    D2 -->|next phase| E["🔨 Implement"]
    E -->|builds from| D1
    E -->|results| E1["Java classes<br/>CSS · Templates<br/>i18n · Tests"]
    E1 --> F["✅ Verify"]
    F -->|Playwright checks| F1["Browser test<br/>Build passes"]
    F1 -->|approved| G["🔬 Learn"]
    G -->|creates| G1["learn.md<br/>✅ 002"]
    G1 --> H["📦 PR → Merge"]

    %% Feedback loops
    E1 -->|issues found| I["🔄 Security Review<br/>(extension)"]
    I -->|critical fixes| E
    I -->|pass| F

    F1 -->|spec mismatch| J["🔄 Refine<br/>(extension)"]
    J -->|updates spec| B1
    J -->|propagates to plan| C1
    J -->|propagates to tasks| D1

    E -->|CRLF issues| K["💾 Memory Capture<br/>(extension)"]
    K -->|durable lessons| L["BUGS.md · DECISIONS.md<br/>WORKLOG.md · INDEX.md"]
    L -->|prevent recurrence| E

    %% Extensions
    M["⚙️ Extensions (13)"] -.-x A
    M -.-x B
    M -.-x C
    M -.-x D
    M -.-x E
    M -.-x F
    M -.-x I
    M -.-x J

    %% Styles — Feature 002 complete
    style A fill:#4CAF50,color:#fff
    style B fill:#4CAF50,color:#fff
    style B1 fill:#4CAF50,color:#fff
    style C fill:#4CAF50,color:#fff
    style C1 fill:#4CAF50,color:#fff
    style D fill:#4CAF50,color:#fff
    style D1 fill:#4CAF50,color:#fff
    style E fill:#4CAF50,color:#fff
    style E1 fill:#4CAF50,color:#fff
    style F fill:#4CAF50,color:#fff
    style F1 fill:#4CAF50,color:#fff
    style G fill:#2196F3,color:#fff
    style G1 fill:#2196F3,color:#fff
    style H fill:#FFC107,color:#000
    style I fill:#FF9800,color:#fff
    style J fill:#FF9800,color:#fff
    style K fill:#9C27B0,color:#fff
    style L fill:#9C27B0,color:#fff
    style M fill:#607D8B,color:#fff
```

## Legend

| Color | Meaning |
|---|---|
| 🟢 Green | Phase completed (both features) |
| 🔵 Blue | Learning phase done |
| 🟡 Yellow | Ready — next action |
| 🟠 Orange | Feedback loop / review |
| 🟣 Purple | Durable memory capture |
| ⚪ Gray-blue | Extension infrastructure |

## Project State

| Metric | Value |
|---|---|
| **Features** | 2 (001 shipped ✅, 002 complete 🎯) |
| **Tasks total** | 49 (21 + 28) |
| **Tests** | 3 (MockMvc, all pass) |
| **Extensions** | 13 installed |
| **Durable memory** | 11 entries (A1, B1-B5, D1-D6, W1-W2) |
| **Current branch** | `feat/002-thymeleaf-landing-page` |
| **Next action** | Create PR → merge to `main` |

## Workflow Summary

```mermaid
pie title Feature 002 — 28/28 Tasks Complete
    "Completed" : 28
```
