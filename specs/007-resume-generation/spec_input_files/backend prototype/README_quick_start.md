# ResumAIner Backend Prototype v3

Executable reference implementation for the Java/OpenCode generation pipeline.

## What v3 demonstrates

- DB-backed OpenRouter model config (`ai_model`).
- DB-backed modular prompt config:
  - `ai_prompt_config`
  - `ai_system_prompt`
  - `ai_request_prompt_language`
  - `ai_request_prompt_adaptation`
  - `ai_request_prompt_cover_letter`
  - `ai_prompt_render_log`
- One generation request can produce `language × adaptation_level` response rows.
- `ALL` is request selection only; generated response rows use only `MINIMAL`, `BALANCED`, `MAXIMUM`.
- Review/finalize flow selects one adaptation level and creates final saved resumes.
- Filled HTML is saved to disk before PDF conversion.
- PDF conversion is placeholder only; Java must call a real HTML-to-PDF converter later.
- Bilingual finalization creates two saved resumes with two different public URLs.

## API key/model in SQLite DB

The seed inserts one active `ai_model` row:

```text
provider = OpenRouter
model_code = deepseek/deepseek-v4-flash
api_key_encrypted = REPLACE_ME_OPENROUTER_API_KEY
```

Edit these values directly in SQLite when you want to test real OpenRouter:

```sql
UPDATE ai_model
SET api_key_encrypted = 'your_openrouter_key_here',
    model_code = 'your_openrouter_model_or_preset_here'
WHERE is_active = 1;
```

In Java/PostgreSQL implementation, API keys must be encrypted and never logged.

## Quick test without OpenRouter

```bash
python add_user.py --db resumainer.db --json user_info.json --reset
python add_request.py --db resumainer.db --json generation_request.json --user-id auto
python generate.py --db resumainer.db --request-id 1 --sample-response samples/sample_ai_response_bilingual_all.json --save-prompts
python finalize_resume.py --db resumainer.db --request-id 1 --selected-level BALANCED
```

Expected result for default `Bilingual + All + cover letter` request:

```text
resume_generation_request: 1 row
resume_generation_response: 6 rows
saved_resume: 2 rows after finalize
output/html: 2 filled HTML files
output/pdf_placeholder: 2 placeholder marker files
public_url_link: 2 different links
```

## Real OpenRouter test

After updating `ai_model.api_key_encrypted` and `ai_model.model_code` in SQLite:

```bash
python add_user.py --db resumainer.db --json user_info.json --reset
python add_request.py --db resumainer.db --json generation_request.json --user-id auto
python generate.py --db resumainer.db --request-id 1 --save-prompts
python finalize_resume.py --db resumainer.db --request-id 1 --selected-level BALANCED
```

If the key is still placeholder, `generate.py` fails intentionally with a clear error.

## CLI scripts

### `add_user.py`

Creates schema, seeds lookup/model/prompt config, and inserts a realistic demo user profile.

```bash
python add_user.py --db resumainer.db --json user_info.json --reset
```

### `add_request.py`

Creates a generation request from frontend-like `generation_request.json`.

```bash
python add_request.py --db resumainer.db --json generation_request.json --user-id auto
```

### `generate.py`

Builds prompt from DB fragments, calls OpenRouter or local sample JSON, parses response, and persists generated rows.

```bash
python generate.py --db resumainer.db --request-id 1 --sample-response samples/sample_ai_response_bilingual_all.json --save-prompts
```

### `finalize_resume.py`

Imitates Review → Save PDF flow.

```bash
python finalize_resume.py --db resumainer.db --request-id 1 --selected-level BALANCED
```

### `fill_htmls.py`

Low-level renderer for one existing response row.

```bash
python fill_htmls.py --db resumainer.db --response-id 1
```

## Java handoff idea

This Python prototype is intentionally layered:

```text
CLI scripts → services → dao → SQLite
```

It should map naturally to:

```text
Spring MVC Controller → Service → DAO → PostgreSQL
```

See:

- `docs/JAVA_MAPPING.md`
- `docs/FLOW_OVERVIEW.md`

## PDF placeholder

During finalize:

1. HTML is rendered and saved to `output/html`.
2. Future PDF path is stored in `saved_resume.pdf_file_path`.
3. A marker file is written to `output/pdf_placeholder`.

Java implementation must replace this placeholder step with a real converter:

```text
filled HTML file → HTML-to-PDF converter → selectable-text PDF file
```


## v3.2 personal info / artifact storage patch

This patch adds:

- bilingual education profile fields (`institution_name_ru/en`, `degree_ru/en`, `field_of_study_ru/en`);
- `generation_response_personal` for AI-generated/reviewed Personal Information;
- final artifacts saved under `generated_results/{username}/{public_code}/`;
- HTML files are persisted before PDF conversion;
- PDF conversion remains placeholder-only for Java `HtmlToPdfConverter`.

Main test flow:

```bash
python add_user.py --db test.db --json user_info.json --reset
python add_request.py --db test.db --json generation_request.json --user-id <USER_UUID>
python generate.py --db test.db --request-id 1 --sample-response samples/sample_ai_response_bilingual_all.json --save-prompts
python finalize_resume.py --db test.db --request-id 1 --selected-level BALANCED
```

Expected artifact folders:

```text
generated_results/demo.ba.java/<PUBLIC_CODE>/
  2026-..._en_balanced.html
  2026-..._en_balanced.PDF_NOT_GENERATED_YET.txt

generated_results/demo.ba.java/<PUBLIC_CODE>/
  2026-..._ru_balanced.html
  2026-..._ru_balanced.PDF_NOT_GENERATED_YET.txt
```
