# Task Dependency Graph: Feature 008 — PDF/HTML Resume Export

```mermaid
flowchart TB
    subgraph WAVE0["Wave 0 — Context Loading"]
        P0["Phase 0: Context & Spike Inspection\nT001-T006 (6 tasks)"]
    end

    subgraph WAVE1["Wave 1 — PG1 Bullet Foundation (parallel start)"]
        P1["Phase 1: Schema + Bullet Persistence\nT007-T015 (9 tasks)"]
        P2["Phase 2: Prompt Builder + Mock AI\nT016-T021 (6 tasks) [P]"]
    end

    subgraph WAVE2["Wave 2 — PG1 Parser & Review"]
        P3["Phase 3: Parser, Validator, Persistence\nT022-T029 (8 tasks)"]
    end

    subgraph WAVE3["Wave 3 — PG1 Frontend Bullet Editing"]
        P4["Phase 4: Review API + Frontend\nT030-T042 (13 tasks)"]
    end

    subgraph WAVE4["Wave 4 — PG2 Foundation (parallel start)"]
        P5["Phase 5: Dependencies, DB Config, Metadata\nT043-T053 (11 tasks)"]
    end

    subgraph WAVE5["Wave 5 — Spike Model Porting"]
        P6["Phase 6: Port Spike Model Classes\nT054-T056 (3 tasks)"]
    end

    subgraph WAVE6["Wave 6 — Core PDF Engine Porting"]
        P7["Phase 7: Port Core PDF Classes (9)\nT057-T070 (14 tasks)"]
    end

    subgraph WAVE7["Wave 7 — Render Data + Page Plan"]
        P8["Phase 8: RenderData Adapter + PagePlan\nT071-T078 (8 tasks)"]
    end

    subgraph WAVE8["Wave 8 — Rendering"]
        P9["Phase 9: XHTML Renderer + Parity\nT079-T087 (9 tasks)"]
    end

    subgraph WAVE9["Wave 9 — Fitting + Validation"]
        P10["Phase 10: FeedbackFitEngine\nT088-T095 (8 tasks)"]
        P14["Phase 14: HTML Escaping\nT122-T126 (5 tasks) [P]"]
    end

    subgraph WAVE10["Wave 10 — Finalization"]
        P11["Phase 11: Finalization Integration\nT096-T104 (9 tasks)"]
    end

    subgraph WAVE11["Wave 11 — Endpoints + Frontend (parallel)"]
        P12["Phase 12: Download Endpoints\nT105-T113 (9 tasks)"]
        P13["Phase 13: Frontend Export\nT114-T121 (8 tasks) [P]"]
    end

    subgraph WAVE12["Wave 12 — Diagnostics + E2E"]
        P15["Phase 15: Logging + Manual Smoke\nT127-T134 (8 tasks)"]
        P16["Phase 16: E2E Regression + Coverage\nT135-T145 (11 tasks)"]
    end

    subgraph WAVE13["Wave 13 — Documentation"]
        P17["Phase 17: Docs + Handoff\nT146-T152 (7 tasks)"]
    end

    %% Phase 0 → PG1
    P0 --> P1
    P0 --> P2

    %% PG1 chain
    P1 --> P3
    P2 --> P3
    P3 --> P4

    %% PG1 → PG2 (Phase Group 1 must complete before PG2 finalization)
    P4 --> P11

    %% PG2 chain
    P0 --> P5
    P5 --> P6
    P6 --> P7
    P7 --> P8
    P8 --> P9
    P9 --> P10
    P9 --> P14
    P10 --> P11
    P14 --> P11
    P11 --> P12
    P11 --> P13
    P12 --> P15
    P13 --> P15
    P15 --> P16
    P16 --> P17

    %% Styles
    style P0 fill:#FFC107,color:#000,stroke:#F57F17
    style P1 fill:#9E9E9E,color:#fff,stroke:#616161
    style P2 fill:#9E9E9E,color:#fff,stroke:#616161
    style P3 fill:#9E9E9E,color:#fff,stroke:#616161
    style P4 fill:#9E9E9E,color:#fff,stroke:#616161
    style P5 fill:#9E9E9E,color:#fff,stroke:#616161
    style P6 fill:#9E9E9E,color:#fff,stroke:#616161
    style P7 fill:#9E9E9E,color:#fff,stroke:#616161
    style P8 fill:#9E9E9E,color:#fff,stroke:#616161
    style P9 fill:#9E9E9E,color:#fff,stroke:#616161
    style P10 fill:#9E9E9E,color:#fff,stroke:#616161
    style P11 fill:#9E9E9E,color:#fff,stroke:#616161
    style P12 fill:#9E9E9E,color:#fff,stroke:#616161
    style P13 fill:#9E9E9E,color:#fff,stroke:#616161
    style P14 fill:#9E9E9E,color:#fff,stroke:#616161
    style P15 fill:#9E9E9E,color:#fff,stroke:#616161
    style P16 fill:#9E9E9E,color:#fff,stroke:#616161
    style P17 fill:#9E9E9E,color:#fff,stroke:#616161
```

## Legend

- 🟡 **Yellow** — Ready to start (no unresolved dependencies)
- ⚪ **Gray** — Blocked (waiting on preceding phases)

## Critical Path

```
Phase 0 → Phase 1 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7
→ Phase 8 → Phase 9 → Phase 10 → Phase 11 → Phase 12 → Phase 15
→ Phase 16 → Phase 17
```

**15 phases, ~152 tasks** — the longest chain from context loading to documentation handoff. Phase 0 is the only unblocked phase.

## Parallel Execution Windows

| Window | Phases | What runs together |
|---|---|---|
| **Wave 1** | P1 + P2 | Schema migration + Prompt builder (independent) |
| **Wave 9** | P10 + P14 | Fit engine + HTML escaping (independent — different packages) |
| **Wave 11** | P12 + P13 | Backend endpoints + Frontend export (contract-stable) |
| **Wave 13** | P17 (all [P]) | All 7 documentation tasks parallel |

## Phase Group Dependency Rule

```
PG1 (Phases 1-4) ──→ PG2 Finalization (Phase 11)
                        ↑
PG2 (Phases 5-10, 14) ──┘
```

Phase Group 1 (bullets) MUST complete before PG2 Finalization wiring, because PDF rendering must consume finalized structured bullets. However, PG2 foundation phases (5-10, 14) can start as soon as Phase 0 completes — they are independent of PG1.

## Statistics

| Metric | Value |
|---|---|
| **Total tasks** | 152 |
| **Completed** | 0 (0%) |
| **Ready to start** | 6 (Phase 0) |
| **Blocked** | 146 |
| **Execution waves** | 14 |
| **Phases** | 18 (0–17) |
| **Critical path length** | 15 phases |
| **Parallel phases** | 3 pairs (P1∥P2, P10∥P14, P12∥P13) |
