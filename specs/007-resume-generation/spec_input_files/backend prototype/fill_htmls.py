#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from app.db import connect
from app.services.html_render_service import render_response_to_html


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="resumainer.db")
    parser.add_argument("--response-id", type=int, required=True)
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    db_path = root / args.db if not Path(args.db).is_absolute() else Path(args.db)
    conn = connect(db_path)
    path = render_response_to_html(conn, args.response_id, root / "output" / "html")
    print(f"Rendered HTML: {path}")


if __name__ == "__main__":
    main()
