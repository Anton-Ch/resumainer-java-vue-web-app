# Learnings

> Engineering decisions, trade-offs, and insights from the ResumAIner project.
> Organized by project stage. New entries are appended as the project progresses.

---

## Stage: Specification & Brainstorming

> Feature: `001-hello-world-tomcat` — Java Spring MVC + Tomcat + Docker local setup.
> Stage: Specification, clarification, and brainstorming before implementation.

---

### 2026-05-30 | Startup Readiness: wait-for-it.sh over Docker HEALTHCHECK

**Tags**: `docker`, `compose`, `startup-order`, `reliability`

**Context**: Docker Compose starts Tomcat and PostgreSQL containers in parallel. Tomcat tries to connect to the database before PostgreSQL is ready, causing connection errors on first startup.

**Decision**: Use a `wait-for-it.sh` script inside the Tomcat entrypoint rather than Docker HEALTHCHECK.

**Why**: Docker HEALTHCHECK only tells _Docker_ the container is unhealthy — it doesn't stop the application process. `wait-for-it.sh` blocks the Tomcat startup _until_ PostgreSQL is reachable, which gives clean first-time startup. It also doubles as a general-purpose tool for any future dependency.

**Alternatives considered**:
- **Docker HEALTHCHECK**: Signals container health to Docker, but doesn't prevent the app from starting too early. Better for orchestration (Kubernetes) than for Compose.
- **depends_on with condition**: Docker Compose v2 supports `depends_on` with `condition: service_healthy`, but it couples startup logic to Compose config rather than keeping it in the container itself.

**Example**:
```
# docker-compose.yml — Tomcat waits for PostgreSQL before starting
command: ["./wait-for-it.sh", "db:5432", "--", "catalina.sh", "run"]
```

**Further reading**: Docker Compose startup order docs, `docker-compose depends_on` vs entrypoint scripts.

---

### 2026-05-30 | Maven Wrapper for Reproducible Builds

**Tags**: `build`, `maven`, `reproducibility`, `ci`

**Context**: The project uses Maven for building. Different developers or CI environments may have different Maven versions (3.6 vs 3.9), which can cause subtle build differences or failures.

**Decision**: Add Maven Wrapper (`mvnw`) to the repository.

**Why**: Maven Wrapper pins the exact Maven version (3.9.x) in a `.mvn/` config file. Everyone — developer laptop, Docker build, CI — uses the same version. No "works on my machine" issues. Developers don't even need Maven installed locally.

**Alternatives considered**:
- **No wrapper, rely on system Maven**: Simple, but version mismatch is a common source of CI failures. Saves ~100KB in repo but costs debugging time.
- **Docker-only Maven**: Build only inside Docker. Works for CI, but slows down local dev — every code change requires a full Docker build to compile.

**Example**:
```
# Generate wrapper (one-time setup):
mvn wrapper:wrapper -Dmaven=3.9.9

# Developer builds without installing Maven:
./mvnw clean package
```

**Further reading**: Maven Wrapper docs (`mvn wrapper:wrapper`), differences between Maven 3.6 and 3.9.

---

### 2026-05-30 | Docker Compose: Include PostgreSQL from Day One

**Tags**: `docker`, `compose`, `infrastructure`, `postgresql`

**Context**: The Hello World feature doesn't need a database — it's a static page. But every future feature (auth, profiles, resume storage) depends on PostgreSQL.

**Decision**: Add PostgreSQL as a service in Docker Compose immediately, alongside Tomcat.

**Why**: Adding a service to Docker Compose later is easy, but removing it from developers' mental model is hard. Including PostgreSQL now means:
- Developers don't need to install PostgreSQL locally — it runs in a container.
- Docker volumes for data persistence are configured once and forgotten.
- The `wait-for-it.sh` approach is tested from day one.

The Hello World page itself doesn't use PostgreSQL — it's just infrastructure scaffolding.

**Alternatives considered**:
- **No PostgreSQL, add later**: Saves one container in early dev. Risk: developer forgets to add it, or adds it incorrectly when the first DB-dependent feature arrives.
- **Local PostgreSQL install**: Each developer manages their own DB. More setup steps in README, more "works on my machine" risk.

**Example**:
```yaml
# docker-compose.yml snippet
services:
  db:
    image: postgres:16-alpine
    volumes:
      - pgdata:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: resumainer
      POSTGRES_PASSWORD: ${DB_PASSWORD}
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    # entrypoint calls wait-for-it.sh db:5432 before starting Tomcat
```

**Further reading**: PostgreSQL Docker image docs, Docker Compose service dependencies, PostgreSQL connection pooling basics.

---

### 2026-05-30 | .gitignore: Community Best Practices

**Tags**: `git`, `hygiene`, `security`, `setup`

**Context**: The repository had no `.gitignore`. Developers risk accidentally committing IDE files, compiled classes, `node_modules`, or — worst case — `.env` files with secrets.

**Decision**: Add a `.gitignore` with rules covering Java, Maven, Node, IDE (IntelliJ, VS Code), OS files (Windows, macOS, Linux), Docker, and secrets.

**Why**: A missing `.gitignore` is a security and hygiene risk. Accidental commits of IDE config or `.env` files are hard to undo (they stay in git history). Better to have a comprehensive ignore file from the start and never think about it again.

**Alternatives considered**:
- **Minimal `.gitignore`** (Java + Maven only): Fewer lines, but misses IDE files (`.idea/`, `.vscode/`) that developers commonly commit by accident.
- **No `.gitignore`**: Every developer manages their own `.git/info/exclude`. Works only in teams that never make mistakes — unrealistic.

**Example**:
```
# Key patterns every Java project should have:
target/
*.class
*.jar
*.war
!.mvn/wrapper/maven-wrapper.jar

# IDE — developers always forget these:
.idea/
.vscode/
*.iml
*.iws

# Secrets — must never be committed:
.env
.env.*

# OS files:
.DS_Store
Thumbs.db
```

**Further reading**: GitHub's `.gitignore` templates (`github/gitignore`), `git secrets` for pre-commit secret scanning.

---

> _To add a new learning: copy the template block above, fill in date, title, tags, context, and append under the relevant project stage._

# Learnings & Dev Notes

## Flyway Migrations — What It Is and How It Works

**Flyway** is a database version control tool. Think of it as a folder of numbered SQL files that Flyway applies to PostgreSQL in order, automatically.

### File Structure

```
backend/src/main/resources/db/migration/
├── V1__create_role_table.sql
├── V2__create_user_status_table.sql
├── V3__create_user_permission_table.sql
├── V4__create_language_table.sql
├── V5__create_users_table.sql
├── V6__create_contact_detail_table.sql
└── V7__seed_lookup_data.sql
```

### How It Works (Simplified)

1. **First run**: Flyway creates a tracking table `flyway_schema_history` in PostgreSQL
2. **Scans `db/migration/`**: finds all files matching `V{number}__{description}.sql`
3. **Sorts by version**: V1 → V2 → V3 → ... → V7
4. **Applies in order**: executes SQL from each file exactly once
5. **Remembers what ran**: `flyway_schema_history` stores checksums of executed migrations

### Important Rules

| Rule | Why |
|------|-----|
| **Never modify an already-run migration** | Flyway detects checksum mismatch → throws error |
| **Need to change a table? Create V8** | New migration with ALTER TABLE, DROP COLUMN, etc. |
| **Naming: `V{number}__{description}.sql`** | Double underscore between number and description |
| **Numbers must be sequential** | V1, V2, V3... no gaps |
| **Seed data is a migration too** | V7 populates lookup tables with initial values |

### Example: V1

```sql
-- V1__create_role_table.sql
CREATE TABLE role (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL
);
```

### How It Starts

Add Flyway dependency to `pom.xml`. On Tomcat startup, Flyway automatically:
1. Connects to PostgreSQL (configured in `application.properties`)
2. Checks `flyway_schema_history` — which migrations have already run
3. Applies any new ones
4. Application starts only after migrations succeed

### Common Beginner Mistake

```bash
# Error: Validation failed
# Cause: modified V1__create_role_table.sql after it already ran
```

**Fix**: Don't touch old migrations. Create `V8__fix_role_table.sql` with ALTER TABLE.

### Why Flyway Instead of Raw SQL Scripts

| Without Flyway | With Flyway |
|---------------|-------------|
| Forget to run script → DB out of sync with code | Auto-applied on startup |
| Don't know which schema version is in production | `flyway_schema_history` has full history |
| Scared to change tables — unclear what exists | Every migration is a documented change |
| Different dev databases drift apart | Always the same schema version everywhere |
