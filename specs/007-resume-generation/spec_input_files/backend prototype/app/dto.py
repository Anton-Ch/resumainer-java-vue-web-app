from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from app.enums import AdaptationLevel, ResumeLanguage


@dataclass(frozen=True)
class PromptBuildResult:
    prompt_config_id: int
    system_prompt: str
    request_prompt: str
    profile_payload_json: str
    prompt_hash: str


@dataclass(frozen=True)
class ParsedVariant:
    language: ResumeLanguage
    adaptation_level: AdaptationLevel
    data: dict[str, Any]


@dataclass(frozen=True)
class SavedResumeResult:
    saved_resume_id: int
    response_id: int
    language_code: str
    adaptation_level: str
    html_file_path: str
    pdf_file_path: str
    public_url_link: str
