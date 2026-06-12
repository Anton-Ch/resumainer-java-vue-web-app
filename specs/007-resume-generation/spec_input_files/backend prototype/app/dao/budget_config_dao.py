from __future__ import annotations

import sqlite3
from typing import Any


def get_budget_payload(conn: sqlite3.Connection) -> dict[str, Any]:
    """Small prototype placeholder.

    Java implementation can replace this with real resume_budget_config / budget rules.
    Keeping this payload explicit helps OpenCode see where budget data enters prompt building.
    """
    return {
        "templateSelection": "rule-based prototype placeholder",
        "pageBudgetHint": "Use concise content. One or two A4 pages will be selected after generation.",
        "workExperienceBullets": {"min": 2, "max": 4},
        "projectBullets": {"min": 1, "max": 3},
        "skillsGroups": {"min": 2, "max": 4},
    }
