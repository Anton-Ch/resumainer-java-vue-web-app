# AGENTS.md

## Project state

Early-stage scaffold. No application code, no build files, no CI yet.

## Branches

| Branch | Contents |
|---|---|
| `main` | Only README.md |
| `chore/spec-kit-configuration` | Spec Kit tooling (CLAUDE.md, .gitignore, .specify/) |
| `chore/spec-kit-setup` (HEAD) | Only README.md (same as main) |

**You likely want `chore/spec-kit-configuration`** checked out — that's where all tooling lives.

## Methodology

Spec-Driven Development with GitHub Spec Kit. The flow is:
```
constitution → specify → plan → tasks → implement
```

Each feature must follow the memory-first workflow in `.specify/memory/workflow.md`.

## Key files (on `chore/spec-kit-configuration` branch)

- `CLAUDE.md` — agent instructions referencing memory-first workflow
- `.specify/memory/constitution.md` — unfilled constitution template
- `.specify/memory/workflow.md` — authoritative memory workflow definition
- `.specify/extensions.yml` — 12 extensions registered, auto-execute hooks on
- `docs/memory/` — empty memory store (INDEX.md + PROJECT_CONTEXT.md templates)

## Planned stack

Backend: Java / Spring MVC / Plain JDBC / DAO / PostgreSQL / Flyway / HTML-to-PDF
Frontend: Vue 3 / Vite / REST API
Infra: Docker / Docker Compose / VPS
AI: OpenRouter API (DeepSeek in dev), Mock AI provider for tests

## Known quirks

- `superspec` and `wireframe` are broken symlinks (tracked but unresolved)
- `.claude/*` is gitignored except `.claude/skills/`
- `chore/spec-kit-configuration` local (`2ae1a7c`) is ahead of remote (`9c35a45`)
- No `pom.xml`, `package.json`, `docker-compose.yml` exist yet
- Constitution and memory store are unfilled

## First milestone

> Java Spring config + Hello World Tomcat page in Docker

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->
