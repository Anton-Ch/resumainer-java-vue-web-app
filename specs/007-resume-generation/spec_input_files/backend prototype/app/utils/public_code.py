from __future__ import annotations

import random
import string


def generate_public_code(length: int = 5) -> str:
    alphabet = string.ascii_uppercase + string.digits
    return "".join(random.choice(alphabet) for _ in range(length))


def build_public_url(code: str) -> str:
    return f"https://resumainer.com/candidate/{code}"
