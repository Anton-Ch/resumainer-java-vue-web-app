from __future__ import annotations

import sqlite3
from typing import Any


def get_active_ai_model(conn: sqlite3.Connection) -> sqlite3.Row:
    row = conn.execute("SELECT * FROM ai_model WHERE is_active = 1 ORDER BY id LIMIT 1").fetchone()
    if not row:
        raise RuntimeError("No active ai_model found. Check seed_prompt_config.sql.")
    return row


def create_generation_request(conn: sqlite3.Connection, data: dict[str, Any]) -> int:
    cur = conn.execute(
        """
        INSERT INTO resume_generation_request
        (user_id, ai_model_id, prompt_config_id, language_mode, adaptation_selection,
         include_cover_letter, vacancy_title, vacancy_description, company_name,
         company_description, additional_comments, status, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)
        """,
        (
            data["user_id"],
            data["ai_model_id"],
            data["prompt_config_id"],
            data["language_mode"],
            data["adaptation_selection"],
            1 if data.get("include_cover_letter") else 0,
            data["vacancy_title"],
            data["vacancy_description"],
            data.get("company_name"),
            data.get("company_description"),
            data.get("additional_comments"),
        ),
    )
    return int(cur.lastrowid)


def get_generation_request(conn: sqlite3.Connection, request_id: int) -> sqlite3.Row:
    row = conn.execute("SELECT * FROM resume_generation_request WHERE id = ?", (request_id,)).fetchone()
    if not row:
        raise RuntimeError(f"Generation request not found: {request_id}")
    return row


def update_request_status(conn: sqlite3.Connection, request_id: int, status: str) -> None:
    conn.execute(
        "UPDATE resume_generation_request SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
        (status, request_id),
    )
