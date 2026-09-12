#!/usr/bin/env python3
"""Validate packaged OpenAPI YAML parse + JSON Schema files. M0 contract check only."""
from __future__ import annotations

import json
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("FAIL: PyYAML is required (python3 -m pip install pyyaml)", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).resolve().parent
OPENAPI = ROOT / "openapi" / "OpenAPI.yaml"
EVENTS = ROOT / "events"
errors: list[str] = []


def main() -> int:
    if not OPENAPI.is_file():
        errors.append(f"missing {OPENAPI}")
    else:
        with OPENAPI.open() as fh:
            spec = yaml.safe_load(fh)
        if not isinstance(spec, dict) or spec.get("openapi") is None:
            errors.append("OpenAPI.yaml is not a valid OpenAPI document")
        else:
            paths = spec.get("paths") or {}
            op_ids = []
            for path, methods in paths.items():
                if not isinstance(methods, dict):
                    continue
                for method, op in methods.items():
                    if method.startswith("x-") or not isinstance(op, dict):
                        continue
                    oid = op.get("operationId")
                    if oid:
                        op_ids.append(oid)
            if len(op_ids) != len(set(op_ids)):
                errors.append("duplicate OpenAPI operationId values")
            print(f"OpenAPI parse OK — {len(op_ids)} operationIds, openapi={spec.get('openapi')}")

    schema_files = sorted(EVENTS.rglob("*.json"))
    if not schema_files:
        errors.append("no JSON files under contracts/events")
    for path in schema_files:
        try:
            json.loads(path.read_text())
        except json.JSONDecodeError as exc:
            errors.append(f"invalid JSON {path}: {exc}")
    print(f"JSON parse OK — {len(schema_files)} files")

    catalog = EVENTS / "event-catalog.v0.2.json"
    if catalog.is_file():
        data = json.loads(catalog.read_text())
        lock = data.get("secConsumptionLock") or {}
        consumes = lock.get("consumes") or []
        excluded = lock.get("explicitlyExcluded") or []
        if consumes != ["UserLoggedIn", "SessionExpired", "AlertCreated", "CaseCreated"]:
            errors.append(f"SEC consume lock mismatch: {consumes}")
        if "DeviceSignalReceived" not in excluded or "EvidenceAttached" not in excluded:
            errors.append(f"SEC exclusion lock mismatch: {excluded}")
        print("SEC event lock OK")

    if errors:
        for e in errors:
            print(f"FAIL: {e}", file=sys.stderr)
        return 1
    print("contracts validation PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
