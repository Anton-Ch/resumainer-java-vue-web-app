from __future__ import annotations

import sqlite3
from typing import Any

from app.dao import generation_request_dao, prompt_config_dao
from app.dao.profile_dao import get_first_user_id
from app.enums import normalize_adaptation_selection, normalize_language_mode


def create_request_from_payload(
    conn: sqlite3.Connection,
    payload: dict[str, Any],
    user_id_override: str | None = None,
) -> int:
    user_id = user_id_override or payload.get("user_id")
    if not user_id or user_id == "auto":
        user_id = get_first_user_id(conn)

    language_mode = normalize_language_mode(payload["language_mode"])
    adaptation_selection = normalize_adaptation_selection(payload["adaptation_selection"])
    ai_model = generation_request_dao.get_active_ai_model(conn)
    prompt_config = prompt_config_dao.get_active_prompt_config(conn)

    data = {
        "user_id": user_id,
        "ai_model_id": int(ai_model["id"]),
        "prompt_config_id": int(prompt_config["id"]),
        "language_mode": language_mode.value,
        "adaptation_selection": adaptation_selection.value,
        "include_cover_letter": bool(payload.get("include_cover_letter")),
        "vacancy_title": payload["vacancy_title"],
        "vacancy_description": payload["vacancy_description"],
        "company_name": payload.get("company_name"),
        "company_description": payload.get("company_description"),
        "additional_comments": payload.get("additional_comments"),
    }
    return generation_request_dao.create_generation_request(conn, data)
