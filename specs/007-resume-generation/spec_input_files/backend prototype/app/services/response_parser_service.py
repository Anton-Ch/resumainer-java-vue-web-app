from __future__ import annotations

from typing import Any

from app.dto import ParsedVariant
from app.enums import (
    AdaptationSelection,
    LanguageMode,
    normalize_adaptation_level,
    normalize_language,
    requested_languages,
    requested_levels,
)


def parse_ai_response(
    response_json: dict[str, Any],
    language_mode: str,
    adaptation_selection: str,
) -> list[ParsedVariant]:
    """Parse nested language × adaptation JSON into flat ParsedVariant objects."""
    expected_languages = requested_languages(LanguageMode(language_mode))
    expected_levels = requested_levels(AdaptationSelection(adaptation_selection))
    parsed: list[ParsedVariant] = []

    for language in expected_languages:
        lang_key = language.value.lower()
        lang_obj = response_json.get(lang_key) or response_json.get(language.value)
        if lang_obj is None:
            # Accept single-language response without explicit language root.
            if len(expected_languages) == 1:
                lang_obj = response_json
            else:
                raise ValueError(f"Missing language object in AI JSON: {lang_key}")

        for level in expected_levels:
            level_key = level.value.lower()
            variant_obj = None
            if isinstance(lang_obj, dict):
                variant_obj = lang_obj.get(level_key) or lang_obj.get(level.value) or lang_obj.get(level.value.title())
            if variant_obj is None:
                # Accept one-level response directly under language object.
                if len(expected_levels) == 1 and isinstance(lang_obj, dict) and looks_like_variant(lang_obj):
                    variant_obj = lang_obj
                else:
                    raise ValueError(f"Missing adaptation variant: {lang_key}.{level_key}")
            if not isinstance(variant_obj, dict):
                raise ValueError(f"Variant is not an object: {lang_key}.{level_key}")
            parsed.append(ParsedVariant(language=language, adaptation_level=level, data=variant_obj))
    return parsed


def looks_like_variant(obj: dict[str, Any]) -> bool:
    marker_keys = {
        "professionalTitle", "professional_title", "valueLine", "value_line",
        "workExperience", "work_experience", "courses", "projects", "skills"
    }
    return any(key in obj for key in marker_keys)
