# Architecture

Last reviewed: YYYY-MM-DD

## System Overview
High-level shape of the system.

## Major Components
- component
- component

## Boundaries
Describe service, module, or package boundaries.

## Integrations
- integration
- integration

## Risks / Complexity Hotspots
- hotspot
- hotspot

## Keep Here
- stable system boundaries
- ownership lines between modules or services
- integration constraints that affect many features

## Never Store Here
- step-by-step implementation plans
- one-off feature details
- stale diagrams without current boundaries

Update the review date when boundaries, ownership, or integrations materially change.

---

### 2026-05-30 - Servlet Container Integration via Java SPI, Not XML Descriptor

**Status**
Active

**Why this is durable**
Defines the project's approach to servlet container setup. All future controllers depend on this initialization mechanism.

**Boundary**
The project does not use web.xml for servlet registration. Servlet initialization is handled exclusively through Java classes extending `AbstractAnnotationConfigDispatcherServletInitializer` or implementing `WebApplicationInitializer`.

**Evidence**
Spring Framework 6.2 documentation. Tomcat 10.1+ requires Jakarta EE 10. Spec and plan for feature 001-hello-world-tomcat.

**Where to look next**
backend/src/main/java/com/resumainer/initializer/

---

### 2026-06-06 — SPA under /app/ routing with landing page at /

**Status**: Active

**Why this is durable**: The landing page (Thymeleaf) stays at `/`, while Vue SPA lives under `/app/...`. Nginx routes `/app/*` to SPA `index.html` and `/` to backend for Thymeleaf. All future feature routes MUST be added under `/app/...`, never at root.

**Constraint**: `try_files $uri /app/index.html` (without `$uri/` directory fallback) with `autoindex off`. Backend AuthInterceptor protects `/api/**`. Vue Router guards handle auth redirects for `/app/*`.
