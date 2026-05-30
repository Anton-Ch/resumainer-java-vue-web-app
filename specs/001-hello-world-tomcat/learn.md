# What I Learned: Hello World Tomcat Setup

**Feature**: Java Spring MVC Hello World with Docker Compose (Tomcat + PostgreSQL)
**Generated**: 2026-05-30
**Last updated**: 2026-05-30 (Dockerfile production optimization)
**Scope**: Docker and Docker Compose decisions
**Implementation status**: 21/21 tasks completed ✅

---

## Key Decisions

### 1. Multi-Stage Build with JRE Runtime and BuildKit Cache

**What we did**: Separated the build (Maven + JDK) from the runtime (Tomcat + JRE) using two stages. Used `ARG` for version pinning, `tomcat:...-jre21` instead of `jdk21` in runtime, and `--mount=type=cache` for Maven dependency caching.

**Why**: 
- **JRE vs JDK**: The runtime only needs to run Java code, not compile it. Switching `tomcat:10.1-jdk21` → `tomcat:10.1-jre21` dropped the image from 715MB to 460MB (-36%) without any functional difference.
- **ARG for versions**: Pinning `MAVEN_VERSION`, `JAVA_VERSION`, `TOMCAT_VERSION` as ARGs makes upgrades a one-line change and documents exact versions.
- **BuildKit cache**: `--mount=type=cache,target=/root/.m2` caches downloaded Maven dependencies between builds. First build downloads everything (~2 min), subsequent rebuilds reuse the cache (~5 sec).
- **Clean webapps**: `rm -rf /usr/local/tomcat/webapps/*` removes default Tomcat apps (manager, docs, examples) — reduces attack surface and startup time.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Single-stage: install Maven in Tomcat image | Runtime would include build tools (+200 MB), increasing attack surface. |
| Build WAR locally, COPY into image | Reproducibility — Docker build is the single source of truth. |
| JDK in runtime | 715 MB vs 460 MB. JDK is unnecessary at runtime. |
| Pin versions in comments only | ARG makes versions explicit and overridable via `--build-arg`. |

**When you'd choose differently**: For a microservice where every MB matters, use `distroless-java` as the base (no shell, no package manager — ~50 MB smaller). For rapid local iteration, mount the WAR as a bind volume with `docker compose watch` instead of rebuilding.

---

### 2. `wait-for-it.sh` with Bash `/dev/tcp` Instead of `nc`

**What we did**: Wrote a pure-bash TCP wait script that uses `/dev/tcp/host/port` instead of the `nc -z` command.

**Why**: The official `tomcat` Docker image doesn't include `netcat` (nc). Our `wait-for-it.sh` initially used `nc -z` and failed silently — Tomcat started before PostgreSQL was ready. Bash's built-in `/dev/tcp` works in any bash 4+ environment without extra packages.

**Alternatives considered**:

| Approach                       | Why it wasn't chosen                                                                                                                       |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `nc -z` (netcat)               | Not available in the Tomcat image. Would need `apt-get install` in Dockerfile (+30 MB).                                                    |
| Docker HEALTHCHECK             | Signals container health to Docker but doesn't block Tomcat startup. Both would still try to connect before DB is ready.                   |
| `depends_on` without condition | Only waits for container start, not service readiness. PostgreSQL container starts but Tomcat connects before the DB is accepting queries. |

**When you'd choose differently**: In Kubernetes, use an `initContainer` with a proper wait script or `postStart` lifecycle hook. In Alpine-based images (no bash), install `curl` instead (`curl -f http://host:port`).

---

### 3. Non-Root User with Fixed UID/GID and Writable Directories

**What we did**: Created a non-root user with fixed UID/GID (10001:10001), granted ownership of `webapps/`, `logs/`, `temp/`, and `work/` directories, and switched to that user via `USER 10001:10001`.

**Why**: 
- **Fixed UID/GID** (`--uid 10001 --gid 10001`): Numeric IDs are portable across environments. UID 10001 is outside the typical range of system users (0-999) and human users (1000+), avoiding collisions.
- **Writable directories**: Tomcat needs write access to `logs/`, `temp/`, and `work/` for runtime operation. Without explicit `chown`, the non-root user would get permission errors at startup.
- **WAR extraction**: With `webapps/` owned by `app:app`, Tomcat CAN extract WARs — fixing the tradeoff from the initial implementation.
- **Numeric `USER`**: `USER 10001:10001` avoids username resolution, which is more portable across different base images.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Run as root (default) | Any compromised container has full host access. |
| Dynamic UID (OpenShift-compatible) | Overkill for Capstone. Fixed UID is simpler and traceable. |
| Only `webapps/` ownership | Tomcat also writes to `logs/`, `temp/`, `work/` — all need ownership. |

**When you'd choose differently**: In OpenShift or platforms that assign random UIDs, use a dynamic user approach (group-writable directories, `chmod g+s`). For Kubernetes, runAsUser/runAsGroup in the PodSecurityContext replaces Dockerfile USER.

---

### 4. Docker Compose with Fail-Fast Env Vars and Healthcheck

**What we did**: Used `${VAR:?error}` syntax for required variables and `${VAR:-default}` for optional ones. Added a `healthcheck` to PostgreSQL and `depends_on: condition: service_healthy` to the app service.

**Why**: Without `:?error`, missing env vars silently use empty strings, causing confusing errors at runtime (wrong DB password, wrong port). The healthcheck + `depends_on` chain ensures Tomcat never starts before PostgreSQL is accepting connections.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Hardcoded credentials in compose | Accidentally committed to Git. `.env` is in `.gitignore` — never leaks. |
| No healthcheck, just `depends_on` | PostgreSQL container starts but isn't ready yet. Race condition. |
| Scripted startup via shell wrapper | Works, but healthcheck is cleaner — Docker manages the retry logic, and `docker ps` shows health status. |

**When you'd choose differently**: For Docker Swarm or Kubernetes, liveness/readiness probes replace Compose healthchecks. For development-only setups, `depends_on` without healthcheck + `wait-for-it.sh` is sufficient.

---

### 5. `.env.example` Committed, `.env` Ignored

**What we did**: Created `docker/.env.example` with placeholder values and committed it to Git. The `.env` file (with real secrets) is excluded via `.gitignore`.

**Why**: Developers need to know what environment variables the application expects. `.env.example` documents all vars with example values and descriptions. Real secrets never enter the repository.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Document env vars only in README | Easy to forget. `.env.example` sits next to `docker-compose.yml` — impossible to miss. |
| Commit `.env` with dummy values | Risk of accidentally committing real secrets later. Keeping `.env` in `.gitignore` from day one. |
| No example file, rely on error messages | `${DB_PASSWORD:?error}` tells you what's missing, but not what values are expected. |

**When you'd choose differently**: For production, use a secrets manager (HashiCorp Vault, AWS Secrets Manager) and inject secrets via environment variables. For team projects, share `.env` contents via a secure channel (password manager).

---

## Concepts to Know

### Multi-Stage Builds

**What it is**: A single Dockerfile with multiple `FROM` statements. Each `FROM` starts a new stage. You can `COPY --from=earlier-stage` artifacts between stages. Only the last stage becomes the final image.

**Where we used it**: `docker/Dockerfile` — stage 1 (`maven:3.9.9-eclipse-temurin-21`) compiles the WAR, stage 2 (`tomcat:10.1.55-jre21-temurin-noble`) runs it with JRE only.

**Why it matters**: Without multi-stage, your production image would contain Maven, the JDK compiler, and `.class` files — not just the deployable WAR. Image size would double (715MB → 460MB), and the attack surface would include unused tools.

---

### BuildKit Cache Mounts

**What it is**: A Docker BuildKit feature (`--mount=type=cache`) that persists directories between builds. Unlike `COPY` + `RUN`, cache mounts are not included in the final image — they live on the host's BuildKit cache.

**Where we used it**: `docker/Dockerfile` — `--mount=type=cache,target=/root/.m2` on both Maven commands. The first build downloads all dependencies (~90MB). Subsequent rebuilds reuse the cache — only changed source code triggers recompilation.

**Why it matters**: Without cache mounts, `mvn dependency:go-offline` downloads all dependencies on every build (~2 minutes). With cache, rebuilds take ~5 seconds for the dependency step. The cache is scoped to the build node — it works on CI, not just local development.

---

### JRE vs JDK in Runtime Images

**What it is**: The JDK (Java Development Kit) includes compilers, debuggers, and tools needed to BUILD Java code. The JRE (Java Runtime Environment) only includes what's needed to RUN compiled code — the JVM, standard library, and runtime tools.

**Where we used it**: `docker/Dockerfile` — build stage uses `eclipse-temurin-21` (JDK), runtime stage uses `jre21-temurin-noble` (JRE-only).

**Why it matters**: A JRE-only runtime is ~250 MB smaller than JDK. No compiler means no accidental code execution at runtime — smaller attack surface. The WAR is already compiled in the build stage, so the JRE is all that's needed.

---

### Fixed UID/GID for Container Users

**What it is**: Specifying numeric UID/GID (`--uid 10001 --gid 10001`) instead of letting the system auto-assign them. The user is referenced by number in `USER 10001:10001`.

**Where we used it**: `docker/Dockerfile` — `useradd --system --uid 10001 --gid app` + `USER 10001:10001`.

**Why it matters**: Auto-assigned UIDs vary between base images and rebuilds. A fixed UID ensures consistent file ownership across environments. Using numeric ID in `USER` avoids username resolution, which fails in minimal images where `/etc/passwd` might not contain the user entry.

---

### Docker Compose Service Dependencies

**What it is**: The `depends_on` keyword tells Compose the startup order. Combined with `condition: service_healthy`, it ensures a service only starts after its dependency is fully ready.

**Where we used it**: `docker-compose.yml` — app `depends_on: db: condition: service_healthy`, and `db` has a `healthcheck` using `pg_isready`.

**Why it matters**: Without this, containers start in parallel. Tomcat tries to connect to PostgreSQL before it's ready, logs connection errors, and may crash. The healthcheck chain prevents this race condition.

---

### Docker Named Volumes

**What it is**: A persistent storage volume managed by Docker, defined in the `volumes:` section of `docker-compose.yml`. Data survives container restarts and recreations.

**Where we used it**: `docker-compose.yml` — `volumes: pgdata:` mounted to `/var/lib/postgresql/data` in the PostgreSQL container.

**Why it matters**: Without a named volume, PostgreSQL data is stored in the container's writable layer. When the container is recreated (e.g., `docker compose down && up`), all database data is lost. Named volumes persist across container lifecycles.

---

### Fail-Fast vs Silent Default Env Vars

**What it is**: Docker Compose supports shell-like variable substitution. `${VAR:?error}` fails immediately if unset. `${VAR:-default}` uses a default value if unset.

**Where we used it**: `docker-compose.yml` — `DB_PASSWORD: ${DB_PASSWORD:?error}` (required), `SERVER_PORT: ${SERVER_PORT:-8080}` (optional default).

**Why it matters**: Required vars (DB password) must never use a default — the app would connect with wrong credentials silently. Default vars (port) can fall back safely. The `:?error` syntax makes failures explicit at startup time.

---

## Architecture Overview

The Docker setup follows a two-tier topology:

```
Developer Machine
    │
    ├── docker compose up
    │
    └── Docker Compose ──────────────────────────────────
        │                                                  │
    ┌── app (tomcat:10.1.55) ──────────────┐     ┌── db (postgres:16-alpine)───┐
    │                                       │     │                             │
    │  Java 21 + Spring MVC 6.2.11          │     │  PostgreSQL 16              │
    │  JRE runtime (no JDK)                 │     │  PORT: 5432 (configurable)  │
    │  PORT: 8080 (configurable)            │     │  VOLUME: pgdata (persistent)│
    │  ENV: SPRING_PROFILES_ACTIVE          │     │  Health: pg_isready          │
    │  User: app (UID 10001, non-root)      │     │                             │
    │  JVM: -XX:MaxRAMPercentage=75.0       │     │                             │
    │  Health: wait-for-it.sh → db:5432     │     │                             │
    └───────────────────────────────────────┘     └─────────────────────────────┘
```

The `app` service depends on `db` via `depends_on: condition: service_healthy`. At container start, Tomcat runs `wait-for-it.sh db:5432`, which blocks using bash's `/dev/tcp` until PostgreSQL responds. Only then does `catalina.sh run` start, deploying the WAR and serving requests.

---

## Glossary

| Term | Meaning |
|------|---------|
| **Multi-stage build** | A Dockerfile with multiple `FROM` instructions — build stage is discarded, only runtime stage becomes the image. |
| **Healthcheck** | A Docker command that periodically checks if a container is functioning (e.g., `pg_isready` for PostgreSQL). |
| **Named volume** | Docker storage that persists data independently of container lifecycle. Defined in `volumes:` in compose. |
| **depends_on** | Docker Compose directive that controls service startup order. |
| **`:?error`** | Shell-style variable substitution that fails if the variable is unset — used for required env vars. |
| **`/dev/tcp`** | Bash built-in pseudo-device for TCP connections — an alternative to `nc` or `curl`. |
| **BuildKit cache mount** | Docker BuildKit feature (`--mount=type=cache`) that persists build artifacts between builds without including them in the final image. |
| **MaxRAMPercentage** | JVM flag that limits heap to a percentage of available container memory (e.g., `75.0` = 75% of memory limit). Prevents OOM in containerized environments. |
| **JRE vs JDK** | JRE = Runtime Environment (run code). JDK = Development Kit (build code + run). Runtime images should use JRE for smaller size and smaller attack surface. | |
