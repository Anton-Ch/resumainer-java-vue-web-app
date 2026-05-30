# Technical Decisions (`docs/memory/`)

This file stores durable technical and implementation decisions. For governance-level decisions or project standards, see `.specify/memory/DECISIONS.md`.

## Entry Lifecycle

Each decision follows this lifecycle:

```
Active → Needs Review → Superseded → (pruned)
```

- **Active**: The decision is current and must be honored by all features and AI agents.
- **Needs Review**: Implementation reality or new context suggests this decision may be outdated. It should still be honored until reviewed and explicitly changed.
- **Superseded**: A newer decision has replaced this one. Keep it for historical context until the next audit, then consider pruning.
- **Pruned**: During an audit, remove superseded entries that no longer provide historical value. This keeps the file focused.

### When to change status

| Current Status | Change To    | When                                                                                                       |
| -------------- | ------------ | ---------------------------------------------------------------------------------------------------------- |
| Active         | Needs Review | Verified implementation or tests contradict the decision, or recurring features follow a different pattern |
| Active         | Superseded   | A newer decision explicitly replaces this one                                                              |
| Needs Review   | Active       | Team confirms the decision still holds after review                                                        |
| Needs Review   | Superseded   | Team confirms a replacement decision                                                                       |
| Superseded     | _(remove)_   | Audit finds no remaining historical value                                                                  |

### Rules

- Never delete an Active decision without replacing or superseding it.
- Never silently ignore a decision. If it feels wrong, mark it Needs Review and resolve it.
- Keep at most 3–5 Superseded entries for context. Prune older ones during audits.

---

## Template

### YYYY-MM-DD - Decision title

**Status**
Active | Superseded | Needs review

**Why this is durable**
What cross-feature choice is likely to matter again?

**Decision**
What was decided and what boundary does it create?

**Tradeoffs**
What was gained, what was made harder, and when should this be reconsidered?

**Future mistake prevented**
What likely incorrect approach does this rule out?

**Evidence**
Diff, tests, review, incident, or repeated implementation evidence.

**Where to look next**
Files, modules, or specs future maintainers should inspect.

---

### 2026-05-30 - Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml)

**Status**
Active

**Why this is durable**
Tomcat 10.1+ uses Jakarta EE 10 (jakarta.servlet.* namespace). web.xml with javax.* namespace causes class loading conflicts. This decision applies to every servlet/controller feature in the project.

**Decision**
Use `AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer` for servlet initialization instead of web.xml. The initializer:
- Registers DispatcherServlet with "/" mapping
- Loads WebConfig (@Configuration, @EnableWebMvc) as servlet config class
- Is auto-discovered by Tomcat via ServletContainerInitializer SPI
- Provides compile-time type safety (vs string-based class names in web.xml)

**Tradeoffs**
- Gained: Type-safe configuration, one less XML file, Jakarta EE 10 compatible, compile-time errors instead of runtime failures
- Made harder: Requires understanding of Servlet 3.0+ SPI mechanism
- Reconsider: If the project migrates to embedded containers (future consideration)

**Future mistake prevented**
Adding a web.xml with javax.servlet.* declarations would cause incompatibility errors with Tomcat 10.1+ and Spring MVC 6.x.

**Evidence**
Spring Framework 6.2 reference docs confirm this is the standard approach for Jakarta EE environments. Spec review feedback identified the javax/jakarta namespace conflict.

**Where to look next**
backend/src/main/java/com/resumainer/initializer/AppInitializer.java

---

### 2026-05-30 - Maven Wrapper Must Be at Same Directory Level as pom.xml

**Status**
Active

**Why this is durable**
Maven Wrapper scripts (mvnw, mvnw.cmd) determine the project root directory. They look for pom.xml in the same directory. If pom.xml is elsewhere (e.g., backend/), running mvnw from the project root fails with "The goal you specified requires a project to execute but there is no POM in this directory."

**Decision**
Place mvnw, mvnw.cmd, and .mvn/wrapper/ in the same directory as pom.xml. For this project: all in backend/ alongside backend/pom.xml.

**Tradeoffs**
- Gained: mvnw clean package works immediately, no -f flag needed, matches Maven Wrapper documentation and community convention.
- Made harder: Developer must cd backend/ before running mvnw, or use cd backend && ./mvnw ... from project root (minor inconvenience).
- Reconsider: If the project restructures to a multi-module build with a parent pom.xml at root, the wrapper should move to root level.

**Future mistake prevented**
Putting mvnw at the project root while pom.xml lives in a subdirectory causes confusing "no POM in this directory" errors. Developers waste time debugging build configuration when the issue is simply directory mismatch.

**Evidence**
Maven Wrapper documentation (maven.apache.org/wrapper/): "mvnw must be placed in the same directory as pom.xml." Spec review flagged the inconsistency during Context7 documentation check.

**Where to look next**
backend/mvnw, backend/pom.xml, and any future multi-module restructure.
