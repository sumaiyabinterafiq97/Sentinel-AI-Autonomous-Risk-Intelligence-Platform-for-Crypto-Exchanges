from __future__ import annotations

from datetime import UTC, datetime
from uuid import UUID


class AiError(Exception):
    def __init__(
        self,
        status: int,
        code: str,
        message: str,
        *,
        retryable: bool = False,
        field: str | None = None,
    ) -> None:
        super().__init__(message)
        self.status = status
        self.code = code
        self.retryable = retryable
        self.field = field


def unauthenticated() -> AiError:
    return AiError(401, "AUTH_AUTHENTICATION_001", "Authentication required")


def forbidden() -> AiError:
    return AiError(403, "AUTHZ_FORBIDDEN_001", "Forbidden")


def not_found() -> AiError:
    return AiError(404, "AI_NOT_FOUND_001", "Not found")


def validation(field: str, message: str) -> AiError:
    return AiError(400, "AI_VALIDATION_001", message, field=field)


def error_body(exc: AiError, request_id: UUID, correlation_id: UUID | None) -> dict:
    error = {
        "code": exc.code,
        "message": str(exc),
        "requestId": str(request_id),
        "correlationId": None if correlation_id is None else str(correlation_id),
        "timestamp": datetime.now(UTC).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "retryable": exc.retryable,
    }
    if exc.field:
        error["details"] = [{"field": exc.field, "message": str(exc)}]
    return {"error": error}
