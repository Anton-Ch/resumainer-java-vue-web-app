# Strategic Context and Gap Analysis

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-18
**Last Updated:** 2026-05-21
**Author:** Anton
**Version:** 7.0
**Status:** Approved
**Related BABOK Area:** 6.1 Analyze Current State / 6.2 Define Future State

***

## 1. Description

This document captures the strategic rationale for the ResumAIner project. It answers two fundamental questions:

- **Why are we building this?**
  by describing the current state, pain points, and root cause analysis.
- **What are we building toward?** 
  by defining the target state and the gap between where we are and where we need to be.

This is the foundational business analysis artifact for the project. It ensures that every requirement, design decision, and implementation task is traceable to a clearly articulated business need.

## 2. Current State (As-Is)

### 2.1 Business Context

Job seekers today must adapt their resume for each job application to pass ATS screening and stand out to recruiters. This is a fully manual process with no dedicated tools. Career information like skills, experience, achievements, education is scattered across Word files, Google Docs, LinkedIn, email attachments, and personal memory.

### 2.2 Pain Points

| # | Pain Point | Impact |
|---|-----------|--------|
| 1 | Manual adaptation takes 2-3 hours per vacancy | Candidates skip potentially suitable opportunities due to time cost |
| 2 | Career data scattered across multiple sources | No single source of truth; data must be re-discovered for each resume |
| 3 | Inconsistent adaptation quality | Quality varies by time, energy, and writing skill available at the moment |
| 4 | No version history | Cannot reliably recall which resume variant was sent to which employer |
| 5 | Dual-language support doubles effort | Russian and English versions must be manually synchronized |
| 6 | Difficulty phrasing achievements professionally | Candidates struggle with keyword optimization for ATS scoring |

### 2.3 Root Cause Analysis

**Technique:** Five Whys
**Problem Statement:** Job seekers spend 2-3 hours manually adapting their resume for each vacancy, causing them to skip opportunities or submit generic, poorly adapted resumes.

| Why?                                                       | Answer                                                                                                                                                          |
| ---------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Why does adaptation take so long?                          | Because there is no structured profile, career data must be gathered from multiple sources for each resume.                                                     |
| Why is there no structured profile?                        | Because general-purpose tools (Word, ChatGPT, email) are used instead of a dedicated system.                                                                    |
| Why are general-purpose tools used?                        | Because no integrated system exists that combines structured profile storage with AI-powered adaptation.                                                        |
| Why hasn't such a system been built for this user segment? | Because building one requires combining structured data management, AI integration, version tracking, and PDF generation — a non-trivial integration challenge. |

**Root Cause:** No integrated system exists that combines structured career profile storage with AI-powered vacancy-specific resume adaptation. Each adaptation starts from scratch.

**Contributing Factors:**
- Data fragmentation across multiple unconnected tools
- No structured data model for career information
- AI tools (ChatGPT) require manual context assembly every time
- No version tracking or vacancy-to-resume mapping
- PDF generation and formatting are manual and inconsistent

## 3. Future State (To-Be)

### 3.1 Target Vision

ResumAIner provides a single, integrated platform where users:

1. **Maintain a structured career profile** — enter career data once (contact, experience, education, skills, projects, certifications, languages, achievements).
2. **Generate adapted resumes in minutes** — paste a job, company description, select AI model and adaptation level, choose language (RU/EN/both), and receive an AI-generated draft.
3. **Review and refine** — edit generated content before saving the final version.
4. **Track version history** — view, search, filter, and manage all saved resume versions.
5. **Share professionally** — download print-friendly selectable-text PDF, share via permanent public URL, access ATS-optimized JSON endpoint (MVP Stretch).

### 3.2 Target Capabilities

| Capability | Current State | Target State |
|-----------|--------------|--------------|
| Profile storage | None (manual/files) | Single structured CRUD system |
| AI integration | Manual copy-paste to ChatGPT | Built-in with model selection and adaptation levels |
| Version management | Manual file naming with no tracking | Full history with search/filter/sort/pagination |
| Vacancy tracking | None | Stored per generation with company information |
| Language support | Manual translation or separate versions | Automated generation (Russian/English/both) |
| PDF generation | Manual export with formatting adjustments | Automated A4 selectable-text PDF |
| Public sharing | Email or file-sharing links | Permanent URL per resume version |

### 3.3 Technology Stack

| Layer | Target Technology |
|-------|------------------|
| Backend | Java, Spring MVC, Servlets |
| Data access | Plain JDBC with custom thread-safe Connection Pool |
| Database | PostgreSQL (3NF normalized) |
| Frontend | Vue 3 (Composition API) + Vite + PrimeVue (authenticated), Thymeleaf (landing page) |
| AI integration | OpenRouter API (isolated behind service interface) |
| PDF generation | Automated (selectable text, A4) |
| Deployment | Docker Compose (backend + frontend + database) |
| Migrations | Flyway (versioned SQL scripts) |
| Logging | SLF4J + Logback |
| API Documentation | Swagger/OpenAPI (springdoc-openapi), ADMIN-only access on prod |
| Deployment profiles | dev and prod Spring profiles via application-dev.yml and application-prod.yml |
| Design patterns | Singleton (Connection Pool), Builder (AI prompt), Factory Method (AI client), Strategy (adaptation level) |
| AOP | Spring AOP with AspectJ for cross-cutting logging and monitoring |
| Interceptors | Spring MVC HandlerInterceptors for request logging and authorization |
| Testing | JUnit 5, Mockito, JaCoCo coverage reports; 50%+ coverage in Service and DAO layers |

## 4. Constraints

| # | Constraint | Category | Status |
|---|-----------|----------|--------|
| 1 | Must use Capstone technology stack: Spring MVC, JDBC, PostgreSQL, Maven, Vue.js, Docker Compose | Technology | Confirmed |
| 2 | Solo developer, scope must be achievable within course timeframe | Resources | Confirmed |
| 3 | Database must be normalized to 3NF; no ORM frameworks allowed | Technology | Confirmed |
| 4 | Custom thread-safe Connection Pool must be implemented manually (no HikariCP, DBCP, or similar libraries) | Technology | Confirmed |
| 5 | Transactions must be managed manually via JDBC commit()/rollback() at Service layer | Technology | Confirmed |
| 6 | Database, connections, and all text columns must use UTF-8 encoding for Cyrillic support | Technology | Confirmed |

## 5. Gap Analysis Summary

| Element | Change Type | Complexity | Summary |
|---------|------------|-----------|---------|
| Business Needs | Improve | High | Transition from fragmented manual process to integrated AI-powered system |
| Capabilities | Improve | High | From zero dedicated capabilities to full-featured profile management and resume generation |
| Technology | Improve | High | From general-purpose office tools to purpose-built full-stack web application |

All three gaps represent a transformation from a tool-fragmented manual workflow to an integrated software solution. This is expected for a new system initiative, the gaps are not risks but rather define the scope of what needs to be built.

## 6. Potential Value Assessment

| Benefit | Type | Magnitude | Confidence |
|---------|------|-----------|-----------|
| Reduce resume adaptation time from 2-3 hours to under 10 minutes | Operational | High | High |
| Improve application quality and ATS performance | Strategic | High | Medium |
| Single source of truth for career data | Operational | Medium | High |
| Professional portfolio showcase (Capstone) | Strategic | Medium | High |

**Investment Level:** Medium
**Profile:** Attractive — high value at moderate investment, three out of four benefits have high confidence.

***

*This document follows the BABOK v3 framework (sections 6.1 and 6.2) adapted for lean portfolio documentation. It serves as the strategic foundation for all downstream requirements, design, and development activities.*