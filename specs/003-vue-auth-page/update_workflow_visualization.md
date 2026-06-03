# SDD Workflow Diagram — ResumAIner Project

**Last updated**: 2026-06-03
**Project state**: 3 features completed (68 tasks total)

```mermaid
flowchart TD
    %% ===== Core SDD Lifecycle =====
    A["🏛️ Constitution"] -->|defines governance| B["📋 Specify"]
    B -->|generates| B1["📄 spec.md"]
    B -->|clarify + refine| C["📐 Plan"]
    C -->|generates| C1["📄 plan.md<br/>research.md<br/>data-model.md"]
    C --> D["📝 Tasks"]
    D -->|generates| D1["📄 tasks.md"]
    D --> E["🔨 Implement"]
    
    %% Per-feature implementation
    E -->|Phase 1: Setup| E1["☕ Backend infra<br/>(pom.xml, Docker, DB)"]
    E1 -->|Phase 2: Foundation| E2["🗄️ Flyway + Model + DAO<br/>(26 TDD tests)"]
    E2 -->|Phase 3: Registration| E3["🔐 AuthService<br/>(BCrypt, transaction)"]
    E3 -->|Phase 4: Login| E4["⏱️ Rate limiting<br/>(5 fails → 15min lock)"]
    E4 -->|Phase 5: Cross-cut| E5["🛡️ Interceptor + CSRF<br/>+ WebConfig + ExHandler"]
    E5 -->|Phase 6: Router| F1["🌐 Vue Router guards<br/>+ authService + useAuth"]
    F1 -->|Phase 7: Pages| F2["📱 AuthPage + HomePages<br/>(PrimeVue + animations)"]
    F2 -->|Phase 8: Forms| F3["📝 LoginForm + RegisterForm<br/>(PrimeVue + Zod)"]
    F3 -->|Phase 9: Docker| F4["🐳 Docker Compose<br/>(Nginx + Tomcat + PG)"]
    F4 -->|Phase 10: Polish| F5["🧹 Security review<br/>+ JaCoCo + cleanup"]

    %% Verification
    F5 --> G["✅ Verify"]
    G -->|mvn test| G1["🧪 74 unit tests<br/>(back-end)"]
    G -->|npm run build| G2["⚡ SPA build<br/>(front-end)"]
    G -->|docker compose up| G3["🐳 Integration test<br/>(3 containers)"]
    G -->|Playwright| G4["🎭 Manual UI test<br/>(i18n + flows)"]

    %% Review loop
    G -->|issues found| H["🔄 Refine"]
    H -->|updates spec| B
    H -->|updates plan| C
    H -->|fixes code| E

    %% Completion
    G -->|all pass| I["✅ Feature Complete"]
    I -->|commit + push| J["📤 Branch ready<br/>for PR"]

    %% ===== Git Flow =====
    K["🌿 Feature Branch"] --> A
    K -.->|merge to| L["⬆️ main branch"]

    %% ===== Styles =====
    style A fill:#4CAF50,color:#fff,stroke:#333
    style B fill:#4CAF50,color:#fff,stroke:#333
    style C fill:#4CAF50,color:#fff,stroke:#333
    style D fill:#4CAF50,color:#fff,stroke:#333
    style E fill:#4CAF50,color:#fff,stroke:#333
    style G fill:#FFC107,color:#000,stroke:#333
    style H fill:#FF9800,color:#fff,stroke:#333
    style I fill:#4CAF50,color:#fff,stroke:#333
    style J fill:#2196F3,color:#fff,stroke:#333

    %% Phase styles
    style E1 fill:#e8f5e9,stroke:#4CAF50
    style E2 fill:#e8f5e9,stroke:#4CAF50
    style E3 fill:#e8f5e9,stroke:#4CAF50
    style E4 fill:#e8f5e9,stroke:#4CAF50
    style E5 fill:#e8f5e9,stroke:#4CAF50
    style F1 fill:#e8f5e9,stroke:#4CAF50
    style F2 fill:#e8f5e9,stroke:#4CAF50
    style F3 fill:#e8f5e9,stroke:#4CAF50
    style F4 fill:#e8f5e9,stroke:#4CAF50
    style F5 fill:#e8f5e9,stroke:#4CAF50

    %% Testing styles
    style G1 fill:#FFF8E1,stroke:#FFC107
    style G2 fill:#FFF8E1,stroke:#FFC107
    style G3 fill:#FFF8E1,stroke:#FFC107
    style G4 fill:#FFF8E1,stroke:#FFC107
```

---

## Legend

| Color | Meaning | Count |
|-------|---------|-------|
| 🟢 **Green** (solid) | Phase completed | All SDD phases |
| 🟢 **Green** (light) | Implementation done | 10/10 phases |
| 🟡 **Yellow** (light) | Verification in progress | 4 test types |
| 🟠 **Orange** | Feedback/refinement | Active loop |
| 🔵 **Blue** | Ready for next step | Branch ready |

## Project Snapshot

| Metric | Value |
|--------|-------|
| **Active branch** | `feat/003-vue-auth-page` |
| **Features completed** | 3 (001 ✅, 002 ✅, 003 ✅) |
| **Total tasks** | 68 (22 + 27 + 63, all complete) |
| **Backend tests** | 74 passing |
| **Frontend build** | ✅ Built |
| **Docker** | 3 containers (Nginx + Tomcat + PG) |
| **Current position** | Feature 003 ready for PR → `main` |

## Phase Breakdown (Feature 003)

```
Phase 1-2:    Backend infrastructure → Flyway + Models + DAO + TDD
Phase 3-4:    Registration + Login with BCrypt + rate limiting
Phase 5:      AuthInterceptor + CsrfFilter + ExceptionHandler
Phase 6-8:    Vue SPA — router, AuthPage, PrimeVue forms, i18n
Phase 9:      Docker Compose — Nginx → Tomcat → PostgreSQL
Phase 10:     JaCoCo + PasswordValidator + security review
Manual test:  6 bugs found and fixed via Playwright + i18n audit
```
