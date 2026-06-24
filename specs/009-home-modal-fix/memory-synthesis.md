# Memory Synthesis

## Current Scope
- Feature: 005-user-home-page
- Spec: Feature Specification: User Home Page & Resume Workspace
- Feature folder: specs\005-user-home-page
- Spec context: # Feature Specification : User Home Page & Resume Workspace **Feature Branch **: `feat/005-user-home-page` **Created**: 2026-06-06 **Status**: Approved **Input**: User description : "Let's create the User Home Page the resume workspace ....

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable HTML-to-PDF conversion requires independent library evaluation (Flying Saucer, OpenPDF, wkhtmltopdf), A4 layout validation, Cyrillic font support, and selectable text verification. Bundling it with AI generation would make the feature too large and risky. This split pattern may apply to other composite features. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable The frontend service generateResumeService.ts had methods downloadPdf(savedResumeId) , openPdf(savedResumeId) , downloadHtml(savedResumeId) that constructed URLs from a RESUME_BASE constant and the saved resume ID. Meanwhile the backend DTO SavedResumeExportDto already carried pdfDownloadUrl , pdfOpenUrl , htmlDownloadUrl , and publicUrlLink — fully resolved canonical URLs. The ID-based construction bypassed backend route changes, ignored the ?disposition=inline parameter, and created fragile coupling where any backend route rename would break the frontend. (Source: `docs/memory/DECISIONS.md`)
- [V2] Status Active Why this is durable Every new download endpoint (PDF, HTML, or any file) needs to set a Content-Disposition header. Without an allowlist, a malicious or malformed disposition request parameter can inject CRLF sequences ( \r\n ) into the HTTP response headers, enabling response splitting or header manipulation attacks. Decision All download controllers MUST validate the Content-Disposition header value against a strict allowlist before setting it. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] W1 | First Feature MVP Achieved : Hello World Tomcat | milestone , mvp , hello-world , docker , spring-mvc , tomcat | WORKLOG .md | active W2 | Second Feature MVP Achieved : Thymeleaf Landing Page | milestone , mvp , landing-page , thymeleaf , i18n , feature-002 , bilingual | WORKLOG .md | active W3 | Feature 003 Planning and Security Review Completed | milestone , feature-003 , vue-auth , planning , security-review , specification | WORKLOG .md... (Source: `docs/memory/INDEX.md`)
- [S2] Status Active Why this is durable Phases 23-27 delivered the complete production PDF pipeline : Phase 23 : Download controller security fixes (Content-Disposition header injection fix , missing exists () check , SecurityException →500 bug ), public route rate limiter , timing hardening Phase 24 : Frontend export /finalize flow repair — DTO URL contract , disabled PDF buttons , absolute public link , duplicate navigation fix , error handling , double-click prevention Phase 25 : V12 .1 budget parity... (Source: `docs/memory/WORKLOG.md`)
- [S3] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms Cover letter is visible on Review page, generate.cover_letter is included in the prompt, ResumeGenerationResponse.getCoverLetter() returns the generated text, but the Export page never shows the cover letter block. DB query shows cover_letter IS NULL for all saved_resumes rows. Root Cause The saved_resumes.cover_letter column was created in V8 migration and read correctly in SELECT queries ( SavedResumeDao.findByGenerationRequestId , findById ), but the INSERT statement in SavedResumeDao.insert() did not include the cover_letter column. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
