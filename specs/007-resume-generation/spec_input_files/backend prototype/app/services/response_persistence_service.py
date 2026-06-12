from __future__ import annotations

import sqlite3

from app.dao import generation_response_dao
from app.dto import ParsedVariant


def persist_variants(conn: sqlite3.Connection, request_id: int, variants: list[ParsedVariant]) -> list[int]:
    """Persist parsed language × adaptation variants.

    Personal Information, including workFormats, is mapped in generation_response_dao.insert_sections().
    Work formats are profile-owned normalized data before generation, and response-owned localized text after AI review.
    """
    response_ids: list[int] = []
    for variant in variants:
        response_ids.append(generation_response_dao.insert_response(conn, request_id, variant))
    return response_ids
