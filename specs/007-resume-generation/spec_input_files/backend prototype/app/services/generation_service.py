from __future__ import annotations

import json
import sqlite3
from pathlib import Path

from app.clients.openrouter_client import call_openrouter
from app.dao.generation_request_dao import get_generation_request, update_request_status
from app.dto import PromptBuildResult
from app.services.prompt_builder_service import build_prompt
from app.services.response_parser_service import parse_ai_response
from app.services.response_persistence_service import persist_variants
from app.utils.json_utils import write_json


def generate_for_request(
    conn: sqlite3.Connection,
    request_id: int,
    sample_response_path: str | None = None,
    save_prompts: bool = False,
    debug_dir: str | Path = "debug_prompts",
) -> list[int]:
    request = get_generation_request(conn, request_id)
    prompt_result = build_prompt(conn, request_id)
    if save_prompts:
        write_debug_prompt(debug_dir, request_id, request, prompt_result)

    if sample_response_path:
        response_json = json.loads(Path(sample_response_path).read_text(encoding="utf-8"))
    else:
        response_json = call_openrouter(
            conn,
            int(request["ai_model_id"]),
            prompt_result.system_prompt,
            prompt_result.request_prompt,
        )

    parsed = parse_ai_response(response_json, str(request["language_mode"]), str(request["adaptation_selection"]))
    response_ids = persist_variants(conn, request_id, parsed)
    update_request_status(conn, request_id, "GENERATED")
    return response_ids


def write_debug_prompt(debug_dir: str | Path, request_id: int, request: sqlite3.Row, prompt: PromptBuildResult) -> None:
    data = {
        "request_id": request_id,
        "language_mode": request["language_mode"],
        "adaptation_selection": request["adaptation_selection"],
        "include_cover_letter": bool(request["include_cover_letter"]),
        "prompt_config_id": prompt.prompt_config_id,
        "prompt_hash": prompt.prompt_hash,
        "system_prompt": prompt.system_prompt,
        "request_prompt": prompt.request_prompt,
    }
    write_json(Path(debug_dir) / f"request_{request_id}_{request['language_mode']}_{request['adaptation_selection']}.json", data)
