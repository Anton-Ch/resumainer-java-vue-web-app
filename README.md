# ResumAIner Java Vue Web App

**Status:** Work in Progress  
**Project Type:** Java + Vue Web Application - Capstone Project  
**Development Approach:** Spec-Driven Development with GitHub Spec Kit

ResumAIner is an AI-assisted resume adaptation platform that helps users generate tailored resume versions based on their profile data, target vacancy, company information, selected language, and adaptation level.

This repository contains the implementation of the ResumAIner application.

The project is being developed from a completed Business and System Analysis documentation package stored in a separate repository:

> `resumainer-business-and-system-analysis`

## Current Repository Status

This repository is in the initial setup stage.

The implementation will be added incrementally using Spec Kit workflow:

1. Define project constitution.
2. Create feature specifications.
3. Prepare implementation plans.
4. Generate task breakdowns.
5. Implement features in small, reviewable steps.
6. Keep documentation, code, and requirements aligned.

## Planned Technology Stack

### Backend

- Java
- Spring MVC
- Plain JDBC
- Custom thread-safe Connection Pool
- DAO pattern
- PostgreSQL
- HTML-to-PDF generation

### Frontend

- Vue 3
- Vite
- REST API integration
- Structured resume review forms
- Admin UI tables and details pages

### Infrastructure

- Docker
- Docker Compose
- VPS deployment target
- GitHub-based version control
- Flyway database migration tool

### AI Integration

- OpenRouter API
- DeepSeek model route for development experiments
- Mock AI provider fallback for stable local testing

## Development Workflow
 
This repository will be maintained using a lightweight but disciplined workflow.
 
The goal is to keep the repository clear, reviewable, and professional for future maintenance and reviews.
 
### Branching Model
 
This project follows **GitHub Flow**.
 
Rules:
 - `main` must represent the latest stable project state.
- New work should be done in short-lived feature branches.
- Branch names should be descriptive and lowercase.
- Each branch should focus on one logical change or feature.
- Changes should be merged through Pull Requests.
- Branches should be deleted after merge.
 
Branch name examples:
 
```text
feature/spec-kit-initialization
feature/backend-hello-world
fix/docker-tomcat-startup
docs/update-setup-guide
chore/configure-gitignore
```
 
### Commit Message Rules
 
This repository uses a practical combination of:
- Conventional Commits
- Semantic Commit Messages
- descriptive commit bodies
 
Commit title format:
 
```text
type(scope): short imperative summary
```
 
Commit body format:
 
```text
Change summary:
- what changed
- what was added
- what was updated
 
Rationale:
- why the change was needed
- how it supports the project
 
Next step:
- what should happen next, if applicable
```
 
Allowed commit types:
 
| Type | Use When |
|---|---|
| `feat` | Add or change user-visible functionality |
| `fix` | Fix a bug or incorrect behavior |
| `docs` | Change documentation only |
| `refactor` | Improve code structure without changing behavior |
| `test` | Add or update tests |
| `style` | Formatting-only changes |
| `build` | Build system, dependencies, Maven, npm, Docker build changes |
| `chore` | Repository maintenance, config, cleanup |
| `security` | Security-related changes |
 
Examples:
 
```text
docs: add initial project README
 
feat(auth): add login form validation
 
fix(docker): resolve Tomcat startup path
 
build(backend): add Maven project skeleton
 
security(admin): mask AI provider API key
```
 
### Commit Discipline
 
Rules:
 
- Prefer small commits with one logical purpose.
- Do not mix unrelated changes in one commit.
- Use a detailed commit body when the change affects architecture, setup, workflow, security, or requirements.
- Use simple one-line commits only for small obvious changes.
- Never commit secrets, API keys, local credentials, generated junk, or IDE-specific noise.
- Review staged files before committing.
 
### Pull Request Rules
 
Pull Requests should be used to explain and review meaningful changes.
 
Each PR should include:
 
```text
## Summary
Short explanation of what this PR changes.
 
## What
- Main implementation or documentation changes.
 
## Where
- Key folders/files affected.
 
## Why
- Reason for the change and relation to Spec Kit / project goals.
 
## Testing
- Commands run.
- Manual checks performed.
- Known limitations.
 
## Impact
- Scope, architecture, data model, UI, deployment, or security impact.
 
## Next
- Follow-up work, if any.
```
 
For small documentation-only changes, the PR description may be shorter, but it should still explain the purpose of the change and keep the structure.

## Planned Repository Structure

```text
resumainer-java-vue-web-app/
├── .specify/
├── specs/
├── backend/
├── frontend/
├── docs/
├── docker/
├── scripts/
├── config/
├── .claude/
├── CLAUDE.md
├── README.md
└── docker-compose.yml
```

## First Implementation Milestone

The first milestone is to validate the development workflow with a minimal technical feature:

> Java Spring configuration + Hello World Tomcat page in Docker

This milestone is intentionally simple. Its purpose is to verify:
- Spec Kit workflow;
- Claude Code-assisted development setup;
- Java backend project structure;
- Docker/Tomcat execution;
- Git workflow;
- documentation update process.

## Related Repository

Business and System Analysis repository:

`https://github.com/Anton-Ch/resumainer-business-and-system-analysis`

## Notes

This repository is not a finished product yet.

It will be gradually evolved as implementation progresses from project setup to backend, frontend, database, PDF generation, public resume links, admin tools, and AI integration.
