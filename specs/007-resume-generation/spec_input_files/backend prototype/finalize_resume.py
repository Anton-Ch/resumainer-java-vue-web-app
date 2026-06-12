#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from app.db import connect
from app.services.finalize_resume_service import finalize_request


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="resumainer.db")
    parser.add_argument("--request-id", type=int, required=True)
    parser.add_argument("--selected-level", required=True, choices=["MINIMAL", "BALANCED", "MAXIMUM", "Minimal", "Balanced", "Maximum"])
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    db_path = root / args.db if not Path(args.db).is_absolute() else Path(args.db)
    conn = connect(db_path)
    with conn:
        results = finalize_request(conn, args.request_id, args.selected_level, root / "generated_results", root / "generated_results")
    print(f"Finalized request {args.request_id} with selected level {args.selected_level}.")
    for result in results:
        print(f"  saved_resume_id={result['saved_resume_id']} {result['language_code']} {result['adaptation_level']}")
        print(f"    HTML: {result['html_file_path']}")
        print(f"    Future PDF path: {result['pdf_file_path']}")
        print(f"    PDF placeholder marker: {result['pdf_placeholder_marker']}")
        print(f"    Public URL: {result['public_url_link']}")


if __name__ == "__main__":
    main()
