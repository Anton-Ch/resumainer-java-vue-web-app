from __future__ import annotations

import sqlite3
from typing import Any


def insert_saved_resume(conn: sqlite3.Connection, data: dict[str, Any]) -> int:
    cur = conn.execute(
        """
        INSERT INTO saved_resume
        (user_id, generation_request_id, response_id, language_id, adaptation_level_id,
         title, public_code, public_url_link, html_file_path, pdf_file_path, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        (
            data["user_id"],
            data["generation_request_id"],
            data["response_id"],
            data["language_id"],
            data["adaptation_level_id"],
            data["title"],
            data["public_code"],
            data["public_url_link"],
            data["html_file_path"],
            data["pdf_file_path"],
        ),
    )
    return int(cur.lastrowid)
