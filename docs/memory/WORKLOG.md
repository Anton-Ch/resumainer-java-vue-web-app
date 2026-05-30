# Worklog

Use concise high-value entries only.
This is not a changelog. Do not record routine releases, version bumps, or implementation summaries.

## Template

---

### 2026-05-30 - First Feature MVP Achieved: Hello World Tomcat

**Milestone**: Feature `001-hello-world-tomcat` reaches MVP.

**What was achieved**: Full end-to-end validation: `git clone → mvnw clean package → docker compose up → browser shows Hello World page with server time`. Spring MVC 6.2.11 + Jakarta EE 10 on Tomcat 10.1, deployed in Docker via multi-stage build (Maven → Tomcat, non-root user). Unit test (MockMvc, standalone setup) passing.

**Key lessons captured**:
- D1: Servlet initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml)
- D2: Maven Wrapper at same level as pom.xml
- D3: Docker Tomcat health check uses bash /dev/tcp (not nc)
- B1: @Controller must be registered as explicit @Bean or via @ComponentScan
- JaCoCo 0.8.13+ required for Java 21 class file support (0.8.12 fails)

**Evidence**
docker compose up → http://localhost:8080 → 200 OK with ResumAIner Hello World page.
mvnw clean package → BUILD SUCCESS with 1 passing test.

### YYYY-MM-DD - Summary

- why this is durable
- what future mistake it prevents
- evidence
- where future contributors should look

## Example

### 2026-03-15 - Pagination cursor must be opaque to clients

- **Why durable**: three features so far have tried to expose raw database offsets as pagination cursors, each time creating breaking changes when the underlying query changes
- **Future mistake prevented**: next time a feature adds pagination, the implementer will know to use opaque cursors from the start
- **Evidence**: specs 018, 024, and 031 all required pagination rework; see DECISIONS.md entry on API pagination
- **Where to look**: `src/api/pagination.ts`, `docs/memory/DECISIONS.md`

## Counter-Example (do not write entries like this)

> ### 2026-03-15 - Updated pagination
>
> - Changed pagination to use cursors
> - Deployed to staging

This is a changelog entry, not a durable lesson. It records what happened, not what was learned.
