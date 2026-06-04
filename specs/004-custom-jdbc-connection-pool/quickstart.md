# Quickstart: Custom JDBC Connection Pool

**Date**: 2026-06-04 | **Feature**: Custom JDBC Connection Pool

## Required Environment Variables

Set these for local development (or add to `.env` for Docker Compose):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=resumainer
export DB_USER=resumainer
export DB_PASSWORD=resumainer
```

## Pool Configuration Properties

Create or update `backend/src/main/resources/application.properties`:

```properties
# Database connection
db.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
db.username=${DB_USER}
db.password=${DB_PASSWORD}

# Pool settings
db.pool.initial-size=2
db.pool.max-size=10
db.pool.borrow-timeout-ms=5000
db.pool.validation-timeout-seconds=2
```

**NOTE**: Pool implementation reads env vars directly via `System.getenv()`. The `${...}` in properties above is for documentation clarity. If using Spring `@Value`, the properties file works with `@Value("${db.url}")` because Spring's `PropertySourcesPlaceholderConfigurer` resolves them.

**SECURITY NOTE — Production**: For production deployments, add SSL parameters to the JDBC URL:
```properties
db.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?ssl=true&sslmode=require
```

**SECURITY NOTE — .env file**: Ensure `.env` is in `.gitignore` and is never committed to the repository. Database credentials in version control are a security incident.

## Running

```bash
# Build and test
mvn clean test

# Package
mvn clean package

# Run in Docker (full stack)
docker compose up --build
```

## Verifying the Pool

After startup, check logs for:
```
INFO  c.r.infrastructure.db.SimpleConnectionPool - Connection pool initialized: initialSize=2, maxSize=10
```

## Testing

```bash
# Unit tests (no PostgreSQL required)
mvn test

# Coverage report
mvn verify
# Open: backend/target/site/jacoco/index.html
```
