from __future__ import annotations

import sqlite3
from typing import Any


def get_active_prompt_config(conn: sqlite3.Connection) -> sqlite3.Row:
    row = conn.execute("SELECT * FROM ai_prompt_config WHERE is_active = 1").fetchone()
    if not row:
        raise RuntimeError("No active ai_prompt_config found.")
    return row


def get_system_prompt(conn: sqlite3.Connection, prompt_config_id: int) -> str:
    row = conn.execute(
        "SELECT prompt FROM ai_system_prompt WHERE prompt_config_id = ?",
        (prompt_config_id,),
    ).fetchone()
    if not row:
        raise RuntimeError(f"No ai_system_prompt for config {prompt_config_id}")
    return str(row["prompt"])


def get_language_prompt(conn: sqlite3.Connection, prompt_config_id: int, language_mode: str) -> str:
    row = conn.execute(
        """
        SELECT prompt FROM ai_request_prompt_language
        WHERE prompt_config_id = ? AND language_mode = ?
        """,
        (prompt_config_id, language_mode),
    ).fetchone()
    if not row:
        raise RuntimeError(f"No language prompt for config={prompt_config_id}, mode={language_mode}")
    return str(row["prompt"])


def get_adaptation_prompt(conn: sqlite3.Connection, prompt_config_id: int, adaptation_selection: str) -> str:
    row = conn.execute(
        """
        SELECT prompt FROM ai_request_prompt_adaptation
        WHERE prompt_config_id = ? AND adaptation_selection = ?
        """,
        (prompt_config_id, adaptation_selection),
    ).fetchone()
    if not row:
        raise RuntimeError(f"No adaptation prompt for config={prompt_config_id}, selection={adaptation_selection}")
    return str(row["prompt"])


def get_cover_letter_prompt(conn: sqlite3.Connection, prompt_config_id: int, include_cover_letter: bool) -> str:
    row = conn.execute(
        """
        SELECT prompt FROM ai_request_prompt_cover_letter
        WHERE prompt_config_id = ? AND include_cover_letter = ?
        """,
        (prompt_config_id, 1 if include_cover_letter else 0),
    ).fetchone()
    if not row:
        raise RuntimeError(
            f"No cover letter prompt for config={prompt_config_id}, include={include_cover_letter}"
        )
    return str(row["prompt"])


def insert_prompt_render_log(
    conn: sqlite3.Connection,
    generation_request_id: int,
    prompt_config_id: int,
    system_prompt: str,
    request_prompt: str,
    profile_payload_json: str,
    prompt_hash: str,
) -> int:
    cur = conn.execute(
        """
        INSERT INTO ai_prompt_render_log
        (generation_request_id, prompt_config_id, system_prompt_rendered,
         request_prompt_rendered, profile_payload_json, prompt_hash)
        VALUES (?, ?, ?, ?, ?, ?)
        """,
        (
            generation_request_id,
            prompt_config_id,
            system_prompt,
            request_prompt,
            profile_payload_json,
            prompt_hash,
        ),
    )
    return int(cur.lastrowid)
