#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from app.db import connect
from app.services.generation_request_service import create_request_from_payload
from app.utils.json_utils import read_json


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="resumainer.db")
    parser.add_argument("--json", default="generation_request.json")
    parser.add_argument("--user-id", default=None)
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    db_path = root / args.db if not Path(args.db).is_absolute() else Path(args.db)
    json_path = root / args.json if not Path(args.json).is_absolute() else Path(args.json)
    payload = read_json(json_path)
    conn = connect(db_path)
    with conn:
        request_id = create_request_from_payload(conn, payload, args.user_id)
        row = conn.execute("SELECT * FROM resume_generation_request WHERE id = ?", (request_id,)).fetchone()
    print(f"Created resume_generation_request.id: {request_id}")
    print(f"Language mode: {row['language_mode']}")
    print(f"Adaptation selection: {row['adaptation_selection']}")
    print(f"Include cover letter: {bool(row['include_cover_letter'])}")
    print("Next:")
    print(f"  python generate.py --db {args.db} --request-id {request_id} --sample-response samples/sample_ai_response_bilingual_all.json --save-prompts")


if __name__ == "__main__":
    main()
