from __future__ import annotations

import hashlib
import json
import sqlite3
from typing import Any

from app.dao import generation_request_dao, prompt_config_dao
from app.dao.budget_config_dao import get_budget_payload
from app.dao.profile_dao import get_profile_payload
from app.dto import PromptBuildResult
from app.enums import AdaptationSelection, LanguageMode, requested_languages, requested_levels


def build_prompt(conn: sqlite3.Connection, request_id: int) -> PromptBuildResult:
    request = generation_request_dao.get_generation_request(conn, request_id)
    prompt_config_id = int(request["prompt_config_id"])

    system_prompt = prompt_config_dao.get_system_prompt(conn, prompt_config_id)
    language_fragment = prompt_config_dao.get_language_prompt(conn, prompt_config_id, request["language_mode"])
    adaptation_fragment = prompt_config_dao.get_adaptation_prompt(conn, prompt_config_id, request["adaptation_selection"])
    cover_fragment = prompt_config_dao.get_cover_letter_prompt(
        conn,
        prompt_config_id,
        bool(request["include_cover_letter"]),
    )

    profile_payload = get_profile_payload(conn, str(request["user_id"]))
    budget_payload = get_budget_payload(conn)
    contract = build_json_contract(str(request["language_mode"]), str(request["adaptation_selection"]))
    payload = {
        "request": dict(request),
        "profile": profile_payload,
        "workFormats": build_work_formats_payload(profile_payload),
        "budget": budget_payload,
        "expectedJsonContract": contract,
    }
    profile_payload_json = json.dumps(payload, ensure_ascii=False, indent=2, default=str)

    request_prompt = "\n\n".join(
        [
            "# Resume generation request",
            language_fragment,
            adaptation_fragment,
            cover_fragment,
            "# Dynamic payload",
            profile_payload_json,
            "# Personal information rule",
            "Return personalInfo for every language/adaptation variant. Education is not AI-generated: use bilingual profile education fields during template rendering. For personalInfo.workFormats, use only profile.work_formats / workFormats from the dynamic payload. For EN output use English display names; for RU output use Russian display names. Do not invent work formats. If no work formats are selected, return null.",
            "# Required response contract",
            contract,
            "Return JSON only. No markdown. No commentary.",
        ]
    )
    prompt_hash = hashlib.sha256((system_prompt + "\n" + request_prompt).encode("utf-8")).hexdigest()

    prompt_config_dao.insert_prompt_render_log(
        conn,
        request_id,
        prompt_config_id,
        system_prompt,
        request_prompt,
        profile_payload_json,
        prompt_hash,
    )
    return PromptBuildResult(prompt_config_id, system_prompt, request_prompt, profile_payload_json, prompt_hash)


def build_work_formats_payload(profile_payload: dict[str, Any]) -> dict[str, Any]:
    work_formats = profile_payload.get("work_formats") or {}
    return {
        "codes": work_formats.get("codes", []),
        "english": work_formats.get("english") or ", ".join(work_formats.get("en", []) or []),
        "russian": work_formats.get("russian") or ", ".join(work_formats.get("ru", []) or []),
        "rule": "Use only these selected work formats. Do not invent extra values.",
    }


def build_json_contract(language_mode: str, adaptation_selection: str) -> str:
    languages = [lang.value.lower() for lang in requested_languages(LanguageMode(language_mode))]
    levels = [level.value.lower() for level in requested_levels(AdaptationSelection(adaptation_selection))]
    example_leaf = {
        "professionalTitle": "string",
        "valueLine": "string",
        "professionalSummary": "string",
        "professionalAspirations": "string",
        "workExperience": [
            {
                "sourceId": "same sourceId as profile work record",
                "jobTitle": "string",
                "companyName": "string",
                "location": "string or null",
                "dateRange": "string",
                "description": "string",
                "bullets": ["string"]
            }
        ],
        "courses": [
            {
                "sourceId": "same sourceId as profile course record",
                "courseName": "string",
                "provider": "string",
                "dateRange": "string",
                "courseFocus": "string"
            }
        ],
        "personalInfo": {
            "location": "localized resume location",
            "spokenLanguages": "localized spoken language line",
            "willingnessToRelocate": "localized value",
            "willingnessForBusinessTrips": "localized value",
            "citizenship": "localized citizenship",
            "dateOfBirth": "YYYY-MM-DD",
            "workFormats": "localized selected work formats from profile.work_formats/workFormats or null",
            "gpaGrade": "grade/GPA or null"
        },
        "projects": [
            {
                "sourceId": "same sourceId as profile project record",
                "projectName": "string",
                "role": "string or null",
                "dateRange": "string",
                "description": "string",
                "bullets": ["string"]
            }
        ],
        "skills": [
            {"groupName": "string", "skills": ["string"]}
        ],
        "coverLetter": "string or null"
    }
    root: dict[str, Any] = {}
    for lang in languages:
        root[lang] = {level: example_leaf for level in levels}
    return json.dumps(root, ensure_ascii=False, indent=2)
