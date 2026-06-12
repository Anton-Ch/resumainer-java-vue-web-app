from __future__ import annotations

import sqlite3
from typing import Any


def row_to_dict(row: sqlite3.Row | None) -> dict[str, Any] | None:
    return dict(row) if row else None


def rows_to_dicts(rows: list[sqlite3.Row]) -> list[dict[str, Any]]:
    return [dict(row) for row in rows]


def get_first_user_id(conn: sqlite3.Connection) -> str:
    row = conn.execute("SELECT id FROM users ORDER BY created_at LIMIT 1").fetchone()
    if not row:
        raise RuntimeError("No users found. Run add_user.py first.")
    return str(row["id"])


def get_user_work_formats(conn: sqlite3.Connection, user_id: str) -> dict[str, Any]:
    """Return normalized work formats in a prompt-friendly shape.

    DEC-022 keeps work formats in work_format + user_work_format. The prompt builder
    receives codes plus EN/RU display names so the AI can localize Personal Information
    without inventing values.
    """
    rows = conn.execute(
        """
        SELECT wf.code, wf.name_en, wf.name_ru
        FROM user_work_format uwf
        JOIN work_format wf ON wf.id = uwf.work_format_id
        WHERE uwf.user_id = ?
        ORDER BY uwf.id
        """,
        (user_id,),
    ).fetchall()
    codes = [str(row["code"]) for row in rows]
    names_en = [str(row["name_en"]) for row in rows]
    names_ru = [str(row["name_ru"]) for row in rows]
    return {
        "codes": codes,
        "en": names_en,
        "ru": names_ru,
        "english": ", ".join(names_en),
        "russian": ", ".join(names_ru),
    }


def get_profile_payload(conn: sqlite3.Connection, user_id: str) -> dict[str, Any]:
    user = row_to_dict(conn.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone())
    if not user:
        raise RuntimeError(f"User not found: {user_id}")

    contact = row_to_dict(conn.execute("SELECT * FROM contact_detail WHERE user_id = ?", (user_id,)).fetchone())
    additional = row_to_dict(conn.execute("SELECT * FROM additional_profile_info WHERE user_id = ?", (user_id,)).fetchone())
    work = rows_to_dicts(conn.execute("SELECT * FROM work_experience WHERE user_id = ? ORDER BY display_order, id", (user_id,)).fetchall())
    education = rows_to_dicts(conn.execute("SELECT * FROM education WHERE user_id = ? ORDER BY display_order, id", (user_id,)).fetchall())
    courses = rows_to_dicts(conn.execute("SELECT * FROM course_certificate WHERE user_id = ? ORDER BY display_order, id", (user_id,)).fetchall())
    projects = rows_to_dicts(conn.execute("SELECT * FROM project WHERE user_id = ? ORDER BY display_order, id", (user_id,)).fetchall())
    work_formats = get_user_work_formats(conn, user_id)

    return {
        "user": user,
        "contact": contact,
        "additional": additional,
        "work_formats": work_formats,
        "work_experience": work,
        "education": education,
        "courses": courses,
        "projects": projects,
    }
