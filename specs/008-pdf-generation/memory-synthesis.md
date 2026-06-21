# Memory Synthesis

## Current Scope
- Feature: 008-pdf-generation
- Spec: Feature Specification: PDF/HTML Resume Export and Bullet-Point Review Hardening
- Feature folder: specs\008-pdf-generation
- Spec context: # Feature Specification : PDF /HTML Resume Export and Bullet-Point Review Hardening **Feature Branch **: `feat/008-pdf-generation` **Created**: 2026-06-20 **Status**: Final *(specified 2026-06-20 , brainstormed 2026-06-20 , planned 2026-06-20 )* **Input**: Implement production...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D2] Status Active Why this is durable During manual testing, hardcoded English strings were found in AuthPage.vue (info panel text, subtitles) and in LoginForm/RegisterForm (Zod validation messages). These were not caught during implementation because they were &quot;invisible&quot; — the page looked correct in English, but switching to Russian revealed untranslated text. Every future feature with UI will have the same risk. (Source: `docs/memory/DECISIONS.md`)
- [D3] Status Active Why this is durable HTML-to-PDF conversion requires independent library evaluation (Flying Saucer, OpenPDF, wkhtmltopdf), A4 layout validation, Cyrillic font support, and selectable text verification. Bundling it with AI generation would make the feature too large and risky. This split pattern may apply to other composite features. (Source: `docs/memory/DECISIONS.md`)
- [D4] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D5] Status Active Why this is durable pom.xml targets Java 21 ( &lt;release&gt;21&lt;/release&gt; , &lt;maven.compiler.source&gt;21&lt;/maven.compiler.source&gt; ). Installing a newer JDK (23+) causes subtle failures: Mockito 5.x inline mock maker cannot self-attach because JDK 23+ disables the Attach API. Every developer setting up this project will face this if their system JDK doesn't match. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)
- [S2] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
