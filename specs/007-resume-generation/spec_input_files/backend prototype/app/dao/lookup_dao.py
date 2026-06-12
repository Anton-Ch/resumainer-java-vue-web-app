from __future__ import annotations

import sqlite3


def get_id_by_code(conn: sqlite3.Connection, table: str, code: str) -> int:
    row = conn.execute(f"SELECT id FROM {table} WHERE code = ?", (code,)).fetchone()
    if not row:
        raise RuntimeError(f"Missing lookup row: {table}.{code}")
    return int(row["id"])


def get_code_by_id(conn: sqlite3.Connection, table: str, row_id: int) -> str:
    row = conn.execute(f"SELECT code FROM {table} WHERE id = ?", (row_id,)).fetchone()
    if not row:
        raise RuntimeError(f"Missing lookup row: {table}.id={row_id}")
    return str(row["code"])
