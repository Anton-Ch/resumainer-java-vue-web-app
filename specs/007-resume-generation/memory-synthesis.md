# Memory Synthesis

## Current Scope
- Feature: 007-resume-generation
- Spec: Feature Specification: Resume Generation
- Feature folder: specs\007-resume-generation
- Spec context: # Feature Specification : Resume Generation **Feature Branch **: `feat/007-resume-generation` **Created**: 2026-06-12 **Status**: Draft **Input**: Build the full Generate Resume feature for ResumAIner : vacancy-specific resume generation using structured profile data ,...

## Relevant Project Context
- [C1] constraint that affects many features constraint that an AI should respect before planning (Source: `docs/memory/PROJECT_CONTEXT.md`)
- [C2] durable product constraints domain language and invariants project-wide priorities that shape feature tradeoffs (Source: `docs/memory/PROJECT_CONTEXT.md`)
- [C3] feature-specific acceptance criteria task lists transient implementation notes changelog entries Update the review date when constraints or priorities materially change. (Source: `docs/memory/PROJECT_CONTEXT.md`)

## Relevant Decisions
- [D1] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)

## Active Architecture Constraints
- [A1] stable system boundaries ownership lines between modules or services integration constraints that affect many features (Source: `docs/memory/ARCHITECTURE.md`)

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)
- [S2] &lt;!-- Sync Impact Report (v1.0.0) Version change: (template) → 1.0.0 Modified principles: - [PRINCIPLE_1_NAME] → I. Code Quality &amp; Maintainability - [PRINCIPLE_2_NAME] → II. Testing Excellence - [PRINCIPLE_3_NAME] → III. (Source: `.specify/memory/constitution.md`)
- [S3] Milestone : Feature 003 (Vue Auth Page) reaches Spec + Plan + Tasks + Security Review complete. (Source: `docs/memory/WORKLOG.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
