# Java Mapping for Backend Prototype v3

| Python prototype | Java/OpenCode target |
|---|---|
| `add_request.py` | `ResumeGenerationController#createRequest` / `ResumeGenerationRequestService` |
| `app/dao/prompt_config_dao.py` | `AiPromptConfigDao`, `AiSystemPromptDao`, prompt fragment DAOs |
| `app/services/prompt_builder_service.py` | `ResumePromptBuilderService` / Builder pattern |
| `app/clients/openrouter_client.py` | `OpenRouterClient` / AI client interface implementation |
| `app/services/generation_service.py` | `ResumeGenerationService` |
| `app/services/response_parser_service.py` | `AiGenerationResponseParser` |
| `app/services/response_persistence_service.py` | `GenerationResponsePersistenceService` |
| `app/services/finalize_resume_service.py` | `ResumeFinalizeService` / Review Save endpoint |
| `app/services/html_render_service.py` | `ResumeTemplateRenderer` |
| PDF placeholder in `finalize_resume_service.py` | `PdfGenerationService` / HTML-to-PDF converter |
| SQLite `schema.sql` | PostgreSQL Flyway migration |

## Important implementation points

1. `ai_model.api_key_encrypted` is plaintext placeholder in Python only. Java must encrypt.
2. `ai_prompt_config` controls one active prompt bundle.
3. Prompt Builder must assemble: system + language + adaptation + cover-letter + dynamic payload.
4. `ai_prompt_render_log` stores final prompts for debugging and QA.
5. `resume_generation_response` uniqueness is `(generation_request_id, language_id, adaptation_level_id)`.
6. `ALL` is not an adaptation level in saved responses; it expands to three response rows.
7. Finalization selects one adaptation level and creates one saved resume per language.
8. Filled HTML must be saved before PDF conversion.
9. PDF conversion is intentionally placeholder in Python and must be implemented in Java.


## v3.2 update

- Education is bilingual profile-owned data and is rendered from profile fields, not AI output.
- Personal Information is AI-generated/reviewed via `generation_response_personal`.
- Final HTML/PDF artifacts are stored under `generated_results/{username}/{public_code}/`.
- PDF conversion is placeholder-only in Python and must become Java HtmlToPdfConverter call.
