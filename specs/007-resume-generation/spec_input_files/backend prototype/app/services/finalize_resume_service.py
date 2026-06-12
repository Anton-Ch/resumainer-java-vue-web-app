from __future__ import annotations

import sqlite3
from pathlib import Path

from app.dao.generation_request_dao import get_generation_request, update_request_status
from app.dao.generation_response_dao import get_response_by_request_language_level
from app.dao.saved_resume_dao import insert_saved_resume
from app.enums import normalize_adaptation_level, requested_languages, LanguageMode
from app.services.html_render_service import render_response_to_html
from app.utils.file_utils import safe_write
from app.utils.public_code import build_public_url, generate_public_code


def finalize_request(
    conn: sqlite3.Connection,
    request_id: int,
    selected_level: str,
    html_output_dir: str | Path = "generated_results",
    pdf_output_dir: str | Path = "generated_results",
) -> list[dict]:
    request = get_generation_request(conn, request_id)
    level = normalize_adaptation_level(selected_level)
    languages = requested_languages(LanguageMode(str(request["language_mode"])))
    results: list[dict] = []

    username = conn.execute("SELECT username FROM users WHERE id = ?", (request["user_id"],)).fetchone()["username"]

    for language in languages:
        response = get_response_by_request_language_level(conn, request_id, language.value, level.value)
        if not response:
            raise RuntimeError(f"Missing response for request={request_id}, language={language.value}, level={level.value}")

        # DEC-073: Persist generated artifacts under generated_results/{username}/{public_code}/.
        public_code = unique_public_code(conn)
        public_url = build_public_url(public_code)
        artifact_dir = Path(html_output_dir) / username / public_code
        html_path = render_response_to_html(conn, int(response["id"]), artifact_dir)
        pdf_file_path = str(artifact_dir / (Path(html_path).stem + ".pdf"))
        marker_path = artifact_dir / (Path(html_path).stem + ".PDF_NOT_GENERATED_YET.txt")
        safe_write(
            marker_path,
            "PDF PLACEHOLDER ONLY. Java implementation must call real HTML-to-PDF converter after HTML rendering.\n"
            f"HTML source: {html_path}\n"
            f"Future PDF path stored in DB: {pdf_file_path}\n",
        )
        title = f"{request['vacancy_title']} — {language.value} — {level.value.title()}"
        saved_id = insert_saved_resume(conn, {
            "user_id": request["user_id"],
            "generation_request_id": request_id,
            "response_id": int(response["id"]),
            "language_id": int(response["language_id"]),
            "adaptation_level_id": int(response["adaptation_level_id"]),
            "title": title,
            "public_code": public_code,
            "public_url_link": public_url,
            "html_file_path": html_path,
            "pdf_file_path": pdf_file_path,
        })
        results.append({
            "saved_resume_id": saved_id,
            "response_id": int(response["id"]),
            "language_code": language.value,
            "adaptation_level": level.value,
            "html_file_path": html_path,
            "pdf_file_path": pdf_file_path,
            "pdf_placeholder_marker": str(marker_path),
            "public_url_link": public_url,
        })

    update_request_status(conn, request_id, "FINALIZED")
    return results


def unique_public_code(conn: sqlite3.Connection) -> str:
    for _ in range(20):
        code = generate_public_code()
        exists = conn.execute("SELECT 1 FROM saved_resume WHERE public_code = ?", (code,)).fetchone()
        if not exists:
            return code
    raise RuntimeError("Could not generate unique public code")
