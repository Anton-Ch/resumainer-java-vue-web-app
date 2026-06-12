# Prototype v3 Local Test Results

Tested locally with sample AI response because OpenRouter API key is intentionally a placeholder in SQLite.

Commands used on a temporary `test.db`:

```bash
python add_user.py --db test.db --json user_info.json --reset
python add_request.py --db test.db --json generation_request.json --user-id auto
python generate.py --db test.db --request-id 1 --sample-response samples/sample_ai_response_bilingual_all.json --save-prompts
python finalize_resume.py --db test.db --request-id 1 --selected-level BALANCED
```

Verified results:

- `resume_generation_request`: 1 row
- `resume_generation_response`: 6 rows
- `ai_prompt_render_log`: 1 row
- `saved_resume`: 2 rows
- EN and RU saved resumes received different public URLs
- filled HTML files were saved to disk
- PDF placeholder marker files were created
- all Python files passed `python -m py_compile`

The ZIP includes a clean `resumainer.db` with:

- seeded lookup data
- seeded `ai_model` row with placeholder API key and model name
- seeded modular prompt config
- one demo user
- one pending/default `BILINGUAL + ALL + cover letter` generation request

To regenerate from scratch, run:

```bash
python add_user.py --db resumainer.db --json user_info.json --reset
python add_request.py --db resumainer.db --json generation_request.json --user-id auto
```


## v3.2 update

- Education is bilingual profile-owned data and is rendered from profile fields, not AI output.
- Personal Information is AI-generated/reviewed via `generation_response_personal`.
- Final HTML/PDF artifacts are stored under `generated_results/{username}/{public_code}/`.
- PDF conversion is placeholder-only in Python and must become Java HtmlToPdfConverter call.
