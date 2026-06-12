from __future__ import annotations

from enum import Enum


class LanguageMode(str, Enum):
    ENGLISH_ONLY = "ENGLISH_ONLY"
    RUSSIAN_ONLY = "RUSSIAN_ONLY"
    BILINGUAL = "BILINGUAL"


class AdaptationSelection(str, Enum):
    MINIMAL = "MINIMAL"
    BALANCED = "BALANCED"
    MAXIMUM = "MAXIMUM"
    ALL = "ALL"


class AdaptationLevel(str, Enum):
    MINIMAL = "MINIMAL"
    BALANCED = "BALANCED"
    MAXIMUM = "MAXIMUM"


class ResumeLanguage(str, Enum):
    EN = "EN"
    RU = "RU"


LANGUAGE_MODE_ALIASES = {
    "English only": LanguageMode.ENGLISH_ONLY,
    "Russian only": LanguageMode.RUSSIAN_ONLY,
    "Bilingual": LanguageMode.BILINGUAL,
    "ENGLISH_ONLY": LanguageMode.ENGLISH_ONLY,
    "RUSSIAN_ONLY": LanguageMode.RUSSIAN_ONLY,
    "BILINGUAL": LanguageMode.BILINGUAL,
}

ADAPTATION_SELECTION_ALIASES = {
    "Minimal": AdaptationSelection.MINIMAL,
    "Balanced": AdaptationSelection.BALANCED,
    "Maximum": AdaptationSelection.MAXIMUM,
    "All": AdaptationSelection.ALL,
    "MINIMAL": AdaptationSelection.MINIMAL,
    "BALANCED": AdaptationSelection.BALANCED,
    "MAXIMUM": AdaptationSelection.MAXIMUM,
    "ALL": AdaptationSelection.ALL,
}

ADAPTATION_LEVEL_ALIASES = {
    "minimal": AdaptationLevel.MINIMAL,
    "balanced": AdaptationLevel.BALANCED,
    "maximum": AdaptationLevel.MAXIMUM,
    "Minimal": AdaptationLevel.MINIMAL,
    "Balanced": AdaptationLevel.BALANCED,
    "Maximum": AdaptationLevel.MAXIMUM,
    "MINIMAL": AdaptationLevel.MINIMAL,
    "BALANCED": AdaptationLevel.BALANCED,
    "MAXIMUM": AdaptationLevel.MAXIMUM,
}

LANGUAGE_ALIASES = {
    "en": ResumeLanguage.EN,
    "EN": ResumeLanguage.EN,
    "english": ResumeLanguage.EN,
    "ru": ResumeLanguage.RU,
    "RU": ResumeLanguage.RU,
    "russian": ResumeLanguage.RU,
}


def normalize_language_mode(value: str) -> LanguageMode:
    try:
        return LANGUAGE_MODE_ALIASES[value]
    except KeyError as exc:
        raise ValueError(f"Unsupported language_mode: {value}") from exc


def normalize_adaptation_selection(value: str) -> AdaptationSelection:
    try:
        return ADAPTATION_SELECTION_ALIASES[value]
    except KeyError as exc:
        raise ValueError(f"Unsupported adaptation_selection: {value}") from exc


def normalize_adaptation_level(value: str) -> AdaptationLevel:
    try:
        return ADAPTATION_LEVEL_ALIASES[value]
    except KeyError as exc:
        raise ValueError(f"Unsupported adaptation level: {value}") from exc


def normalize_language(value: str) -> ResumeLanguage:
    try:
        return LANGUAGE_ALIASES[value]
    except KeyError as exc:
        raise ValueError(f"Unsupported language: {value}") from exc


def requested_languages(language_mode: LanguageMode) -> list[ResumeLanguage]:
    if language_mode == LanguageMode.ENGLISH_ONLY:
        return [ResumeLanguage.EN]
    if language_mode == LanguageMode.RUSSIAN_ONLY:
        return [ResumeLanguage.RU]
    return [ResumeLanguage.EN, ResumeLanguage.RU]


def requested_levels(selection: AdaptationSelection) -> list[AdaptationLevel]:
    if selection == AdaptationSelection.ALL:
        return [AdaptationLevel.MINIMAL, AdaptationLevel.BALANCED, AdaptationLevel.MAXIMUM]
    return [AdaptationLevel(selection.value)]
