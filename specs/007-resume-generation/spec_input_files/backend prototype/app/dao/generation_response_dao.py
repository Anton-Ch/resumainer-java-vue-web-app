from __future__ import annotations

import json
import sqlite3
from typing import Any

from app.dao.lookup_dao import get_id_by_code
from app.dto import ParsedVariant


def insert_response(conn: sqlite3.Connection, request_id: int, variant: ParsedVariant) -> int:
    language_id = get_id_by_code(conn, "language", variant.language.value)
    adaptation_level_id = get_id_by_code(conn, "adaptation_level", variant.adaptation_level.value)
    data = variant.data
    cur = conn.execute(
        """
        INSERT INTO resume_generation_response
        (generation_request_id, language_id, adaptation_level_id, professional_title,
         value_line, professional_summary, professional_aspirations, cover_letter,
         raw_variant_json, status, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'GENERATED', CURRENT_TIMESTAMP)
        """,
        (
            request_id,
            language_id,
            adaptation_level_id,
            pick(data, "professionalTitle", "professional_title"),
            pick(data, "valueLine", "value_line"),
            pick(data, "professionalSummary", "professional_summary"),
            pick(data, "professionalAspirations", "professional_aspirations"),
            pick(data, "coverLetter", "cover_letter"),
            json.dumps(data, ensure_ascii=False),
        ),
    )
    response_id = int(cur.lastrowid)
    insert_sections(conn, response_id, data)
    return response_id


def pick(data: dict[str, Any], *keys: str, default: Any = None) -> Any:
    for key in keys:
        if key in data:
            return data[key]
    return default


def as_list(value: Any) -> list[Any]:
    return value if isinstance(value, list) else []


def as_text(value: Any) -> str | None:
    if value is None:
        return None
    if isinstance(value, list):
        return ", ".join(str(item) for item in value if item)
    text = str(value).strip()
    return text if text else None


def insert_sections(conn: sqlite3.Connection, response_id: int, data: dict[str, Any]) -> None:
    for order, item in enumerate(as_list(pick(data, "workExperience", "work_experience"))):
        cur = conn.execute(
            """
            INSERT INTO generation_response_experience
            (response_id, source_id, job_title, company_name, location, date_range,
             description, order_in_resume, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                response_id,
                str(pick(item, "sourceId", "source_id", default=f"work-{order+1}")),
                str(pick(item, "jobTitle", "job_title", default="")),
                str(pick(item, "companyName", "company_name", default="")),
                pick(item, "location"),
                pick(item, "dateRange", "date_range"),
                pick(item, "description"),
                order,
            ),
        )
        exp_id = int(cur.lastrowid)
        for b_order, bullet in enumerate(as_list(item.get("bullets"))):
            if bullet:
                conn.execute(
                    """
                    INSERT INTO generation_response_experience_bullet
                    (experience_id, bullet_text, order_in_experience)
                    VALUES (?, ?, ?)
                    """,
                    (exp_id, str(bullet), b_order),
                )

    for order, item in enumerate(as_list(pick(data, "courses", "courseCertificates", "course_certificates"))):
        conn.execute(
            """
            INSERT INTO generation_response_course
            (response_id, source_id, name, provider, is_first_page, course_focus,
             order_in_resume, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                response_id,
                str(pick(item, "sourceId", "source_id", default=f"course-{order+1}")),
                str(pick(item, "courseName", "name", "course_name", default="")),
                str(pick(item, "provider", default="")),
                1,
                pick(item, "courseFocus", "course_focus"),
                order,
            ),
        )

    for order, item in enumerate(as_list(pick(data, "projects", "projectsAndVolunteering", "projects_and_volunteering"))):
        cur = conn.execute(
            """
            INSERT INTO generation_response_project
            (response_id, source_id, project_name, role, date_range, description,
             order_in_resume, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                response_id,
                str(pick(item, "sourceId", "source_id", default=f"project-{order+1}")),
                str(pick(item, "projectName", "project_name", default="")),
                pick(item, "role"),
                pick(item, "dateRange", "date_range"),
                pick(item, "description"),
                order,
            ),
        )
        project_id = int(cur.lastrowid)
        for b_order, bullet in enumerate(as_list(item.get("bullets"))):
            if bullet:
                conn.execute(
                    """
                    INSERT INTO generation_response_project_bullet
                    (project_id, bullet_text, order_in_project)
                    VALUES (?, ?, ?)
                    """,
                    (project_id, str(bullet), b_order),
                )

    for order, item in enumerate(as_list(pick(data, "skills", "skillGroups", "skill_groups"))):
        cur = conn.execute(
            """
            INSERT INTO generation_response_skill_group
            (response_id, group_name, order_in_resume, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """,
            (
                response_id,
                str(pick(item, "groupName", "group_name", default=f"Skill group {order+1}")),
                order,
            ),
        )
        group_id = int(cur.lastrowid)
        for s_order, skill in enumerate(as_list(item.get("skills"))):
            if skill:
                conn.execute(
                    """
                    INSERT INTO generation_response_skill
                    (skill_group_id, skill_name, order_in_group)
                    VALUES (?, ?, ?)
                    """,
                    (group_id, str(skill), s_order),
                )


    personal = pick(data, "personalInfo", "personal_info")
    if isinstance(personal, dict):
        conn.execute(
            """
            INSERT INTO generation_response_personal
            (response_id, location, spoken_languages, willingness_to_relocate,
             willingness_for_business_trips, citizenship, date_of_birth,
             work_formats, gpa_grade, order_in_resume, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP)
            """,
            (
                response_id,
                str(pick(personal, "location", default="")),
                str(pick(personal, "spokenLanguages", "spoken_languages", default="")),
                str(pick(personal, "willingnessToRelocate", "willingness_to_relocate", default="")),
                str(pick(personal, "willingnessForBusinessTrips", "willingness_for_business_trips", default="")),
                str(pick(personal, "citizenship", default="")),
                str(pick(personal, "dateOfBirth", "date_of_birth", default="")),
                as_text(pick(personal, "workFormats", "work_formats")),
                as_text(pick(personal, "gpaGrade", "gpa_grade")),
            ),
        )


def get_response_by_request_language_level(
    conn: sqlite3.Connection,
    request_id: int,
    language_code: str,
    adaptation_level_code: str,
) -> sqlite3.Row | None:
    return conn.execute(
        """
        SELECT r.*
        FROM resume_generation_response r
        JOIN language l ON l.id = r.language_id
        JOIN adaptation_level a ON a.id = r.adaptation_level_id
        WHERE r.generation_request_id = ? AND l.code = ? AND a.code = ?
        """,
        (request_id, language_code, adaptation_level_code),
    ).fetchone()


def list_responses_for_request(conn: sqlite3.Connection, request_id: int) -> list[sqlite3.Row]:
    return conn.execute(
        """
        SELECT r.*, l.code AS language_code, a.code AS adaptation_level_code
        FROM resume_generation_response r
        JOIN language l ON l.id = r.language_id
        JOIN adaptation_level a ON a.id = r.adaptation_level_id
        WHERE r.generation_request_id = ?
        ORDER BY l.code, a.id
        """,
        (request_id,),
    ).fetchall()


def get_response_bundle(conn: sqlite3.Connection, response_id: int) -> dict[str, Any]:
    row = conn.execute(
        """
        SELECT r.*, l.code AS language_code, a.code AS adaptation_level_code
        FROM resume_generation_response r
        JOIN language l ON l.id = r.language_id
        JOIN adaptation_level a ON a.id = r.adaptation_level_id
        WHERE r.id = ?
        """,
        (response_id,),
    ).fetchone()
    if not row:
        raise RuntimeError(f"Response not found: {response_id}")

    experiences = conn.execute(
        "SELECT * FROM generation_response_experience WHERE response_id = ? ORDER BY order_in_resume, id",
        (response_id,),
    ).fetchall()
    exp_list = []
    for exp in experiences:
        bullets = conn.execute(
            "SELECT bullet_text FROM generation_response_experience_bullet WHERE experience_id = ? ORDER BY order_in_experience, id",
            (exp["id"],),
        ).fetchall()
        d = dict(exp)
        d["bullets"] = [b["bullet_text"] for b in bullets]
        exp_list.append(d)

    courses = [dict(r) for r in conn.execute(
        "SELECT * FROM generation_response_course WHERE response_id = ? ORDER BY order_in_resume, id",
        (response_id,),
    ).fetchall()]

    projects_rows = conn.execute(
        "SELECT * FROM generation_response_project WHERE response_id = ? ORDER BY order_in_resume, id",
        (response_id,),
    ).fetchall()
    project_list = []
    for project in projects_rows:
        bullets = conn.execute(
            "SELECT bullet_text FROM generation_response_project_bullet WHERE project_id = ? ORDER BY order_in_project, id",
            (project["id"],),
        ).fetchall()
        d = dict(project)
        d["bullets"] = [b["bullet_text"] for b in bullets]
        project_list.append(d)

    skill_rows = conn.execute(
        "SELECT * FROM generation_response_skill_group WHERE response_id = ? ORDER BY order_in_resume, id",
        (response_id,),
    ).fetchall()
    skill_groups = []
    for group in skill_rows:
        skills = conn.execute(
            "SELECT skill_name FROM generation_response_skill WHERE skill_group_id = ? ORDER BY order_in_group, id",
            (group["id"],),
        ).fetchall()
        d = dict(group)
        d["skills"] = [s["skill_name"] for s in skills]
        skill_groups.append(d)

    personal = conn.execute(
        "SELECT * FROM generation_response_personal WHERE response_id = ?",
        (response_id,),
    ).fetchone()

    return {
        "response": dict(row),
        "experiences": exp_list,
        "courses": courses,
        "projects": project_list,
        "skill_groups": skill_groups,
        "personal": dict(personal) if personal else None,
    }
