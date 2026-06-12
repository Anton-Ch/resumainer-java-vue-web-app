# Backend Prototype v3 Flow Overview

```mermaid
flowchart TD
  A[Frontend Vacancy + Settings] --> B[add_request.py / resume_generation_request]
  B --> C[PromptBuilderService]
  C --> C1[Load active ai_prompt_config]
  C --> C2[Load ai_system_prompt]
  C --> C3[Load language prompt fragment]
  C --> C4[Load adaptation prompt fragment]
  C --> C5[Load cover-letter prompt fragment]
  C --> C6[Attach profile + vacancy + budget payload]
  C --> D[OpenRouterClient or sample response]
  D --> E[AI JSON]
  E --> F[ResponseParserService]
  F --> G[ParsedVariant list: language x adaptation]
  G --> H[ResponsePersistenceService]
  H --> I[resume_generation_response + generation_response_*]
  I --> J[Review UI edits generated rows]
  J --> K[finalize_resume.py selected adaptation]
  K --> L[HtmlRenderService saves filled HTML to disk]
  L --> M[PDF placeholder path stored]
  M --> N[saved_resume row + public URL]
```

## Default test scenario

`generation_request.json` uses:

- language mode: `Bilingual`
- adaptation selection: `All`
- cover letter: `true`

That expands to six generated response rows:

- EN / MINIMAL
- EN / BALANCED
- EN / MAXIMUM
- RU / MINIMAL
- RU / BALANCED
- RU / MAXIMUM

Finalization with `BALANCED` creates:

- EN saved resume
- RU saved resume

Each saved resume receives its own public URL.


## v3.2 update

- Education is bilingual profile-owned data and is rendered from profile fields, not AI output.
- Personal Information is AI-generated/reviewed via `generation_response_personal`.
- Final HTML/PDF artifacts are stored under `generated_results/{username}/{public_code}/`.
- PDF conversion is placeholder-only in Python and must become Java HtmlToPdfConverter call.
