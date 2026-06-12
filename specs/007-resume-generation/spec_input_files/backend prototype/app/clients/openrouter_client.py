from __future__ import annotations

import json
import sqlite3
import urllib.error
import urllib.request


def call_openrouter(
    conn: sqlite3.Connection,
    ai_model_id: int,
    system_prompt: str,
    request_prompt: str,
    temperature: float = 0.2,
) -> dict:
    model = conn.execute("SELECT * FROM ai_model WHERE id = ?", (ai_model_id,)).fetchone()
    if not model:
        raise RuntimeError(f"ai_model not found: {ai_model_id}")

    api_key = str(model["api_key_encrypted"])
    if not api_key or api_key == "REPLACE_ME_OPENROUTER_API_KEY":
        raise RuntimeError(
            "OpenRouter API key is still placeholder in SQLite ai_model.api_key_encrypted. "
            "Update it in the DB or use --sample-response for local parser testing."
        )

    payload = {
        "model": str(model["model_code"]),
        "temperature": temperature,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": request_prompt},
        ],
    }
    body = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        str(model["provider_api_url"]),
        data=body,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://resumainer.local/prototype",
            "X-Title": "ResumAIner Backend Prototype v3",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            response_body = response.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        error_text = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"OpenRouter HTTP error {exc.code}: {error_text}") from exc

    api_response = json.loads(response_body)
    content = api_response["choices"][0]["message"]["content"]
    return extract_json_object(content)


def extract_json_object(content: str) -> dict:
    text = content.strip()
    if text.startswith("```"):
        text = text.strip("`")
        if text.lower().startswith("json"):
            text = text[4:].strip()
    start = text.find("{")
    end = text.rfind("}")
    if start == -1 or end == -1:
        raise RuntimeError("AI response does not contain a JSON object.")
    return json.loads(text[start:end + 1])
