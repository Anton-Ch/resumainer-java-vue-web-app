# Task Dependency Graph: Hello World Tomcat Setup

```mermaid
flowchart LR
    subgraph W0["Wave 0 — Setup"]
        T001["T001: Create dirs"]
        T002["T002: Maven Wrapper"]
        T004["T004: .gitignore"]
    end

    subgraph W1["Wave 1 — POM"]
        T003["T003: pom.xml deps"]
    end

    subgraph W2["Wave 2 — App Init"]
        T005["T005: AppInitializer"]
        T006["T006: WebConfig"]
    end

    subgraph W3["Wave 3 — App Code"]
        T007["T007: Controller"]
        T008["T008: hello.jsp"]
        T009["T009: properties"]
    end

    subgraph W4["Wave 4 — Build Gate"]
        BUILD["mvnw clean package"]
    end

    subgraph W5["Wave 5 — Docker Setup"]
        T010["T010: Dockerfile"]
        T011["T011: docker-compose.yml"]
        T011b["T011b: .env.example"]
    end

    subgraph W6["Wave 6 — Startup Script"]
        T012["T012: wait-for-it.sh"]
    end

    subgraph W7["Wave 7 — MVP Gate 🎯"]
        T013["T013: docker compose up ✓"]
    end

    subgraph W89["Waves 8-10 — US2+US3"]
        T014["T014: mvnw verify"]
        T015["T015: Manual Tomcat deploy"]
        T016["T016: docker build check"]
        T017["T017: Container graceful stop"]
    end

    subgraph W11["Wave 11 — US4"]
        T018["T018: prod profile"]
        T019["T019: port override"]
    end

    subgraph W12["Wave 12 — Unit Tests"]
        T020["T020: Controller test"]
    end

    subgraph W13["Wave 13 — Final Gate"]
        T021["T021: Full verification"]
    end

    %% Wave 0 → Wave 1
    T001 --> T003
    T002 --> T003

    %% Wave 1 → Wave 2
    T003 --> T005
    T003 --> T006

    %% Wave 2 → Wave 3
    T005 --> T007
    T005 --> T008
    T006 --> T007
    T006 --> T008
    T005 --> T009
    T006 --> T009

    %% Wave 3 → Wave 4
    T007 --> BUILD
    T008 --> BUILD
    T009 --> BUILD

    %% Wave 3 → Wave 5
    T009 --> T010
    T009 --> T011
    T009 --> T011b

    %% Wave 5 → Wave 6
    T010 --> T012

    %% Waves 5-6 → Wave 7
    T010 ---> T013
    T011 ---> T013
    T012 ---> T013
    T005 ---> T013
    T006 ---> T013
    T007 ---> T013
    T008 ---> T013
    T009 ---> T013

    %% Wave 3+5 → Wave 8
    BUILD ---> T014
    T010 ---> T016
    T014 ---> T015
    T016 ---> T017

    %% Wave 3+5 → Wave 11
    T009 ---> T018
    T010 ---> T018
    T011 ---> T018
    T009 ---> T019
    T010 ---> T019
    T011 ---> T019

    %% Wave 2 → Wave 12
    T005 ---> T020
    T006 ---> T020
    T007 ---> T020
    T008 ---> T020

    %% All → Wave 13
    T013 ---> T021
    T015 ---> T021
    T017 ---> T021
    T018 ---> T021
    T019 ---> T021
    T020 ---> T021

    %% Styles
    style T001 fill:#9E9E9E,color:#fff
    style T002 fill:#9E9E9E,color:#fff
    style T004 fill:#9E9E9E,color:#fff
    style T003 fill:#9E9E9E,color:#fff
    style T005 fill:#9E9E9E,color:#fff
    style T006 fill:#9E9E9E,color:#fff
    style T007 fill:#9E9E9E,color:#fff
    style T008 fill:#9E9E9E,color:#fff
    style T009 fill:#9E9E9E,color:#fff
    style T010 fill:#9E9E9E,color:#fff
    style T011 fill:#9E9E9E,color:#fff
    style T011b fill:#9E9E9E,color:#fff
    style T012 fill:#9E9E9E,color:#fff
    style T013 fill:#FFC107,color:#000
    style T014 fill:#9E9E9E,color:#fff
    style T015 fill:#9E9E9E,color:#fff
    style T016 fill:#9E9E9E,color:#fff
    style T017 fill:#9E9E9E,color:#fff
    style T018 fill:#9E9E9E,color:#fff
    style T019 fill:#9E9E9E,color:#fff
    style T020 fill:#9E9E9E,color:#fff
    style T021 fill:#FFC107,color:#000
    style BUILD fill:#E0E0E0,color:#000
```

## Legend
- ⚪ Gray — Pending (not started)
- 🟡 Yellow — MVP gate / final gate (milestones)
- ⬜ White — Build step (automated)

## Critical Path
```
T001 → T003 → T005/T006 → T007/T008/T009 → BUILD → T010 → T012 → T013  (7 waves)
```

Longest chain determines minimum completion time: **7 sequential waves** to MVP.

## Statistics

| Метрика | Значение |
|---|---|
| **Total tasks** | 21 (+ 1 build gate) |
| **Completed** | 0 (0%) |
| **Ready to start** (Wave 0) | 3 (T001, T002, T004) |
| **Blocked** | 18 |
| **Execution waves** | 14 |
| **Waves to MVP** | 7 |
| **Parallel waves** | W0(3), W2(2), W3(3), W5(3), W8(2), W11(2) |

## Notes

- No circular dependencies detected ✅
- MVP reachable after Wave 7 (T013)
- Waves 8-13 can proceed independently after MVP milestone
- Generated from `specs/001-hello-world-tomcat/tasks.md`
