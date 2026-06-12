#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sqlite3
import uuid
from pathlib import Path
from typing import Any

from app.dao.lookup_dao import get_id_by_code
from app.db import apply_sql_file, connect
from app.utils.json_utils import read_json


def apply_schema_and_seed(conn: sqlite3.Connection, root: Path) -> None:
    apply_sql_file(conn, root / "schema.sql")
    apply_sql_file(conn, root / "seed_prompt_config.sql")


def insert_user(conn: sqlite3.Connection, data: dict[str, Any]) -> str:
    user = data["user"]
    user_id = str(uuid.uuid4()) if user.get("id") in (None, "auto") else str(user["id"])
    default_language_id = get_id_by_code(conn, "language", user.get("default_language_code", "EN"))
    secondary_language_id = get_id_by_code(conn, "language", user.get("secondary_language_code", "RU"))
    conn.execute(
        """
        INSERT INTO users
        (id, username, email, password_hash, role_id, status_id, permission_id,
         default_language_id, secondary_language_id, is_privileged, updated_at)
        VALUES (?, ?, ?, ?, 1, 1, 1, ?, ?, 0, CURRENT_TIMESTAMP)
        """,
        (user_id, user["username"], user["email"], user["password_hash"], default_language_id, secondary_language_id),
    )
    return user_id


def insert_contact(conn: sqlite3.Connection, user_id: str, contact: dict[str, Any]) -> None:
    conn.execute(
        """
        INSERT INTO contact_detail
        (user_id, full_name, phone, resume_email, location, professional_title,
         linkedin_url, portfolio_url, telegram, whatsapp, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        (
            user_id,
            contact["full_name"],
            contact["phone"],
            contact["resume_email"],
            contact["location"],
            contact.get("professional_title"),
            contact.get("linkedin_url"),
            contact.get("portfolio_url"),
            contact.get("telegram"),
            contact.get("whatsapp"),
        ),
    )


def insert_additional(conn: sqlite3.Connection, user_id: str, info: dict[str, Any]) -> None:
    default_lang = get_id_by_code(conn, "language", info.get("default_resume_language_code", "EN"))
    additional_lang = get_id_by_code(conn, "language", info.get("additional_resume_language_code", "RU"))
    conn.execute(
        """
        INSERT INTO additional_profile_info
        (user_id, skills, languages, professional_aspirations, achievements, general_information,
         default_resume_language_id, additional_resume_language_id, ready_for_relocation,
         ready_for_business_trips, date_of_birth, citizenship, photo_file_path, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        (
            user_id,
            info.get("skills"),
            info.get("languages"),
            info.get("professional_aspirations"),
            info.get("achievements"),
            info.get("general_information"),
            default_lang,
            additional_lang,
            1 if info.get("ready_for_relocation") else 0,
            1 if info.get("ready_for_business_trips") else 0,
            info.get("date_of_birth"),
            info.get("citizenship"),
            info.get("photo_file_path"),
        ),
    )


def insert_user_work_formats(conn: sqlite3.Connection, user_id: str, work_format_codes: list[str]) -> None:
    """Persist preferred work formats using DEC-022 3NF lookup + junction model."""
    for raw_code in work_format_codes or []:
        code = str(raw_code).strip().upper()
        if not code:
            continue
        row = conn.execute(
            "SELECT id FROM work_format WHERE code = ?",
            (code,),
        ).fetchone()
        if not row:
            raise ValueError(f"Unknown work format code in user_info.json: {raw_code}")
        conn.execute(
            """
            INSERT OR IGNORE INTO user_work_format (user_id, work_format_id)
            VALUES (?, ?)
            """,
            (user_id, int(row["id"])),
        )


def insert_work(conn: sqlite3.Connection, user_id: str, items: list[dict[str, Any]]) -> None:
    for order, item in enumerate(items):
        conn.execute(
            """
            INSERT INTO work_experience
            (user_id, source_id, job_title, company_name, location, start_date, end_date,
             is_current, description, company_url, display_order, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                user_id,
                item["source_id"],
                item["job_title"],
                item["company_name"],
                item.get("location"),
                item["start_date"],
                item.get("end_date"),
                1 if item.get("is_current") else 0,
                item["description"],
                item.get("company_url"),
                order,
            ),
        )


def insert_education(conn: sqlite3.Connection, user_id: str, items: list[dict[str, Any]]) -> None:
    for order, item in enumerate(items):
        # DEC-070: Education is profile-owned and not AI-generated, so RU/EN values are mandatory in profile.
        conn.execute(
            """
            INSERT INTO education
            (user_id, source_id, institution_name_ru, institution_name_en, degree_ru, degree_en,
             field_of_study_ru, field_of_study_en, education_type, description, start_date,
             end_date, location, gpa_grade, display_order, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                user_id,
                item["source_id"],
                item["institution_name_ru"],
                item["institution_name_en"],
                item["degree_ru"],
                item["degree_en"],
                item["field_of_study_ru"],
                item["field_of_study_en"],
                item.get("education_type"),
                (item.get("description") or {}).get("en") if isinstance(item.get("description"), dict) else item.get("description"),
                item["start_date"],
                item.get("end_date"),
                item.get("location"),
                item.get("gpa_grade"),
                order,
            ),
        )


def insert_courses(conn: sqlite3.Connection, user_id: str, items: list[dict[str, Any]]) -> None:
    for order, item in enumerate(items):
        conn.execute(
            """
            INSERT INTO course_certificate
            (user_id, source_id, name, provider, start_date, end_date, credential_url,
             skills_topics, description, display_order, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                user_id,
                item["source_id"],
                item["name"],
                item["provider"],
                item.get("start_date"),
                item.get("end_date"),
                item.get("credential_url"),
                item.get("skills_topics"),
                item.get("description"),
                order,
            ),
        )


def insert_projects(conn: sqlite3.Connection, user_id: str, items: list[dict[str, Any]]) -> None:
    for order, item in enumerate(items):
        conn.execute(
            """
            INSERT INTO project
            (user_id, source_id, project_name, role, start_date, end_date, description,
             project_url, display_order, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                user_id,
                item["source_id"],
                item["project_name"],
                item.get("role"),
                item.get("start_date"),
                item.get("end_date"),
                item["description"],
                item.get("project_url"),
                order,
            ),
        )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="resumainer.db")
    parser.add_argument("--json", default="user_info.json")
    parser.add_argument("--reset", action="store_true")
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    db_path = root / args.db if not Path(args.db).is_absolute() else Path(args.db)
    json_path = root / args.json if not Path(args.json).is_absolute() else Path(args.json)
    if args.reset and db_path.exists():
        db_path.unlink()
    conn = connect(db_path)
    with conn:
        if args.reset or not conn.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='users'").fetchone():
            apply_schema_and_seed(conn, root)
        data = read_json(json_path)
        user_id = insert_user(conn, data)
        insert_contact(conn, user_id, data["contact_detail"])
        additional_info = data["additional_profile_info"]
        insert_additional(conn, user_id, additional_info)
        insert_user_work_formats(conn, user_id, additional_info.get("work_formats", []))
        insert_work(conn, user_id, data.get("work_experience", []))
        insert_education(conn, user_id, data.get("education", []))
        insert_courses(conn, user_id, data.get("courses", []))
        insert_projects(conn, user_id, data.get("projects", []))
    print(f"Created user_id: {user_id}")
    print("Seeded ai_model row with placeholder API key/model in SQLite DB.")
    print("Next:")
    print(f"  python add_request.py --db {args.db} --json generation_request.json --user-id {user_id}")


if __name__ == "__main__":
    main()
