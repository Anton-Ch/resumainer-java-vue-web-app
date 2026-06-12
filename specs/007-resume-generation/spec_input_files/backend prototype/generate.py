#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from app.dao.generation_response_dao import list_responses_for_request
from app.db import connect
from app.services.generation_service import generate_for_request


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="resumainer.db")
    parser.add_argument("--request-id", type=int, required=True)
    parser.add_argument("--sample-response", default=None, help="Optional local JSON response for parser/persistence testing without OpenRouter")
    parser.add_argument("--save-prompts", action="store_true")
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    db_path = root / args.db if not Path(args.db).is_absolute() else Path(args.db)
    sample_path = None
    if args.sample_response:
        sample_path = root / args.sample_response if not Path(args.sample_response).is_absolute() else Path(args.sample_response)
    conn = connect(db_path)
    with conn:
        response_ids = generate_for_request(conn, args.request_id, str(sample_path) if sample_path else None, args.save_prompts)
        rows = list_responses_for_request(conn, args.request_id)
    print(f"Generated request {args.request_id}; response IDs: {response_ids}")
    for row in rows:
        print(f"  response_id={row['id']} language={row['language_code']} adaptation={row['adaptation_level_code']}")
    print("Next example:")
    print(f"  python finalize_resume.py --db {args.db} --request-id {args.request_id} --selected-level BALANCED")


if __name__ == "__main__":
    main()
