# Quickstart: Feature 003 — Vue Auth Page

## Prerequisites

- Java 21 JDK
- Docker & Docker Compose
- Node.js 20+ (for frontend)

## Backend Setup

```bash
# 1. Build backend
cd backend
./mvnw clean package

# 2. Start PostgreSQL + build backend image + run
cd ..
docker compose up -d db
# Wait for DB to be ready, then:
docker compose up -d --build backend
```

## Frontend Setup

```bash
# 1. Install dependencies
cd frontend
npm install

# 2. Start dev server (hot reload)
npm run dev
# Frontend available at http://localhost:5173

# 3. Production build
npm run build
# Output in frontend/dist/
```

## Docker Compose (full stack)

```bash
docker compose up -d --build
```

Access:
- Frontend: http://localhost
- Backend API: http://localhost:8080/api/
- Database: localhost:5432 (user: resumainer, password from .env)

## Verification

1. Open http://localhost → Landing Page
2. Click "Register" → Register form
3. Create account → redirected to User Home placeholder
4. Logout → Login page
5. Login with created account → User Home placeholder
6. Verify admin: set role_id = ADMIN in DB → login → Admin Home placeholder

## Key Commands

| Action | Command |
|--------|---------|
| Build backend | `cd backend && ./mvnw clean package` |
| Run backend tests | `cd backend && ./mvnw test` |
| Build frontend | `cd frontend && npm run build` |
| Run frontend dev | `cd frontend && npm run dev` |
| Full stack up | `docker compose up -d --build` |
| View logs | `docker compose logs -f [service]` |
