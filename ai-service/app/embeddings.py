from __future__ import annotations

import hashlib
import math
import re

DIM = 32
MODEL_ID = "sentinel-hash-embed-simulation"
MODEL_VERSION = "1"


def tokenize(text: str) -> list[str]:
    return re.findall(r"[a-z0-9]+", text.lower())


def embed(text: str) -> list[float]:
    vec = [0.0] * DIM
    for tok in tokenize(text):
        digest = hashlib.sha256(tok.encode("utf-8")).digest()
        idx = int.from_bytes(digest[:2], "big") % DIM
        vec[idx] += 1.0
    norm = math.sqrt(sum(v * v for v in vec)) or 1.0
    return [v / norm for v in vec]


def cosine(a: list[float], b: list[float]) -> float:
    return sum(x * y for x, y in zip(a, b, strict=True))


INJECTION_MARKERS = (
    "ignore previous",
    "ignore all previous",
    "system prompt",
    "you are now",
    "close the case",
    "approve kyc",
    "approve sanctions",
    "create alert",
    "set priority",
)


def looks_like_injection(text: str) -> bool:
    lowered = text.lower()
    return any(marker in lowered for marker in INJECTION_MARKERS)
