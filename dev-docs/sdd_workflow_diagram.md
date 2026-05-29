# Workflow Diagram

```mermaid
flowchart TD
    %% === PHASES ===
    A["🏛️ Constitution"] -->|defines governance| B["📋 Specify"]

    subgraph Specify_Phase[" "]
        B -->|generates| B1["📄 spec.md"]
        B1 --> C["📐 Plan"]
    end

    subgraph Plan_Phase[" "]
        C -->|generates| C1["📄 plan.md"]
        C1 --> D["📝 Tasks"]
    end

    subgraph Tasks_Phase[" "]
        D -->|generates| D1["📄 tasks.md"]
        D1 --> E["🔨 Implement"]
    end

    subgraph Implement_Phase[" "]
        E -->|builds| E1["📂 Source code"]
        E1 --> F["✅ Verify"]
    end

    subgraph Verify_Phase[" "]
        F -->|validates against| B1
    end

    %% === EXTENSION HOOKS ===
    B -.->|hooks:| H1["🔗 speckit.git.feature<br>🔗 speckit.branch-convention.validate"]
    C -.->|hooks:| H2["🔗 speckit.git.commit<br>🔗 speckit.wireframe.review<br>🔗 speckit.memory-md.plan-with-memory"]
    D -.->|hooks:| H3["🔗 speckit.git.commit<br>🔗 speckit.security-review.tasks<br>🔗 speckit.superpowers.tasks<br>🔗 speckit.diagram.dependencies"]
    E -.->|hooks:| H4["🔗 speckit.git.commit<br>🔗 speckit.superpowers.execute"]
    F -.->|hooks:| H5["🔗 speckit.git.commit<br>🔗 speckit.wireframe.screenshots<br>🔗 speckit.security-review.branch<br>🔗 speckit.superpowers.review<br>🔗 speckit.learn.review<br>🔗 speckit.memory-md.capture-from-diff<br>🔗 speckit.review.run"]

    %% === FEEDBACK LOOPS ===
    F -->|issues found| G["🔄 Refine"]
    G -.->|updates spec| B1
    G -.->|updates plan| C1
    G -.->|updates tasks| D1
    G -.->|resume| E

    %% === STYLING ===
    style A fill:#4CAF50,color:#fff,stroke:#333,stroke-width:2px
    style B fill:#FFC107,color:#000,stroke:#333,stroke-width:2px
    style C fill:#9E9E9E,color:#fff,stroke:#666
    style D fill:#9E9E9E,color:#fff,stroke:#666
    style E fill:#9E9E9E,color:#fff,stroke:#666
    style F fill:#9E9E9E,color:#fff,stroke:#666
    style G fill:#FF9800,color:#fff,stroke:#333

    style B1 fill:#E8F5E9,color:#333
    style C1 fill:#F5F5F5,color:#666
    style D1 fill:#F5F5F5,color:#666
    style E1 fill:#F5F5F5,color:#666

    style H1 fill:#E3F2FD,color:#333,stroke:#90CAF9
    style H2 fill:#E3F2FD,color:#333,stroke:#90CAF9
    style H3 fill:#E3F2FD,color:#333,stroke:#90CAF9
    style H4 fill:#E3F2FD,color:#333,stroke:#90CAF9
    style H5 fill:#E3F2FD,color:#333,stroke:#90CAF9
```

# Legend

| Color        | Status                  | Phase                                |
| ------------ | ----------------------- | ------------------------------------ |
| 🟢 Green     | **Completed**           | Constitution v1.0.0 ✅               |
| 🟡 Yellow    | **Current / Next**      | Specify — ready to start             |
| ⚪ Gray       | **Not Started**         | Plan, Tasks, Implement, Verify       |
| 🟠 Orange    | **Refinement Cycle**    | Refine (feedback loop)               |
| 🔵 Blue      | **Extension Hooks**     | Integration points for extensions    |

# Current Status

| Phase | Status | Artifacts |
|------|--------|-----------|
| **Constitution** | ✅ Completed | `.specify/memory/constitution.md` v1.0.0 |
| **Specify** | 🟡 **Ready to Start** | `/speckit.specify` |
| **Plan** | ⏸ Pending | will follow specify |
| **Tasks** | ⏸ Pending | will follow plan |
| **Implement** | ⏸ Pending | will follow tasks |
| **Verify** | ⏸ Pending | will follow implement |

**12 extensions** active: `branch-convention`, `diagram`, `doctor`, `git`, `learn`, `memory-md`, `refine`, `review`, `security-review`, `status`, `superpowers`, `wireframe`