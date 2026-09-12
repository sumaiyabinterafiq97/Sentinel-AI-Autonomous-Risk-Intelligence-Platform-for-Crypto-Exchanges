"""M2 HMAC session token verification. Not OIDC. Shared IDENTITY_TOKEN_HMAC_KEY."""

from __future__ import annotations

import base64
import hashlib
import hmac
import json
import os
import time
from dataclasses import dataclass
from uuid import UUID

DEFAULT_HMAC_KEY = "local-m2-simulation-hmac-key-not-for-production"


def _b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def _b64url_decode(value: str) -> bytes:
    pad = "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode(value + pad)


@dataclass(frozen=True)
class AccessPrincipal:
    user_id: UUID
    organization_id: UUID
    session_id: UUID
    permissions: frozenset[str]
    exp: int

    def has(self, permission: str) -> bool:
        return permission in self.permissions


class AccessTokenCodec:
    def __init__(self, hmac_key: str | None = None) -> None:
        key = hmac_key or os.environ.get("IDENTITY_TOKEN_HMAC_KEY") or DEFAULT_HMAC_KEY
        if not key.strip():
            raise ValueError("token HMAC key is required")
        self._key = key.encode("utf-8")

    def issue(
        self,
        user_id: UUID,
        organization_id: UUID,
        session_id: UUID,
        permissions: set[str],
        expires_at_epoch: int | None = None,
    ) -> str:
        exp = expires_at_epoch or int(time.time()) + 3600
        header = _b64url(b'{"alg":"HS256","typ":"JWT"}')
        perms = ",".join(f'"{p}"' for p in sorted(permissions))
        payload = (
            f'{{"sub":"{user_id}","org":"{organization_id}","sid":"{session_id}",'
            f'"exp":{exp},"perms":[{perms}]}}'
        )
        payload_enc = _b64url(payload.encode("utf-8"))
        signing_input = f"{header}.{payload_enc}"
        sig = _b64url(hmac.new(self._key, signing_input.encode("utf-8"), hashlib.sha256).digest())
        return f"{signing_input}.{sig}"

    def verify(self, token: str | None) -> AccessPrincipal | None:
        if not token or token.count(".") != 2:
            return None
        header, payload_enc, signature = token.split(".")
        signing_input = f"{header}.{payload_enc}"
        expected = _b64url(hmac.new(self._key, signing_input.encode("utf-8"), hashlib.sha256).digest())
        if not hmac.compare_digest(expected, signature):
            return None
        try:
            raw = _b64url_decode(payload_enc).decode("utf-8")
            data = json.loads(raw)
            if int(time.time()) > int(data["exp"]):
                return None
            perms = data.get("perms") or []
            return AccessPrincipal(
                user_id=UUID(str(data["sub"])),
                organization_id=UUID(str(data["org"])),
                session_id=UUID(str(data["sid"])),
                permissions=frozenset(str(p) for p in perms),
                exp=int(data["exp"]),
            )
        except (KeyError, ValueError, json.JSONDecodeError):
            return None
